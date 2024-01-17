package com.example.childguard;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GeneratedQrFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GeneratedQrFragment extends Fragment {

    public GeneratedQrFragment() {
        // Required empty public constructor
    }
    public static GeneratedQrFragment newInstance() {
        GeneratedQrFragment fragment = new GeneratedQrFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_generated_qr, container, false);
    }
}