package com.example.edgeviewer.nativebridge

object NativeProcessor {
    init {
        System.loadLibrary("edgeproc")
    }

    external fun processNV21ToRGBA(
        nv21: ByteArray,
        width: Int,
        height: Int,
        outRgba: ByteArray
    )
}


