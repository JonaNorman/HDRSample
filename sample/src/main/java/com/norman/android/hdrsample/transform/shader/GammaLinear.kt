package com.norman.android.hdrsample.transform.shader


object GammaLinear : GammaFunction() {
    override val methodOETF = "LINEAR_OETF"
    override val methodEOTF = "LINEAR_EOTF"

    override val code: String
        get() = """
        vec3 $methodOETF(vec3 x)
        {
            return x;
        }
        
        vec3 $methodEOTF(vec3 x)
        {
            return x;
        }
        """.trimIndent()
}