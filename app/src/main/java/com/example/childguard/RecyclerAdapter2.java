package com.example.childguard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

//RecyclerView.Adapterクラスを継承
public class RecyclerAdapter2 extends RecyclerView.Adapter<ItemViewHolder> {

    ArrayList<String> arrayList;

    //RecyclerAdapterのコンストラクタ
    public RecyclerAdapter2(ArrayList<String> arrayList, Context applicationContext) {
        this.arrayList = arrayList;
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
            //クリックされた行を取得
            int position = holder.getAdapterPosition();

            //クリックされた文字をトースト表示
            Toast.makeText(v.getContext(), arrayList.get(position), Toast.LENGTH_SHORT).show();

            //クリックされた行を削除
            arrayList.remove(position);

            //行が削除されたことを画面に通知
            notifyItemRemoved(position);
        });

        //生成したViewHolderを戻す
        return holder;
    }

    //1行分のレイアウトの詳細設定
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        //指定された位置の値を取得
        holder.getTextView().setText(arrayList.get(position));
    }

    //ArrayListのデータ件数を取得
    @Override
    public int getItemCount() {
        return arrayList.size();
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
