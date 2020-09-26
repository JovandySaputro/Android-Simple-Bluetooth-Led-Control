package com.mcuhq.simplebluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import java.io.BufferedReader;

public class Sending extends AppCompatActivity {
    EditText editVar1, editVar2;
    Button btnSend, btnOn, btnOff, btnBlue;
    String address = null;
    BluetoothAdapter mBluetoothAdapter;
    private UUID applicationUUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ProgressDialog mBluetoothConnectProgressDialog;
    private BluetoothSocket mBluetoothSocket;
    BluetoothDevice mBluetoothDevice;
    OutputStream os;
    final static String on="92";    //On
    final static String off="79"; //Off
    private TextView txtStatus;
    private BluetoothSocket mBTSocket;
    String mDeviceAddress;

    protected static final String TAG = "TAG";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        //view of the Sending
        setContentView(R.layout.activity_sending);
        txtStatus = findViewById(R.id.ID_STATUSTEXT);
        btnBlue = findViewById(R.id.btnBluethoot);
        editVar1 = (EditText) findViewById(R.id.variable_1);
        editVar2 = (EditText) findViewById(R.id.variable_2);
        btnSend = (Button) findViewById(R.id.buttonSend);
        btnOn = (Button)findViewById(R.id.buttonON);
        btnOff = (Button) findViewById(R.id.buttonOFF);
        btnBlue.setOnClickListener(view -> {

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                Toast.makeText(mContext, "Message1", Toast.LENGTH_SHORT).show();
            } else {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent,
                            REQUEST_ENABLE_BT);
                } else {
                    ListPairedDevices();
                    Intent connectIntent = new Intent(mContext,
                            DeviceListUtil.class);
                    startActivityForResult(connectIntent,
                            REQUEST_CONNECT_DEVICE);
                }
            }

        });
        btnSend.setOnClickListener(v->{

                    String variabel1 = editVar1.getText().toString();
                    String variabel2 = editVar2.getText().toString();

        });


    //Button LED ON
    btnOn.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {

                try {
                    mBTSocket.getOutputStream().write(on.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                    //Toast.makeText(getBaseContext(),"LED ON", Toast.LENGTH_SHORT).show();
                }

        }
    });
    //Button LED OFF
    btnOff.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
//            if(btsocket == null){
//                Intent BTIntent = new Intent(getApplicationContext(), DeviceList.class);
//                startActivityForResult(BTIntent, DeviceList.REQUEST_CONNECT_BT);
//            }
            try{
                mBTSocket.getOutputStream().write(off.getBytes());
            } catch (IOException e){
                e.printStackTrace();
                //Toast.makeText(getBaseContext(),"LED OFF", Toast.LENGTH_SHORT).show();
            }
        }
    });
    }
    private void ListPairedDevices() {
        Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter
                .getBondedDevices();
        if (mPairedDevices.size() > 0) {
            for (BluetoothDevice mDevice : mPairedDevices) {
                Log.v(TAG, "PairedDevices: " + mDevice.getName() + "  "
                        + mDevice.getAddress());
            }
        }
    }
    public void onActivityResult(int mRequestCode, int mResultCode,
                                 Intent mDataIntent) {
        super.onActivityResult(mRequestCode, mResultCode, mDataIntent);


        switch (mRequestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (mResultCode == Activity.RESULT_OK) {
                    Bundle mExtra = mDataIntent.getExtras();
                    mDeviceAddress = mExtra.getString("DeviceAddress");
                    Log.v(TAG, "Coming incoming address " + mDeviceAddress);
                    mBluetoothDevice = mBluetoothAdapter
                            .getRemoteDevice(mDeviceAddress);
                    mBluetoothConnectProgressDialog = ProgressDialog.show(this,
                            "Connecting...", mBluetoothDevice.getName() + " : "
                                    + mBluetoothDevice.getAddress(), true, true);
                    Thread mBlutoothConnectThread = new Thread(this::run);
                    mBlutoothConnectThread.start();

                }
                break;

            case REQUEST_ENABLE_BT:
                if (mResultCode == Activity.RESULT_OK) {
                    ListPairedDevices();
                    Intent connectIntent = new Intent(mContext,
                            DeviceListUtil.class);
                    startActivityForResult(connectIntent, REQUEST_CONNECT_DEVICE);
                } else {
                    Toast.makeText(mContext, "Message", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void run() {
        try {
            mBluetoothSocket = mBluetoothDevice
                    .createRfcommSocketToServiceRecord(applicationUUID);
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothSocket.connect();
            mHandler.sendEmptyMessage(0);
            txtStatus.setText("Terhubung");
        } catch (IOException eConnectException) {
            mBluetoothConnectProgressDialog.dismiss();
            txtStatus.setText("Tidak Terhubung");
            Log.d(TAG, "CouldNotConnectToSocket", eConnectException);
            closeSocket(mBluetoothSocket);
            return;
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mBluetoothConnectProgressDialog.dismiss();
            txtStatus.setText("Terhubung");
            Toast.makeText(mContext, "DeviceConnected", Toast.LENGTH_SHORT).show();
        }
    };

    private void closeSocket(BluetoothSocket nOpenSocket) {
        try {
            nOpenSocket.close();
            Log.d(TAG, "SocketClosed");
        } catch (IOException ex) {
            Log.d(TAG, "CouldNotCloseSocket");
        }
    }


}