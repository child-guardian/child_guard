//package com.example.childguard;
//
//import android.graphics.Color;
//import android.graphics.drawable.Drawable;
//import android.os.Bundle;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//public class QR extends AppCompatActivity {
//    String get_on="乗車状態";
//    String get_off ="降車状態";
//    TextView tv=findViewById(R.id.situation);
//    @Override
//    protected void onCreate(Bundle savedInstanceState){
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.fragment_qr);
//        //多分いらないコード
//        findViewById(R.id.camera).setOnClickListener(
//                v -> {
//                    if(get_on.equals(tv.getText().toString())){
//                        tv.setText(get_off);
//                        findViewById(R.id.situation_bg).setBackgroundColor(Color.parseColor("#dcdcdc"));
//                    }
//                    else {
//                        tv.setText(get_on);
//                        findViewById(R.id.situation_bg).setBackgroundColor(Color.parseColor("#ff4500"));
//                    }
//                }
//        );
//    }
//
//}
