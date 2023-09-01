package com.norman.android.hdrsample.opengl

/**
 * 用Kotlin写Shader解决Java的String换行需要加""的问题
 */
abstract class GLShaderCode{
      abstract val code:String
      override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GLShaderCode

            return code == other.code
      }


}
