package com.example.eventghar

import com.example.eventghar.data.StorageUtil
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StorageUtilUnitTests {

    @Test
    fun storageUtil_isRemoteUrl_identifiesCorrectly() {
        assertTrue(StorageUtil.isRemoteUrl("https://firebase.google.com/image.png"))
        assertTrue(StorageUtil.isRemoteUrl("http://example.com/photo.jpg"))
        assertTrue(StorageUtil.isRemoteUrl("data:image/jpeg;base64,encodedstring"))
        
        assertFalse(StorageUtil.isRemoteUrl("content://media/external/images/media/123"))
        assertFalse(StorageUtil.isRemoteUrl("/storage/emulated/0/DCIM/camera.jpg"))
        assertFalse(StorageUtil.isRemoteUrl(""))
        assertFalse(StorageUtil.isRemoteUrl(null))
    }
}
