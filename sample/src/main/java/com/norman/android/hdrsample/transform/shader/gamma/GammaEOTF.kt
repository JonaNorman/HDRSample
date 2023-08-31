package com.norman.android.hdrsample.transform.shader.gamma

abstract class GammaEOTF : GammaFunction(){


    companion object{
        @JvmField
        val BT1886 = BT1886EOTF()

        @JvmField
        val S170M = S170MEOTF()

        @JvmField
        val BT709 = BT709EOTF()

        @JvmField
        val HLGDisplay = HLGDisplayEOTF()

        @JvmField
        val HLGScene = HLGSceneEOTF()

        @JvmField
        val PQDisPlay = PQDisplayEOTF()

        @JvmField
        val PQScene = PQSceneEOTF()

        @JvmField
        val NONE = NoneEOTF()
    }
}
