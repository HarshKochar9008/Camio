package com.example.edgeviewer.gl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class GLSurfaceRendererView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private val rendererImpl = GLRenderer()

    init {
        setEGLContextClientVersion(2)
        setRenderer(rendererImpl)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun updateFrame(width: Int, height: Int, rgba: ByteArray) {
        rendererImpl.updateFrame(width, height, rgba)
        requestRender()
    }
}


