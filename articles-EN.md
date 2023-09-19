# Background
As a video developer, you may find that there is a lack of systematic summaries on HDR-related information, and it feels like walking in the dark. I want to light a lamp and record and summarize good articles I come across in development to share with everyone. If you come across good articles, you can submit them on GitHub, maybe it will help a fellow developer.

If you find this information helpful, please give a thumbs up to the [HDR to SDR open source code](https://github.com/JonaNorman/HDRSample). Your encouragement is my greatest motivation to move forward.

# Experience Sharing

## Development Sharing

| Name | Notes |
| --- | --- |
| [Journey of HDR to SDR Conversion](https://juejin.cn/column/7206577654933471292)⭐ | This series explains HDR conversion theory in a simple and understandable way based on the problems encountered in development. Code repository: [HDRSample🔥](https://github.com/JonaNorman/HDRSample) |
| [HDR in Android](https://blog.csdn.net/a360940265a/category_11625435.html)⭐ | Using 10-bit textures to reference ffmpeg for HDR to SDR conversion using OpenGL and hable tone mapping. |
| [BT.2446 Method C (HDR to SDR Conversion)](https://trev16.hatenablog.com/entry/2020/08/01/131907)⭐ | Research on BT2446's C method, provides [BT2446C, Youtube HDR to SDR LUT](https://drive.google.com/file/d/1B5il7F9DRdg6ipcZdyMcLHv465jimFvt/view) |
| [MovieLabs_Mapping_PQ_to_HLG_v1.0](https://movielabs.com/ngvideo/MovieLabs_Mapping_PQ_to_HLG_v1.0.pdf)⭐ | MovieLabs' practice of mapping PQ to HLG using the MaxRGB method. |
| [Introduction to Android Game Development on Adreno GPU](https://blog.csdn.net/weixin_38498942/article/details/118160665) | How to implement true HDR playback using OpenGL. |
| [Conversion between HLG and PQ Curves in HDR](https://blog.csdn.net/qq26983255/article/details/109824531/) | Practical implementation of HLG and PQ formula conversion. |

## Open Source Code

| Name | Notes                                                                                                            |
| --- |------------------------------------------------------------------------------------------------------------------|
| [HDRSample🔥](https://github.com/JonaNorman/HDRSample) | Open source code for the [Journey of HDR to SDR Conversion](https://juejin.cn/column/7206577654933471292) series. |
| [hdrtoys](https://github.com/natural-harmonia-gropius/hdr-toys)⭐ | HDR to SDR plugin for the mpv player.                                                                            |
| [android](https://android.googlesource.com/platform/frameworks/native/+/refs/heads/master/libs/tonemap/tonemap.cpp)⭐ | Tonemap in Android.                                                                                              |
| [libplacebo tonemap](https://code.videolan.org/videolan/libplacebo/-/blob/master/src/tone_mapping.c)⭐ | Tonemap for the mpv player.                                                                                      |
| [ffmpeg](https://github.com/FFmpeg/FFmpeg/blob/master/libavfilter/vf_tonemap.c) | Tonemap in ffmpeg.                                                                                               |
|[kodi](https://github.com/xbmc/xbmc/blob/1e499e091f7950c70366d64ab2d8c4f3a18cfbfa/system/shaders/GL/1.5/gl_tonemap.glsl#L4)| Tonemap in kodi.                                                                                                 |
| [gopro](https://github.com/gopro/gopro-lib-node.gl/blob/main/libnodegl/src/glsl/hdr_pq2sdr.frag) | Implementation of BT2446a tone mapping.                                                                          |
| [glsl-tone-map](https://github.com/dmnsgn/glsl-tone-map) | Common tone mapping curves.                                                                                      |
| [HDRTVNET](https://github.com/chxy95/HDRTVNet) | SDR to HDR conversion using deep learning.                                                                       |
| [OpenCV TMO](https://github.com/opencv/opencv/blob/17234f82d025e3bbfbf611089637e5aa2038e7b8/modules/photo/src/tonemap.cpp) | OpenCV's tone mapping.                                                                                           |

## Theoretical Sharing

| Title |  |
| --- | --- |
| [Why is Color Space so Empty](https://www.bilibili.com/video/BV19e4y1y7Mo)⭐ | Perhaps the best explanation of color space in Chinese videos. |
| [Yaming's Column](https://www.imaschina.com/author/u/108.html)⭐ | Comprehensive explanation of BT2100 and BT2048 (reference white level, scene reference, display reference, comparison of HDR and SDR up-conversion and down-conversion). The column has 13 articles, or you can directly view the [PPT](http://gongj.cbgcloud.com/TrainManage/PDF/6440201C-C099-4046-9E35-60CA38D866A3.pdf). |
| [Research and Application of Inverse Tone Mapping Algorithm in Film and Television Field](https://m.fx361.com/news/2021/0304/10852880.html)⭐ | Summarizes various inverse tone mapping algorithms in SDR to HDR conversion. |
| [Talking about HDR and Color Management](https://zhuanlan.zhihu.com/p/129095380)⭐ | Step-by-step explanation of HDR in games starting from color space. |
| [HDR in Call of Duty](https://research.activision.com/publications/archives/hdr-in-call-of-duty) | HDR in Call of Duty. |
| [HDR Hardware Imaging Technology](https://blog.csdn.net/nyist_yangguang/article/details/123056556) | [HDR Imaging Technology Study (Part 1)](https://blog.csdn.net/nyist_yangguang/article/details/123056556)</br>[HDR Imaging Technology Study (Part 2)](https://blog.csdn.net/nyist_yangguang/article/details/123094698)</br>[HDR Imaging Technology Study (Part 3)](https://blog.csdn.net/nyist_yangguang/article/details/123122096) |
| Tone Curve Summary | [HDR Tone Mapping](https://xiaoiver.github.io/coding/2019/02/05/HDR-Tone-Mapping.html)</br>[tonemapping](https://64.github.io/tonemapping/)</br>[local-tonemapping](https://bartwronski.com/2022/02/28/exposure-fusion-local-tonemapping-for-real-time-rendering/) |

# Industry Sharing

| Name | Notes |
| --- | --- |
|[Technical Implementation of SDR to HDR Conversion at Qiniu Cloud](https://mp.weixin.qq.com/s/MLUTVNLWN1u_v7h7JxlVgA)| Introduces the SDR to HDR tone mapping formula, chroma enhancement formula, and detail preservation method at Qiniu Cloud. |
|[Tencent's SDR to HDR Sharing](https://mp.weixin.qq.com/s/wjDEgCQ2wY2EVpAKT3ooEg)| Explanation of the SDR to HDR up-conversion solution. |
|[MicroFrame HDR Sharing](https://mp.weixin.qq.com/s/2-inQKbDBGERIWiMCAPKgw)| Enhances contrast in different regions of a video frame before transcoding, achieving an SDR with HDR appearance. |
|[Kuaishou HDR Video Generation Algorithm Sharing](https://mp.weixin.qq.com/s/ditOdwY3hSOvn1go3YMQVw) & [Research Paper](https://arxiv.org/abs/2207.00319)| 1. Uses the HDCFM (Hierarchical Dynamic Context Feature Mapping) model for enhancing dark parts and suppressing highlights in video frames. </br> 2. Uses the PDCG (Patch Discriminator for Highlight Generation) model for generating highlights in HDR video frames. |
|[Kuaishou HDR to SDR Sharing](https://blog.csdn.net/vn9PLgZvnPs1522s82g/article/details/118212043)| Provides an introduction to HDR video characteristics, brightness perception model, and what Kuaishou does in hybrid editing between SDR and HDR. |
|[Weibo HDR Sharing](https://blog.csdn.net/vn9PLgZvnPs1522s82g/article/details/122227335)| Introduces HDR video characteristics and the transformation made to Weibo's video pipeline. |
|[Xigua Video HDR Sharing](https://www.infoq.cn/article/otnx8vjwhanup2altsiu)| Introduces the solution for HDR video uploading, transcoding, distribution, and terminal playback in Xigua Engine Lab. |
|[OPPO HDR Sharing](https://blog.csdn.net/zhying719/article/details/123606237)| Introduces HDR video characteristics and the industry ecosystem of OPPO. |
|[Baidu HDR Sharing](https://xie.infoq.cn/article/7f362dffbece97640e26b6caa)| Introduces Baidu's application of intelligent super-clear HDR. |
|[Huawei Cloud Video AI Transcoding Area Sharing](https://blog.csdn.net/vn9PLgZvnPs1522s82g/article/details/124008425)| Introduces Huawei Cloud's technical practices in image enhancement and SDR to HDR conversion. |
|[Taobao Audio and Video Algorithm Sharing](https://blog.csdn.net/vn9PLgZvnPs1522s82g/article/details/117004023)| Introduces Alibaba's self-developed APG high compression ratio image format, H.265 implementation and optimization, high-definition transcoding, HDR10 end-to-end playback, self-developed AliDenoise intelligent noise reduction, and TaoAudio audio solution. |


# Standard Documents
## International
| Name | Notes |
| --- | --- |
| [BT1886](https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.1886-0-201103-I!!PDF-C.pdf) | Defines the transfer function for CRT displays in BT1886. |
| [BT709](https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.709-6-201506-I!!PDF-C.pdf) | Defines the BT709 color space for high-definition video. |
| [BT2020](https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.2020-2-201510-I!!PDF-C.pdf)| Defines the BT20202 color space for ultra high-definition video. |
| [BT2087](https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.2087-0-201510-I!!PDF-C.pdf)⭐| 1. Provides a method for direct mapping from BT709 to BT2020.</br>2. Compares scene mapping and display mapping. |
| [BT2100](https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.2100-2-201807-I!!PDF-E.pdf)| Extends BT2020 with PQ and HLG transfer functions. |
|[BT2111](https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.2111-2-202012-I!!PDF-C.pdf)| PQ and HLG color bar test pattern specifications. |
|[BT2250](https://www.itu.int/dms_pub/itu-r/opb/rep/R-REP-BT.2250-2012-PDF-E.pdf)| Deriving color conversion matrices |
|[BT2390](https://www.itu.int/dms_pub/itu-r/opb/rep/R-REP-BT.2390-10-2021-PDF-E.pdf)⭐| 1. Supplementary explanation of BT2100 PQ and HLG transfer functions.</br>2. Comparison of YCBCR and ICTCP color handling. |
| [BT2407](https://www.itu.int/dms_pub/itu-r/opb/rep/R-REP-BT.2407-2017-PDF-E.pdf)⭐|1. How to use lab to complete tone, brightness, and chromaticity mapping to avoid saturation and chromaticity problems caused by direct mapping from BT2020 to BT709.</br> 2. A method for BT2020 to BT709 soft clipping using luv. |
|[BT2408](https://www.itu.int/dms_pub/itu-r/opb/rep/R-REP-BT.2408-5-2022-PDF-E.pdf)⭐| 1. HDR TV production practice guide</br>2. Redefines reference white levels for BT2100 PQ and HLG.</br>3. How to lower PQ brightness through the EETF function.</br>4. Direct mapping methods for SDR to HDR conversion.</br>5. Methods for PQ to HLG and HLG to PQ conversions. |
| [BT2446](https://www.itu.int/dms_pub/itu-r/opb/rep/R-REP-BT.2446-1-2021-PDF-E.pdf)| Three dynamic mapping methods for converting between HDR and SDR. |
|[ST2084](https://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=7291452)| Defines the PQ OETF transfer function. |
|[ST2086](https://www.voukoder.org/attachment/1647-mastering-display-color-volume-metadata-supporting-high-luminanc-pdf/)| Defines HDR static metadata. |
|[ST2094](https://forum.selur.net/attachment.php?aid=611)| Defines HDR dynamic metadata.</br>[ST 2094-1](https://ieeexplore.ieee.org/document/7513361)</br>[ST 2094-2](https://ieeexplore.ieee.org/document/7839894)</br>[ST 2094-10](https://ieeexplore.ieee.org/document/9405553)</br>[ST 2094-20](https://ieeexplore.ieee.org/document/7523881)</br>[ST 2094-30](https://ieeexplore.ieee.org/document/7523878)</br>[ST 2094-40](https://ieeexplore.ieee.org/document/9095450)|
| [SL-HDR](https://en.wikipedia.org/wiki/High-dynamic-range_television#Other_formats) | Another HDR standard besides HLG and PQ.</br>[SL-HDR1](https://www.etsi.org/deliver/etsi_ts/103400_103499/10343301/01.04.01_60/ts_10343301v010401p.pdf)</br>[SL-HDR2](https://www.etsi.org/deliver/etsi_ts/103400_103499/10343302/01.03.01_60/ts_10343302v010301p.pdf)</br>[SL-HDR3](https://www.etsi.org/deliver/etsi_ts/103400_103499/10343303/01.02.01_60/ts_10343303v010201p.pdf) |
| [Khronos Data Format Specifications](https://registry.khronos.org/DataFormat/specs/1.3/dataformat.1.3.inline.html) | PQ and HLG transfer function formulas compiled in Khronos' documentation. |

## Domestic (China)
| Title | Notes |
| --- | --- |
| [Technical Requirements for High Dynamic Range Television System Display Adaptation Metadata](http://www.nrta.gov.cn/art/2022/9/29/art_3715_61973.html)⭐ | SARFT's definition of HDR metadata in video for H.265. |
| [Research and Testing on Standard Dynamic Range and High Dynamic Range Conversion Methods](http://www.tvoao.com/a/206282.aspx)⭐ | Testing and research on the direct mapping conversion of HDR and SDR proposed by BT2087 and BT2390. |
| [Guidelines for 4K Ultra High Definition Television Program Production 2020 Edition](http://www.nrta.gov.cn/module/download/downfile.jsp?classid=0&filename=f6761c8580244d56a7ac6ced475c99f4.pdf) | SARFT's practical guide on BT2408.</br>Considerations in HDR production.</br>Reference white levels at 75% HLG, 58% PQ. |
| [CCTV HDR Video Production White Paper 2022 Edition](https://sysbc.scmc.edu.cn/2022_09/15_15/content-33329.html) | SARFT's practical guide on BT2408.</br>Considerations in HDR production.</br>Common skin tone brightness.</br>Level mapping relationship for HLG to SDR conversion.</br>LUT for HLG to SDR conversion (LUT file not found). |
| [Image Parameter Values for High Dynamic Range Television Program Production and Exchange](http://www.nrta.gov.cn/art/2020/4/24/art_3715_50842.html) | SARFT's explanation of BT2309, defining the transfer functions for PQ and HLG. |
| [Ultra High Definition High Dynamic Range Video System Color Bar Test Pattern](http://www.nrta.gov.cn/art/2020/4/24/art_3715_50842.html) | SARFT's explanation of the color bar defined in BT2111. |

# Cube LUT
LUTs for Direct HDR-to-SDR Conversion

| Name | Notes |
| --- | --- |
| [NBCU Open Source HDRLUT](https://github.com/digitaltvguy/NBCUniversal-UHD-HDR-SDR-Single-Master-Production-Workflow-Recommendation-LUTs/tree/main/LUTS_for_Hardware_Devices_DaVinci_Resolve_CUBE_LUT_Format)⭐ | [NBCU_SDR2HLG_DL_v1.1.cube](https://github.com/digitaltvguy/NBCUniversal-UHD-HDR-SDR-Single-Master-Production-Workflow-Recommendation-LUTs/blob/main/LUTS_for_Hardware_Devices_DaVinci_Resolve_CUBE_LUT_Format/1-NBCU_SDR2HLG_DL_v1.1.cube)</br>[NBCU_SDR2HLG_SL_v1.cube](https://github.com/digitaltvguy/NBCUniversal-UHD-HDR-SDR-Single-Master-Production-Workflow-Recommendation-LUTs/blob/main/LUTS_for_Hardware_Devices_DaVinci_Resolve_CUBE_LUT_Format/2-NBCU_SDR2HLG_SL_v1.cube)</br>[NBCU_HLG2SDR_DL_v1.1.cube](https://github.com/digitaltvguy/NBCUniversal-UHD-HDR-SDR-Single-Master-Production-Workflow-Recommendation-LUTs/blob/main/LUTS_for_Hardware_Devices_DaVinci_Resolve_CUBE_LUT_Format/3-NBCU_HLG2SDR_DL_v1.1.cube)</br>[NBCU_SDR2PQ_DL_v1.cube](https://github.com/digitaltvguy/NBCUniversal-UHD-HDR-SDR-Single-Master-Production-Workflow-Recommendation-LUTs/blob/main/LUTS_for_Hardware_Devices_DaVinci_Resolve_CUBE_LUT_Format/4-NBCU_SDR2PQ_DL_v1.cube)</br>[NBCU_PQ2SDR_DL_v1.cube](https://github.com/digitaltvguy/NBCUniversal-UHD-HDR-SDR-Single-Master-Production-Workflow-Recommendation-LUTs/blob/main/LUTS_for_Hardware_Devices_DaVinci_Resolve_CUBE_LUT_Format/5-NBCU_PQ2SDR_DL_v1.cube)</br>[NBCU-HLG2PQ_1000nit_v1.cube](https://github.com/digitaltvguy/NBCUniversal-UHD-HDR-SDR-Single-Master-Production-Workflow-Recommendation-LUTs/blob/main/LUTS_for_Hardware_Devices_DaVinci_Resolve_CUBE_LUT_Format/7-NBCU-HLG2PQ_1000nit_v1.cube) |
| [Linear Transformation](https://github.com/FranceBB/LinearTransformation)⭐ | Linear color space transformations, includes conversions between BT2020 PQ, BT2020 HLG, and BT709. |

# Test Videos

| Name | Notes |
| --- | --- |
| [HDRVideo](https://github.com/JonaNorman/HDRSample/tree/main/sample/src/main/assets/video) | Collection of 10 commonly used HDR videos. |
| [Mehanik HDR10 Test Patterns](https://drive.google.com/drive/folders/1m4IBq0euAxamL9ePgfdFuf8_5nLcRwHA)⭐ | Grayscale, hue, dithering, white level, black level test videos. |
| [tfb-video Test Video Generation Code](https://github.com/test-full-band/tfb-video)⭐ | Code to generate test videos.</br>[Video Links](https://github.com/test-full-band/tfb-video/releases) |
| [ARIB STD-B72 Color Bar Video Code](https://github.com/kcamovie/arib_std-b72) | Color bar test video generated based on ARIB STD-B72. Adding a waveform monitor allows for accurate color matching.</br>[ARIB STD-B72 Colour Bar Test Pattern for HLG](https://www.youtube.com/watch?v=YiFP1xrRE4c) |
| [BT2111-DV](https://github.com/saindriches/BT2111-DV) | Dolby Vision Profile 5 color bar test video modified according to BT2111. Adding a waveform monitor allows for accurate color matching.</br>[BT2111-MOD_DV5_UHD_24P.mp4](https://github.com/saindriches/BT2111-DV/blob/master/BT2111-MOD_DV5_UHD_24P.mp4) |
| [Kodi Test Videos](https://kodi.wiki/view/Samples)⭐ | [HDR 10-bit HEVC 25fps](https://drive.google.com/file/d/0BwxFVkl63-lEdU9Ma0VYc3YxSVE/view?usp=sharing&resourcekey=0-FJsSx7fEhiVEtQMRwo_ePQ)</br>[HDR 10-bit Grayscale Rotating Gradients](https://www.projectorcentral.com/images/articles/10bit_HDR_Grayscale_ProjectorCentral_4K_(HEVC_10-bit).mp4)</br>[HDR10 HEVC 59.94](https://drive.google.com/file/d/1Ic9DZXMSo07EJMqCFaQRKSSrSw6y1mYv/view?usp=sharing)</br>[HDR10+ Profile A HEVC 10-bit 23.976 Sample](https://mega.nz/file/af4zSAbQ#gBiHRiX3oLnBvxMNnytC08v8DRkKzQIkhGpg96nAWXE)</br>[HDR10+ Profile B HEVC 10-bit 23.976 Sample](https://mega.nz/file/nehDka6Z#C5_OPbSZkONdOp1jRmc09C9-viDc3zMj8ZHruHcWKyA)</br>[HDR10+ Dynamic Metadata Test 60fps](https://we.tl/t-3mO1E8BGEZ) |
| [TV Test Videos](https://www.aliyundrive.com/s/5Q2cYnGobLj/folder/613d9d1cf6c1d70510764f529d585cc543fdc3db) | Television screen test videos. |
| [DisplayHDRTest](https://github.com/vesa-org/DisplayHDRTest)⭐ | Test HDR display and video color standards. |

# Tools

| Name | Notes |
| --- | --- |
| [Color Space Calculator](http://color.support/colorspacecalculator.html)⭐ | Online color space conversion matrix calculator. |
| [Rainbow Space](http://rainbowspace.xyz/) | Visualization of color space in images. |
| [Color Converter](https://ajalt.github.io/colormath/converter/) | Online color conversion tool. |
| [Colour](https://github.com/colour-science/colour) | Python package for color science algorithms. |
| [XYZ Color Space Visualization](https://horizon-lab.org/colorvis/xyz.html) | Visualization of the XYZ color space. |