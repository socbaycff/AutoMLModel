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

import kotlinx.android.synthetic.main.activity_label_image.*
import java.util.concurrent.Executors


class LabelerActivity : AppCompatActivity() {
    lateinit var labeler: FirebaseVisionImageLabeler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_image)

        // lấy model tu train ra từ thư mục asset
        val localModel =
            FirebaseAutoMLLocalModel.Builder().setAssetFilePath("models/manifest.json").build()
        // tạo options sử dụng model trên
        val options = FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel)
            .setConfidenceThreshold(0.5f)
            .build()

        // tạo bộ đặt tên ảnh
        //  labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler()  //trường hợp sử dụng model của google
        labeler = FirebaseVision.getInstance()
            .getOnDeviceAutoMLImageLabeler(options) // sư dụng option có model tự train

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
        }, ContextCompat.getMainExecutor(this))
    }


    /**
     * Hàm này biết lý thuyết cameraX mới hiểu code
     *
     */
    @SuppressLint("RestrictedApi", "UnsafeExperimentalUsageError")
    private fun bindCamera(get: ProcessCameraProvider) {
//        val displayMetrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
//        val screenSize = Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
//        val aspectRatio = Rational(displayMetrics.widthPixels, displayMetrics.heightPixels)
//        val rotation = viewFinder.display.rotation

        // khởi tạo preview
        val preview = Preview.Builder().build()
        // bộ chọn camera
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK) // su dung camera sau
            .build()

        // khởi tạo analysis
        val imageAnalysis = ImageAnalysis.Builder()
            .setCameraSelector(cameraSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        // set bộ phân tích ảnh
        imageAnalysis.setAnalyzer(
            Executors.newSingleThreadExecutor(),
            ImageAnalysis.Analyzer { image ->
                val mediaImage: Image = image.getImage()!!
                val rotation1 = degreesToFirebaseRotation(image.imageInfo.rotationDegrees)
                val visionImage = FirebaseVisionImage.fromMediaImage(
                    mediaImage,
                    rotation1
                ) // chuyen doi image -> firebasevisionimage


                // sử dụng mlkit để phân tích và lấy kết quả từ CompleteListener (k phải sucessListener)
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
                            Log.i("value----------", it.text)
                        }
                    } else {
                        textView.setText("Không nhận ra")
                    }

                    image.close() // đóng proxy phân tích: quan trọng
                }

            })

        // bind tất cả preview, analysis vào lifecycle
        val camera =
            CameraX.bindToLifecycle(this as LifecycleOwner, cameraSelector, imageAnalysis, preview)
        // liên kết preview với previewView (set bộ cung cap surface của preview là bộ cung cap provider tạo từ previewview)
        preview.setSurfaceProvider(viewFinder.createSurfaceProvider(camera.cameraInfo))
    }

    // xử lý xoay màn hình
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




