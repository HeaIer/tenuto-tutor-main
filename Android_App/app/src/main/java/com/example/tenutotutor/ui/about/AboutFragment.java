package com.example.tenutotutor.ui.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.tenutotutor.MainActivity;
import com.example.tenutotutor.ResourceActivity;
import com.example.tenutotutor.databinding.FragmentAboutBinding;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;


public class AboutFragment extends Fragment {
    private FragmentAboutBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAboutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView description = binding.aboutDescription;
        TextView tnc = binding.aboutTnc;

        try {
            InputStreamReader inputStreamReader = new InputStreamReader(getActivity().getAssets().open("about.txt"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString;
            StringBuilder stringBuilder = new StringBuilder();

            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append("\n").append(receiveString);
            }

            getActivity().getAssets().open("about.txt").close();
            description.setText(stringBuilder.toString());

        } catch (FileNotFoundException e) {
            System.err.println("ERROR: About file not found!");
        } catch (IOException e) {
            System.err.println("ERROR: Failed to read the About file!");
        }
        binding.musicResources.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), ResourceActivity.class);
            startActivity(intent);
        });

        tnc.setOnClickListener(view -> {
            Intent intent  = new Intent(getActivity(), TncActivity.class);
            startActivity(intent);
        });

        return root;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}