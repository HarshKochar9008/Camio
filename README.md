## EdgeViewer

Android + OpenCV (C++) + OpenGL ES + Web (TypeScript) minimal assessment implementation.

### Project Structure

- `app/` Android app (Kotlin)
  - Camera2 via CameraX feeds frames to JNI
  - Native C++ processes frames (OpenCV optional) and returns RGBA
  - OpenGL ES 2.0 renders RGBA as texture
- `app/src/main/cpp/` C++ sources, `CMakeLists.txt`
- `web/` TypeScript web viewer (static sample, FPS/res overlay)

### Requirements

- Android Studio Hedgehog/Koala+ (AGP 8.6)
- NDK r26+, CMake 3.22+
- Optional: OpenCV Android SDK (for Canny). Without it, app falls back to grayscale.

### Build & Run (Android)

1. Open the project in Android Studio.
2. Ensure NDK and CMake are installed via SDK Manager.
3. (Optional) Enable OpenCV:
   - Download OpenCV Android SDK and set `OpenCV_DIR` in `app/CMakeLists.txt` environment or in Android Studio CMake profile to the SDK's `sdk/native/jni` path.
   - Rebuild. If found, native defines `HAVE_OPENCV` and uses Canny.
4. Run on a device (minSdk 24). Grant camera permission.

Controls:
- Displays processed frames (grayscale fallback or Canny if OpenCV present) with FPS overlay.

### Web Viewer

```
cd web
npm install
npm run build
npm run serve
```

Open `http://localhost:8080` to view a static processed frame and live-updating FPS text (simulated). Replace `sample` data URL in `web/src/index.ts` with a base64 captured from the Android app if desired.

### Notes

- JNI lib name: `edgeproc`.
- If OpenCV is not detected, the native code converts to grayscale. With OpenCV, it performs Canny edge detection and returns RGBA.
- The OpenGL renderer (`GLSurfaceRendererView` + `GLRenderer`) uploads RGBA frames to a texture and renders a fullscreen quad.

### Git Workflow

- Use meaningful, incremental commits:
  - Scaffold project
  - Camera feed
  - JNI/C++ processing
  - OpenGL rendering
  - Web viewer
  - README/instructions
- Push to a public or shareable private repository for evaluation.


