package ru.startandroid.app1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    int i=0;
    Button btscan;
    TextView text;
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btscan= (Button)findViewById(R.id.button);
        text= (TextView) findViewById(R.id.textView);
        text.setMovementMethod(new ScrollingMovementMethod());
        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }

    }

    public void scan(View v){
        if(i==0)
        {
            text.setText("");
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    //btScanner.startScan(ScanCallback);
                    btAdapter.startLeScan(leScanCallback);
                }
            });
            btscan.setText("Stop");
            i=1;
        }
        else
        {
            text.append("Stopped Scanning");
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    //btScanner.stopScan(ScanCallback);
                    btAdapter.stopLeScan(leScanCallback);
                }
            });
            btscan.setText("Scan");
            i=0;
        }
    }

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord)
        {
            text.append("Device Name: " + device.getName()+ "\n" +"Device Address: " + device.getAddress() + "\n" + "RSSI: " + rssi + "\n");

            int startByte = 5;
            // boolean patternFound = false;
            /** while (startByte <= 5)
             {
             if (    ((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
             ((int) scanRecord[startByte + 3] & 0xff) == 0x15)
             { //Identifies correct data length
             patternFound = true;
             break;
             }
             startByte++;
             }*/

            // if (patternFound)
            //{
            //Convert to hex String

            byte[] uuidBytes = new byte[16];
            System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
            String hexString = bytesToHex(uuidBytes);

            //UUID detection
            String uuid =  hexString.substring(0,8) + "-" +
                    hexString.substring(8,12) + "-" +
                    hexString.substring(12,16) + "-" +
                    hexString.substring(16,20) + "-" +
                    hexString.substring(20,32);

            // major
            final int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);

            // minor
            final int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);

            Log.i(LOG_TAG,"UUID: " +uuid + "\\nmajor: " +major +"\\nminor" +minor);

            text.setText(text.getText() + "UUID: " +uuid + "\n" + "\\nmajor: " +major + "\n" +"\\nminor" +minor + "\n");
            // }


            // auto scroll for text view
            final int scrollAmount = text.getLayout().getLineTop(text.getLineCount()) - text.getHeight();
            // if there is no need to scroll, scrollAmount will be <=0
            if (scrollAmount > 0)
                text.scrollTo(0, scrollAmount);

        }
    };


    /**
     * bytesToHex method
     */
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ )
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
