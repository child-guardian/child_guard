package com.example.childguard;

import android.Manifest;
import android.app.Notification;
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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SurveillanceService extends Service {

    private final Handler handler = new Handler();
    private Runnable notificationRunnable;

    public static class NotificationContent {
        private final String title;
        private final String description;
        private final String channelId;
        private final int notificationId;

        public NotificationContent(String title, String description, String channelId, int notificationId) {
            this.title = title;
            this.description = description;
            this.channelId = channelId;
            this.notificationId = notificationId;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getChannelId() {
            return channelId;
        }

        public int getNotificationId() {
            return notificationId;
        }
    }

    public static final String TAG = "InspirationQuote";
    private static final String BT_ALERT_CHANNEL_ID = "child_guard_bt_alert";
    private static final String REPORTED_CHANNEL_ID = "child_guard_reported";
    private static final String BACKGROUND_CHANNEL_ID = "child_guard_background";
    private static final int REQUEST_CODE = 100;
    private static final int NOTIFICATION_DELAY = 5 * 60 * 1000; // 5 minutes


    private static final NotificationContent REPORTED_NOTIFICATION =
            new NotificationContent("子供の置き去りをしていませんか？", "第三者からの通報が行われました。", REPORTED_CHANNEL_ID, 2);
    private static final NotificationContent BLUETOOTH_NOTIFICATION =
            new NotificationContent("子供の置き去りをしていませんか？🐈", "Bluetoothと車の切断から5分が経過しました", BT_ALERT_CHANNEL_ID, 3);

    private String userId = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        this.userId = getSharedPreferences("app_situation", MODE_PRIVATE).getString("ID", null);
        if (this.userId == null) {
            Log.d("onResume", "ID not initialized.");
            return flags; // IDが初期化されていない場合は何もしない
        }

        if (isNotificationChannelCreated(BACKGROUND_CHANNEL_ID)) {
            createRunningNotificationChannel();
        }
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, BACKGROUND_CHANNEL_ID)
                .setContentTitle("ChildGuardバックグラウンドサービス")
                .setContentText("接続/通報監視サービスがバックグラウンドで実行されています")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        setSnapshotListener(FirebaseFirestore.getInstance().document("status/" + this.userId));

        if (isNotBluetoothGranted()) return flags;

        registerReceiver(receiver);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (isNotificationChannelCreated(BT_ALERT_CHANNEL_ID)) createAlertNotificationChannel(BT_ALERT_CHANNEL_ID);
        if (isNotificationChannelCreated(REPORTED_CHANNEL_ID)) createAlertNotificationChannel(REPORTED_CHANNEL_ID);
    }

    /**
     * 通知チャネルが作成されているか確認
     * @return 通知チャンネルの有無 true: 作成済み false: 未作成
     */
    private boolean isNotificationChannelCreated(String channelId) {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        return notificationManager.getNotificationChannel(channelId) == null;
    }

    /**
     * 通知チャネルの作成
     */
    private void createAlertNotificationChannel(String channelId) {
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(channelId, "通知", importance);
        channel.setDescription("第三者により置き去りの通報が行われたときに通知します。");
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * バックグラウンドで実行中の通知チャネルを作成
     */
    private void createRunningNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                BACKGROUND_CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_NONE
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    /**
     * 通知が許可がされているかどうかを確認
     * @return 通知の許可の有無 true: 許可されていない false: 許可されている
     */
    private boolean isNotNotificationEnabled() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        if (!notificationManagerCompat.areNotificationsEnabled()) {
            Log.d(TAG, "通知が許可されていません");
            return true;
        } else {
            Log.d(TAG, "通知が許可されています");
            return false;
        }
    }

    /**
     * Bluetoothの権限が許可されているかどうかを確認
     * @return Bluetoothの権限の有無 true: 許可されていない false: 許可されている
     */
    private boolean isNotBluetoothGranted() {
        String btPermission = getBluetoothConnectPermission();
        if (ActivityCompat.checkSelfPermission(this, btPermission) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Bluetoothの権限が許可されていません");
            return true;
        } else {
            Log.d(TAG, "Bluetoothの権限が許可されています");
            return false;
        }
    }

    /**
     * ブロードキャストレシーバーを登録
     * @param receiver ブロードキャストレシーバー
     */
    private void registerReceiver(BroadcastReceiver receiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        registerReceiver(receiver, intentFilter);
    }

    /**
     * Bluetoothの接続権限を取得
     * @return Bluetoothの接続権限
     */
    private String getBluetoothConnectPermission() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ?
                android.Manifest.permission.BLUETOOTH_CONNECT :
                Manifest.permission.BLUETOOTH;
    }

    /**
     * Firestoreのスナップショットリスナーを設定
     * @param mDocRef Firestoreのドキュメントリファレンス
     */
    private void setSnapshotListener(DocumentReference mDocRef) {
        // Initialize the PeriodicTaskManager
        // (Assuming it's done elsewhere as it's not shown in the original code)

        // Prepare SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("app_situation", MODE_PRIVATE);

        // Add a snapshot listener to the document reference
        mDocRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.w("nt", "Listen failed.", e);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                Log.d("nt", "イベント開始");

                // Handle document snapshot
                SharedPreferences.Editor editor = sharedPreferences.edit();
                boolean isInCar = sharedPreferences.getBoolean("isInCarPref", false);
                boolean newIsInCarState = Boolean.TRUE.equals(documentSnapshot.getBoolean("isInCar"));

                editor.putBoolean("isInCarPref", newIsInCarState);
                editor.apply();

                Log.d("nt", "レスポンスを検知しました1");
                Log.d("SurveillanceService", "Bluetooth: "+sharedPreferences.getBoolean("BluetoothStatusLocal", false));

                if (isInCar&&!sharedPreferences.getBoolean("BluetoothStatusLocal", false)) {
                    if (Boolean.TRUE.equals(documentSnapshot.getBoolean("isReported"))) {
                        resetReported();
                        sendNotification(getApplicationContext(), REPORTED_NOTIFICATION);
                    }
                } else {
                    resetReported();
                }
            } else {
                Log.d("nt", "Current data: null");
            }
        });
    }

    /**
     * 通報フラグをリセットする
     */
    private void resetReported() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();//Firebaseとの紐づけ
        DocumentReference isReported = db.collection("status").document(this.userId);
        //isReportedをfalseに更新
        isReported.update("isReported", false).addOnSuccessListener(unused ->
                Log.d(TAG, "DocumentSnapshot successfully updated!")).addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
    }

    /**
     * 通知をタップしたときにアプリを起動するPendingIntentを取得
     *
     * @param context コンテキスト
     * @return PendingIntent
     */
    private PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction("OPEN_ACTIVITY");
        return PendingIntent.getActivity(context, SurveillanceService.REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    /**
     * デバイスをバイブレーションさせる
     */
    private void vibrateDevice() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    /**
     * 通知を送信する
     * @param context コンテキスト
     * @param content NotificationContent 通知内容
     */
    private void sendNotification(Context context, NotificationContent content) {//通知を行うメソッド
        // 権限の保有を確認
        if (isNotNotificationEnabled()) return;

        vibrateDevice();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, content.getChannelId())
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentTitle(content.getTitle())//通知のタイトル
                .setContentText(content.getDescription())//通知の内容
                .setContentIntent(getPendingIntent(context))//通知をタップするとActivityへ移動する
                .setAutoCancel(true)//通知をタップすると削除する
                .setPriority(NotificationCompat.PRIORITY_HIGH) // プライオリティを高く設定
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC); // ロック画面に表示する

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        notificationManager.notify(content.getNotificationId(), builder.build());
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 処理対象か確認 ----------------------------------------
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device == null) {
                Log.d("BT", "No device found");
                return;
            }
            String deviceHardwareAddress = device.getAddress(); // MAC address
            if (deviceHardwareAddress == null) {
                Log.d("BT", "No device address found");
                return;
            }
            String registeredId = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("bluetooth_device_id", null);
            if (registeredId == null) {
                Log.d("BT_Judge", "No registered device");
                return;
            }
            if (!registeredId.equals(deviceHardwareAddress)) {
                Log.d("BT_Judge", "Not registered device");
                return;
            }
            boolean isInCar = getSharedPreferences("app_situation", MODE_PRIVATE).getBoolean("change", false);
