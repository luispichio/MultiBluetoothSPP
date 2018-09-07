package com.luispichio.multibluetoothsppdemo;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class BluetoothDeviceListAdapter extends RecyclerView.Adapter<BluetoothDeviceListAdapter.ViewHolder> {
    private static final String TAG = "B...DeviceListAdapter";
    private List<BluetoothDevice> mPairedDeviceList;
    private List<BluetoothDevice> mConnectedDeviceList;
    private View.OnClickListener mOnClickListener;

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTextView;
        private final Button mButton;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Element " + getAdapterPosition() + " clicked.");
                }
            });
            mTextView = (TextView) v.findViewById(R.id.bluetooth_device_list_adapter_text_view);
            mButton = (Button) v.findViewById(R.id.bluetooth_device_list_adapter_button);
        }

        public TextView getTextView() {
            return mTextView;
        }

        public Button getButton(){
            return mButton;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     */
    public BluetoothDeviceListAdapter(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public void setDeviceLists(List<BluetoothDevice> pairedDeviceList, List<BluetoothDevice> connectedDeviceList) {
        mPairedDeviceList = pairedDeviceList;
        mConnectedDeviceList = connectedDeviceList;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bluetooth_devices_list_adapter, viewGroup, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d(TAG, "Element " + position + " set.");

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        Button button = viewHolder.getButton();
        button.setOnClickListener(mOnClickListener);
        BluetoothDevice device = mPairedDeviceList.get(position);
        if (mConnectedDeviceList.contains(device))
            button.setText("Desconectar");
        else
            button.setText("Conectar");
        viewHolder.getTextView().setText(device.getName());
        button.setTag(device);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mPairedDeviceList.size();
    }
}