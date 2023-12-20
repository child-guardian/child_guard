package com.example.childguard;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("HomeFragment", "onResume: called");
        TextView situationTextView = getView().findViewById(R.id.situation);
        FrameLayout situation_bg=getView().findViewById(R.id.situation_bg);
        updateInCarStatus(situationTextView,situation_bg);
    }

    public void updateInCarStatus(TextView situationTextView,FrameLayout situation_bg) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("default", 0);

        Log.d("HomeFragment", "updateInCarStatus: " + sharedPreferences.getBoolean("inCar", false));
        if (sharedPreferences.getBoolean("inCar", false)) {
            situationTextView.setText("\n降車状態");
            situation_bg.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.frame_style, null));
        } else {
            situationTextView.setText("\n乗車状態");
            situation_bg.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.frame_style_orange, null));
        }
    }
}