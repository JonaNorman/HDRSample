package com.norman.android.hdrsample.todo;

import android.opengl.GLES30;

import com.norman.android.hdrsample.util.CubeLut;

public class CubeLutEffect {
   private int lutTextureId;

   private CubeLut cube;






   public void sss() {
      cube.buffer.rewind();
      GLES30.glTexImage3D(GLES30.GL_TEXTURE_3D, 0, GLES30.GL_RGB32F, cube.size, cube.size, cube.size, 0, GLES30.GL_RGB, GLES30.GL_FLOAT, cube.buffer);
   }



}
