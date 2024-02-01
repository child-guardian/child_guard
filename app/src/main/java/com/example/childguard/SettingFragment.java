package com.example.childguard;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {

    FirebaseFirestore db;

    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();//Firebaseとの紐づけ

        view.findViewById(R.id.button_bluetooth_setting).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction().replace(R.id.fragmentContainerView, bluetooth_setupFragment.newInstance("test", "test")).addToBackStack(null).commit();
        });

        view.findViewById(R.id.button_print_qr).setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("app_situation", MODE_PRIVATE);
            boolean alreadySaved = sharedPreferences.getBoolean("alreadySaved", false);
            //falseのときにFirebaseへの登録
            if (alreadySaved) {
                Log.d("HomeFragment", "already printed");
                //画面遷移＆ID受け渡し
                Toast.makeText(getActivity(), "再印刷", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragmentContainerView, GenerateQrFragment.newInstance(sharedPreferences.getString("ID", "none"))).commit();
            } else {
                Map<String, Boolean> DEFAULT_ITEM = new HashMap<>();//mapの宣言

                Log.d("HomeFragment", "onClick is called");

                //mapに入れる
                DEFAULT_ITEM.put("isInCar", false);
                DEFAULT_ITEM.put("isReported", false);
                //新しいドキュメントにIDを作って追加
                db.collection("status")
                        .add(DEFAULT_ITEM)
                        .addOnSuccessListener(documentReference -> {
                            //成功したら
                            //documentReference.getId()でID取得
                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            SharedPreferences.Editor e = sharedPreferences.edit();
                            // キー"alreadySaved"の値をtrueにする
                            e.putBoolean("alreadySaved", true);

                            //確定処理
                            e.apply();
                            //画面遷移＆ID受け渡し
                            SharedPreferences sharedPreferences1 = getActivity().getSharedPreferences("app_situation", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences1.edit();
                            editor.putString("ID", documentReference.getId());
                            editor.apply();

                            Toast.makeText(getActivity(), "初回登録", Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragmentContainerView, GenerateQrFragment.newInstance(documentReference.getId())).commit();
                        })
                        .addOnFailureListener(e -> {
                            //失敗したら
                            Log.w(TAG, "Error adding document", e);
                        });
            }
        });
    }
}