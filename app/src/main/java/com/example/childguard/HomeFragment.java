package com.example.childguard;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.print.PrintHelper;

import android.preference.PreferenceManager;
import android.text.PrecomputedText;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_home,container,false);
        MainActivity activity = (MainActivity) getActivity();
        //QRコード印刷の処理
        Button bt1=view.findViewById(R.id.QRprinting);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new QrPrintFragment());

           }
        });
        //bluetooth設定ボタンの処理
        Button bt2=view.findViewById(R.id.Bluetooth_setup);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new bluetooth_setupFragment());
            }
        });

        //デバック用ボタン
        view.findViewById(R.id.bt_debug).setOnClickListener( v -> {
            Toast.makeText(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext()).getString("bluetooth_device1", "none"), Toast.LENGTH_SHORT).show();
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("HomeFragment", "onResume: called");
        TextView situationTextView = getView().findViewById(R.id.situation);
        FrameLayout situation_bg=getView().findViewById(R.id.situation_bg);
        updateInCarStatus(situationTextView,situation_bg);
    }

    public void updateInCarStatus(TextView situationTextView,FrameLayout situation_bg) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("default", 0);

        Log.d("HomeFragment", "updateInCarStatus: " + sharedPreferences.getBoolean("inCar", false));
        if (sharedPreferences.getBoolean("inCar", false)) {
            situationTextView.setText("\n降車状態");
            situation_bg.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.frame_style, null));
        } else {
            situationTextView.setText("\n乗車状態");
            situation_bg.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.frame_style_orange, null));
        }

    }
    //画面遷移メソッド
    private void replaceFragment(Fragment fragment){
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
}