package com.example.automlmodel.ui.objectTracking

import android.annotation.SuppressLint
import android.graphics.Rect
import android.media.Image
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraX
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.example.automlmodel.R
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import kotlinx.android.synthetic.main.fragment_tracking_object.*
import kotlinx.android.synthetic.main.fragment_tracking_object.view.*
import java.util.concurrent.Executors

class ObjectTrack : Fragment() {
    lateinit var objectDetector: FirebaseVisionObjectDetector
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_tracking_object, container, false)
        root.post {
            root.trackingView.updateViewSize(Size(root.width, root.height))
        }
        val options = FirebaseVisionObjectDetectorOptions.Builder()
            .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .build()

        objectDetector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options)


        initCamera()

        return root
    }

    // setup cemra
    private fun initCamera() {
        // cac thu tuc setup camera
        // b1: tao listenablefuture<ProcessCameraProvider>
        val cameraProvider = ProcessCameraProvider.getInstance(context!!)
        //b2: bind camera sau khi chuan bi xong camera provider
        cameraProvider.addListener(Runnable {
            val get = cameraProvider.get()
            bindCamera(get)
        }, ContextCompat.getMainExecutor(context))
    }

    @SuppressLint("UnsafeExperimentalUsageError", "RestrictedApi")
    private fun bindCamera(get: ProcessCameraProvider?) {
        // khoi tao preview
        val preview = Preview.Builder().build()
        // chon camera sau
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        // khoi tao analysis
        val imageAnalysis = ImageAnalysis.Builder()
            .setCameraSelector(cameraSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        // set bo phan tich anh
        imageAnalysis.setAnalyzer(
            Executors.newSingleThreadExecutor(),
            ImageAnalysis.Analyzer { image ->
                val mediaImage: Image = image.getImage()!!
                val rotation1 = degreesToFirebaseRotation(image.imageInfo.rotationDegrees)
                val visionImage = FirebaseVisionImage.fromMediaImage(
                    mediaImage,
                    rotation1
                ) // chuyen doi image -> firebasevisionimage

                // su dung mlkit xu ly va lay ket qua qua completeListener
                objectDetector.processImage(visionImage).addOnCompleteListener {
                    val result = it.result
                    val size = result?.size
                    if (size != 0) {
                        val boundList = arrayListOf<Rect>()
                        result?.forEach {
                            boundList.add(it.boundingBox)
                        }
                        if (trackingView != null) {
                            trackingView.updateBound(boundList)
                        }

                    }
                    image.close() // dong luong
                }


            })

        // bind tat preview, analysis vao lifecycle
        val camera =
            CameraX.bindToLifecycle(this as LifecycleOwner, cameraSelector, imageAnalysis, preview)
        // lien ket preview voi previewView (set bo cung cap surface cua preview la bo cung cap provider tao tu previewview)
        preview.setSurfaceProvider(trackFinder.createSurfaceProvider(camera.cameraInfo))

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
