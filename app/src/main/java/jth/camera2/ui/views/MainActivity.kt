package jth.camera2.ui.views

import android.Manifest
import android.R.attr.maxHeight
import android.R.attr.maxWidth
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import jth.camera2.R
import jth.camera2.databinding.ActivityMainBinding
import jth.camera2.ui.viewmodels.MainViewModel
import java.io.*
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val viewModel: MainViewModel by viewModels()

    companion object {
        private const val MIME_TYPE_JPG = "image/jpeg"
        private const val IMAGE_RELATIVE_PATH = "Pictures/CameraX-Image"
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    private var sourceFile: File? = null
    private var destinationFile: File? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                viewModel.getContentValues(false, MIME_TYPE_JPG, IMAGE_RELATIVE_PATH)
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { savedUri ->
                        sourceFile = File(viewModel.getRealPathFromUri(contentResolver, savedUri))

                        openCropActivity(
                            Uri.fromFile(sourceFile),
                            viewModel.getImageUri(dir = cacheDir)
                        )

                        val msg = "Photo capture succeeded: $savedUri"
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    private fun openCropActivity(sourceUri: Uri, destinationUri: Uri) {
        val options = UCrop.Options()
        options.setCompressionQuality(70)

        UCrop.of(sourceUri, destinationUri)
            .withMaxResultSize(maxWidth, maxHeight)
            .withOptions(options)
            .withAspectRatio(1f, 1f)
            .start(this)
    }

    private fun saveCroppedImage(croppedUri: Uri) {
        croppedUri.path?.let { path ->
            val bitmap: Bitmap

            if (Build.VERSION.SDK_INT < 28) {
                bitmap = MediaStore.Images.Media.getBitmap(
                    this.contentResolver,
                    croppedUri
                )
            } else {
                val source = ImageDecoder.createSource(this.contentResolver, croppedUri)
                bitmap = ImageDecoder.decodeBitmap(source)
            }

            val fos = FileOutputStream(path)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos)
            fos.close()

            destinationFile = File(path);
            //destinationFile?.delete()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val croppedUri = data?.let {
                UCrop.getOutput(it)
            }

            croppedUri?.let { uri ->
                saveCroppedImage(
                    croppedUri = uri
                )
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            data?.let {
                val cropError = UCrop.getError(it)
                cropError?.let { error ->
                    Toast.makeText(baseContext, error.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startCamera() {
        imageCapture = ImageCapture.Builder().build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding?.viewFinder?.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                        Log.d(TAG, "Average luminosity: $luma")
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        override fun analyze(image: ImageProxy) {

            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()

            listener(luma)

            image.close()
        }
    }

    override fun getLayoutResId(): Int = R.layout.activity_main

    override fun initializeViewModel() {
        binding?.viewModel = viewModel
    }

    override fun initializeUiEvent() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        binding?.imageCaptureButton?.setOnClickListener { takePhoto() }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }
}