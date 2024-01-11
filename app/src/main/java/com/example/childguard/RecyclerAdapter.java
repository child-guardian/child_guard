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
        Log.d("RecyclerAdapter", "Constructor");
        // Init
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public RecyclerAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("RecyclerAdapter", "onCreateViewHolder");
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.recycler_row, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Log.d("RecyclerAdapter", "onBindViewHolder");
        holder.textView.setText(deviceList.get(position)[0]);
        holder.textView.setOnClickListener( v -> {
            Toast.makeText(v.getContext(), deviceList.get(position)[1], Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        Log.d("RecyclerAdapter", "getItemCount");
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

