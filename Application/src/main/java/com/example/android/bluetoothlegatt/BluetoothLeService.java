/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.example.android.bluetoothlegatt;

import android.app.KeyguardManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.os.Handler;
import com.example.android.bluetoothlegatt.DeviceControlActivity;
import java.util.List;
import java.util.UUID;
import com.example.android.bluetoothlegatt.DeviceControlActivity;
/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private int temp = 0;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String ACTION_DOUBLE_TAP =
            "com.example.bluetooth.le.ACTION_DOUBLE_TAP";
    public final static UUID UUID_DOUBLE_TAP_CHARACTERISTIC =
            UUID.fromString(SampleGattAttributes.DOUBLE_TAP_CHARACTERISTIC);
    public final static UUID UUID_ACC_VAL_CHARACTERISTIC =
            UUID.fromString(SampleGattAttributes.ACC_VAL_CHARACTERISTIC);
    public final static UUID UUID_TEMP_VAL_CHARACTERISTIC=
            UUID.fromString(SampleGattAttributes.TEMP_VAL_CHARACTERISTIC);

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            Log.d(TAG, "------------- onCharacteristicWrite status: " + status);

            // do somethings here.
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    public void wakeDevice() {

        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
        wakeLock.acquire();

        Log.d(TAG, "JACK!!!! WAKE UP!!!");


        KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
        keyguardLock.disableKeyguard();
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        if (UUID_DOUBLE_TAP_CHARACTERISTIC.equals(characteristic.getUuid())){
//            temp = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
//            intent = new Intent(ACTION_DOUBLE_TAP);
            wakeDevice();
            final String s = (temp==1) ? "@D@ENABLED" : "@D@DISABLED";
            Log.d(TAG, String.format("Received Interrupt: %s", s));
            intent.putExtra(EXTRA_DATA, s);

        } else if (UUID_ACC_VAL_CHARACTERISTIC.equals(characteristic.getUuid())) {
            final float pitch = ((float)characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 0))/10;
            final String sPitch  = Float.toString(pitch);

            final float roll = ((float)characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 4))/10;
            final String sRoll  = Float.toString(roll);


            final String s = "@P@Pitch:  " + sPitch + ";@R@Roll:  " + sRoll;
            Log.d(TAG, String.format("Received acc: %s", s));
            intent.putExtra(EXTRA_DATA, s);
        } else if (UUID_TEMP_VAL_CHARACTERISTIC.equals(characteristic.getUuid())) {
            final float temp = ((float)characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0))/10;
            final String sTemp = Float.toString(temp);

            final String s = "@T@Temperature: " + sTemp + "° Celsius";
            Log.d(TAG, String.format("Received temperature: %s", s));
            intent.putExtra(EXTRA_DATA, s);
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Requst a write on a give {@code BluetoothGattCharacteristic}. The write result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicWrite(andorid.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        boolean ret = mBluetoothGatt.writeCharacteristic(characteristic);
        if (ret){
            Log.w(TAG, "YESSSS");
        } else {
            Log.w(TAG, "NOOOOO");
        }
    }


    private boolean isFirstTry = true;
    private int count = 0;
    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        if (UUID_DOUBLE_TAP_CHARACTERISTIC.equals(characteristic.getUuid())) {
            final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
            if (isFirstTry) {
                count = 2;
                isFirstTry = false;
            } else {
                count++;
            }
            if (count == 2){
                if (temp == 0){
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                            UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(descriptor);
                } else if (temp==1) {
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                            UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                    descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(descriptor);
                }
                temp = (temp==0) ? 1 : 0;
                count = 0;
            }
            final String s = (temp==1) ? "@D@ENABLED" : "@D@DISABLED";
            Log.d(TAG, s);
            intent.putExtra(EXTRA_DATA, s);
            sendBroadcast(intent);

        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}
