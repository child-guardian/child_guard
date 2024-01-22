package com.example.childguard;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TestService extends Service {
    FirebaseFirestore db;
    DocumentReference mDocRef;

    public static final String TAG = "InspirationQuote";


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        //共有プリファレンス全体の準備
        SharedPreferences sharedPreferences = getSharedPreferences("app_situation", MODE_PRIVATE);
        String IdPref = sharedPreferences.getString("ID", null);//アプリに記録されているIDの取得
        if (IdPref == null) {//FireBaseのIDがアプリに登録されているとき
            Log.d("onResume", "ID not initialized.");
        } else {
            mDocRef = FirebaseFirestore.getInstance().document("status/" + IdPref);//現在の位置を取得
            initNotification(mDocRef);//現在の位置を引数に initNotification()を処理
        }

        Bluetooth_status();
        return flags;


    }

    private void initNotification(DocumentReference mDocRef) {//サイト上で押されたボタンの管理
        // PeriodicTaskManagerのインスタンス化

        // 共有プリファレンス全体の準備
        SharedPreferences sharedPreferences = getSharedPreferences("app_situation", MODE_PRIVATE);
        //車の乗り降りを管理するtrue=乗車、false=降車
        //exists()でdocumentSnapshotの中のファイルの存在の確認
        mDocRef.addSnapshotListener((documentSnapshot, e) -> {

            Log.d("nt", "イベント開始");
            //共有プリファレンス 書き込みの準備
            SharedPreferences.Editor E = sharedPreferences.edit();
            //車の乗り降りを管理するtrue=乗車、false=降車
            if (documentSnapshot.exists()) {//exists()でdocumentSnapshotの中のファイルの存在の確認
                Boolean isInCar = sharedPreferences.getBoolean("isInCarPref", false);//現在の乗降状態を保存する共有プリファレンス
                E.putBoolean("isInCarPref", documentSnapshot.getBoolean("isInCar"));//乗降状態の判定
                E.apply();//確定処理
                Log.d("nt", "レスポンスを検知しました1");
                if (isInCar) {//isReportedがtrue=サイト上で乗車状態のとき
                    if (documentSnapshot.getBoolean("isReported")) {
                        ResetReported();// ResetReported();を処理→FireBaseのisReportedをfalseにする
                        NotificationSetting();//通知に関する設定のメソッド
                        Notification(getApplicationContext());//通知を行うメソッド
                    }
                } else {//isReportedがfalse=サイト上で降車状態のとき
                    ResetReported();//ResetReported();を処理→FireBaseのisReportedをfalseにする
                }
            }

        });
    }

    public void ResetReported() {//FireBaseのisReportedをfalseに初期化するメソッド
        //共有プリファレンス全体の準備
        SharedPreferences sharedPreferences = getSharedPreferences("app_situation", MODE_PRIVATE);
        String IdPref = sharedPreferences.getString("ID", null);//アプリに記録されているIDの取得
        db = FirebaseFirestore.getInstance();//Firebaseとの紐づけ
        DocumentReference isReported = db.collection("status").document(IdPref);//更新するドキュメントとの紐づけ
        Map<String, Boolean> DEFAULT_ITEM = new HashMap<>();//mapの宣言
        DEFAULT_ITEM.put("isReported", false);
        //isReportedをfalseに更新
        isReported.update("isReported", false).addOnSuccessListener(unused -> Log.d(TAG, "DocumentSnapshot successfully updated!")).addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
    }

    public void NotificationSetting() {//通知に関する設定の処理を行うメソッド
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            //通知チャネルの実装
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", "通知", importance);
            channel.setDescription("第三者により置き去りの通報が行われたときに通知します。");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

    }

    public void Notification(Context context) {//実際に通知を行うメソッド
         final String CHANNEL_ID = "my_channel_id";
        // 通知がクリックされたときに送信されるIntent
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction("OPEN_ACTIVITY");
        // PendingIntentの作成
        int requestCode = 100;
        int flags = 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, intent, flags | PendingIntent.FLAG_IMMUTABLE);

        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(2000);//バイブレーション

        @SuppressLint("NotificationTrampoline") NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CHANNEL_ID")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentTitle("子供の置き去りをしていませんか？")//通知のタイトル
                .setContentText("第三者からの通報が行われました。")//通知の本文
                .setContentIntent(pendingIntent)//通知をタップするとActivityへ移動する
                .setAutoCancel(true)//通知をタップすると削除する
                .setPriority(NotificationCompat.PRIORITY_HIGH) // プライオリティを高く設定
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC); // ロック画面に表示する

        // NotificationChannelの作成（Android 8.0以降）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Channel Name",
                        NotificationManager.IMPORTANCE_HIGH
                );

                channel.setDescription("Channel Description");
                channel.enableLights(true);
                channel.setLightColor(Color.RED);
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);
            }
        }


        NotificationManager notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(R.string.app_name, builder.build());//通知の表示
    }
    public void NotificationBluetooth(Context context) {//実際に通知を行うメソッド
        final String CHANNEL_ID = "my_channel_id";
        // 通知がクリックされたときに送信されるIntent
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction("OPEN_ACTIVITY");
        // PendingIntentの作成
        int requestCode = 100;
        int flags = 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, intent, flags | PendingIntent.FLAG_IMMUTABLE);

        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(2000);//バイブレーション

        @SuppressLint("NotificationTrampoline") NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CHANNEL_ID")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentTitle("子供の置き去りをしていませんか？")//通知のタイトル
                .setContentText("Bluetoothと車の切断から5分が経過しました")//通知の本文
                .setContentIntent(pendingIntent)//通知をタップするとActivityへ移動する
                .setAutoCancel(true)//通知をタップすると削除する
                .setPriority(NotificationCompat.PRIORITY_HIGH) // プライオリティを高く設定
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC); // ロック画面に表示する

        // NotificationChannelの作成（Android 8.0以降）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Channel Name",
                        NotificationManager.IMPORTANCE_HIGH
                );

                channel.setDescription("Channel Description");
                channel.enableLights(true);
                channel.setLightColor(Color.RED);
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);
            }
        }


        NotificationManager notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(R.string.app_name, builder.build());//通知の表示
    }






    public void Bluetooth_status() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("BT", "No permission to connect bluetooth devices");
            return;
        } else {
            Log.d("BT", "Permission to connect bluetooth devices granted");
        }
        registerReceiver(receiver, intentFilter);
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {


        //PreferenceManager.getDefaultSharedPreferences("myPreferences",Context.MODE_PRIVATE);

        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences pref=getSharedPreferences("Bluetooth_situation",MODE_PRIVATE);
            SharedPreferences.Editor e=pref.edit();
            String action = intent.getAction(); // may need to chain this to a recognizing function
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Boolean isInCar = pref.getBoolean("isInCarPref", false);


            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d("BT", "No permission to connect bluetooth devices");
                return;
            }
            String deviceHardwareAddress = device.getAddress(); // MAC address

            String registeredId = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("bluetooth_device_id", "none");

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Do something if connected
                //Bluetoothデバイスが接続されたときの処理
                Log.d("BT", "Device connected");



                Log.d("BT_Judge", "Registered: " + registeredId);

                if (deviceHardwareAddress.equals(registeredId)) {
                    //登録済みのデバイスだったときの処理
                    Log.d("BT_Judge", "登録済み");
                    e.putBoolean("connection_status",true);

                } else{
                    //登録していないデバイスだったときの処理
                    Log.d("BT_Judge", "未登録");
                    e.putBoolean("connection_status",false);
                }
                e.apply();

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)&&!isInCar) {//bluetoothが切断されたときに乗車状態のとき

                //Do something if disconnected
                //デバイスが切断されたときの処理
                if (deviceHardwareAddress.equals(registeredId)) {
                    // 5分待機する
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)&&!isInCar) {//その後bluetoothを再接続したり降車状態になったりしていない＝置き去りが発生した可能性大
                                NotificationBluetooth(getApplicationContext());//通知を行うメソッド
                            }}

                    }, 5 *60 *1000); // 5分をミリ秒に変換
                }
            }else {
                Log.d("BT", "Device disconnected");
            }
        }
    };





    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}