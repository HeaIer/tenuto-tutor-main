package com.example.tenutotutor.ui.me;

import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.tenutotutor.MainActivity;
import com.example.tenutotutor.R;
import com.example.tenutotutor.databinding.FragmentMeBinding;
import com.example.tenutotutor.ui.library.Midi;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Random;

public class MeFragment extends Fragment {
    private FragmentMeBinding binding;
    private static ArrayList<Midi> MidiList = new ArrayList<>();

    public static boolean first = false;
    private PopupWindow popupWindow;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMeBinding.inflate(inflater, container, false);
        TextView fire = binding.fire;
        fire.setText("30" + new String(Character.toChars(0x1F525)));
        TextView love1 = binding.love1;
        TextView love2 = binding.love2;
        TextView love3 = binding.love3;
        View root = binding.getRoot();
        StrictMode.ThreadPolicy Policy1 = StrictMode.allowThreadDiskReads();
        StrictMode.ThreadPolicy Policy2 = StrictMode.allowThreadDiskWrites();
        Gson gson = new Gson();
        String midilist = MainActivity.getSp().getString("midilist", "");
        Type type = new TypeToken<ArrayList<Midi>>() {
        }.getType();

            MidiList = gson.fromJson(midilist, type);
            if (MidiList == null) {
                MidiList = new ArrayList<>();
            }
            StrictMode.setThreadPolicy(Policy1);
            StrictMode.setThreadPolicy(Policy2);
            if (MidiList.size() >= 3) {
                ArrayList<Integer> randomInt = new ArrayList<Integer>();
                Random random = new Random();
                int count = 0;
                while (randomInt.size() != 3 & count != 1000) {
                    count += 1;
                    int r = random.nextInt(MidiList.size());
                    if (!randomInt.contains(r)) {
                        randomInt.add(r);
                    }
                }
                if (!MainActivity.meFragSet){
                    MainActivity.randIntMe=randomInt;
                    MainActivity.meFragSet=true;
                }
                int i = MainActivity.randIntMe.get(0);
                love1.setText(MidiList.get(i).getSongName());
                i = MainActivity.randIntMe.get(1);
                love2.setText(MidiList.get(i).getSongName());
                i = MainActivity.randIntMe.get(2);
                love3.setText(MidiList.get(i).getSongName());

            } else {
                love1.setText("");
                love2.setText("");
                love3.setText("");
            }
            MainActivity.meFragSet=true;

        binding.meA1.setOnClickListener(view -> {
            Toast.makeText(getActivity(),"You learnt how to upload a MIDI file!",Toast.LENGTH_SHORT).show();
        });

        binding.meA2.setOnClickListener(view -> {
            Toast.makeText(getActivity(),"You learnt playing piano 10 days in a row!",Toast.LENGTH_SHORT).show();
        });

        binding.meA3.setOnClickListener(view -> {
            Toast.makeText(getContext(),"Achieve me!",Toast.LENGTH_SHORT).show();
        });

        binding.meA4.setOnClickListener(view -> {
            Toast.makeText(getContext(),"Achieve me!",Toast.LENGTH_SHORT).show();
        });

        binding.meA5.setOnClickListener(view -> {
            Toast.makeText(getContext(),"Achieve me!",Toast.LENGTH_SHORT).show();
        });

        binding.meA6.setOnClickListener(view -> {
            Toast.makeText(getContext(),"Achieve me!",Toast.LENGTH_SHORT).show();
        });

        binding.meA7.setOnClickListener(view -> {
            Toast.makeText(getContext(),"Achieve me!",Toast.LENGTH_SHORT).show();
        });

        binding.meA8.setOnClickListener(view -> {
            Toast.makeText(getContext(),"Achieve me!",Toast.LENGTH_SHORT).show();
        });

        binding.meA9.setOnClickListener(view -> {
            Toast.makeText(getContext(),"Achieve me!",Toast.LENGTH_SHORT).show();
        });

        binding.meA10.setOnClickListener(view -> {
            Toast.makeText(getContext(),"Achieve me!",Toast.LENGTH_SHORT).show();
        });

        if (first) {
            first = false;
            View popupView = inflater.inflate(R.layout.popup_achievement, getActivity().findViewById(R.id.ach_popup));

            boolean focusable = true; // lets taps outside the popup also dismiss it
            popupWindow = new PopupWindow(popupView, 600, 1000, focusable);

            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window token
            popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

            View container1 = (View) popupWindow.getContentView().getParent();
            WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams p = (WindowManager.LayoutParams) container1.getLayoutParams();

            p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            p.dimAmount = 0.5f;
            wm.updateViewLayout(container1, p);
        }

        return root;
    }

}
