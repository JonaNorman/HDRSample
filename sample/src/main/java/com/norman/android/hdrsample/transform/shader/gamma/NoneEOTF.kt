package com.norman.android.hdrsample.transform.shader.gamma


class NoneEOTF : GammaEOTF() {
    override val code: String
        get() = """
      
        vec3 ${methodGamma}(vec3 V) {
            return V;
        }
        """.trimIndent()
}