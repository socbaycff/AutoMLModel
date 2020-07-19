package com.example.automlmodel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    val MAIN_PERMISSION = arrayOf(Manifest.permission.CAMERA)
    val MAIN_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions() 
    }

    // kiểm tra permission (= checkselfPermission(string))
    private fun checkPermissions() {
        MAIN_PERMISSION.forEach {
            when {
                ContextCompat.checkSelfPermission(this,it) == PackageManager.PERMISSION_GRANTED -> startActivity(
                    (Intent(
                        this,
                        Main2Activity::class.java
                    ))
                )
                shouldShowRequestPermissionRationale(it) -> {
                    PermissionDialogFragment({
                        requestPermissions(MAIN_PERMISSION, MAIN_PERMISSION_REQUEST_CODE)
                    }, {
                        Toast.makeText(
                            applicationContext,
                            "Bạn sẽ không thể dùng chức năng camera",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                        .show(supportFragmentManager,"permissiondialog")

                }
                else -> requestPermissions(MAIN_PERMISSION, MAIN_PERMISSION_REQUEST_CODE)
            }
        }


    }


    // Xử lý sau khi user chọn quyền
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MAIN_PERMISSION_REQUEST_CODE) {
            val all = grantResults.all {
                it == PackageManager.PERMISSION_GRANTED
            }
            if (all) {
                startActivity((Intent(this, Main2Activity::class.java)))
            } else {
                Toast.makeText(
                    applicationContext,
                    "Bạn sẽ không thể dùng chức năng camera",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }
}
