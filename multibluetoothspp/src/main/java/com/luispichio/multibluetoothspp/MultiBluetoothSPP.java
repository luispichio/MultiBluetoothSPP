package com.luispichio.multibluetoothspp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Luis Pichio on 8/9/2017.
 */

public class MultiBluetoothSPP {
    private final Object mLock = new Object();
    private BluetoothAdapter mBluetoothAdapter;
    private List<MultiBluetoothSPPListener> mMultiBluetoothSPPListener;
    private Set<BluetoothDevice> mConnectedBluetoothDevice;
    private Map<BluetoothDevice, BluetoothSocket> mConnectedBluetoothSocket;
    private Set<BluetoothDevice> mInConnectionProgress;
    private Map<BluetoothDevice, CircularByteBuffer> mBluetoothDeviceRXBuffer;
    private Map<BluetoothDevice, CircularByteBuffer> mBluetoothDeviceTXBuffer;

    public MultiBluetoothSPP(MultiBluetoothSPPListener multiBluetoothSerialListener) {
        mMultiBluetoothSPPListener = new ArrayList<>();
        mConnectedBluetoothDevice = new HashSet<>();
        mConnectedBluetoothSocket = new HashMap<>();
        mInConnectionProgress = new HashSet<>();
        mBluetoothDeviceRXBuffer = new HashMap<>();
        mBluetoothDeviceTXBuffer = new HashMap<>();
        addListener(multiBluetoothSerialListener);
    }

    private static boolean _socketConnected(BluetoothSocket socket) {
        return socket != null && socket.isConnected();
    }

    private static void _socketClose(BluetoothSocket socket) throws IOException {
        if (socket == null)
            return;
        if (socket.isConnected())
            socket.close();
    }

    public void addListener(MultiBluetoothSPPListener listener){
        synchronized (mLock) {
            if (mMultiBluetoothSPPListener.indexOf(listener) == -1)
                mMultiBluetoothSPPListener.add(listener);
        }
    }

    public void removeListener(MultiBluetoothSPPListener listener){
        synchronized (mLock) {
            if (mMultiBluetoothSPPListener.indexOf(listener) != -1)
                mMultiBluetoothSPPListener.remove(listener);
        }
    }

