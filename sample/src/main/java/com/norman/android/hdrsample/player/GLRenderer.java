package com.norman.android.hdrsample.player;

import android.opengl.GLES20;

import androidx.annotation.NonNull;

import com.norman.android.hdrsample.opengl.GLShaderCode;
import com.norman.android.hdrsample.util.GLESUtil;

import java.util.Objects;

abstract class GLRenderer {
    boolean create = false;

    boolean requestProgram;

    GLRenderTarget renderTarget;

    protected int programId;

    private GLShaderCode vertexShader;

    private GLShaderCode frameShader;

    boolean renderSuccess;

    /**
     * 渲染到目标
     *
     * @param renderTarget
     */
    synchronized void renderToTarget(GLRenderTarget renderTarget) {
        this.renderTarget = renderTarget;
        renderTarget.startRender();
        if (!create) {//没有创建会创建
            create = true;
            onCreate();
        }
        onRenderTarget();
        renderTarget.finishRender();
    }


    protected void onCreate() {

    }

   private  void onRenderTarget() {
        if (!onRenderStart()) {
            renderSuccess = false;
            return;
        }
        if (requestProgram) {
            requestProgram = false;
            GLESUtil.delProgramId(programId);
            programId = 0;
            if (vertexShader != null
                    && frameShader != null){
                programId = GLESUtil.createProgramId(vertexShader.getCode(),frameShader.getCode());
                onProgramChange(programId);
            }
        }
        if (programId <= 0) {
            renderSuccess = false;
            return;
        }
        GLES20.glUseProgram(programId);
        onRender();
        onRenderFinish();
        GLES20.glUseProgram(0);
        GLESUtil.checkGLError();
        renderSuccess = true;
    }

    protected synchronized void setVertexShader(GLShaderCode vertexShader) {
        if (!Objects.equals(this.vertexShader,vertexShader)){
            this.vertexShader = vertexShader;
            requestProgram = true;
        }
    }

    protected synchronized void setVertexShader(String vertexShader) {
         setVertexShader(new GLShaderCode() {
             @NonNull
             @Override
             public String getCode() {
                 return vertexShader;
             }
         });
    }


    protected synchronized void setFrameShader(GLShaderCode frameShader) {
        if (!Objects.equals(this.frameShader,frameShader)){
            this.frameShader = frameShader;
            requestProgram = true;
        }
    }

    protected synchronized void setFrameShader(String frameShader) {
       setFrameShader(new GLShaderCode() {
           @NonNull
           @Override
           public String getCode() {
               return frameShader;
           }
       });
    }

    protected abstract void onProgramChange(int programId);

    abstract void onRender();

    boolean onRenderStart() {
        return true;
    }

    void onRenderFinish() {

    }
}
