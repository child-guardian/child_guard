package com.example.childguard;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class MainActivity extends AppCompatActivity {

    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;

    public static final String TAG = "InspirationQuote";

    boolean flg = false;

    //↓日付を取得するやつ
    public static String getNowDate() {
        @SuppressLint("SimpleDateFormat") final DateFormat df = new SimpleDateFormat("yyy/MM/dd HH:mm:ss");
        final Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        super.onStart();

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);

        bottomNavigationView.setOnNavigationItemSelectedListener(v ->

        {
            if (v.getItemId() == findViewById(R.id.navigation_home).getId()) {
                getSupportFragmentManager().beginTransaction()
                        .replace(findViewById(R.id.fragmentContainerView).getId(), HomeFragment.newInstance("test", "tset"))
                        .commit();
            } else if (v.getItemId() == findViewById(R.id.navigation_QR).getId()) {
                getSupportFragmentManager().beginTransaction()
                        .replace(findViewById(R.id.fragmentContainerView).getId(), QRFragment.newInstance("test", "tset"))
                        .commit();
            } else if (v.getItemId() == findViewById(R.id.navigation_notification).getId()) {
                getSupportFragmentManager().beginTransaction()
                        .replace(findViewById(R.id.fragmentContainerView).getId(), NotificationFragment.newInstance("test", "test"))
                        .commit();
            }

            return true;


        });

        //Bluetooth検知機能
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("BT", "No permission to connect bluetooth devices");
            return;
        }
        else {
            Log.d("BT", "Permission to connect bluetooth devices granted");
        }
        registerReceiver(receiver, intentFilter);

    }

    @Override
    protected void onResume() {
        SharedPreferences sharedPreferences = getSharedPreferences("app_situation", MODE_PRIVATE);
        String IdPref = sharedPreferences.getString("ID", null);
        if (IdPref == null) {
            Log.d("onResume", "ID not initialized.");
            return;
        }
        DocumentReference mDocRef = FirebaseFirestore.getInstance().document("users/" + IdPref);//現在の位置を取得
//        initNotification(mDocRef);

        super.onResume();
    }

    private void initNotification(DocumentReference mDocRef) {

        // Init pref
        SharedPreferences sharedPreferences = getSharedPreferences("app_situation",MODE_PRIVATE);

        mDocRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Log.d("nt", "イベント開始");
                //共有プリファレンス 書き込みの準備
                SharedPreferences.Editor E=sharedPreferences.edit();
                //車の乗り降りを管理するtrue=乗車、false=降車
                boolean zyoukouzyoutai = sharedPreferences.getBoolean("car", false);
                if (flg && documentSnapshot != null && documentSnapshot.exists()) {

                    String parent = documentSnapshot.getString("parent");
                    Log.d("nt", "レスポンスを検知しました1");

                    if (parent.equals("s")) {//FireBaseの更新情報が"S"のとき＝サイト上で第三者ボタンが押されたとき
                        if(zyoukouzyoutai==false) {//いたずら防止
                            //通知のやつ↓

                            int importance = NotificationManager.IMPORTANCE_DEFAULT;

                            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", "通報通知", importance);
                            //説明・説明　ここに通知の説明を書くことができる↓
                            channel.setDescription("第3者からの通報を検知しました");

                            NotificationManager notificationManager = getSystemService(NotificationManager.class);
                            notificationManager.createNotificationChannel(channel);
                            //通知のやつ↑
                            Log.d("nt", "レスポンスを検知しました2");

                            notifyMain();

                        }
                    } else {
                        if(zyoukouzyoutai==true){//乗降状態の判定
                            E.putBoolean("car", false);//降車状態にする
                            E.apply();//確定処理
                        }else{
                            E.putBoolean("car", true);//乗車状態にする
                            E.apply();//確定処理
                        }
                        Log.w(TAG, "Got an exceptiion!", e);
                        //HomeFragmentへ遷移する
                        HomeFragment fragment = new HomeFragment();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("親",zyoukouzyoutai);
                        fragment.setArguments(bundle);
                        getSupportFragmentManager()
                                .beginTransaction()
                                .add(android.R.id.content, fragment)
                                .commit();
                    }

                }
                flg = true;
            }
        });

    }


    //Bluetoothの検知機能
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

    //↓通知のやつ
    public void notifyMain() {
        //↓通知をする際に起動するバイブレーション
        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(1000);
        //↓通知の詳細設定的な奴
        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(this, "CHANNEL_ID")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentTitle("通報検知")
                .setContentText("子供の置き去りを検知しました。")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

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

    //Bluetooth_setupの戻るボタン
    public void setupBackButton(boolean enableBackButton) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(enableBackButton);
    }
}
