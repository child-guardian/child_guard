package com.example.childguard;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.print.PrintHelper;

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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements OnEventListener{
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
        //共有プリファレンス全体の準備
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("app_situation", MODE_PRIVATE);
        //QRコード印刷の処理
        Button bt1 = view.findViewById(R.id.QRprinting);
        bt1.setOnClickListener(v -> {
            //初回起動かを保存する変数
            boolean alreadySaved = sharedPreferences.getBoolean("alreadySaved", false);
            //ボタン変数の宣言
            Button parent = view.findViewById(R.id.QRprinting);
            Button born = view.findViewById(R.id.QRprinting);
            //falseのときにFirebaseへの登録
            if (alreadySaved) {
                Log.d("HomeFragment", "already printed");
                //画面遷移＆ID受け渡し
                Toast.makeText(getActivity(), "再印刷", Toast.LENGTH_SHORT).show();
                QrUtils qrUtils = new QrUtils();
                PrintHelper printHelper = new PrintHelper(requireContext());
                printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                printHelper.printBitmap("QRコード", qrUtils.setContext(getContext()).getBitmap(sharedPreferences.getString("ID", "placeholder")), new PrintHelper.OnPrintFinishCallback() {
                    @Override
                    public void onFinish() {
                        Toast.makeText(getContext(), "印刷完了", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            } else {
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
                                SharedPreferences.Editor e = sharedPreferences.edit();
                                // キー"alreadySaved"の値をtrueにする
                                e.putBoolean("alreadySaved", true);
                                //確定処理
                                e.apply();
                                //画面遷移＆ID受け渡し
                                str_key = "" + documentReference.getId();
                                Toast.makeText(getActivity(), "初回登録", Toast.LENGTH_SHORT).show();
                                QrUtils qrUtils = new QrUtils();
                                PrintHelper printHelper = new PrintHelper(requireContext());
                                printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                                printHelper.printBitmap("QRコード", qrUtils.setContext(getContext()).getBitmap(documentReference.getId()), new PrintHelper.OnPrintFinishCallback() {
                                    @Override
                                    public void onFinish() {
                                        Toast.makeText(getContext(), "印刷完了", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //失敗したら
                                Log.w(TAG, "Error adding document", e);
                            }
                        });
                }
        });
        //bluetooth設定ボタンの処理
        Button bt2 = view.findViewById(R.id.Bluetooth_setup);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new bluetooth_setupFragment());
            }
        });

        //デバック用ボタン
        view.findViewById(R.id.bt_debug).setOnClickListener( v -> {
            Toast.makeText(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext()).getString("bluetooth_device_id", "none"), Toast.LENGTH_SHORT).show();
        });

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d("HomeFragment", "onResume: called");
        updateUiState(getActivity().getSharedPreferences("app_situation", MODE_PRIVATE).getBoolean("car", false));
    }

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

    private void updateUiState(boolean state) {
        Log.d("HomeFragment", "updateUiState: called");
        // Init
        TextView tv;
        FrameLayout fl;
        try {
            tv = requireView().findViewById(R.id.situation);
            fl = getView().findViewById(R.id.situation_bg);
        } catch (NullPointerException e) {
            Log.d("HomeFragment", "updateUiState: view is null");
            return;
        }
        String get_on = "\n乗車状態";
        String get_off = "\n降車状態";
        if (state) {
            //乗車状態にする
            fl.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.frame_style_orange, null));
            tv.setText(get_on);
        } else {
            //降車状態にする
            fl.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.frame_style, null));
            tv.setText(get_off);
        }
    }

    @Override
    public void onEvent(boolean state) {
        Log.d("HomeFragment", "onEvent: called");
        updateUiState(state);
    }
}

