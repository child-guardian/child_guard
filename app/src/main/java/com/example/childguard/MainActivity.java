package com.example.childguard;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

// Manifest
import android.Manifest;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore db;


    DocumentReference mDocRef;

    private HomeFragment homeFragment;

    public static final String TAG = "InspirationQuote";


    private final ActivityResultLauncher<ScanOptions> QrLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                String contents = result.getContents();
                if (contents == null) {
                    Toast.makeText(this, "QRコードが読み取れませんでした", Toast.LENGTH_LONG).show();
                } else {
                    if (!contents.contains("https://practicefirestore1-8808c.web.app/")) {
                        Toast.makeText(this, "Child Guardに対応するQRコードではありません", Toast.LENGTH_LONG).show();
                    } else {
                        changeIsInCar();
                    }
                }
            }
    );

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Bluetooth接続判定用
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor e = pref.edit();
        e.putBoolean("connection_status", false);


        if (!hasPermissions()) {
            requestPermissions();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);

        this.homeFragment = HomeFragment.newInstance("test", "test");

        bottomNavigationView.setOnNavigationItemSelectedListener(v ->

        {
            if (v.getItemId() == findViewById(R.id.navigation_home).getId()) {
                findViewById(R.id.fab_scan_qr_code).setVisibility(FrameLayout.VISIBLE);
                getSupportFragmentManager().beginTransaction()
                        .replace(findViewById(R.id.fragmentContainerView).getId(), this.homeFragment)
                        .addToBackStack(null)
                        .commit();
                firebaseLink();

            } else if (v.getItemId() == findViewById(R.id.navigation_settings).getId()) {
                findViewById(R.id.fab_scan_qr_code).setVisibility(FrameLayout.GONE);
                getSupportFragmentManager().beginTransaction()
                        .replace(findViewById(R.id.fragmentContainerView).getId(), SettingFragment.newInstance())
                        .addToBackStack(null)
                        .commit();
            }
            firebaseLink();
            Bluetooth_status();
            return true;
        });

        findViewById(R.id.fab_scan_qr_code).setOnClickListener(v -> {
            Log.d("MainActivity/Fab", "onClick: called");
            //QRリーダ起動
            ScanOptions options = new ScanOptions();
            options.setPrompt("QRコードを読み取ってください");
            QrLauncher.launch(options);

        });

        //Bluetooth検知機能
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        db = FirebaseFirestore.getInstance();//Firebaseとの紐づけ
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("BT", "No permission to connect bluetooth devices");
            return;
        } else {
            Log.d("BT", "Permission to connect bluetooth devices granted");
        }
        registerReceiver(receiver, intentFilter);

        startForegroundService(new Intent(this, TestService.class));

    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "called");
        Log.d("onResume", "mDocRef is null");
        firebaseLink();
    }

    private void initNotification(DocumentReference mDocRef) {//サイト上で押されたボタンの管理
        // 共有プリファレンス全体の準備
        SharedPreferences sharedPreferences = getSharedPreferences("app_situation", MODE_PRIVATE);

        mDocRef.addSnapshotListener(this, (documentSnapshot, e) -> {

            Log.d("nt", "イベント開始");
            //共有プリファレンス 書き込みの準備
            SharedPreferences.Editor E = sharedPreferences.edit();
            //車の乗り降りを管理するtrue=乗車、false=降車
            if (documentSnapshot.exists()) {//exists()でdocumentSnapshotの中のファイルの存在の確認
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
                Boolean isInCar = sharedPreferences.getBoolean("isInCarPref", false);//現在の乗降状態を保存する共有プリファレンス
                E.putBoolean("isInCarPref", documentSnapshot.getBoolean("isInCar"));//乗降状態の判定
                E.apply();//確定処理
                Log.d("nt", "レスポンスを検知しました1");
                //FireBaseで更新された情報の判定
                if (!documentSnapshot.getBoolean("isReported")) {//isReportedがfalseのとき=サイト上で保護者ボタンが押されたとき
                    if (fragment instanceof HomeFragment) {//fragmentがHomeFragmentのインスタンスかの判定
//                        changes-situation();//  changes-situation()メソッドを処理→アプリ側の乗降状態を変化
                        ((HomeFragment) fragment).onEvent(!isInCar);
                    }
                } else if (isInCar) {//第三者ボタンが押されたときにisInCarがtrueのとき＝乗車状態のとき→いたずら防止
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                    NotificationChannel channel = new NotificationChannel("CHANNEL_ID", "通報通知", importance);
                    channel.setDescription("第3者からの通報を検知しました");
                    NotificationManager notificationManager = getSystemService(NotificationManager.class);
                    notificationManager.createNotificationChannel(channel);
                    Log.d("nt", "レスポンスを検知しました2");
                    NotificationSetting();//通知に関する設定のメソッド
                    Notification(getApplicationContext());//通知を行うメソッド
                    ResetReported();// ResetReported();メソッドを処理→FireBaseのisReportedをfalseにする
                } else {//第三者ボタンが押されたときにisInCarがfalseのとき=降車状態のとき
                    ResetReported();// ResetReported();を処理→FireBaseのisReportedをfalseにする
                    Log.d("nt", "何もなし");
                }
            }
        });
    }

    private boolean hasPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.VIBRATE,
                        Manifest.permission.FOREGROUND_SERVICE
                },
                2);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {
            if (!hasPermissions()) {
                Toast.makeText(this, "Permissions not granted.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * FireBaseのIDの取得
     */
    public void firebaseLink() {//Firebaseのドキュメントの取得
        //共有プリファレンス全体の準備
        SharedPreferences sharedPreferences = getSharedPreferences("app_situation", MODE_PRIVATE);
        String IdPref = sharedPreferences.getString("ID", null);//アプリに記録されているIDの取得
        if (IdPref == null) {//FireBaseのIDがアプリに登録されているとき
            Log.d("onResume", "ID not initialized.");
        } else {
            mDocRef = FirebaseFirestore.getInstance().document("status/" + IdPref);//現在の位置を取得
            initNotification(mDocRef);//現在の位置を引数に initNotification()を処理
        }
    }
    /**
     * FireBaseのisReportedをfalseに初期化するメソッド
     */
    public void ResetReported() {
        //共有プリファレンス全体の準備
        SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences("app_situation", MODE_PRIVATE);
        String IdPref = sharedPreferences.getString("ID", null);//アプリに記録されているIDの取得
        db = FirebaseFirestore.getInstance();//Firebaseとの紐づけ
        DocumentReference isReported = db.collection("status").document(IdPref);//更新するドキュメントとの紐づけ
        Map<String, Boolean> DEFAULT_ITEM = new HashMap<>();//mapの宣言
        //isReportedをfalseに更新
        isReported.update("isReported", false).addOnSuccessListener(unused -> Log.d(TAG, "DocumentSnapshot successfully updated!")).addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
    }

    /**
     * 乗車状態の変更
     */
    public void changeIsInCar() {
        //共有プリファレンス全体の準備
        SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences("app_situation", MODE_PRIVATE);
        //共有プリファレンス 書き込みの準備
        SharedPreferences.Editor E = sharedPreferences.edit();
        String IdPref = sharedPreferences.getString("ID", null);//アプリに記録されているIDの取得
        Boolean change = sharedPreferences.getBoolean("change", false);
        db = FirebaseFirestore.getInstance();//Firebaseとの紐づけ
        DocumentReference isReported = db.collection("status").document(IdPref);//更新するドキュメントとの紐づけ
        Map<String, Boolean> DEFAULT_ITEM = new HashMap<>();//mapの宣言
        if (!change) {
            //isInCarをtrueに更新
            E.putBoolean("change", true);
        } else {
            E.putBoolean("change", false);
        }
        isReported.update("isInCar", change).addOnSuccessListener(unused -> Log.d(TAG, "DocumentSnapshot successfully updated!")).addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
        E.apply();
    }

    /**
     * 第三者通知に関する設定のメソッド
     */
    public void NotificationSetting() {//通知に関する設定の処理を行うメソッド
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        //通知チャネルの実装
        NotificationChannel channel = new NotificationChannel("CHANNEL_ID", "通知", importance);
        channel.setDescription("第三者により置き去りの通報が行われたときに通知します。");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * 第三者通知を行うメソッド
     */
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


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(R.string.app_name, builder.build());//通知の表示
    }


    @Override
    public void onStop() {//アプリをバックグラウンドにした時のメソッド
        super.onStop();
//        Intent intent = new Intent(getApplication(), TestService.class);
//        startService(intent);//TestServiceを起動
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


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
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
            SharedPreferences pref = getSharedPreferences("Bluetooth_situation", MODE_PRIVATE);
            SharedPreferences.Editor e = pref.edit();
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
                changeBluetooth(true);
                Log.d("BT", "Device connected");

                Log.d("BT_Judge", "Registered: " + registeredId);

                if (deviceHardwareAddress.equals(registeredId)) {
                    //登録済みのデバイスだったときの処理
                    Log.d("BT_Judge", "登録済み");
                    e.putBoolean("connection_status", true);

                } else {
                    //登録していないデバイスだったときの処理
                    Log.d("BT_Judge", "未登録");
                    e.putBoolean("connection_status", false);
                }
                e.apply();

            }else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action) && !isInCar) {//bluetoothが切断されたときに乗車状態のとき
//
//                //Do something if disconnected
                //デバイスが切断されたときの処理
                changeBluetooth(false);
//                if (deviceHardwareAddress.equals(registeredId)) {
//                    // 5分待機する
//                    Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action) && !isInCar) {//その後bluetoothを再接続したり降車状態になったりしていない＝置き去りが発生した可能性大
//                                NotificationBluetooth(getApplicationContext());//通知を行うメソッド
//                            }
//                        }
//
//                    }, 5 * 1000); // 5分をミリ秒に変換
//                }
//            } else {
//                Log.d("BT", " Device disconnected");
            }
        }
    };

    /**
     * Bluetoothの接続状態を変更するメソッド
     */
    public void changeBluetooth(boolean actual) {
        getSharedPreferences("Bluetooth_situation", MODE_PRIVATE).edit().putBoolean("status", actual).apply();
    }
}

