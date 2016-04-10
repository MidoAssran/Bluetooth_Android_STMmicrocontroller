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

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    /*
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    */

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static String DOUBLE_TAP_CHARACTERISTIC = "e23e78a0-cf4a-11e1-8ffc-0002a5d5c51b";
    public static String ACC_VAL_CHARACTERISTIC = "340a1b80-cf4b-11e1-ac36-0002a5d5c51b";
    public static String TEMP_VAL_CHARACTERISTIC = "a32e5520-e477-11e2-a9e3-0002a5d5c51b";
    public static String LED_TOGGLE_CHARACTERISTIC = "f32e5520-e477-11e2-a9e3-0002a5d5c51b";

    static {
        // Sample Services.
        attributes.put("02366e80-cf3a-11e1-9ab4-0002a5d5c51b", "Accelerometer Service");
        attributes.put("42821a40-e477-11e2-82d0-0002a5d5c51b", "Temperature Service");
        attributes.put("37821a40-d497-11e3-32d0-0002a5d5c51b", "LED Control Service");

        // Sample Characteristics.
        attributes.put(DOUBLE_TAP_CHARACTERISTIC, "Double Tap Characteristic");
        attributes.put(ACC_VAL_CHARACTERISTIC, "Accelerometer Value Characteristic");
        attributes.put(TEMP_VAL_CHARACTERISTIC, "Temperature Value Characteristic");
        attributes.put(LED_TOGGLE_CHARACTERISTIC, "LED Toggle Characteristic");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
