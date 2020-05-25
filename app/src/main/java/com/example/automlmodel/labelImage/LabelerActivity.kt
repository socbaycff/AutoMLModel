package com.example.automlmodel.labelImage

import android.annotation.SuppressLint
import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.automlmodel.R
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions

import kotlinx.android.synthetic.main.activity_camera.*
import java.util.concurrent.Executors


class LabelerActivity : AppCompatActivity() {
    lateinit var labeler: FirebaseVisionImageLabeler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // lay automl ra
        val localModel =
            FirebaseAutoMLLocalModel.Builder().setAssetFilePath("models/manifest.json").build()
        // tao option su dung automl model
        val options = FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel)
            .setConfidenceThreshold(0.5f)
            .build()

        // tao bo dat ten anh
         labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options)

        initCamera()

    }


    // setup cemra
    private fun initCamera() {
        // cac thu tuc setup camera
        // b1: tao listenablefuture<ProcessCameraProvider>
        val cameraProvider = ProcessCameraProvider.getInstance(this)
        //b2: bind camera sau khi chuan bi xong camera provider
        cameraProvider.addListener(Runnable {
            val get = cameraProvider.get()
            bindCamera(get)
        },ContextCompat.getMainExecutor(this))
    }



    @SuppressLint("RestrictedApi", "UnsafeExperimentalUsageError")
    private fun bindCamera(get: ProcessCameraProvider) {
//        val displayMetrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
//        val screenSize = Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
//        val aspectRatio = Rational(displayMetrics.widthPixels, displayMetrics.heightPixels)
//        val rotation = viewFinder.display.rotation

        // khoi tao preview
        val preview = Preview.Builder().build()
        // chon camera sau
        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        // khoi tao analysis
        val imageAnalysis = ImageAnalysis.Builder()
            .setCameraSelector(cameraSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        // set bo phan tich anh
        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(),ImageAnalysis.Analyzer { image ->
            val mediaImage: Image = image.getImage()!!
            val rotation1 = degreesToFirebaseRotation(image.imageInfo.rotationDegrees)
            val visionImage = FirebaseVisionImage.fromMediaImage(mediaImage, rotation1) // chuyen doi image -> firebasevisionimage


            // su dung mlkit xu ly va lay ket qua qua completeListener
            labeler.processImage(visionImage).addOnCompleteListener {
                val result = it.result
                val size = it.result?.size ?: 0
                if (size != 0) {
                    result?.get(0)?.let {
                        var tv = ""
                        // chuyen ve tieng viet
                        when (it.text) {
                            "circle" -> tv = "hình tròn"
                            "triangle" -> tv = "tam giác"
                            "rectangle" -> tv = "hình chữ nhật"
                        }

                        textView.setText(tv)
                        Log.i("value----------",  it.text)
                    }
                } else {
                    textView.setText("Không nhận ra")
                }

                image.close() // dong proxy phan tich: Quan trong
            }

        })

        // bind tat preview, analysis vao lifecycle
        val camera =
            CameraX.bindToLifecycle(this as LifecycleOwner, cameraSelector,imageAnalysis, preview)
        // lien ket preview voi previewView (set bo cung cap surface cua preview la bo cung cap provider tao tu previewview)
        preview.setSurfaceProvider(viewFinder.createSurfaceProvider(camera.cameraInfo))


    }
    // xu ly xoay man hinh
    private fun degreesToFirebaseRotation(degrees: Int): Int {
        return when (degrees) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> throw IllegalArgumentException(
                "Rotation must be 0, 90, 180, or 270."
            )
        }
    }


}




