package com.tuvalutorture.gamejam;

public class BytePacker {
    public static int bytesToInt(byte byte1, byte byte2, byte byte3, byte byte4) {
        return ((byte1 & 0xFF) << 24) | ((byte2 & 0xFF) << 16) | ((byte3 & 0xFF) << 8) | (byte4 & 0xFF);
    }

    public static int bytesToInt(byte[] array, int startIndex) {
        return bytesToInt(array[startIndex], array[startIndex + 1], array[startIndex + 2], array[startIndex + 3]);
    }

    public static short bytesToShort(byte byte1, byte byte2) {
        return (short) (((byte1 & 0xFF) << 8) | (byte2 & 0xFF));
    }

    public static int shortsToInt(short short1, short short2) {
        return ((short1 & 0xFFFF) << 16) | (short2 & 0xFFFF);
    }

    public static byte[] intToBytes(int value) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((value >> 24)  & 0xFF);
        bytes[1] = (byte) ((value >> 16) & 0xFF);
        bytes[2] = (byte) ((value >> 8) & 0xFF);
        bytes[3] = (byte) (value & 0xFF);
        return bytes;
    }

    public static byte[] shortToBytes(short value) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) ((value >> 8) & 0xFF);
        bytes[1] = (byte) (value & 0xFF);
        return bytes;
    }

    public static short[] intToShorts(int value) {
        short[] shorts = new short[2];
        shorts[0] = (short) ((value >> 8) & 0xFF);
        shorts[1] = (short) (value & 0xFF);
        return shorts;
    }

    public static byte retrieveByteFromNumber(long number, byte byteNumber) {
        return (byte) ((number >> 8 * byteNumber) & 0xFF);
    }

    public static short retrieveShortFromNumber(long number, byte byteNumber) {
        return (byte) ((number >> 16 * byteNumber) & 0xFFFF);
    }
}
