package com.norman.android.hdrsample.transform.shader.gamma


class NoneOETF : GammaOETF() {
    override val code: String
        get() = """
      
        vec3 ${methodGamma}(vec3 V) {
            return V;
        }
        """.trimIndent()
}