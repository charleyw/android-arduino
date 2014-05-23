/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lbs.xrobot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.koushikdutta.async.http.socketio.SocketIOClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lbs.xrobot.handler.ArduinoCommandCallback;
import lbs.xrobot.handler.ArduinoController;
import lbs.xrobot.handler.RTCClient;
import lbs.xrobot.handler.ServerConnectCallback;

/**
 * This class provides a basic demonstration of how to write an Android
 * activity. Inside of its window, it places a single view: an EditText that
 * displays and edits some internal text.
 */
public class MainActivity extends Activity {
    public static String TAG = "X_ROBOT_ACTIVITY";
    private ToggleButton startBtn;
    private EditText serverAddressEditor;
    private Spinner bluetoothDevicesList;
    private String serverAddress;
    private BluetoothDevice bluetoothDevice;
    private ArduinoController arduinoController;
    private ServerConnectCallback serverConnectCallback;
    private PowerManager.WakeLock mWakeLock;

    public MainActivity() {
    }

    /** Called with the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate our UI from its XML layout description.
        setContentView(R.layout.xrobot_activity);
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
        initialize();
        bind();
    }

    private void initialize() {
        arduinoController = new ArduinoController(this);
        initializeControls();
    }

    private void bind() {
        startBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    updateConfiguration();
                    connect();
                } else {
                    disconnectAll();
                    Toast.makeText(getBaseContext(), "Disconnected all", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void disconnectAll() {
        serverConnectCallback.disconnect();
        arduinoController.disconnect();
    }

    private void connect() {
        try {
            connectServer(connectArduino(), new RTCClient(this));
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "Fail to connect arduino: " + e, Toast.LENGTH_SHORT).show();
        }
    }

    private ArduinoCommandCallback connectArduino() throws Exception {
        try {
            arduinoController.connect(bluetoothDevice);
            Toast.makeText(getBaseContext(), "arduino via bluetooth: " + bluetoothDevice.getAddress(), Toast.LENGTH_SHORT).show();
            return new ArduinoCommandCallback(this, arduinoController);
        } catch (IOException e) {
            throw new Exception("Connect arduino failed", e);
        } catch (InterruptedException e) {
            throw new Exception("Connect arduino failed", e);
        }
    }

    private void connectServer(ArduinoCommandCallback arduinoCommandCallback , RTCClient rtcClient) {
        serverConnectCallback = new ServerConnectCallback(this, arduinoCommandCallback, rtcClient);
        SocketIOClient.connect(serverAddress, serverConnectCallback,new Handler());
    }

    private void updateConfiguration() {
        serverAddress = serverAddressEditor.getText().toString();
    }

    private void initializeControls() {
        startBtn = (ToggleButton) findViewById(R.id.start);
        serverAddressEditor = (EditText) findViewById(R.id.serverAddress);
        bluetoothDevicesList = (Spinner) findViewById(R.id.bluetoothDevices);
        createBluetoothList();
    }

    private void createBluetoothList() {
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        final Map<String, BluetoothDevice> btMap = new HashMap<String, BluetoothDevice>();
        for(BluetoothDevice bt : bondedDevices){
            btMap.put(bt.getName(), bt);
        }
        List<String> arrayList = new ArrayList(btMap.keySet());
        ArrayAdapter<String> btNames = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrayList);
        btNames.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bluetoothDevicesList.setAdapter(btNames);
        bluetoothDevicesList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String btName = (String) adapterView.getItemAtPosition(i);
                bluetoothDevice = btMap.get(btName);
                Toast.makeText(getBaseContext(), "bluetooth mac: " + bluetoothDevice.getAddress(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /**
     * Called when the activity is about to start interacting with the user.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        this.mWakeLock.release();
        super.onDestroy();
    }
}
