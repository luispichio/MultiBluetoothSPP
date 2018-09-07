package com.luispichio.multibluetoothspp;

import android.bluetooth.BluetoothDevice;

import java.io.IOException;
import java.io.OutputStream;

public class MultiBluetoothSPPOutputStream extends OutputStream {
    private final MultiBluetoothSPP mMultiBluetoothSPP;
    private final BluetoothDevice mBluetoothDevice;

    public MultiBluetoothSPPOutputStream(MultiBluetoothSPP multiBluetoothSPP, BluetoothDevice bluetoothDevice){
        mMultiBluetoothSPP = multiBluetoothSPP;
        mBluetoothDevice = bluetoothDevice;
    }

    @Override
    public void write(int i) throws IOException {
        mMultiBluetoothSPP.write(mBluetoothDevice, (byte) i);
    }

    @Override
    public void write(byte[] b) throws IOException {
        mMultiBluetoothSPP.write(mBluetoothDevice, b);
    }
}
