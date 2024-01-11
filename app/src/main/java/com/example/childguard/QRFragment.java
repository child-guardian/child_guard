package com.example.childguard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QRFragment extends Fragment {
    //QRコードから受け取ったURLの受け渡しの宣言
//   OnDataPass dataPass;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public QRFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment QRFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QRFragment newInstance(String param1, String param2) {
        QRFragment fragment = new QRFragment();
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

    //FragmentからActivityへデータの受け渡しをするためのInterface
//    public interface OnDataPass{
//        void onDataPass(String urlPass);
//    }
    private final ActivityResultLauncher<ScanOptions> fragmentLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                    //QRコードからデータを読み取れたかの確認
                if(result.getContents() == null) {
                    Toast.makeText(getContext(), "Cancelled from fragment", Toast.LENGTH_LONG).show();
                } else {
//                    dataPass.onDataPass(result.getContents());
                    //画面遷移
                    Toast.makeText(getContext(), result.getContents(), Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(getActivity(),UrlPageActivity.class);
                    startActivity(intent);
                }

            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_qr, container, false);

        Button cameraButton = view.findViewById(R.id.camera);

        cameraButton.setOnClickListener(v -> {
            Log.d("QRFragment", "onClick: called");
            //QRリーダ起動
            fragmentLauncher.launch(new ScanOptions());
        });

        return view;
    }
}

