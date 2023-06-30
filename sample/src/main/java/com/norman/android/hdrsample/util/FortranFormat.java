package com.norman.android.hdrsample.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;

public class FortranFormat {
    private static final byte BYTE_0 = '0';

    private static final byte BYTE_9 = '9';

    private static final byte BYTE_DOT = '.';

    private static final byte BYTE_e= 'e';
    private static final byte BYTE_E= 'E';

    private static final byte BYTE_d= 'd';

    private static final byte BYTE_D= 'D';

    private static final byte BYTE_a= 'a';

    private static final byte BYTE_A= 'A';

    private static final byte BYTE_jian= '-';

    private static final byte BYTE_jia= '+';
    public static float toFloat(ByteBuffer buffer,int start,int end) {
        int oldPos = buffer.position();
        int oldLimit = buffer.limit();
        try {
            buffer.limit(end);
            buffer.position(start);

             ByteBuffer  byteBuffer =   buffer.slice();

            int i = 0;
            int sign = 1;
            float r = 0; // integer part
            float p = 1; // exponent of fractional part
            int state = 0; // 0 = int part, 1 = frac part

            if (i < byteBuffer.capacity() && byteBuffer.get(i) == (byte) '-') {
                sign = -1;
                i++;
            } else if (i < byteBuffer.capacity() && byteBuffer.get(i) == (byte)'+') {
                i++;
            }
            while (i < byteBuffer.capacity()) {
                byte ch = byteBuffer.get(i);
                if (BYTE_0 <= ch && ch <= BYTE_9) {
                    if (state == 0)
                        r = r * 10 + ch - BYTE_0;
                    else if (state == 1) {
                        p = p / 10;
                        r = r + p * (ch - BYTE_0);
                    }
                } else if (ch == BYTE_DOT) {
                    if (state == 0)
                        state = 1;
                    else
                        return sign * r;
                } else if (ch == BYTE_e || ch == BYTE_E || ch == BYTE_d || ch == BYTE_D) {

                    buffer.position(i+1);
                    long e = (int) parseLong(buffer.slice(), 10);
                    return (float) (sign * r * Math.pow(10, e));
                } else
                    return sign * r;
                i++;
            }
            return sign * r;
        }finally {
            buffer.limit(oldLimit);
            buffer.position(oldPos);
            buffer.order(ByteOrder.BIG_ENDIAN);
        }


    }

    private static long parseLong(ByteBuffer byteBuffer, int base) {
        int i = 0;
        int sign = 1;
        long r = 0;

        while (i < byteBuffer.capacity())
            i++;
        if (i < byteBuffer.capacity() && byteBuffer.get(i) ==  BYTE_jian) {
            sign = -1;
            i++;
        } else if (i < byteBuffer.capacity() && byteBuffer.get(i) ==  BYTE_jia) {
            i++;
        }
        while (i < byteBuffer.capacity()) {
            byte ch = byteBuffer.get(i);
            if (BYTE_0 <= ch && ch < BYTE_0 + base)
                r = r * base + ch - BYTE_0;
            else if (BYTE_A <= ch && ch < BYTE_A + base - 10)
                r = r * base + ch - BYTE_A + 10;
            else if (BYTE_a <= ch && ch < BYTE_a+ base - 10)
                r = r * base + ch - BYTE_a + 10;
            else
                return r * sign;
            i++;
        }
        return r * sign;
    }
}
