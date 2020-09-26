package com.mcuhq.simplebluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;

public class BluetoothStateUtil implements Serializable {
    public static final int STATE_NOT_CONNECTED = 1;
    public static final int STATE_CONNECTED = 1;

    public static final String filename = "btState.pref";

    public static int connectionState = STATE_NOT_CONNECTED;
    public static String deviceAddress = "00:00:00:00:00:00";
    public static String deviceName = "";

    public static void setConnectionState(boolean connected, BluetoothDevice device) {
        if(connected)
            connectionState = STATE_CONNECTED;
        else
            connectionState = STATE_NOT_CONNECTED;

        if(device != null) {
            deviceAddress = device.getAddress();
            deviceName = device.getName();
        }

    }

    public static void saveConnectionState(Context cxt) throws IOException {
        FileOutputStream fos = cxt.openFileOutput(filename, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeInt(connectionState);
        oos.writeUTF(deviceAddress);
        oos.writeUTF(deviceName);
    }

    public static void loadConnectionState(Context cxt) throws IOException {
        FileInputStream fis = cxt.openFileInput(filename);
        ObjectInputStream ois = new ObjectInputStream(fis);
        connectionState = ois.readInt();
        deviceAddress = ois.readUTF();
        deviceName = ois.readUTF();
    }

    public static BluetoothDevice getDevice() {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!btAdapter.isEnabled())
            btAdapter.enable();

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        for(BluetoothDevice d : pairedDevices)
            if(d.getAddress().equalsIgnoreCase(deviceAddress))
                return d;

        return null;
    }

}
