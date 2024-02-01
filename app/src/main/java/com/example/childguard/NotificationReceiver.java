package com.example.childguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {//通知をタップしたときにアプリを起動する処理

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("OPEN_ACTIVITY")) {// 通知がタップされたときの処理
            Intent openIntent = new Intent(context, MainActivity.class);   // MainActivityを起動
            openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(openIntent);
        }
    }
}