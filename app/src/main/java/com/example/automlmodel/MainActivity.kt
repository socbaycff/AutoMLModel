package com.example.automlmodel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    val MAIN_PERMISSION = arrayOf(Manifest.permission.CAMERA)
    val MAIN_PERMISSION_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(checkPermissions()) {
          startActivity((Intent(this,CameraActivity::class.java)))
        } else {
            requestPermissions(MAIN_PERMISSION,MAIN_PERMISSION_CODE)

        }

    }


    private fun checkPermissions():Boolean {
        return MAIN_PERMISSION.all {
            checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MAIN_PERMISSION_CODE) {
            val all = grantResults.all {
                it == PackageManager.PERMISSION_GRANTED
            }
            if (all) {
                startActivity((Intent(this,CameraActivity::class.java)))
            } else {
                // bao error
            }
        }

    }
}
