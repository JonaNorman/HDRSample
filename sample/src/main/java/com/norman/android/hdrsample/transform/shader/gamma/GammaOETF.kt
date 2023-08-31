package com.norman.android.hdrsample.transform.shader.gamma


abstract class GammaOETF : GammaFunction() {

    companion object{
        @JvmField
        val BT1886 = BT1886OETF()

        @JvmField
        val S170M = S170MOETF()

        @JvmField
        val BT709 = BT709OETF()

        @JvmField
        val HLGScene = HLGSceneOETF()

        @JvmField
        val HLGDisplay = HLGDisplayOETF()

        @JvmField
        val PQDisplay = PQDisplayOETF()

        @JvmField
        val PQScene = PQSceneOETF()

        @JvmField
        val NONE = NoneOETF()
    }
}
