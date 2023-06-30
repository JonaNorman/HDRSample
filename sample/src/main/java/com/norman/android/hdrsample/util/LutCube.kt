package com.norman.android.hdrsample.util

import android.content.res.AssetFileDescriptor
import android.util.Log
import org.apache.commons.lang3.StringUtils
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.util.Spliterator
import java.util.Spliterators
import java.util.stream.Stream
import java.util.stream.StreamSupport


class LutCube private constructor(assetName: String) {


    lateinit var title: String
        private set
    var  size: Int = 0
        private set
    lateinit var domainMin: FloatArray
        private set
    lateinit var domainMax: FloatArray
        private set
    lateinit var buffer: ByteBuffer
        private set

    init {
        var time = System.currentTimeMillis()
        var inputStream: FileInputStream? = null
        var assetFileDescriptor: AssetFileDescriptor? = null

        try {
            assetFileDescriptor = FileUtil.openAssetFileDescriptor(assetName)
            inputStream = assetFileDescriptor.createInputStream()
            val channel =  inputStream.channel;
            val mappedByteBuffer: MappedByteBuffer = channel
                .map(FileChannel.MapMode.READ_ONLY, assetFileDescriptor.startOffset, assetFileDescriptor.length)
            val limit = mappedByteBuffer.limit()

            while (mappedByteBuffer.hasRemaining()){
                val  linePos = nextLinePosition(mappedByteBuffer)
                mappedByteBuffer.limit(linePos)
                val line  = UTF8_CHARSET.decode(mappedByteBuffer).toString()
                mappedByteBuffer.limit(limit)
                mappedByteBuffer.position(linePos+1)
                if (line.startsWith("#") || line.isEmpty()) {
                    continue
                }
                val parts = line.split("\\s+".toRegex())
                if (parts[0] == "LUT_IN_VIDEO_RANGE" || parts[0] == "LUT_OUT_VIDEO_RANGE") {
                    continue
                }
                if (parts[0] == "LUT_1D_SIZE") {
                    throw RuntimeException("not support LUT_1D_SIZE")
                } else if (parts[0] == "LUT_2D_SIZE") {
                    throw RuntimeException("not support LUT_2D_SIZE")
                } else if (parts[0] == "LUT_3D_SIZE") {
                    size = parts[1].toInt()
                } else if (parts[0] == "TITLE") {
                    title = parts[0]
                } else if (parts[0] == "DOMAIN_MIN") {
                    domainMin =
                        floatArrayOf(parts[1].toFloat(), parts[2].toFloat(), parts[3].toFloat())
                } else if (parts[0] == "DOMAIN_MAX") {
                    domainMax =
                        floatArrayOf(parts[1].toFloat(), parts[2].toFloat(), parts[3].toFloat())
                }else{
                    mappedByteBuffer.reset()

                    break
                }
                mappedByteBuffer.mark()
            }





            val numChannels = 3
            val bytesPerChannel = 4
            val bytesPerPixel = numChannels * bytesPerChannel
            buffer = ByteBuffer.allocateDirect(size * size * size * bytesPerPixel)
            buffer.order(ByteOrder.nativeOrder())

            var start = mappedByteBuffer.position();

            var floatflag = false


            var  byteArray = ByteArray(0);

            while (mappedByteBuffer.hasRemaining()){
                val b = mappedByteBuffer.get()
                if (b == LINE_BYTE || b == EMPTY_SPACE_BYTE) {
                    if (floatflag){
                        var end = mappedByteBuffer.position()
                        buffer.putFloat(FortranFormat.toFloat(mappedByteBuffer,start,end-1))
                    }
                    floatflag = false

                    start = mappedByteBuffer.position();
                }else{
                    mappedByteBuffer.position(mappedByteBuffer.position()+8);
                    floatflag = true
                }
            }

            Log.v("1111111", "ffffffff" + (System.currentTimeMillis() - time) + "ms")//ssssss



        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (ignored: IOException) {
                }
            }
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close()
                } catch (ignored: IOException) {
                }
            }
        }
    }





    private fun nextLinePosition(byteBuffer: ByteBuffer): Int {
        var oldPos = byteBuffer.position()
        var linePos   = byteBuffer.limit()
        while (byteBuffer.hasRemaining()){
            val b = byteBuffer.get()
            if (b == LINE_BYTE) {
                linePos =  byteBuffer.position() - 1
                break
            }
        }
        byteBuffer.position(oldPos)
        return linePos
    }

    companion object {
        private const val  LINE_BYTE = '\n'.toByte()
        private const val  EMPTY_SPACE_BYTE = ' '.toByte()
        private  val  UTF8_CHARSET =   Charset.forName("utf-8")
        @JvmStatic
        fun createForAsset(assetName: String): LutCube {
            return LutCube(assetName)
        }
    }
}
