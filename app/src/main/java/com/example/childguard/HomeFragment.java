package com.example.childguard;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements OnEventListener {

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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d("HomeFragment", "onResume: called");
        this.updateUiState(getIsInCarLocal());
        this.updateBluetoothSituation(isBluetoothConnected());
    }

    private boolean isBluetoothConnected() {
        SharedPreferences pref = requireActivity().getSharedPreferences("Bluetooth_situation", requireActivity().MODE_PRIVATE);
        return pref.getBoolean("status", false);
    }

    private boolean getIsInCarLocal() {
        SharedPreferences pref = requireActivity().getSharedPreferences("app_situation", requireActivity().MODE_PRIVATE);
        return pref.getBoolean("isInCar", false);
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

    private boolean updateUiState(boolean isInCar) {
        Log.d("HomeFragment", "updateUiState: called");
        // Init
        TextView tv;
        FrameLayout fl;
        try {
            tv = requireView().findViewById(R.id.situation);
            fl = requireView().findViewById(R.id.situation_bg);
        } catch (NullPointerException e) {
            Log.d("HomeFragment", "updateUiState: view is null");
            return false;
        } catch (IllegalStateException e) {
            Log.d("HomeFragment", "updateUiState: view is not attached");
            getParentFragmentManager().beginTransaction().replace(R.id.fragmentContainerView, HomeFragment.newInstance("test", "test")).commit();
            updateUiState(isInCar);
            return false;
        }
        String get_on = "\n乗車状態";
        String get_off = "\n降車状態";
        if (!isInCar) {
            //乗車状態にする
            fl.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.frame_style_orange, null));
            tv.setText(get_on);
        } else {
            //降車状態にする
            fl.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.frame_style, null));
            tv.setText(get_off);
        }

        return true;
    }

    /**
     * Bluetoothの接続状態の画面を切り替える
     */
    private boolean updateBluetoothSituation(Boolean BluetoothConnect) {
        FrameLayout frameLayout;
        TextView textView;
        ImageView imageView;
        try {
            frameLayout = requireView().findViewById(R.id.situation_bg_bluetooth);
            textView = requireView().findViewById(R.id.BluetoothSituation);
            imageView = requireView().findViewById(R.id.BluetoothSituationImage);
        } catch (NullPointerException e) {
            Log.d("HomeFragment", "updateUiState: view is null");
            return false;
        } catch (IllegalStateException e) {
            Log.d("HomeFragment", "updateUiState: view is not attached");
            getParentFragmentManager().beginTransaction().replace(R.id.fragmentContainerView, HomeFragment.newInstance("test", "test")).commit();
            updateBluetoothSituation(BluetoothConnect);
            return false;
        }
        final String CONNECT = "接続中";
        final String DISCONNECT = "切断中";
        if (BluetoothConnect) {
            //接続状態にする
            frameLayout.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.frame_style_orange, null));
            textView.setText(CONNECT);
            imageView.setVisibility(View.GONE);
        } else {
            //降車状態にする
            frameLayout.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.frame_style, null));
            textView.setText(DISCONNECT);
            imageView.setVisibility(View.VISIBLE);
        }

        return true;
    }


    @Override
    public boolean onEvent(boolean isInCar) {//乗車状態と降車状態の変更を受け取ってupdateUiState()に渡す
        Log.d("HomeFragment", "onEvent: called");

        return updateUiState(isInCar);
    }
}

