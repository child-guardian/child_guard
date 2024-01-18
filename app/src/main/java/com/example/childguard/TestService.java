package com.example.childguard;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class TestService extends Service  {
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
        return flags;
    }

    private void initNotification(DocumentReference mDocRef) {//サイト上で押されたボタンの管理

        // 共有プリファレンス全体の準備
        SharedPreferences sharedPreferences = getSharedPreferences("app_situation",MODE_PRIVATE);
        //車の乗り降りを管理するtrue=乗車、false=降車
        mDocRef.addSnapshotListener( new EventListener<DocumentSnapshot>() {//exists()でdocumentSnapshotの中のファイルの存在の確認
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                Log.d("nt", "イベント開始");
                //共有プリファレンス 書き込みの準備
                SharedPreferences.Editor E = sharedPreferences.edit();
                //車の乗り降りを管理するtrue=乗車、false=降車
                if (documentSnapshot.exists()) {//exists()でdocumentSnapshotの中のファイルの存在の確認
                    Boolean isInCar = sharedPreferences.getBoolean("isInCarPref", false);//現在の乗降状態を保存する共有プリファレンス
                    E.putBoolean("isInCarPref", documentSnapshot.getBoolean("isInCar"));//乗降状態の判定
                    E.apply();//確定処理
                    Log.d("nt", "レスポンスを検知しました1");
                    if (documentSnapshot.getBoolean("isReported")==true && isInCar==true) {//isReportedがtrueかつisInCarがtrueのとき=サイト上で第三者ボタンが押されたときに乗車状態のとき
                        ResetReported();// ResetReported();を処理→FireBaseのisReportedをfalseにする
                        int importance = NotificationManager.IMPORTANCE_DEFAULT;
                        NotificationChannel channel = new NotificationChannel("CHANNEL_ID", "通報通知", importance);
                        channel.setDescription("第3者からの通報を検知しました");
                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
                        notificationManager.createNotificationChannel(channel);
                        Log.d("nt", "レスポンスを検知しました2");
                        notifyMain();//  notifyMain()メソッドを処理→通知のメソッド
                    } else{//それ以外のとき
                        ResetReported();//ResetReported();を処理→FireBaseのisReportedをfalseにする
                        Log.d("nt", "何もなし" );
                    }
                }

            }

        });
    }
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

    public void ResetReported(){//FireBaseのisReportedをfalseに初期化するメソッド
        //共有プリファレンス全体の準備
        SharedPreferences sharedPreferences =getSharedPreferences("app_situation", MODE_PRIVATE);
        String IdPref = sharedPreferences.getString("ID", null);//アプリに記録されているIDの取得
        db = FirebaseFirestore.getInstance();//Firebaseとの紐づけ
        DocumentReference isReported = db.collection("status").document(IdPref);//更新するドキュメントとの紐づけ
        Map<String, Boolean> DEFAULT_ITEM = new HashMap<>();//mapの宣言
        DEFAULT_ITEM.put("isReported", false);
        isReported.update("isReported",false).addOnSuccessListener(new OnSuccessListener<Void>() {//isReportedをfalseに更新
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "DocumentSnapshot successfully updated!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error updating document", e);
            }
        });
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}