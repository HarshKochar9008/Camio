package com.example.edgeviewer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.edgeviewer.camera.CameraController
import com.example.edgeviewer.gl.GLSurfaceRendererView

class MainActivity : ComponentActivity() {

    private lateinit var glView: GLSurfaceRendererView
    private lateinit var fpsView: TextView
    private lateinit var cameraController: CameraController

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera() else finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        glView = findViewById(R.id.glView)
        fpsView = findViewById(R.id.fpsView)

        cameraController = CameraController(this) { width, height, rgbaBytes, fps ->
            runOnUiThread {
                glView.updateFrame(width, height, rgbaBytes)
                fpsView.text = "FPS: %.1f".format(fps)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        glView.onResume()
        cameraController.start()
    }

    override fun onPause() {
        super.onPause()
        cameraController.stop()
        glView.onPause()
    }
}


