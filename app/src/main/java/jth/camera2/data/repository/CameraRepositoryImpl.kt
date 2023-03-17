package jth.camera2.data.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import jth.camera2.data.datasource.CameraRemoteSource
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class CameraRepositoryImpl @Inject constructor(
    private val remoteSource: CameraRemoteSource,
) : CameraRepository {
    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    override fun getImageUri(dir: File): Uri {
        return Uri.fromFile(
            File(
                dir,
                "cropped_${getFileName()}.jpg"
            )
        )
    }

    override fun getFileName(): String = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
        .format(System.currentTimeMillis())

    override fun getContentValues(
        isCroppedFile: Boolean,
        type: String,
        path: String
    ): ContentValues {
        return ContentValues().apply {
            if (isCroppedFile) {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "cropped_" + getFileName())
            } else {
                put(MediaStore.MediaColumns.DISPLAY_NAME, getFileName())
            }

            put(MediaStore.MediaColumns.MIME_TYPE, type)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, path)
            }
        }
    }

    override fun getRealPathFromUri(resolver: ContentResolver, uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = resolver.query(uri, projection, null, null, null)
        return cursor?.let { c ->
            val columnIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            c.moveToFirst()
            val path = c.getString(columnIndex)
            c.close()
            path
        } ?: uri.path ?: ""
    }
}