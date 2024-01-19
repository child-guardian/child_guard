package com.example.childguard;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class TestService extends Service  {

    public int onStartCommand(Intent intent, int flags, int startId) {

        //Bluetooth検知機能
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("BT", "No permission to connect bluetooth devices");

        }
        else {
            Log.d("BT", "Permission to connect bluetooth devices granted");
        }


        registerReceiver(receiver, intentFilter);
        //audioStart();

        return START_NOT_STICKY;


    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); // may need to chain this to a recognizing function
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            HomeFragment homeFragment=new HomeFragment();

            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d("BT", "No permission to connect bluetooth devices");
                return;
            }
            String deviceName = device.getName();
            String deviceHardwareAddress = device.getAddress(); // MAC address

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Do something if connected
                Log.d("BT", "Device connected");

                String registeredId = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("bluetooth_device_id", "none");

                Log.d("BT_Judge", "Registered: " + registeredId);

                if (deviceHardwareAddress.equals(registeredId)) {
                    Log.d("BT_Judge", "登録済み");
                } else Log.d("BT_Judge", "未登録");

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Do something if disconnected
                Log.d("BT", "Device disconnected");

            }
        }
    };

    private void audioStart(){
        //↓通知をする際に起動するバイブレーション
        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(1000);
        //通知のやつ↓
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel("CHANNEL_ID", "通報通知", importance);
        //説明・説明　ここに通知の説明を書くことができる↓
        channel.setDescription("第3者からの通報を検知しました");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        //通知のやつ↑
        Log.d("nt","レスポンスを検知しました2");
        //↓通知の詳細設定的な奴
        NotificationCompat.Builder builder = new NotificationCompat

                .Builder(this, "CHANNEL_ID")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentTitle("通報検知")
                .setContentText("子供の置き去りを検知しました。")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(R.string.app_name, builder.build());

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {


        return null;
    }
}