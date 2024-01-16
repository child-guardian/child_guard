package com.example.childguard;

import static android.content.ContentValues.TAG;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();//Firebaseとの紐づけ

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String str_key;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //    Log.d("HomeFlagment_cnt", "aaaaa");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        MainActivity activity = (MainActivity) getActivity();
        //共有プリファレンス 全体の準備
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //QRコード印刷の処理
        Button bt1 = view.findViewById(R.id.QRprinting);
        bt1.setOnClickListener(v -> {
            //初回起動かを保存する変数
            boolean alreadySaved = preferences.getBoolean("alreadySaved", false);
            //ボタン変数の宣言
            Button parent = view.findViewById(R.id.QRprinting);
            Button born = view.findViewById(R.id.QRprinting);
            //falseのときにFirebaseへの登録
            if (alreadySaved) {
                Log.d("HomeFragment", "already printed");
               //画面遷移＆ID受け渡し
                Toast.makeText(getActivity(),"再印刷",Toast.LENGTH_SHORT).show();
                QrPrintFragment qrPrintFragment = new QrPrintFragment();
                replaceFragment(qrPrintFragment);
                return;
            } else Log.d("HomeFragment", "not printed yet"); // debug

            String valueParent = parent.getText().toString();//変数に文字列を代入
            String valueBorn = born.getText().toString();//変数に文字列を代入
            Map<String, String> user = new HashMap<>();//mapの宣言

            Log.d("HomeFragment", "onClick is called");

            //mapに入れる
            user.put("parent", valueParent);
            user.put("born", valueBorn);
            //新しいドキュメントにIDを作って追加
            db.collection("users")
                    .add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            //成功したら
                            //documentReference.getId()でID取得
                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            SharedPreferences.Editor e = preferences.edit();
                            // キー"alreadySaved"の値をtrueにする
                            e.putBoolean("alreadySaved", true);
                            //確定処理
                            e.apply();
                            //画面遷移＆ID受け渡し
                            str_key = "" + documentReference.getId();
                            Toast.makeText(getActivity(),"初回登録",Toast.LENGTH_SHORT).show();
                            QrPrintFragment qrPrintFragment = new QrPrintFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("STR_KEY",str_key);
                            //値を書き込む
                            qrPrintFragment.setArguments(bundle);
                            replaceFragment(qrPrintFragment);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //失敗したら
                            Log.w(TAG, "Error adding document", e);
                        }
                    });


        });
        //bluetooth設定ボタンの処理
        Button bt2 = view.findViewById(R.id.Bluetooth_setup);
        bt2.setOnClickListener(v -> replaceFragment(new bluetooth_setupFragment()));
        return view;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.d("HomeFragment", "onResume: called");
//        Cargettingonandoff();
//        TextView situationTextView = getView().findViewById(R.id.situation);
//        FrameLayout situation_bg = getView().findViewById(R.id.situation_bg);
//        updateInCarStatus(situationTextView, situation_bg);
   // }
//    public void updateInCarStatus(TextView situationTextView, FrameLayout situation_bg) {
//        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("default", 0);
//
//        Log.d("HomeFragment", "updateInCarStatus: " + sharedPreferences.getBoolean("inCar", false));
//        if (sharedPreferences.getBoolean("inCar", false)) {
//            situationTextView.setText("\n降車状態");
//            situation_bg.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.frame_style, null));
//        } else {
//            situationTextView.setText("\n乗車状態");
//            situation_bg.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.frame_style_orange, null));
//        }
//
//    }

    //画面遷移メソッド
    private void replaceFragment(Fragment fragment) {
        // フラグメントマネージャーの取得
        FragmentManager manager = getParentFragmentManager(); // アクティビティではgetSupportFragmentManager()?
        // フラグメントトランザクションの開始
        FragmentTransaction transaction = manager.beginTransaction();
        // レイアウトをfragmentに置き換え（追加）
        transaction.replace(R.id.fragmentContainerView, fragment);
        // 置き換えのトランザクションをバックスタックに保存する
        transaction.addToBackStack(null);
        // フラグメントトランザクションをコミット
        transaction.commit();
    }
//    public void Cargettingonandoff() {
//        //共有プリファレンス 全体の準備
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        //車の乗り降りを管理するtrue=乗車、false=降車
//        boolean zyoukouzyoutai = preferences.getBoolean("car", false);
//        SharedPreferences.Editor e = preferences.edit();
//        String get_on = "\n乗車状態";
//        String get_off = "\n降車状態";
//        TextView tv = getView().findViewById(R.id.situation);
//        FrameLayout fl = getView().findViewById(R.id.situation_bg);
//
//        if (zyoukouzyoutai == true) {   //乗降状態の判定
//            //降車状態にする
//            fl.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.frame_style, null));
//            tv.setText(get_off);
//            e.putBoolean("car", false);
//            e.apply();
//        } else {
//            //乗車状態にする
//            fl.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.frame_style_orange, null));
//            tv.setText(get_on);
//            e.putBoolean("car", true);
//            e.apply();
//        }
//
//
//    }
}