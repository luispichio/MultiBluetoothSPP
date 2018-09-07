package com.luispichio.multibluetoothspp;

import android.bluetooth.BluetoothDevice;

import java.io.IOException;
import java.io.InputStream;

public class MultiBluetoothSPPInputStream extends InputStream {
    private final MultiBluetoothSPP mMultiBluetoothSPP;
    private final BluetoothDevice mBluetoothDevice;

    public MultiBluetoothSPPInputStream(MultiBluetoothSPP multiBluetoothSPP, BluetoothDevice bluetoothDevice){
        mMultiBluetoothSPP = multiBluetoothSPP;
        mBluetoothDevice = bluetoothDevice;
    }

    @Override
    public int available() throws IOException {
        return mMultiBluetoothSPP.available(mBluetoothDevice);
    }

    @Override
    public int read() throws IOException {
        return mMultiBluetoothSPP.read(mBluetoothDevice);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        mMultiBluetoothSPP.readBytes(mBluetoothDevice, off);
        byte[] bytes = mMultiBluetoothSPP.readBytes(mBluetoothDevice, len);
        System.arraycopy(bytes, 0, b, 0, len);
        return len;
    }
}
