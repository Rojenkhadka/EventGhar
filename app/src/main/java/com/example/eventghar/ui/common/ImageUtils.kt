package com.example.eventghar.ui.common

import android.graphics.BitmapFactory
import android.util.Base64
import android.net.Uri
import java.io.File

/**
 * Resolves any image URI string into a model that Coil's AsyncImage can display.
 *
 * Supported formats:
 *  - HTTPS URL          → String (Coil loads from network)
 *  - data:image/...     → android.graphics.Bitmap (decoded from Base64)
 *  - content://...      → android.net.Uri
 *  - /absolute/path     → java.io.File (if exists)
 *  - anything else      → String (Coil tries to parse)
 */
fun resolveImageModel(uri: String?): Any? {
    if (uri.isNullOrBlank()) return null
    return when {
        uri.startsWith("https://") || uri.startsWith("http://") -> uri
        uri.startsWith("data:image") -> {
            // Decode Base64 data URI to Bitmap
            try {
                val base64Part = uri.substringAfter("base64,")
                val bytes = Base64.decode(base64Part, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }
        }
        uri.startsWith("content://") -> Uri.parse(uri)
        uri.startsWith("file://")    -> Uri.parse(uri)
        uri.startsWith("/") -> {
            val f = File(uri)
            if (f.exists()) f else null
        }
        else -> uri
    }
}

