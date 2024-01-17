package com.example.childguard;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class TestService extends Service  {

    public int onStartCommand(Intent intent, int flags, int startId) {


        audioStart();

        return START_NOT_STICKY;

    }

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