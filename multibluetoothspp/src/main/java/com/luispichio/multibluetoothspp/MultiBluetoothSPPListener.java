package com.luispichio.multibluetoothspp;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;

/**
 * Created by Luis Picho on 8/9/2017.
 */

public abstract class MultiBluetoothSPPListener extends Handler {
    static final int MESSAGE_DEVICE_CONNECTED = 0;
    static final int MESSAGE_DEVICE_DISCONNECTED = 1;
    static final int MESSAGE_DEVICE_CONNECTION_FAILURE = 2;
    static final int MESSAGE_FRAME_RECEIVED = 3;

    abstract public void onBluetoothNotSupported();
    abstract public void onBluetoothDisabled();
    abstract public void onBluetoothDeviceConnected(BluetoothDevice device);
    abstract public void onBluetoothDeviceDisconnected(BluetoothDevice device);
    abstract public void onBluetoothDeviceConnectionFailure(BluetoothDevice device);
    abstract public void onBluetoothFrameReceived(BluetoothDevice device);

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_DEVICE_CONNECTED:
                onBluetoothDeviceConnected((BluetoothDevice) msg.obj);
                break;
            case MESSAGE_DEVICE_CONNECTION_FAILURE:
                onBluetoothDeviceConnectionFailure((BluetoothDevice) msg.obj);
                break;
            case MESSAGE_DEVICE_DISCONNECTED:
                onBluetoothDeviceDisconnected((BluetoothDevice) msg.obj);
                break;
            case MESSAGE_FRAME_RECEIVED:
                onBluetoothFrameReceived((BluetoothDevice) msg.obj);
                break;
        }
    }
}
