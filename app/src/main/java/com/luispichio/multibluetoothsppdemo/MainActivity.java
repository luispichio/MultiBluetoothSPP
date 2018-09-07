package com.luispichio.multibluetoothsppdemo;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.luispichio.multibluetoothspp.MultiBluetoothSPP;
import com.luispichio.multibluetoothspp.MultiBluetoothSPPListener;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private RecyclerView mRecyclerView;
    private BluetoothDeviceListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button mButtonTX;
    private EditText mEditTextTX;
    private EditText mEditTextRX;
    private String mRXBuffer = "";

    MultiBluetoothSPP mMultiBluetoothSPP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewDevices);
        mButtonTX = (Button) findViewById(R.id.buttonTX);
        mEditTextTX = (EditText) findViewById(R.id.editTextTX);
        mEditTextRX = (EditText) findViewById(R.id.editTextRX);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mMultiBluetoothSPP = new MultiBluetoothSPP(mMultiBluetoothSPPListener);
        mMultiBluetoothSPP.setup();

        // specify an adapter (see also next example)
        mAdapter = new BluetoothDeviceListAdapter(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BluetoothDevice device = (BluetoothDevice)view.getTag();
                        Log.d(TAG, "Element " + device.getName() + " clicked.");
                        if (mMultiBluetoothSPP.isConnected(device))
                            mMultiBluetoothSPP.disconnect(device);
                        else
                            mMultiBluetoothSPP.connect(device);
                    }
                }
        );
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setDeviceLists(mMultiBluetoothSPP.getPairedDevices(),  mMultiBluetoothSPP.getConnectedDevices());

        mButtonTX.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        broadcast(mEditTextTX.getText());
                    }
                }
        );
    }

    private void broadcast(Editable text) {
        for (BluetoothDevice device : mMultiBluetoothSPP.getConnectedDevices()){
            try {
                mMultiBluetoothSPP.write(device, text.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private MultiBluetoothSPPListener mMultiBluetoothSPPListener = new MultiBluetoothSPPListener() {
        @Override
        public void onBluetoothNotSupported() {
        }

        @Override
        public void onBluetoothDisabled() {
        }

        @Override
        public void onBluetoothDeviceConnected(BluetoothDevice device) {
            mAdapter.setDeviceLists(mMultiBluetoothSPP.getPairedDevices(),  mMultiBluetoothSPP.getConnectedDevices());
//            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onBluetoothDeviceDisconnected(BluetoothDevice device) {
            mAdapter.setDeviceLists(mMultiBluetoothSPP.getPairedDevices(),  mMultiBluetoothSPP.getConnectedDevices());
//            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onBluetoothDeviceConnectionFailure(BluetoothDevice device) {
        }

        @Override
        public void onBluetoothFrameReceived(BluetoothDevice device) {
            try {
                mRXBuffer = device.getName() + " " + device.getAddress() + "\n" + new String(mMultiBluetoothSPP.readBytes(device)) + "\n" + mRXBuffer;
                mEditTextRX.setText(mRXBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}
