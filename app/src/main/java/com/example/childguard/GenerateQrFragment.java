package com.example.childguard;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.print.PrintHelper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GenerateQrFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GenerateQrFragment extends Fragment {

    public GenerateQrFragment() {
        // Required empty public constructor
    }
    public static GenerateQrFragment newInstance(String key) {
        GenerateQrFragment fragment = new GenerateQrFragment();
        Bundle args = new Bundle();
        args.putString("key", key);
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

        QrUtils qrUtils = new QrUtils();

        assert getArguments() != null;
        Bitmap result = qrUtils.setContext(getContext()).getBitmap(getArguments().getString("key"));

        ImageView imageView = view.findViewById(R.id.result_bitmap_image_view);
        imageView.setImageBitmap(result);

        view.findViewById(R.id.button_print).setOnClickListener( v -> {
            PrintHelper photoPrinter = new PrintHelper(requireContext());
            photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            photoPrinter.printBitmap("placeholder", result, () -> {
                Toast.makeText(getContext(), "印刷完了", Toast.LENGTH_SHORT).show();
            });
        });

        view.findViewById(R.id.button_cancel).setOnClickListener( v -> {
            getParentFragmentManager().popBackStack();
        });
    }
}