    public boolean setup() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            synchronized (mLock) {
                for (MultiBluetoothSPPListener listener : mMultiBluetoothSPPListener)
                    listener.onBluetoothNotSupported();
            }
            return false;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            synchronized (mLock) {
                for (MultiBluetoothSPPListener listener : mMultiBluetoothSPPListener)
                    listener.onBluetoothDisabled();
            }
            return false;
        }
        return true;
    }

    public boolean isSupported(){
        return mBluetoothAdapter != null;
    }

    public boolean isEnabled(){
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    public void write(BluetoothDevice device, byte[] frame) throws IOException {
        synchronized (mLock) {
            CircularByteBuffer buffer = mBluetoothDeviceTXBuffer.get(device);
            if (buffer == null)
                throw new IOException();
            buffer.writeBytes(frame);
        }
    }

    public List<BluetoothDevice> getPairedDevices() {
        return new ArrayList<>(mBluetoothAdapter.getBondedDevices());
    }

    public List<BluetoothDevice> getConnectedDevices() {
        synchronized (mLock) {
            return new ArrayList<>(mConnectedBluetoothDevice);
        }
    }

    public int getConnectedDevicesCount() {
        synchronized (mLock) {
            return mConnectedBluetoothDevice.size();
        }
    }

    public int getInConnectionProgressDevicesCount() {
        synchronized (mLock) {
            return mInConnectionProgress.size();
        }
    }

    public BluetoothDevice connect(String deviceAddress){
        BluetoothDevice bluetoothDevice = null;
        try {
            bluetoothDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
            connect(bluetoothDevice);
        } catch (IllegalArgumentException e) {
        }
        return bluetoothDevice;
    }

    public void connect(BluetoothDevice device) {
        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();
        if (isInConnectionProgress(device))
            return;
        new ConnectionWorker(device).start();
    }

    public boolean isConnected(BluetoothDevice device) {
        synchronized (mLock){
            BluetoothSocket socket = mConnectedBluetoothSocket.get(device);
            return _socketConnected(socket);
        }
    }

    public boolean isInConnectionProgress(BluetoothDevice device) {
        synchronized (mLock){
            return mInConnectionProgress.contains(device);
        }
    }

    public void disconnect(BluetoothDevice device) {
        synchronized (mLock) {
            BluetoothSocket socket = mConnectedBluetoothSocket.get(device);
            try {
                _socketClose(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnectAll() {
        List<BluetoothDevice> connectedDevices = getConnectedDevices();
        for (BluetoothDevice device : connectedDevices)
            disconnect(device);
    }

    private void addConnectedDevice(BluetoothDevice device, BluetoothSocket socket){
        synchronized (mLock) {
            if (!mConnectedBluetoothDevice.contains(device)){
                mConnectedBluetoothDevice.add(device);
                mConnectedBluetoothSocket.put(device, socket);
                mBluetoothDeviceRXBuffer.put(device, new CircularByteBuffer(4096));
                mBluetoothDeviceTXBuffer.put(device, new CircularByteBuffer(4096));
            }
        }
    }

    private void removeConnectedDevice(BluetoothDevice device){
        synchronized (mLock) {
            if (mConnectedBluetoothDevice.contains(device)){
                mConnectedBluetoothDevice.remove(device);
                mConnectedBluetoothSocket.remove(device);
                mBluetoothDeviceRXBuffer.remove(device);
                mBluetoothDeviceTXBuffer.remove(device);
            }
        }
    }

    private void addInConnectionProgressDevice(BluetoothDevice device){
        synchronized (mLock) {
            if (!mInConnectionProgress.contains(device))
                mInConnectionProgress.add(device);
        }
    }

    private void removeInConnectionProgressDevice(BluetoothDevice device){
        synchronized (mLock) {
            if (mInConnectionProgress.contains(device))
                mInConnectionProgress.remove(device);
        }
    }

    private void dispatchEvent(int what, int arg1, int arg2, Object obj){
        synchronized (mLock) {
            for (MultiBluetoothSPPListener listener : mMultiBluetoothSPPListener) {
                try {
                    listener.obtainMessage(what, arg1, arg2, obj).sendToTarget();
                } catch (RuntimeException e) {
                }
            }
        }
    }

    private void _rx(BluetoothDevice device, byte[] frame){
        synchronized (mLock) {
            CircularByteBuffer buffer = mBluetoothDeviceRXBuffer.get(device);
            if (buffer != null){
                try {
                    buffer.writeBytes(frame);
                } catch (IOException e) {
                }
            }
        }
    }

    private void _tx(BluetoothDevice device, OutputStream outputStream){
        synchronized (mLock) {
            CircularByteBuffer buffer = mBluetoothDeviceTXBuffer.get(device);
            if (buffer != null && buffer.available() > 0){
                try {
                    outputStream.write(buffer.readBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int available(BluetoothDevice device) throws IOException {
        synchronized (mLock) {
            CircularByteBuffer buffer = mBluetoothDeviceRXBuffer.get(device);
            if (buffer != null)
                return buffer.available();
            throw new IOException();
        }
    }

    public int read(BluetoothDevice device) throws IOException {
        synchronized (mLock) {
            CircularByteBuffer buffer = mBluetoothDeviceRXBuffer.get(device);
            if (buffer != null)
                return buffer.read();
            throw new IOException();
        }
    }

    public byte[] readBytes(BluetoothDevice device) throws IOException {
        synchronized (mLock) {
            CircularByteBuffer buffer = mBluetoothDeviceRXBuffer.get(device);
            if (buffer != null)
                return buffer.readBytes();
            throw new IOException();
        }
    }

    public void write(BluetoothDevice device, byte _byte) throws IOException {
        synchronized (mLock) {
            CircularByteBuffer buffer = mBluetoothDeviceTXBuffer.get(device);
            if (buffer != null)
                buffer.write(_byte);
            else
                throw new IOException();
        }
    }

    public void writeBytes(BluetoothDevice device, byte[] bytes) throws IOException {
        synchronized (mLock) {
            CircularByteBuffer buffer = mBluetoothDeviceTXBuffer.get(device);
            if (buffer != null)
                buffer.writeBytes(bytes);
            else
                throw new IOException();
        }
    }

    public byte[] readBytes(BluetoothDevice device, int size) throws IOException {
        synchronized (mLock) {
            CircularByteBuffer buffer = mBluetoothDeviceRXBuffer.get(device);
            if (buffer != null)
                return buffer.readBytes(size);
            throw new IOException();
        }
    }

    private class ConnectionWorker extends Thread {
        BluetoothDevice mBluetoothDevice;
        BluetoothSocket mBluetoothSocket;

        public ConnectionWorker(BluetoothDevice device) {
            mBluetoothDevice = device;
            addInConnectionProgressDevice(mBluetoothDevice);
        }

        @Override
        public void run() {
            int retrys = 5;
            while (!isInterrupted() && retrys > 0) {
                try {
                    try {
                        // Standard SerialPortService ID
                        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                        mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                    } catch (Exception ce){
    //                    serialSocket = connectViaReflection(device);
                    }
                    mBluetoothSocket.connect();
                    interrupt();
                } catch (IOException e) {
                    retrys--;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                }
            }
            removeInConnectionProgressDevice(mBluetoothDevice);
            if (_socketConnected(mBluetoothSocket))
                new ConnectedWorker(mBluetoothDevice, mBluetoothSocket).start();
            else
                dispatchEvent(MultiBluetoothSPPListener.MESSAGE_DEVICE_CONNECTION_FAILURE, -1, 1, mBluetoothDevice);
        }
    }

    private class ConnectedWorker extends Thread {
        BluetoothDevice mBluetoothDevice;
        BluetoothSocket mBluetoothSocket;
        InputStream mInputStream;
        OutputStream mOutputStream;

        public ConnectedWorker(BluetoothDevice bluetoothDevice, BluetoothSocket serialSocket) {
            mBluetoothDevice = bluetoothDevice;
            mBluetoothSocket = serialSocket;
            try {
                mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
                addConnectedDevice(mBluetoothDevice, mBluetoothSocket);
                dispatchEvent(MultiBluetoothSPPListener.MESSAGE_DEVICE_CONNECTED, -1, 1, mBluetoothDevice);
            } catch (IOException e) {
                e.printStackTrace();
                interrupt();
            }
        }

        public void run() {
            int available;
            while (!isInterrupted() && _socketConnected(mBluetoothSocket)) {
                try {
                    if ((available = mInputStream.available()) > 0) {
                        byte[] frame = new byte[available];
                        mInputStream.read(frame, 0, available);
                        _rx(mBluetoothDevice, frame);
                        dispatchEvent(MultiBluetoothSPPListener.MESSAGE_FRAME_RECEIVED, -1, 1, mBluetoothDevice);
                    }
                    _tx(mBluetoothDevice, mOutputStream);
                } catch (IOException e) {
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                }
            }
            try {
                _socketClose(mBluetoothSocket);
            } catch (IOException e) {
            }
            removeConnectedDevice(mBluetoothDevice);
            dispatchEvent(MultiBluetoothSPPListener.MESSAGE_DEVICE_DISCONNECTED, -1, 1, mBluetoothDevice);
        }
    }
}
