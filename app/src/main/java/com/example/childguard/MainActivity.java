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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import android.Manifest;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore db;
    DocumentReference mDocRef;

    private HomeFragment homeFragment = HomeFragment.newInstance("test", "test");

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
        Log.d(TAG, "onCreate: called");
        //Bluetooth接続判定用
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor e = pref.edit();
        e.putBoolean("connection_status", false);

        // Check permissions
        if (!hasPermissions()) {
            requestPermissions();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);

        this.homeFragment = HomeFragment.newInstance("test", "test");
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(findViewById(R.id.fragmentContainerView).getId(), this.homeFragment)
                    .addToBackStack(null)
                    .commit();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(v -> {
            if (v.getItemId() == findViewById(R.id.navigation_home).getId()) {
                Log.d("MainActivity", "navigation_home: called");
                findViewById(R.id.fab_scan_qr_code).setVisibility(FrameLayout.VISIBLE);
                getSupportFragmentManager().beginTransaction()
                        .replace(findViewById(R.id.fragmentContainerView).getId(), this.homeFragment)
                        .addToBackStack(null)
                        .commit();
            } else if (v.getItemId() == findViewById(R.id.navigation_settings).getId()) {
                Log.d("MainActivity", "navigation_settings: called");
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

        String btPermission = getBluetoothConnectPermission();
        if (ActivityCompat.checkSelfPermission(this, btPermission) != PackageManager.PERMISSION_GRANTED) {
            Log.d("BT", "No permission to connect bluetooth devices");
            return;
        } else {
            Log.d("BT", "Permission to connect bluetooth devices granted");
        }
        registerReceiver(receiver, intentFilter);

        //startForegroundService(new Intent(this, SurveillanceService.class));
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity onResume", "called");
        firebaseLink();
    }

    // 権限チェック用ヘルパーメソッドを追加
    private String getBluetoothConnectPermission() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ?
                Manifest.permission.BLUETOOTH_CONNECT :
                Manifest.permission.BLUETOOTH;
    }

    /**
     * 乗車状態の変更
     */
    public void changeIsInCar() {
        SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences("app_situation", MODE_PRIVATE);
        SharedPreferences.Editor E = sharedPreferences.edit();
        SharedPreferences.Editor E2 = sharedPreferences.edit();
        String IdPref = sharedPreferences.getString("ID", null);//アプリに記録されているIDの取得
        Log.d("MainActivity", "changeIsInCar: ID= " + IdPref);
        boolean change = sharedPreferences.getBoolean("change", false);
        Log.d("MainActivity", "changeIsInCar: " + change);
        db = FirebaseFirestore.getInstance();//Firebaseとの紐づけ
        DocumentReference isReported = db.collection("status").document(IdPref);//更新するドキュメントとの紐づけ
        Map<String, Boolean> DEFAULT_ITEM = new HashMap<>();//mapの宣言
        if (!change) {
            //isInCarをtrueに更新
            Log.d("MainActivity", "change:" + change);
            E.putBoolean("change", true);
            E.apply();
            Log.d("MainActivity", "change:" + sharedPreferences.getBoolean("change", false));
        } else {
            Log.d("MainActivity", "change:" + change);
            E.putBoolean("change", false);
            E.apply();
            Log.d("MainActivity", "change:" + sharedPreferences.getBoolean("change", false));
        }
        Log.d("MainActivity", "changeIsInCar: " + sharedPreferences.getBoolean("change", false));
        isReported.update("isInCar", sharedPreferences.getBoolean("change", false))
                .addOnSuccessListener(unused -> Log.d(TAG, "DocumentSnapshot successfully updated!!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
        E2.putBoolean("isInCarPref", sharedPreferences.getBoolean("change", false));
        E2.apply();
    }

    /**
     * 権限の保有を確認する
     * @return
     */
    private boolean hasPermissions() {
        boolean allGranted = true;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            // <= Android 11 (API 30)
            allGranted &= (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                    == PackageManager.PERMISSION_GRANTED);
            allGranted &= (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                    == PackageManager.PERMISSION_GRANTED);
        } else {
            // >= Android 12 (API 31)
            allGranted &= (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    == PackageManager.PERMISSION_GRANTED);
            allGranted &= (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED);
        }

        // Location
        allGranted &= (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
        allGranted &= (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);

        // >= Android 13 (API 32)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            allGranted &= (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED);
        }

        // Others
        allGranted &= (ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE)
                == PackageManager.PERMISSION_GRANTED);
        allGranted &= (ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
                == PackageManager.PERMISSION_GRANTED);

        return allGranted;
    }

    /**
     * 権限のリクエストを行う
     */
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            // <= Android 11 (API 30)
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.VIBRATE,
                            Manifest.permission.FOREGROUND_SERVICE,
                    },
                    2
            );
        } else {
            // >= Android 12 (API 31)
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.POST_NOTIFICATIONS,
                            Manifest.permission.VIBRATE,
                            Manifest.permission.FOREGROUND_SERVICE
                    },
                    2
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
        Log.d("MainActivity", "firebaseLink: called");
        SharedPreferences sharedPreferences = getSharedPreferences("app_situation", MODE_PRIVATE);
        String IdPref = sharedPreferences.getString("ID", null);//アプリに記録されているIDの取得
        if (IdPref == null) {
            Log.d("onResume", "ID not initialized.");
        } else {
            mDocRef = FirebaseFirestore.getInstance().document("status/" + IdPref);//現在の位置を取得
        }
    }

    public void Bluetooth_status() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        String btPermission = getBluetoothConnectPermission();
        if (ActivityCompat.checkSelfPermission(this, btPermission) != PackageManager.PERMISSION_GRANTED) {
            Log.d("BT", "No permission to connect bluetooth devices");
            return;
        } else {
            Log.d("BT", "Permission to connect bluetooth devices granted");
        }
        registerReceiver(receiver, intentFilter);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences pref = getSharedPreferences("Bluetooth_situation", MODE_PRIVATE);
            SharedPreferences.Editor e = pref.edit();
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            boolean isInCar = pref.getBoolean("isInCarPref", false);
            String btPermission = getBluetoothConnectPermission();
            if (ActivityCompat.checkSelfPermission(context, btPermission) != PackageManager.PERMISSION_GRANTED) {
                Log.d("BT", "No permission to connect bluetooth devices");
                return;
            }
            String deviceHardwareAddress = device.getAddress(); // MAC address

            String registeredId = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getString("bluetooth_device_id", "none");

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Bluetoothデバイスが接続されたときの処理
                Log.d("BT", "Device connected");

                Log.d("BT_Judge", "Registered: " + registeredId);

                if (deviceHardwareAddress.equals(registeredId)) {
                    //登録済みのデバイスだったときの処理
                    Log.d("BT_Judge", "登録済み");
                    changeBluetooth(true);
                    e.putBoolean("connection_status", true);
                    if (homeFragment != null && homeFragment.isVisible()) {
                        homeFragment.updateBluetoothSituation(true);
                    }
                } else {
                    //登録していないデバイスだったときの処理
                    Log.d("BT_Judge", "未登録");
                    e.putBoolean("connection_status", false);
                }
                e.apply();

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //bluetoothデバイスが切断されたときの処理
                changeBluetooth(false);
                if (homeFragment != null && homeFragment.isVisible()) {
                    homeFragment.updateBluetoothSituation(false);
                }
            }
        }
    };

    /**
     * Bluetoothの接続状態を変更するメソッド
     */
    public void changeBluetooth(boolean actual) {
        getSharedPreferences("Bluetooth_situation", MODE_PRIVATE)
                .edit()
                .putBoolean("status", actual)
                .apply();
    }
}
