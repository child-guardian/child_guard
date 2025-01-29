package com.example.childguard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link bluetooth_setupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class bluetooth_setupFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public bluetooth_setupFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment bluetooth_setupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static bluetooth_setupFragment newInstance(String param1, String param2) {
        bluetooth_setupFragment fragment = new bluetooth_setupFragment();
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
        View view = inflater.inflate(R.layout.fragment_bluetooth_setup, container, false);

        // init
        BluetoothManager bluetoothManager = requireActivity().getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }


//        if (ActivityCompat.checkSelfPermission(requireActivity().getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return view;
//        }

        // >= Android 12
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(requireActivity().getApplicationContext(), android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                Log.w("Bluetooth", "Permission not granted(Android 12-)");
                // show toast then force close the app
                Toast.makeText(requireActivity().getApplicationContext(), "Bluetoothの権限が必須です!", Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            } else {
                Log.w("Bluetooth", "Permission granted(Android 12-)");
            }
        } else {
            if (ActivityCompat.checkSelfPermission(requireActivity().getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.w("Bluetooth", "Permission not granted(Android 12+)");
                // show toast then force close the app
                Toast.makeText(requireActivity().getApplicationContext(), "Bluetoothの権限が必須です!", Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            } else {
                Log.w("Bluetooth", "Permission granted(Android 12+)");
            }
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        RecyclerView recyclerView1 = view.findViewById(R.id.recyclerView1);
        //RecyclerViewのサイズを固定
        recyclerView1.setHasFixedSize(true);

        //RecyclerViewに区切り線を入れる
//        RecyclerView.ItemDecoration itemDecoration =
//                new DividerItemDecoration(getContext() ,DividerItemDecoration.VERTICAL);
//        recyclerView.addItemDecoration(itemDecoration);

        //レイアウトマネージャを設
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView1.setLayoutManager(layoutManager);
        //recyclerView2.setLayoutManager(layoutManager);

        //①リスト構造(String型の可変長の配列)を宣言
        ArrayList<String[]> arrayList = new ArrayList<>();

        if (pairedDevices.size() > 0) {

            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {


                String[] deviceInfo = new String[2];
                deviceInfo[0] = device.getName();
                deviceInfo[1] = device.getAddress(); // MAC address

                Log.d("a", deviceInfo[0]);
                arrayList.add(deviceInfo);
            }
            for (String[] s : arrayList) {
                Log.d("b", s[0]);
            }
            Log.d(" ", String.valueOf(arrayList.size()));
            RecyclerAdapter adapter = new RecyclerAdapter(arrayList, requireActivity().getApplicationContext(), view);


            //④RecyclerViewとAdapterの結び付け
            recyclerView1.setAdapter(adapter);
            TextView textView = view.findViewById(R.id.registered_device);
            textView.setText(PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext()).getString("bluetooth_device_name", "none"));


        }

        return view;

    }


}