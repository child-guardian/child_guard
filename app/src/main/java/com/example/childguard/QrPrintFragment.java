package com.example.childguard;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.print.PrintHelper;

import android.preference.PreferenceManager;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QrPrintFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QrPrintFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public QrPrintFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment QrPrintFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QrPrintFragment newInstance(String param1, String param2) {
        QrPrintFragment fragment = new QrPrintFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }
    }

   // @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //共有プリファレンス全体の準備
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("app_situation", MODE_PRIVATE);
        //User毎のドメインを保存する
        String IdPref=sharedPreferences.getString("ID",null);
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_qr_print, container, false);;
        //固定のドメイン
        String KoteiURL = "https://practicefirestore1-8808c.web.app/?id=";
        //すべてのドメイン
        String AllURL;
        //IdPrefにの値が初期値の場合
        if(IdPref==null) {
            //User毎のドメイン
            String userURL = getArguments().getString("STR_KEY");
            //キー"ID"の値をuserURLの値にする
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putString("ID", userURL);
            //確定処理
            e.apply();
            //二つのドメインを合成する
            AllURL=KoteiURL+userURL;
        }else{
            //二つのドメインを合成する
            AllURL=KoteiURL+IdPref;
        }

        int size = 2500;
        ImageView imageViewQrCode;
        Bitmap QRGazou;
        try {
            //QRコード生成
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmapqr = barcodeEncoder.encodeBitmap(AllURL, BarcodeFormat.QR_CODE, size, size);
            imageViewQrCode = (ImageView) view.findViewById(R.id.qr_view);
            imageViewQrCode.setTranslationX(1000);
            imageViewQrCode.setTranslationY(1000);
            imageViewQrCode.setImageBitmap(bitmapqr);
        } catch (WriterException e) {
            throw new AndroidRuntimeException("Barcode Error.", e);
        }
        //    画像合成の準備
        //    ここのエラーは直すと何故か動かなくなる。このままで動くので放置
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.a_group_qr_sos);
        Bitmap bitmap1 = ((BitmapDrawable) imageViewQrCode.getDrawable()).getBitmap();
        int width = bitmap.getWidth(); // 元ファイルの幅取得
        int height = bitmap.getHeight(); // 元ファイルの高さ取得
        QRGazou = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //Canvasの準備
        Canvas canvas = new Canvas(QRGazou);
        //画像のサイズの調整
        int disWidth = (width - bitmap1.getWidth()) / 2;
        int disHeight = (int) ((height - bitmap1.getHeight()) / 1.3);
        canvas.drawBitmap(bitmap, 0, 0, (Paint) null); // image, x座標, y座標, Paintインスタンス
        canvas.drawBitmap(bitmap1, disWidth, disHeight, (Paint) null); // 画像合成
        //Androidからプリンターへ印刷指示を出すサポートライブラリ
        PrintHelper printHelper = new PrintHelper(getActivity());
        printHelper.setColorMode(PrintHelper.COLOR_MODE_COLOR);
        printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        printHelper.printBitmap("job_name", QRGazou);
        HomeFragment homeFragment=new HomeFragment();
        replaceFragment(homeFragment);
        return view;
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
}