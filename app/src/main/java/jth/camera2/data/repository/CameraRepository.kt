package jth.camera2.data.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import java.io.File

interface CameraRepository {
    fun getImageUri(dir: File): Uri
    fun getFileName(): String
    fun getContentValues(isCroppedFile: Boolean, type: String, path: String): ContentValues
    fun getRealPathFromUri(resolver: ContentResolver, uri: Uri): String
}