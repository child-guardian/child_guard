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

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {

    ArrayList<String[]> deviceList;

    // Constructor
    public RecyclerAdapter(ArrayList<String[]> deviceList) {
        // Init
        Log.d("RecyclerAdapter", "Constructor called");
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public RecyclerAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.recycler_row, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.textView.setText(deviceList.get(position)[0]);
        holder.textView.setOnClickListener( v -> {
            Toast.makeText(v.getContext(), deviceList.get(position)[1], Toast.LENGTH_SHORT).show();

            // アラートダイアログを表示
            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setTitle("登録")
                    .setMessage("このデバイスを登録しますか？")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        // OK button pressed
                        Toast.makeText(v.getContext(), "OK button clicked", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView1);
        }
    }

}

