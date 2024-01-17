package com.example.childguard;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GenerateQrFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GenerateQrFragment extends Fragment {

    public GenerateQrFragment() {
        // Required empty public constructor
    }
    public static GenerateQrFragment newInstance() {
        GenerateQrFragment fragment = new GenerateQrFragment();
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
        return inflater.inflate(R.layout.fragment_generate_qr, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {



    }
}