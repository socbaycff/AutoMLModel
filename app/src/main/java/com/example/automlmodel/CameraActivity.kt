package com.example.automlmodel

import android.annotation.SuppressLint
import android.media.Image
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions

import kotlinx.android.synthetic.main.activity_camera.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class CameraActivity : AppCompatActivity() {
    lateinit var labelDetector: FirebaseVisionImageLabeler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val localModel =
            FirebaseAutoMLLocalModel.Builder().setAssetFilePath("models/manifest.json").build()
        val options = FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel)
            .setConfidenceThreshold(0.2f)
            .build()

         labelDetector = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options)

        initCamera()

    }



    private fun initCamera() {
        val cameraProvider = ProcessCameraProvider.getInstance(this)
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

        val preview = Preview.Builder().build()
        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setCameraSelector(cameraSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(),ImageAnalysis.Analyzer { image ->
            val mediaImage: Image = image.getImage()!!
            val rotation1 = degreesToFirebaseRotation(image.imageInfo.rotationDegrees)
            val image1 = FirebaseVisionImage.fromMediaImage(mediaImage, rotation1)
            labelDetector.processImage(image1).addOnSuccessListener {
                //   val label = it[0].text
                //  textView.text = label
                Log.i("suscess-------------","")
            }.addOnCompleteListener {
                it.result?.get(0)?.let {
                    textView.setText(it.text)
                    Log.i("value----------",  it.text)
                    image.close()
                }

            }

        })


        val camera =
            CameraX.bindToLifecycle(this as LifecycleOwner, cameraSelector,imageAnalysis, preview)

        preview.setSurfaceProvider(viewFinder.createSurfaceProvider(camera.cameraInfo))


    }
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




