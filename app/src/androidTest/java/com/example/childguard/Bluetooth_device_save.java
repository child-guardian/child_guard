package com.example.childguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

public class Bluetooth_device_save extends AppCompatActivity {
    final SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(this);
    public Bluetooth_device_save() {
    }
    public void device_save(String deviceAddress){
        SharedPreferences.Editor e=pref.edit();
        e.putString("bluetooth_device1",deviceAddress);
        e.apply();
    }
    public String device_info(){
        return pref.getString("bluetooth_device1","not_device");
    }
}
