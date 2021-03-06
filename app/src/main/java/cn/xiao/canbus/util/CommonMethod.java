package cn.xiao.canbus.util;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class CommonMethod {

    static final long fx = 0xffl;

    /**
     * 将十六进制的字符串转换成字节数组
     *
     * @param hexString
     * @return
     */
    public static byte[] hexStrToBinaryStr(String hexString) {

        if (TextUtils.isEmpty(hexString)) {
            return null;
        }

        hexString = hexString.replaceAll(" ", "");

        int len = hexString.length();
        int index = 0;

        byte[] bytes = new byte[len / 2];

        while (index < len) {

            String sub = hexString.substring(index, index + 2);

            bytes[index / 2] = (byte) Integer.parseInt(sub, 16);

            index += 2;
        }


        return bytes;
    }

    /**
     * hex字符串转byte数组
     *
     * @param inHex 待转换的Hex字符串
     * @return 转换后的byte数组结果
     */
    public static byte[] hexToByteArray(String inHex) {
        inHex = inHex.replaceAll(" ", "");

        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1) {
            //奇数
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            //偶数
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    /**
     * Hex字符串转byte
     *
     * @param inHex 待转换的Hex字符串
     * @return 转换后的byte
     */
    public static byte hexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }

    /**
     * 将字节数组转换成十六进制的字符串
     *
     * @return
     */
    public static String BinaryToHexString(byte[] bytes) {
        String hexStr = "0123456789ABCDEF";
        String result = "";
        String hex = "";
        for (byte b : bytes) {
            hex = String.valueOf(hexStr.charAt((b & 0xF0) >> 4));
            hex += String.valueOf(hexStr.charAt(b & 0x0F));
            result += hex + " ";
        }
        return result;
    }

    /**
     * short 转 byte[]
     * 小端
     *
     * @param data
     * @return
     */
    public static byte[] getShortBytes(short data) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (data & fx);
        bytes[1] = (byte) ((data >> 8) & fx);
        return bytes;
    }

    /**
     * chart 转 byte[]
     * 小端
     *
     * @param data
     * @return
     */
    public static byte[] getCharBytes(char data) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (data & fx);
        bytes[1] = (byte) ((data >> 8) & fx);
        return bytes;
    }

    /**
     * int 转 byte[]
     * 小端
     *
     * @param data
     * @return
     */
    public static byte[] getIntBytes(int data) {
        int length = 4;
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) ((data >> (i * 8)) & fx);
        }
        return bytes;
    }

    /**
     * long 转 byte[]
     * 小端
     *
     * @param data
     * @return
     */
    public static byte[] getLongBytes(long data) {
        int length = 8;
        byte[] bytes = new byte[length];

        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) ((data >> (i * 8)) & fx);
        }
        return bytes;
    }

    /**
     * float 转 byte[]
     * 小端
     *
     * @param data
     * @return
     */
    public static byte[] getFloatBytes(float data) {
        int intBits = Float.floatToIntBits(data);

        byte[] bytes = getIntBytes(intBits);

        return bytes;
    }

    /**
     * double 转 byte[]
     * 小端
     *
     * @param data
     * @return
     */
    public static byte[] getDoubleBytes(double data) {
        long intBits = Double.doubleToLongBits(data);
        byte[] bytes = getLongBytes(intBits);
        return bytes;
    }

    /**
     * String 转 byte[]
     *
     * @param data
     * @param charsetName
     * @return
     */
    public static byte[] getStringBytes(String data, String charsetName) {
        Charset charset = Charset.forName(charsetName);
        byte[] bytes = data.getBytes(charset);
        return bytes;
    }

    /**
     * String 转 byte[]
     *
     * @param data
     * @return
     */
    public static byte[] getStringBytes(String data) {
        byte[] bytes = null;
        if (data != null) {
            bytes = data.getBytes();
        } else {
            bytes = new byte[0];
        }
        return bytes;
    }

    /**
     * String 转 byte[]
     *
     * @param data
     * @return
     */
    public static byte[] getGBKStringBytes(String data) {
        byte[] bytes;
        if (data != null) {
            try {
                String gbk = URLEncoder.encode(data, "GBK");
                bytes = gbk.getBytes();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                bytes = new byte[1];
                bytes[0] = 0x0;
            }

        } else {
            bytes = new byte[1];
            bytes[0] = 0x0;
        }
        return bytes;
    }

    /**
     * byte[] 转short
     * 小端
     *
     * @param bytes
     * @return
     */
    public static short getShort(byte[] bytes) {
        short result = (short) ((fx & bytes[0])
                | ((fx & bytes[1]) << 8));
        return result;
    }

    /**
     * byte[] 转 char
     * 小端
     *
     * @param bytes
     * @return
     */
    public static char getChar(byte[] bytes) {
        char result = (char) ((fx & bytes[0])
                | ((fx & bytes[1]) << 8));
        return result;
    }

    /**
     * byte[] 转 int
     *
     * @param bytes
     * @return
     */
    public static int getInt(byte[] bytes) {
        int result = (int) ((fx & bytes[0])
                | ((fx & bytes[1]) << 8)
                | ((fx & bytes[2]) << 16)
                | ((fx & bytes[3]) << 24));

        return result;
    }

    /**
     * byte[] 转 long
     *
     * @param bytes
     * @return
     */
    public static long getLong(byte[] bytes) {
        long result = (long) ((long) (fx & bytes[0])
                | (long) ((fx & bytes[1]) << 8)
                | (long) ((fx & bytes[2]) << 16)
                | (long) ((fx & bytes[3]) << 24)
                | (long) ((fx & bytes[4]) << 32)
                | (long) ((fx & bytes[5]) << 40)
                | (long) ((fx & bytes[6]) << 48)
                | (long) ((fx & bytes[7]) << 56));

        return result;
    }

    /**
     * byte[] 转 float
     *
     * @param b
     * @return
     */
    public static float getFloat(byte[] b) {
        int l = getInt(b);
        return Float.intBitsToFloat(l);
    }

    /**
     * byte[] 转 double
     *
     * @param bytes
     * @return
     */
    public static double getDouble(byte[] bytes) {

        long l = getLong(bytes);
        return Double.longBitsToDouble(l);
    }

    /**
     * byte[] 转 String
     *
     * @param bytes
     * @param charsetName
     * @return
     */
    public static String getString(byte[] bytes, String charsetName) {
        String result = new String(bytes, Charset.forName(charsetName));
        return result;
    }

    /**
     * byte[] 转 String
     *
     * @param bytes
     * @return
     */
    public static String getString(byte[] bytes) {
        String result = new String(bytes);
        return result;
    }

    /**
     * byte[] 转 String
     *
     * @param bytes
     * @return
     */
    public static String getGBKString(byte[] bytes) {
        String result = new String(bytes);
        try {
            result = URLEncoder.encode(result, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String byteToHexString(byte b) {
        return Integer.toHexString(Integer.valueOf(String.valueOf(b)));
    }


    /**
     * 字节数组转16进制
     *
     * @param bytes 需要转换的byte数组
     * @return 转换后的Hex字符串
     */
    public static String bytesToHex(byte[] bytes, int length) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
            if (1 == i % 2) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
