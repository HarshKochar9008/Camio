#include <jni.h>
#include <android/log.h>
#include <vector>

#ifdef HAVE_OPENCV
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#endif

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "edgeproc", __VA_ARGS__)

static void nv21ToRGBA(const uint8_t* nv21, int width, int height, std::vector<uint8_t>& rgbaOut) {
    // Simple NV21 -> RGBA conversion (nearest). Not optimized.
    auto clamp = [](int v){ return v < 0 ? 0 : (v > 255 ? 255 : v); };
    const int frameSize = width * height;
    rgbaOut.resize(frameSize * 4);
    for (int j = 0, yp = 0; j < height; j++) {
        int uvp = frameSize + (j >> 1) * width;
        int u = 0, v = 0;
        for (int i = 0; i < width; i++, yp++) {
            if ((i & 1) == 0) {
                v = nv21[uvp++] - 128;
                u = nv21[uvp++] - 128;
            }
            int y = nv21[yp] & 0xff;
            int y1192 = 1192 * (y - 16);
            if (y1192 < 0) y1192 = 0;
            int r = (y1192 + 1634 * v) >> 10;
            int g = (y1192 - 833 * v - 400 * u) >> 10;
            int b = (y1192 + 2066 * u) >> 10;
            r = clamp(r); g = clamp(g); b = clamp(b);
            int idx = yp * 4;
            rgbaOut[idx + 0] = (uint8_t)r;
            rgbaOut[idx + 1] = (uint8_t)g;
            rgbaOut[idx + 2] = (uint8_t)b;
            rgbaOut[idx + 3] = 255;
        }
    }
}

static void rgbaToGray(const uint8_t* rgba, int width, int height, std::vector<uint8_t>& grayOut) {
    grayOut.resize(width * height);
    for (int i = 0; i < width * height; ++i) {
        const uint8_t r = rgba[4*i + 0];
        const uint8_t g = rgba[4*i + 1];
        const uint8_t b = rgba[4*i + 2];
        grayOut[i] = static_cast<uint8_t>(0.299f*r + 0.587f*g + 0.114f*b);
    }
}

static void grayToRGBA(const uint8_t* gray, int width, int height, std::vector<uint8_t>& rgbaOut) {
    rgbaOut.resize(width * height * 4);
    for (int i = 0; i < width * height; ++i) {
        uint8_t v = gray[i];
        rgbaOut[4*i + 0] = v;
        rgbaOut[4*i + 1] = v;
        rgbaOut[4*i + 2] = v;
        rgbaOut[4*i + 3] = 255;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_edgeviewer_nativebridge_NativeProcessor_processNV21ToRGBA(
        JNIEnv* env, jobject /*thiz*/, jbyteArray nv21_, jint width, jint height, jbyteArray outRgba_) {
    const int w = width;
    const int h = height;
    const int inSize = env->GetArrayLength(nv21_);
    const int outSize = env->GetArrayLength(outRgba_);
    std::vector<uint8_t> nv21(inSize);
    env->GetByteArrayRegion(nv21_, 0, inSize, reinterpret_cast<jbyte*>(nv21.data()));

    std::vector<uint8_t> rgba;
    nv21ToRGBA(nv21.data(), w, h, rgba);

#ifdef HAVE_OPENCV
    try {
        cv::Mat rgbaMat(h, w, CV_8UC4, rgba.data());
        cv::Mat gray;
        cv::cvtColor(rgbaMat, gray, cv::COLOR_RGBA2GRAY);
        cv::Mat edges;
        cv::Canny(gray, edges, 80, 160);
        cv::Mat edgesRgba;
        cv::cvtColor(edges, edgesRgba, cv::COLOR_GRAY2RGBA);
        if (edgesRgba.total() * 4 == (size_t)outSize) {
            env->SetByteArrayRegion(outRgba_, 0, outSize, reinterpret_cast<const jbyte*>(edgesRgba.data));
            return;
        }
    } catch(...) {
        // Fallback below
    }
#endif

    // Fallback: grayscale only
    std::vector<uint8_t> gray;
    rgbaToGray(rgba.data(), w, h, gray);
    std::vector<uint8_t> out;
    grayToRGBA(gray.data(), w, h, out);
    env->SetByteArrayRegion(outRgba_, 0, out.size(), reinterpret_cast<const jbyte*>(out.data()));
}


