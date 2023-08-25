package com.norman.android.hdrsample.transform.shader

// PQ公式参数详解见 https://juejin.cn/post/7231369710024310821#heading-13
object GammaPQ : GammaFunction() {
    private const val methodPQOOTF = "PQ_OOTF"
    private const val methodPQEOTFInv = "PQ_EOTF_1"
    override val methodOETF = "PQ_OETF"
    override val methodEOTF = "PQ_EOTF"

    override val code: String
        get() = """
        #define PQ_M1  0.1593017578125
        #define PQ_M2  78.84375
        #define PQ_C1  0.8359375
        #define PQ_C2  18.8515625
        #define PQ_C3  18.6875

    

        // EOTF的逆函数
        vec3 $methodPQEOTFInv(vec3 x)
        {
            vec3 Y = x ;
            vec3 Ym = pow(Y, vec3(PQ_M1));
            return pow((PQ_C1 + PQ_C2 * Ym) / (1.0 + PQ_C3 * Ym), vec3(PQ_M2));
        }

        float $methodPQEOTFInv(float x)
        {
            float Y = x ;
            float Ym = pow(Y, PQ_M1);
            return pow((PQ_C1 + PQ_C2 * Ym) / (1.0 + PQ_C3 * Ym), PQ_M2);
        }
        
        vec3 $methodPQOOTF(vec3 x){
             vec3 x1 =  mix(267.84*x, 1.099*pow(59.5208*x,vec3(0.45))-0.099, step(0.0003024, x));
             return 100.0* pow(x1,vec3(2.4));
        }
        
        
        
        vec3 $methodOETF(vec3 x){
             return $methodPQEOTFInv($methodPQOOTF(x));
        }
        
        float $methodOETF(float x){
             return $methodOETF(vec3(x)).x;
        }
        
        vec3 $methodEOTF(vec3 x)
        {
            vec3 p = pow(x, vec3(1.0 / PQ_M2));
            vec3 num = max(p - PQ_C1, 0.0);
            vec3 den = PQ_C2 - PQ_C3 * p;
            vec3 Y = pow(num / den, vec3(1.0 / PQ_M1));
            return  Y;
        }
        
        float $methodEOTF(float x)
        {
            return $methodEOTF(vec3(x)).x;
        }
        """.trimIndent()
}