package com.example.eventghar.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

/**
 * Utility to upload images to Firebase Storage and get download URLs.
 */
object StorageUtil {

    private fun getStorage() = FirebaseStorage.getInstance()

    /**
     * Upload an image. Strategy:
     * 1. Try Firebase Storage first (gives an HTTPS URL)
     * 2. If Storage fails, compress + encode as Base64 data URI stored directly in Firestore
     *
     * Both strategies return a string that Coil's AsyncImage can display.
     */
    suspend fun uploadImage(context: Context, path: String, localUri: String): String? {
        if (localUri.isBlank()) return null

        // If already a remote URL or base64, return as-is
        if (isRemoteUrl(localUri) || localUri.startsWith("data:image")) return localUri

        // Read the raw bytes from the URI
        val bytes: ByteArray? = readBytes(context, localUri)
        if (bytes == null || bytes.isEmpty()) {
            Log.e("StorageUtil", "Could not read bytes from $localUri")
            return null
        }

        // ── Strategy 1: Firebase Storage ──────────────────────────────────
        try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val ref = getStorage().reference.child("$path/$fileName")
            Log.d("StorageUtil", "Attempting Firebase Storage upload: ${bytes.size} bytes")
            ref.putBytes(bytes).await()
            val downloadUri = ref.downloadUrl.await()
            val url = downloadUri.toString()
            Log.d("StorageUtil", "Firebase Storage upload SUCCESS: $url")
            return url
        } catch (e: Exception) {
            Log.w("StorageUtil", "Firebase Storage upload FAILED: ${e.message}. Falling back to Base64.")
        }

        // ── Strategy 2: Compress + Base64 encode (stored in Firestore) ────
        return try {
            val compressedBytes = compressImage(bytes, maxSizeKb = 150)
            val base64 = Base64.encodeToString(compressedBytes, Base64.DEFAULT)
            val dataUri = "data:image/jpeg;base64,$base64"
            Log.d("StorageUtil", "Base64 fallback SUCCESS: ${compressedBytes.size} bytes compressed")
            dataUri
        } catch (e: Exception) {
            Log.e("StorageUtil", "Base64 fallback FAILED: ${e.message}")
            null
        }
    }

    /** Read raw bytes from a content://, file://, or absolute path URI */
    private fun readBytes(context: Context, localUri: String): ByteArray? {
        return try {
            val parsedUri: Uri = when {
                localUri.startsWith("content://") -> Uri.parse(localUri)
                localUri.startsWith("file://")    -> Uri.parse(localUri)
                localUri.startsWith("/")           -> Uri.fromFile(File(localUri))
                else                               -> Uri.parse(localUri)
            }
            context.contentResolver.openInputStream(parsedUri)?.use { it.readBytes() }
        } catch (e: Exception) {
            Log.w("StorageUtil", "readBytes failed for $localUri: ${e.message}")
            null
        }
    }

    /** Compress image to JPEG, reducing quality until under maxSizeKb */
    private fun compressImage(bytes: ByteArray, maxSizeKb: Int): ByteArray {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: return bytes

        // Scale down if image is too large (max 600px on longest side)
        val maxDim = 600
        val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val ratio = minOf(maxDim.toFloat() / bitmap.width, maxDim.toFloat() / bitmap.height)
            val newW = (bitmap.width * ratio).toInt()
            val newH = (bitmap.height * ratio).toInt()
            Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        } else bitmap

        // Try decreasing quality until under maxSizeKb
        var quality = 80
        var out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
        while (out.size() > maxSizeKb * 1024 && quality > 20) {
            quality -= 10
            out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
        return out.toByteArray()
    }

    /**
     * Check if a string is already a remote URL (no upload needed).
     */
    fun isRemoteUrl(uri: String?): Boolean {
        if (uri.isNullOrBlank()) return false
        return uri.startsWith("http://") || uri.startsWith("https://") || uri.startsWith("data:image")
    }

    /**
     * Delete an image from Firebase Storage by its download URL.
     */
    suspend fun deleteImage(downloadUrl: String?) {
        if (downloadUrl.isNullOrBlank()) return
        if (!downloadUrl.startsWith("http://") && !downloadUrl.startsWith("https://")) return
        try {
            getStorage().getReferenceFromUrl(downloadUrl).delete().await()
        } catch (_: Exception) { }
    }
}
