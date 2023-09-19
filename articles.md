# 背景
如果你是一名视频开发者，你会发现关于HDR相关的资料很少没有成体系的总结，感觉像在黑夜里走路，我想点亮一盏灯，把开发中看到的好文章记录总结下来分享给大家，大家有看到好的文章可以在github提交，也许就能帮到某个小伙伴。
如果你觉得有所收获，来给[HDR转SDR开源代码](https://github.com/JonaNorman/HDRSample)点个赞吧，你的鼓励是我前进最大的动力。
# 经验分享

## 开发分享

| 名称 |备注  |
| --- | --- |
| [HDR转SDR实践之旅](https://juejin.cn/column/7206577654933471292)⭐ | 该系列从开发遇到的问题作为切入点浅显易懂讲解HDR转换理论</br>代码地址[HDRSample🔥](https://github.com/JonaNorman/HDRSample)|
|[HDR in Android](https://blog.csdn.net/a360940265a/category_11625435.html)⭐|用10位纹理参考ffmpeg用OpenGL+hable色调映射完成hdr转sdr|
|[BT.2446 Method C (HDR to SDR 变换)](https://trev16.hatenablog.com/entry/2020/08/01/131907)⭐|对BT2446的C方法进行研究提供了[BT2446C、Youtube的HDR转SDR LUT](https://drive.google.com/file/d/1B5il7F9DRdg6ipcZdyMcLHv465jimFvt/view)|
|[MovieLabs_Mapping_PQ_to_HLG_v1.0](https://movielabs.com/ngvideo/MovieLabs_Mapping_PQ_to_HLG_v1.0.pdf)⭐|MovieLabs使用MaxRGB方法映射PQ到HLG的实践|
|[Adreno GPU上Android 游戏开发介绍](https://blog.csdn.net/weixin_38498942/article/details/118160665)|如何用OpenGL实现真HDR播放|
|[HDR中HLG与PQ曲线的互转](https://blog.csdn.net/qq26983255/article/details/109824531/)|HLG和PQ公式互转实践|

## 开源代码
| 名称 |备注  |
| --- | --- |
| [HDRSample🔥](https://github.com/JonaNorman/HDRSample) |[HDR转SDR实践之旅](https://juejin.cn/column/7206577654933471292)的开源代码|
|[hdrtoys](https://github.com/natural-harmonia-gropius/hdr-toys)⭐|为mpv播放器实现的HDR转SDR插件|
|[android](https://android.googlesource.com/platform/frameworks/native/+/refs/heads/master/libs/tonemap/tonemap.cpp)⭐|android中的tonemap|
|[libplacebo tonemap](https://code.videolan.org/videolan/libplacebo/-/blob/master/src/tone_mapping.c)⭐|mpv播放器的tonemap|
|[ffmpeg](https://github.com/FFmpeg/FFmpeg/blob/master/libavfilter/vf_tonemap.c)|ffmpeg中的tonemap|
|[kodi](https://github.com/xbmc/xbmc/blob/1e499e091f7950c70366d64ab2d8c4f3a18cfbfa/system/shaders/GL/1.5/gl_tonemap.glsl#L4)|kodi中的色调映射|
|[gopro](https://github.com/gopro/gopro-lib-node.gl/blob/main/libnodegl/src/glsl/hdr_pq2sdr.frag)|bt2446a的色调映射实现|
|[glsl-tone-map](https://github.com/dmnsgn/glsl-tone-map)|常见的色调映射曲线|
|[HDRTVNET](https://github.com/chxy95/HDRTVNet)|通过深度学习实现SDR转HDR|
|[Opencv TMO](https://github.com/opencv/opencv/blob/17234f82d025e3bbfbf611089637e5aa2038e7b8/modules/photo/src/tonemap.cpp)|Opencv的色调映射|

## 理论分享
| 标题 |  |
| --- | --- |
|[色彩空间为什么那么空](https://www.bilibili.com/video/BV19e4y1y7Mo)⭐|可能是中文视频里面色彩空间讲得最好的|
|[亚明专栏](https://www.imaschina.com/author/u/108.html)⭐|充分讲解了BT2100、BT2048的内容(参考白电平、场景参考、显示参考、HDR和SDR上转下转的对比)，专栏有13篇文章，也可以直接看[PPT](http://gongj.cbgcloud.com/TrainManage/PDF/6440201C-C099-4046-9E35-60CA38D866A3.pdf)|
|[逆色调映射算法的研究及其在影视领域中的应用](https://m.fx361.com/news/2021/0304/10852880.html)⭐|总结了多种逆色调映射算法在SDR转HDR的应用|
|[漫谈HDR和色彩管理](https://zhuanlan.zhihu.com/p/129095380)⭐|从色彩空间一步步讲解游戏中的HDR|
| [HDR in Call of Duty](https://research.activision.com/publications/archives/hdr-in-call-of-duty)| 使命召唤中的HDR|
| [HDR硬件成像技术](https://blog.csdn.net/nyist_yangguang/article/details/123056556) | [HDR 成像技术学习（一）](https://blog.csdn.net/nyist_yangguang/article/details/123056556)</br>[HDR 成像技术学习（二）](https://blog.csdn.net/nyist_yangguang/article/details/123094698)</br>[HDR 成像技术学习（三）](https://blog.csdn.net/nyist_yangguang/article/details/123122096) |
|色调曲线汇总|[HDR Tone Mapping](https://xiaoiver.github.io/coding/2019/02/05/HDR-Tone-Mapping.html)</br>[tonemapping](https://64.github.io/tonemapping/)</br>[local-tonemapping](https://bartwronski.com/2022/02/28/exposure-fusion-local-tonemapping-for-real-time-rendering/)|



## 业界分享

| 名称 |备注  |
| --- | --- |
|[浅谈七牛云SDR转HDR的技术实现](https://mp.weixin.qq.com/s/MLUTVNLWN1u_v7h7JxlVgA)|介绍了七牛云SDR转HDR色调映射公式、色度提升公式、细节保留方法|
|[腾讯SDR转HDR分享](https://mp.weixin.qq.com/s/wjDEgCQ2wY2EVpAKT3ooEg)|介绍了SDR转HDR流程，亮度范围扩展、细节恢复、噪声和失真抑制、色域扩展、动态亮度调整|
|[微帧科技HDR分享](https://mp.weixin.qq.com/s/2-inQKbDBGERIWiMCAPKgw)|在视频转码前对一个视频帧中的不同区域推导出不同的直方图均衡化转换函数，增强对比度达到SDR有HDR的观感|
|[快手HDR视频生成算法分享](https://mp.weixin.qq.com/s/ditOdwY3hSOvn1go3YMQVw)与[论文](https://arxiv.org/abs/2207.00319)|1.用HDCFM（分层动态上下文特征映射模型)完成视频帧的暗部增强与亮部抑制</br>2.用PDCG(补丁判别器的高光生成模型)完成HDR视频帧的高光生成|
|[快手HDR转SDR分享](https://blog.csdn.net/vn9PLgZvnPs1522s82g/article/details/118212043)|介绍了HDR视频的特性和亮度感知模型、快手在SDR和HDR混合编辑做的事情|
|[微博HDR分享](https://blog.csdn.net/vn9PLgZvnPs1522s82g/article/details/122227335)|介绍HDR视频的特性和对微博视频链路的改造|
|[西瓜视频HDR分享](https://www.infoq.cn/article/otnx8vjwhanup2altsiu)|介绍了火山引擎实验室HDR视频上传、转码、分发、终端播放的解决方案|
|[OPPOHDR分享](https://blog.csdn.net/zhying719/article/details/123606237)|介绍了HDR视频的特性和业界生态|
|[百度HDR分享](https://xie.infoq.cn/article/7f362dffbece97640e26b6caa)|介绍了百度的智感超清HDR应用实践|
|[华为云视频AI转码领域分享](https://blog.csdn.net/vn9PLgZvnPs1522s82g/article/details/124008425)|介绍了华为云在画质增强、SDR转HDR的技术实践|
|[淘宝音视频算法分享](https://blog.csdn.net/vn9PLgZvnPs1522s82g/article/details/117004023)|介绍了淘宝自研APG高压缩率图片格式、H265的落地与优化、高清转码、HDR10端到端播放、自研AliDenoise声音智能降噪、TaoAudio音频解决方案|


# 标准文档
## 国外
| 名称 |备注  |
| --- | --- |
| [BT1886](https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.1886-0-201103-I!!PDF-C.pdf) | BT1886定义CRT显示器的传递函数 |
| [BT709](https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.709-6-201506-I!!PDF-C.pdf) | BT709定义高清视频BT709色彩空间 |
| [BT2020](https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.2020-2-201510-I!!PDF-C.pdf)| BT2020定义超高清视频BT20202色彩空间|
| [BT2087](https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.2087-0-201510-I!!PDF-C.pdf)⭐| 1. 提供了BT709转BT2020直接映射的方法</br>2.使用场景映射、显示映射的对比|
| [BT2100](https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.2100-2-201807-I!!PDF-E.pdf)| 在BT2020色彩空间基础上增加了PQ和HLG传递函数|
|[BT2111](https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.2111-2-202012-I!!PDF-C.pdf)|PQ、HLG色彩条测试图规范|
|[BT2250](https://www.itu.int/dms_pub/itu-r/opb/rep/R-REP-BT.2250-2012-PDF-E.pdf)|  如何推导色域转换矩阵|
|[BT2390](https://www.itu.int/dms_pub/itu-r/opb/rep/R-REP-BT.2390-10-2021-PDF-E.pdf)⭐| 1. 对BT2100PQ和HLG的传递函数进行了补充讲解</br>2.YCBCR和ICTCP的颜色处理比较</br>|
| [BT2407](https://www.itu.int/dms_pub/itu-r/opb/rep/R-REP-BT.2407-2017-PDF-E.pdf)⭐|1. 如何用lab实现色调、亮度、色度映射完成BT2020转BT709避免直接映射带来的饱和度和色差问题</br> 2. 一种luv软削波方法完成BT2020转BT709 |
|[BT2408](https://www.itu.int/dms_pub/itu-r/opb/rep/R-REP-BT.2408-5-2022-PDF-E.pdf)⭐| HDR电视制作操作实践指南</br>1.重新定义BT2100PQ和HLG的参考白电平</br>2.如何通过EETF函数降低PQ的亮度</br>3. sdr和hdr转换的直接映射方法</br>4.PQ和HLG的互转方法|
| [BT2446](https://www.itu.int/dms_pub/itu-r/opb/rep/R-REP-BT.2446-1-2021-PDF-E.pdf)| HDR和SDR互相转换的3个动态映射方法|
|[ST2084](https://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=7291452)|PQ传递函数OETF定义|
|[ST2086](https://www.voukoder.org/attachment/1647-mastering-display-color-volume-metadata-supporting-high-luminanc-pdf/)|HDR静态元数据定义|
|[ST2094](https://forum.selur.net/attachment.php?aid=611)|HDR动态元数据定义</br>[ST 2094-1](https://ieeexplore.ieee.org/document/7513361)</br>[ST 2094-2](https://ieeexplore.ieee.org/document/7839894)</br>[ST 2094-10](https://ieeexplore.ieee.org/document/9405553)</br>[ST 2094-20](https://ieeexplore.ieee.org/document/7523881)</br>[ST 2094-30](https://ieeexplore.ieee.org/document/7523878)</br>[ST 2094-40](https://ieeexplore.ieee.org/document/9095450)|
|[SL-HDR](https://en.wikipedia.org/wiki/High-dynamic-range_television#Other_formats)|HLG和PQ以外的另外一个HDR标准</br>[SL-HDR1](https://www.etsi.org/deliver/etsi_ts/103400_103499/10343301/01.04.01_60/ts_10343301v010401p.pdf)</br>[SL-HDR2](https://www.etsi.org/deliver/etsi_ts/103400_103499/10343302/01.03.01_60/ts_10343302v010301p.pdf)</br>[SL-HDR3](https://www.etsi.org/deliver/etsi_ts/103400_103499/10343303/01.02.01_60/ts_10343303v010201p.pdf)</br>|
|[Khronos数据规范](https://registry.khronos.org/DataFormat/specs/1.3/dataformat.1.3.inline.html)|Khronosd的文档中整理了PQ和HLG传递函数公式|

## 国内
| 标题 |备注  |
| --- | --- |
|[高动态范围电视系统显示适配元数据技术要求](http://www.nrta.gov.cn/art/2022/9/29/art_3715_61973.html)⭐|广电总局关于H.265中视频对于HDR元数据定义|
|[标准动态范围和高动态范围转换方法研究与测试](http://www.tvoao.com/a/206282.aspx)⭐|对BT2087和BT2390提出的HDR和SDR直接映射转换的测试研究|
|[4K超高清电视节目制作技术实施指南2020版](http://www.nrta.gov.cn/module/download/downfile.jsp?classid=0&filename=f6761c8580244d56a7ac6ced475c99f4.pdf)|广电总局关于BT2408的实践指南</br>HDR制作过程中的注意事项</br>参考白电平75%HLG、58%PQ的解释|
|[中央广播电视总台HDR视频制作白皮书2022版](https://sysbc.scmc.edu.cn/2022_09/15_15/content-33329.html)|广电总局关于BT2408的实践指南</br>HDR制作过程中的注意事项</br>常见肤色亮度</br>HLG和SDR转换时电平映射关系</br>光电的HLG和SDR转换LUT(没找到LUT文件)|
|[高动态范围电视节目制作和交换图像参数值](http://www.nrta.gov.cn/art/2020/4/24/art_3715_50842.html)| 广电总局关于BT2309的解释，定义了PQ和HLG的传递函数|
|[超高清高动态范围视频系统彩条测试图](http://www.nrta.gov.cn/art/2020/4/24/art_3715_50842.html)| 广电总局关于BT2111色彩条的解释|

# Cube LUT
LUT直接转换HDR和SDR

| 名称 |备注  |
| --- | --- |
|[NBCU开源HDRLUT](https://github.com/digitaltvguy/NBCUniversal-UHD-HDR-SDR-Single-Master-Production-Workflow-Recommendation-LUTs/tree/main/LUTS_for_Hardware_Devices_DaVinci_Resolve_CUBE_LUT_Format)⭐|[NBCU_SDR2HLG_DL_v1.1.cube](https://github.com/digitaltvguy/NBCUniversal-UHD-HDR-SDR-Single-Master-Production-Workflow-Recommendation-LUTs/blob/main/LUTS_for_Hardware_Devices_DaVinci_Resolve_CUBE_LUT_Format/1-NBCU_SDR2HLG_DL_v1.1.cube)</br>[NBCU_SDR2HLG_SL_v1.cube](https://github.com/digitaltvguy/NBCUniversal-UHD-HDR-SDR-Single-Master-Production-Workflow-Recommendation-LUTs/blob/main/LUTS_for_Hardware_Devices_DaVinci_Resolve_CUBE_LUT_Format/2-NBCU_SDR2HLG_SL_v1.cube)</br>[NBCU_HLG2SDR_DL_v1.1.cube](https://github.com/digitaltvguy/NBCUniversal-UHD-HDR-SDR-Single-Master-Production-Workflow-Recommendation-LUTs/blob/main/LUTS_for_Hardware_Devices_DaVinci_Resolve_CUBE_LUT_Format/3-NBCU_HLG2SDR_DL_v1.1.cube)</br>[NBCU_SDR2PQ_DL_v1.cube](https://github.com/digitaltvguy/NBCUniversal-UHD-HDR-SDR-Single-Master-Production-Workflow-Recommendation-LUTs/blob/main/LUTS_for_Hardware_Devices_DaVinci_Resolve_CUBE_LUT_Format/4-NBCU_SDR2PQ_DL_v1.cube)</br>[NBCU_PQ2SDR_DL_v1.cube](https://github.com/digitaltvguy/NBCUniversal-UHD-HDR-SDR-Single-Master-Production-Workflow-Recommendation-LUTs/blob/main/LUTS_for_Hardware_Devices_DaVinci_Resolve_CUBE_LUT_Format/5-NBCU_PQ2SDR_DL_v1.cube)</br>[NBCU-HLG2PQ_1000nit_v1.cube](https://github.com/digitaltvguy/NBCUniversal-UHD-HDR-SDR-Single-Master-Production-Workflow-Recommendation-LUTs/blob/main/LUTS_for_Hardware_Devices_DaVinci_Resolve_CUBE_LUT_Format/7-NBCU-HLG2PQ_1000nit_v1.cube)|
|[LinearTransformation](https://github.com/FranceBB/LinearTransformation)⭐|线性色域转换，包含了BT2020PQ、BT2020HLG、BT709互相转换|



# 测试视频

| 名称 |备注  |
| --- | --- |
|[HDRVideo](https://github.com/JonaNorman/HDRSample/tree/main/sample/src/main/assets/video)|收集了10个常见的HDR视频|
|[Mehanik HDR10 test patterns](https://drive.google.com/drive/folders/1m4IBq0euAxamL9ePgfdFuf8_5nLcRwHA)⭐|灰度、色调、抖动、白电平、黑电平测试视频|
|[tfb-video测试视频生成代码](https://github.com/test-full-band/tfb-video)⭐|代码生成测试视频</br>[视频地址](https://github.com/test-full-band/tfb-video/releases)|
|[arib_std-b72色彩条视频代码](https://github.com/kcamovie/arib_std-b72)|根据arib_std-b72生成的色彩条测试视频，该视频加上波形监视器可以准确检测色准</br>[ARIB STD-B72 Colour Bar Test Pattern for HLG](https://www.youtube.com/watch?v=YiFP1xrRE4c)|
|[BT2111-DV](https://github.com/saindriches/BT2111-DV)|根据BT2111修改的杜比视界Profile 5色彩条测试视频，该视频加上波形监视器可以准确检测色准</br>[BT2111-MOD_DV5_UHD_24P.mp4](https://github.com/saindriches/BT2111-DV/blob/master/BT2111-MOD_DV5_UHD_24P.mp4)|
| [Kodi测试视频](https://kodi.wiki/view/Samples) ⭐| [HDR 10-bit HEVC 25fps](https://drive.google.com/file/d/0BwxFVkl63-lEdU9Ma0VYc3YxSVE/view?usp=sharing&resourcekey=0-FJsSx7fEhiVEtQMRwo_ePQ)</br>[HDR 10-bit Grayscale Rotating Gradients](https://www.projectorcentral.com/images/articles/10bit_HDR_Grayscale_ProjectorCentral_4K_(HEVC_10-bit).mp4)</br>[HDR10 HEVC 59.94](https://drive.google.com/file/d/1Ic9DZXMSo07EJMqCFaQRKSSrSw6y1mYv/view?usp=sharing)</br>[HDR10+ Profile A HEVC 10-bit 23.976 Sample](https://mega.nz/file/af4zSAbQ#gBiHRiX3oLnBvxMNnytC08v8DRkKzQIkhGpg96nAWXE)</br>[HDR10+ Profile B HEVC 10-bit 23.976 Sample](https://mega.nz/file/nehDka6Z#C5_OPbSZkONdOp1jRmc09C9-viDc3zMj8ZHruHcWKyA)</br>[HDR10+ Dynamic Metadata Test 60fps](https://we.tl/t-3mO1E8BGEZ) |
|[电视测试视频](https://www.aliyundrive.com/s/5Q2cYnGobLj/folder/613d9d1cf6c1d70510764f529d585cc543fdc3db)|电视机屏幕测试视频|
|[DisplayHDRTest](https://github.com/vesa-org/DisplayHDRTest) ⭐|测试HDR显示器和视频颜色是否标准|


# 工具

| 名称 |备注  |
| --- | --- |
| [colorspacecalculator](http://color.support/colorspacecalculator.html)⭐|  在线色域转换矩阵计算器|
| [rainbowspace](http://rainbowspace.xyz/)|  图片色域可视化|
| [colorconverter](https://ajalt.github.io/colormath/converter/)|  在线颜色转换工具|
| [colour](https://github.com/colour-science/colour)|  颜色算法python工具包|
| [XYZ色彩空间](https://horizon-lab.org/colorvis/xyz.html)|  XYZ色彩空间可视化|



