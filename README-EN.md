# HDRSample
This library implements HDR and SDR conversion using MediaCodec and OpenGL In Android. If you find it helpful, please give it a star. Your support is my greatest motivation to move forward.

HDR and SDR conversion aims to address the following issues:
1. Playback process: Not all phones support HDR screens, which can result in videos appearing gray. They need to be converted to SDR videos.
2. Editing process: When mixing HDR and SDR content for editing, either HDR needs to be converted to SDR or SDR needs to be converted to HDR to avoid color differences.

I have collected [articles related to HDR](articles-EN.md) and summarized the [journey of HDR to SDR conversion](https://juejin.cn/column/7206577654933471292). I developed this library with the hope of helping everyone.

![image](https://user-images.githubusercontent.com/4536178/222448632-f8dbfb59-11bc-4c5e-a0eb-e34f1dc72431.png)

The current implemented features are as follows, for everyone to learn and progress together:
1. **Output Modes** (Directly to Surface, or through OpenGL)
2. **View Modes** (Seamless switching between SurfaceView and TextureView)
3. Configuration for various texture sources (Auto, YUV420Buffer, external texture OES, Y2Y), and texture bit depth (8-bit, 10-bit, 16-bit)
4. **HDR to SDR CubeLut Configuration**, including PQ to SDR (12 options), HLG to SDR (4 options)
5. **HDR to SDR Shader Configuration**, this Shader supports chromatic correction, hue reference, hue mapping, color space conversion, gamma compression for both PQ and HLG videos.
6. Tone mapping is supported for **Android 8's Tonemap**, **Android 13's Tonemap**, **BT2446A**, **BT2446C**, and **Hable**.
7. Color space conversion is supported for **BT2020 to BT709Clip**, **Compress**, and **adaptive_l0_cusp**.
8. Seamless switching between 10 test videos.

**Planned Features**

- [ ] SDR to HDR inverse tone mapping
- [ ] Integration with Exoplayer
- [ ] Recognizing dynamic metadata for HDR10+

# Preview

[APK Download Link](https://github.com/JonaNorman/HDRSample/releases)

![HDR to SDR](preview/preview.gif)

# Key Code

All codes are annotated. 


1. [Shader](sample/src/main/java/com/norman/android/hdrsample/transform/shader) directory implements chromatic correction, hue reference, hue mapping, color space conversion, and gamma compression.
2. [YUV420FragmentShader](sample/src/main/java/com/norman/android/hdrsample/player/shader/YUV420FragmentShader.kt) and [GLYUV420Renderer](sample/src/main/java/com/norman/android/hdrsample/player/GLYUV420Renderer.java) implement the conversion of four types of YUV420 buffers to textures using pure Shader.
3. [Java code reads CubeLut file, optimized from about 3 seconds to 70 milliseconds](sample/src/main/java/com/norman/android/hdrsample/transform/CubeLutBuffer.java)
4. [Directly load CubeLut data with 3D textures](sample/src/main/java/com/norman/android/hdrsample/transform/CubeLutVideoTransform.java)
5. [Rendering of 2D textures, OES textures, Y2Y textures](sample/src/main/java/com/norman/android/hdrsample/player/shader/TextureFragmentShader.kt)
6. [Determine if MediaCodec supports 10-bit decoding](sample/src/main/java/com/norman/android/hdrsample/player/decode/ColorFormatHelper.java)
7. [Creating textures with different bit depths](sample/src/main/java/com/norman/android/hdrsample/util/GLESUtil.java)
8. [GLVideoOutputImpl](sample/src/main/java/com/norman/android/hdrsample/player/GLVideoOutputImpl.java)
9. [Asynchronous decoding with MediaCodec](sample/src/main/java/com/norman/android/hdrsample/player/decode/MediaCodecAsyncAdapter.java)
10. [OpenGL runtime environment encapsulation](sample/src/main/java/com/norman/android/hdrsample/opengl/GLEnvThreadManager.java)

# Practical Summary

- [x] [Journey of HDR to SDR Conversion (Part One) Process Summary](https://juejin.cn/post/7205908717886865469)
- [x] [Journey of HDR to SDR Conversion (Part Two) Decoding 10-bit YUV Textures](https://juejin.cn/post/7206577654933520444)
- [x] [Journey of HDR to SDR Conversion (Part Three) YUV420 to YUV Formula](https://juejin.cn/post/7207637337572606007)
- [x] [Journey of HDR to SDR Conversion (Part Four) YUV to RGB Matrix Derivation](https://juejin.cn/post/7208015274079256635)
- [x] [Journey of HDR to SDR Conversion (Part Five) Color Space Conversion BT2020 to BT709](https://juejin.cn/post/7208367266533949498)
- [x] [Journey of HDR to SDR Conversion (Part Six) Transfer Function and Chromatic Aberration Correction](https://juejin.cn/post/7208817601850277949)
- [x] [Journey of HDR to SDR Conversion (Part Seven) Gamma, HLG, PQ Formulas in Detail](https://juejin.cn/post/7231369710024310821)
- [x] [Journey of HDR to SDR Conversion (Part Eight) Tone Mapping](https://juejin.cn/post/7277875323165147190)
- [x] [Journey of HDR to SDR Conversion (Part Nine) Compilation of HDR Development Resources](https://juejin.cn/post/7278247059517227027)
- [ ] Journey of HDR to SDR Conversion (Part Ten) Exploration of SDR to HDR Inverse Tone Mapping

# ⭐ Star History

![Star History Chart](https://api.star-history.com/svg?repos=JonaNorman/HDRSample&type=Date)

# Special Thanks

| Stargazers                                 | Forkers                                      |
|--------------------------------------------|----------------------------------------------|
| [![Stargazers repo roster for @JonaNorman/HDRSample](https://reporoster.com/stars/JonaNorman/HDRSample)](https://github.com/JonaNorman/HDRSample/stargazers)  | [![Forkers repo roster for @JonaNorman/HDRSample](https://reporoster.com/forks/JonaNorman/HDRSample)](https://github.com/JonaNorman/HDRSample/network/members)  |

