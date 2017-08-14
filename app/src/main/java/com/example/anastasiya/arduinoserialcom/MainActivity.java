package com.example.anastasiya.arduinoserialcom;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {
    TextView tv;
    private static String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    protected static final String TAG = null;

    UsbManager usbManager;
    UsbDevice device;
    UsbDeviceConnection connection;
    UsbSerialDevice serialPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.textView);
        tv.setText("");
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    }

    public void connectUsbDevice() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        IntentFilter deviceAttachedFilter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        IntentFilter deviceUsbPermissions = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(broadcastReceiver, deviceAttachedFilter);
        registerReceiver(broadcastReceiver, deviceUsbPermissions);
        while(deviceIterator.hasNext()){
            device = deviceIterator.next();
            String s = device.getDeviceName();
            int pid = device.getProductId();
            if(pid == 29987) {
                int vid = device.getVendorId();
                tv.append("\nDevice name: " + s + "\n" + "Product id: " +Integer.toString(pid) + "\n" + "Vendor id: " + Integer.toString(vid));
                PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                usbManager.requestPermission(device, pi);
            }
            else {
                connection = null;
                device = null;
            }
        }
        if(device == null) {
            tv.append("\nNo devices found!");
        }
    }

    public void disconnectUsbDevice(){
        serialPort.close();
    }

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                data.concat("/n");
                tv.append(data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                tv.append(new String("dsdsd"));
            }
        }
    };



    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            tv.append("BroadcastReceiver");
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                tv.append("ACTION_USB_PERMISSION");
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) {
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback); //
                            tv.append("Serial Connection Opened!\n");

                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
               tv.append("ACTION_USB_DEVICE_ATTACHED");
               connectUsbDevice();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                tv.append("ACTION_USB_DEVICE_DETACHED");
                disconnectUsbDevice();
            }
        };
    };
}
