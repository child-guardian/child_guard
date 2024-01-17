package com.example.childguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

public class Bluetooth_device_save {
    SharedPreferences pref;

//    public void device_save(String deviceAddress){
//        pref = PreferenceManager.getDefaultSharedPreferencesName()
//        SharedPreferences.Editor e=pref.edit();
//        e.putString("bluetooth_device1",deviceAddress);
//        e.apply();
//    }
    public String device_info(){
        return pref.getString("bluetooth_device1","not_device");
    }
}
