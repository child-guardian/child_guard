package com.example.childguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AndroidRuntimeException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QrUtils {

    private Context context;

    public QrUtils setContext(Context context) {
        this.context = context;
        return this;
    }

    public Bitmap getBitmap(String key) {
        Log.d("getBitmap", "getBitmap: " + key);
        String fixationURL = "https://practicefirestore1-8808c.web.app/?id=";
        //すべてのドメイン
        String AllURL;
        //IdPrefにの値が初期値の場合
        AllURL = fixationURL + key;


        int qrCodeSize = calculateQRCodeSize(); // 画面密度に応じてサイズを計算
        Bitmap QRImage;
        Bitmap bitmapQR;
        try {
            //QRコード生成
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmapQR = barcodeEncoder.encodeBitmap(AllURL, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize);
        } catch (WriterException e) {
            throw new AndroidRuntimeException("Barcode Error.", e);
        }
        // 画像合成の準備
        Bitmap bitmap = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.a_group_qr_sos_2);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        QRImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Canvasの準備
        Canvas canvas = new Canvas(QRImage);

        // 画像のサイズの調整
        int disWidth = (width - bitmapQR.getWidth()) / 2;
        int disHeight = (int) ((height - bitmapQR.getHeight()) / 1.5);
        canvas.drawBitmap(bitmap, 0, 0, (Paint) null);
        canvas.drawBitmap(bitmapQR, disWidth, disHeight, (Paint) null); // 画像合成
        //Androidからプリンターへ印刷指示を出すサポートライブラリ
        return QRImage;
    }

    private int calculateQRCodeSize() {
        // 画面解像度を取得
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        // 画面密度に基づいてQRコードのサイズを計算
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (700 * density);
    }

}