//            if (!isInCar) {
//                Log.d("BT_Judge", "Not in car");
//                return;
//            }
            // -----------------------------------------------------

            // debug log
            Log.d("BT", "Bluetooth device found: " + deviceHardwareAddress);
            Log.d("BT", "Registered device: " + registeredId);
            Log.d("BT", "Is in car: " + isInCar);
            SharedPreferences sharedPreferences = SurveillanceService.this.getSharedPreferences("app_situation", MODE_PRIVATE);
            SharedPreferences.Editor E = sharedPreferences.edit();
            String action = intent.getAction(); // may need to chain this to a recognizing function
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                E.putBoolean("BluetoothStatusLocal", false);
                // bluetoothが切断されたときに乗車状態のとき
                if (isInCar) {
                    notificationRunnable = () -> {
                        // 5分経過した時点でも車に乗っていない場合
                        sendNotification(context, BLUETOOTH_NOTIFICATION);
                    };
                }
                handler.postDelayed(notificationRunnable, NOTIFICATION_DELAY);
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                E.putBoolean("BluetoothStatusLocal", true);
                // 再接続された場合、通知をキャンセルする
                if (notificationRunnable != null) {
                    handler.removeCallbacks(notificationRunnable);
                    notificationRunnable = null;
                    Log.d("BT", "Notification canceled due to reconnection");
                }
            }
            E.apply();
            Log.d("SurveillanceService:BT", "Bluetooth status: " + sharedPreferences.getBoolean("BluetoothStatusLocal", false));
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}