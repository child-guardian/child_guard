package com.example.childguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {

    ArrayList<String[]> deviceList;

    Context applicationContext;
    View parentView;


    // Constructor
    public RecyclerAdapter(ArrayList<String[]> deviceList, Context applicationContext, View parentView) {
        // Init
        Log.d("RecyclerAdapter", "Constructor called");
        this.deviceList = deviceList;
        this.applicationContext = applicationContext;
        this.parentView = parentView;

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
        holder.textView.setOnClickListener(v -> {

            // アラートダイアログを表示
            new AlertDialog.Builder(v.getContext())
                    .setTitle("登録")
                    .setMessage("このデバイスを登録しますか？")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        // OK button pressed
//                        Toast.makeText(v.getContext(), "OK button clicked", Toast.LENGTH_SHORT).show();
                        //共有プリファレンスに保存
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.applicationContext);
                        sharedPreferences.edit().putString("bluetooth_device_id", deviceList.get(position)[1]).apply();
                        sharedPreferences.edit().putString("bluetooth_device_name", deviceList.get(position)[0]).apply();
//                        Toast.makeText(v.getContext(), PreferenceManager.getDefaultSharedPreferences(this.applicationContext).getString("bluetooth_device_id", "none"), Toast.LENGTH_SHORT).show();

                        TextView textView = this.parentView.findViewById(R.id.registered_device);
                        textView.setText(PreferenceManager.getDefaultSharedPreferences(this.applicationContext).getString("bluetooth_device_name", "登録されていません"));


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

