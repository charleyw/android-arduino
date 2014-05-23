package lbs.xrobot.handler;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.shokai.firmata.ArduinoFirmata;

import java.io.IOException;

import lbs.xrobot.BluetoothSerialService;

public class ArduinoController {
    private BluetoothSerialService bluetoothSerialService;

    // Debugging
    private static final String TAG = "ArduinoController";
    private static final boolean D = true;

    public ArduinoController(Activity activity) {
        bluetoothSerialService = new BluetoothSerialService(mHandler);
    }


    public boolean isConnected() {
        return bluetoothSerialService.getState() == BluetoothSerialService.STATE_CONNECTED;
    }

    public void connect(BluetoothDevice bluetoothDevice) throws IOException, InterruptedException {
        bluetoothSerialService.connect(bluetoothDevice, false);

    }

    public void disconnect(){
        bluetoothSerialService.stop();
    }

    public void forward(){
        Log.i(TAG, "SEND TO ARDUINO: w ");
        sendToArduino("1,1;");
    }

    public void turnLeft(){
        Log.i(TAG, "SEND TO ARDUINO: a ");
        sendToArduino("-1,1;");
    }

    public void turnRight(){
        Log.i(TAG, "SEND TO ARDUINO: d ");
        sendToArduino("1,-1;");
    }


    public void backward(){
        Log.i(TAG, "SEND TO ARDUINO: s ");
        sendToArduino("-1,-1;");
    }

    public void sendToArduino(String s) {
        bluetoothSerialService.write((s+";").getBytes());
    }

    StringBuffer buffer = new StringBuffer();

    private final Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothSerialService.MESSAGE_READ:
                    buffer.append((String)msg.obj);

//                    if (dataAvailableCallback != null) {
//                        sendDataToSubscriber();
//                    }
                    break;
                case BluetoothSerialService.MESSAGE_STATE_CHANGE:

                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothSerialService.STATE_CONNECTED:
                            Log.i(TAG, "BluetoothSerialService.STATE_CONNECTED");
                            notifyConnectionSuccess();
                            break;
                        case BluetoothSerialService.STATE_CONNECTING:
                            Log.i(TAG, "BluetoothSerialService.STATE_CONNECTING");
                            break;
                        case BluetoothSerialService.STATE_LISTEN:
                            Log.i(TAG, "BluetoothSerialService.STATE_LISTEN");
                            break;
                        case BluetoothSerialService.STATE_NONE:
                            Log.i(TAG, "BluetoothSerialService.STATE_NONE");
                            break;
                    }
                    break;
                case BluetoothSerialService.MESSAGE_WRITE:
                    //  byte[] writeBuf = (byte[]) msg.obj;
                    //  String writeMessage = new String(writeBuf);
                    //  Log.i(TAG, "Wrote: " + writeMessage);
                    break;
                case BluetoothSerialService.MESSAGE_DEVICE_NAME:
//                    Log.i(TAG, msg.getData().getString(DEVICE_NAME));
                    break;
                case BluetoothSerialService.MESSAGE_TOAST:
                    String message = msg.getData().getString(BluetoothSerialService.TOAST);
                    notifyConnectionLost(message);
                    break;
            }
        }


    };

    private void notifyConnectionLost(String message) {

    }

    private void notifyConnectionSuccess() {

    }

}
