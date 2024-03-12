# HDRSample
[EN](README-EN.md)

这个库用MediaCode和OpenGL实现了HDR和SDR转换，如果你觉得有所收获，给这个库点个赞吧，你的鼓励是我前进最大的动力。
HDR和SDR转换是为了解决以下问题
1. 播放流程，不是所有手机都支持HDR屏幕会导致视频变灰，需要转换成SDR视频
2. 编辑流程，HDR和SDR混合编辑时，要不HDR转SDR，要不SDR转HDR，不然会出现色差

我搜集了和[HDR相关的资料](articles.md)，总结了[HDR转SDR实践之旅](https://juejin.cn/column/7206577654933471292)，开发了这个库希望能帮到大家

![image](https://user-images.githubusercontent.com/4536178/222448632-f8dbfb59-11bc-4c5e-a0eb-e34f1dc72431.png)

现有功能实现如下，供大家一起学习一起上进
1. **输出模式**(直接输出到Surface、经过OpenGL中转)
2. **视图模式**(无缝切换SurfaceView和TextureView)
3. **多种纹理来源配置**(Auto、YUV420Buffer、外部纹理OES、Y2Y)、纹理位深配置(8位、10位、16位)
4. **HDR转SDR CubeLut配置**，PQ转SDR12种、HLG转SDR4种
5. **HDR转SDR Shader配置**，该Shader支持对PQ视频和HLG视频进行色度矫正、色调参考、色调映射、色域转换、Gamma压缩
6. 色调映射已支持**Android8的Tonemap、Android13的Tonemap、BT2446A、BT2446C、Hable**
7. 色域转换已支持**BT2020转BT709Clip、Compress、adpative_l0_cusp**
8. 10个测试视频无缝切换

**待开发功能** 

- [ ] SDR转HDR逆色调映射 
- [ ] 对接Exoplayer
- [ ] 识别HDR10+的动态元信息


# 效果预览
[安装包地址](https://github.com/JonaNorman/HDRSample/releases)

![HDR转SDR](preview/preview.gif)

# 关键代码

代码已经都加上注释了，有疑惑可以扫码加HDR视频开发讨论群，如果二维码显示过期了，可以搜索微信号JonaNorman加我个人微信拉你进群(请备注HDR视频开发)

<img src="/preview/wechat.jpg" width="300px">


1. [Shader](sample/src/main/java/com/norman/android/hdrsample/transform/shader)目录下实现了色度矫正、色调参考、色调映射、色域转换、Gamma压缩
2. [YUV420FragmentShader](sample/src/main/java/com/norman/android/hdrsample/player/shader/YUV420FragmentShader.kt)和[GLYUV420Renderer](sample/src/main/java/com/norman/android/hdrsample/player/GLYUV420Renderer.java) 纯Shader实现四种YUV420的Buffer转换成纹理
3. [Java代码读取CubeLut文件，从3s左右优化成70ms](sample/src/main/java/com/norman/android/hdrsample/transform/CubeLutBuffer.java)
4. [直接使用3D纹理加载CubeLut数据](sample/src/main/java/com/norman/android/hdrsample/transform/CubeLutVideoTransform.java)
5. [2D纹理、OES纹理、Y2Y纹理渲染](sample/src/main/java/com/norman/android/hdrsample/player/shader/TextureFragmentShader.kt)
6. [判断MediaCodec是否支持10位解码](sample/src/main/java/com/norman/android/hdrsample/player/decode/ColorFormatHelper.java)
7. [不同位深的纹理创建](sample/src/main/java/com/norman/android/hdrsample/util/GLESUtil.java)
8. [GLVideoOutputImpl](sample/src/main/java/com/norman/android/hdrsample/player/GLVideoOutputImpl.java)
9. [MediaCodec异步解码](sample/src/main/java/com/norman/android/hdrsample/player/decode/MediaCodecAsyncAdapter.java)
10. [OpenGL运行环境封装](sample/src/main/java/com/norman/android/hdrsample/opengl/GLEnvThreadManager.java)



# 实践总结

- [x] [HDR转SDR实践之旅(一)流程总结](https://juejin.cn/post/7205908717886865469)
- [x] [HDR转SDR实践之旅(二)解码10位YUV纹理 ](https://juejin.cn/post/7206577654933520444)
- [x] [HDR转SDR实践之旅(三)YUV420转YUV公式](https://juejin.cn/post/7207637337572606007)
- [x] [HDR转SDR实践之旅(四)YUV转RGB矩阵推导](https://juejin.cn/post/7208015274079256635) 
- [x] [HDR转SDR实践之旅(五)色域转换BT2020转BT709](https://juejin.cn/post/7208367266533949498) 
- [x] [HDR转SDR实践之旅(六)传递函数与色差矫正](https://juejin.cn/post/7208817601850277949)
- [x] [HDR转SDR实践之旅(七)Gamma、HLG、PQ公式详解](https://juejin.cn/post/7231369710024310821) 
- [x] [HDR转SDR实践之旅(八)色调映射](https://juejin.cn/post/7277875323165147190) 
- [x] [HDR转SDR实践之旅(九)HDR开发资源汇总](https://juejin.cn/post/7278247059517227027)
- [ ] HDR转SDR实践之旅(十)SDR转HDR逆色调映射探索

# ⭐ star历史

![Star History Chart](https://api.star-history.com/svg?repos=JonaNorman/HDRSample&type=Date)


# 特别感谢

| Stargazers                                                                                                 | Forkers                                                                                                                 |
|---------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------|
| [![Stargazers repo roster for @JonaNorman/HDRSample](https://reporoster.com/stars/JonaNorman/HDRSample)](https://github.com/JonaNorman/HDRSample/stargazers)                                          | [![Forkers repo roster for @JonaNorman/HDRSample](https://reporoster.com/forks/JonaNorman/HDRSample)](https://github.com/JonaNorman/HDRSample/network/members)                            |
