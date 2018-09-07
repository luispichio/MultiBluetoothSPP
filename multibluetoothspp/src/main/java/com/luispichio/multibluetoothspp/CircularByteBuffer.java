package com.luispichio.multibluetoothspp;

import java.io.IOException;

public class CircularByteBuffer {
    private final byte[] mBuffer;
    private int mFirst;
    private int mLast;
    private int mSize;

    public CircularByteBuffer(int size){
        mBuffer = new byte[size];
        clear();
    }

    private void clear() {
        mFirst = mLast = mSize = 0;
    }

    private byte _read() throws IOException {
        if (mSize > 0){
            byte result = mBuffer[mFirst];
            mFirst++;
            mFirst %= mBuffer.length;
            mSize--;
            return result;
        }
        throw new IOException();
    }

    private void _write(byte _byte) throws IOException {
        if (mSize < mBuffer.length) {
            mBuffer[mLast++] = _byte;
            mLast %= mBuffer.length;
            mSize++;
        } else
            throw new IOException();
    }

    public synchronized byte read() throws IOException {
        return _read();
    }

    public synchronized byte[] readBytes(int size) throws IOException {
        byte[] result = new byte[size];
        for (int i = 0 ; i < size ; i++)
            result[i] = _read();
        return result;
    }

    public synchronized byte[] readBytes() throws IOException {
        return readBytes(available());
    }

    public synchronized void write(byte _byte) throws IOException {
        _write(_byte);
    }

    /**
     * Escribe una trama de bytes en el buffer circular
     * @param bytes bytes a escribir
     * @throws IOException en caso de overflow
     */
    public synchronized void writeBytes(byte[] bytes) throws IOException {
        for (int i = 0 ; i < bytes.length ; i++)
            _write(bytes[i]);
    }

    public synchronized int available() {
        return mSize;
    }

    public synchronized int room() {
        return mBuffer.length - mSize;
    }
}
