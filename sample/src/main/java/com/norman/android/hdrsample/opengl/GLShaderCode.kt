package com.norman.android.hdrsample.opengl

abstract class GLShaderCode{
      abstract val code:String
      val includeList:ArrayList<GLShaderCode> = ArrayList()
}
