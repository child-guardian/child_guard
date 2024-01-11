package com.example.childguard;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    ArrayList<String> arrayListDN=new ArrayList<>();
    ArrayList<String> arrayListDA=new ArrayList<>();
    //RecyclerAdapterのコンストラクタ
    public RecyclerAdapter(ArrayList<String[]> arrayList) {
        for (String[] deviceInfo:arrayList) {
            this.arrayListDN.add(deviceInfo[0]);
            Log.d("c",deviceInfo[0]);
        }
        for (String[] deviceInfo:arrayList) {
            this.arrayListDA.add(deviceInfo[1]);
            Log.d("c",deviceInfo[1]);
        }
    }

    //新しいViewHolderを生成すると呼び出される
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {



        //recycler_row.xmlをactivity_main.xmlの部品にする
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_row, parent, false);

        //新しいViewHolderを作成
        //ItemViewHolderクラスを呼び出す
        ItemViewHolder holder = new ItemViewHolder(view);

        //クリックイベントを登録
        holder.itemView.setOnClickListener(v -> {

            int position = holder.getAdapterPosition();
            Toast.makeText(v.getContext(),arrayListDA.get(position),Toast.LENGTH_SHORT).show();


        });

        //生成したViewHolderを戻す
        return holder;
    }

    //1行分のレイアウトの詳細設定
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        //指定された位置の値を取得
        holder.getTextView().setText(arrayListDN.get(position));
    }

    //ArrayListのデータ件数を取得
    @Override
    public int getItemCount() {
        return arrayListDN.size();
    }
}

//RecyclerView.ViewHolderクラスを継承
class ItemViewHolder extends RecyclerView.ViewHolder {
    private final TextView textView;

    //ItemViewHolderのコンストラクタ
    public ItemViewHolder(View view) {
        super(view);
        //ViewHolderのビューにテキストを定義する
        textView = view.findViewById(R.id.textView1);
    }

    //テキストの値を取得
    public TextView getTextView() {
        return textView;
    }
}
