package com.example.tenutotutor.ui.library;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tenutotutor.MainActivity;
import com.example.tenutotutor.R;
import com.example.tenutotutor.ResourceActivity;
import com.example.tenutotutor.databinding.FragmentLibraryBinding;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

//import javax.sound.midi.InvalidMidiDataException;
//import javax.sound.midi.MidiSystem;

public class LibraryFragment extends Fragment implements ListAdapter.onCLickListener, ListAdapter.onCreateContextMenuListener {
    private FragmentLibraryBinding binding;
    private static ArrayList<Midi> MidiList = new ArrayList<>();
    private boolean first = true;
    private ListAdapter adapter;
    private static int pos;
    private PopupWindow popupWindow;
    private LinearLayoutManager llm;
    private RecyclerView view;
    private List<String> list = new ArrayList<String>();
    private TextView textview;
    private Spinner spinnertext;
    private ArrayAdapter<String> adapterSpinner;

    //mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
//AssetFileDescriptor descriptor = getActivity().getAssets().openFd("bach_846.mid");
//descriptor.close();

    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        llm = new LinearLayoutManager(getActivity().getApplicationContext());
        view = binding.view;
        TextView textView = binding.libraryRetrieving;


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

        if (MidiList == null) {
            MidiList = new ArrayList<>();
        }

        if (first) {
            first = false;
            adapter = new ListAdapter(MidiList, this, this);
            view.setLayoutManager(llm);
            view.setAdapter(adapter);
            registerForContextMenu(view);
        }
        textView.setVisibility(View.INVISIBLE);
        view.setVisibility(View.VISIBLE);

        binding.libraryAdd.setOnClickListener(view1 -> {
            if (MainActivity.ticked) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/midi");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(Intent.createChooser(intent, "请选择文件"), 1);

            } else {
                View popupView = inflater.inflate(R.layout.popup_upload, getActivity().findViewById(R.id.add_popup));

                boolean focusable = true; // lets taps outside the popup also dismiss it
                popupWindow = new PopupWindow(popupView, 600, 1000, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window token
                popupWindow.showAtLocation(view1, Gravity.CENTER, 0, 0);

                View container1 = (View) popupWindow.getContentView().getParent();
                WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
                WindowManager.LayoutParams p = (WindowManager.LayoutParams) container1.getLayoutParams();

                p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                p.dimAmount = 0.5f;
                wm.updateViewLayout(container1, p);

                Button upload = popupWindow.getContentView().findViewById(R.id.popup_upload);
                upload.setOnClickListener(view11 -> {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("audio/midi");
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(Intent.createChooser(intent, "请选择文件"), 1);
                });
            }
        });
        list.add("Ascending By Name");
        list.add("Decending By Name");
        list.add("Recently Played");
        list.add("Most Played");
        //textview = (TextView) findViewByld(R.id.textViewl);
        spinnertext = binding.sorting;
        adapterSpinner = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, list);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnertext.setAdapter(adapterSpinner);
        spinnertext.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View argl, int arg2, long arg3) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                String way = adapterSpinner.getItem(arg2);
                if (way.equals("Decending By Name")){
                    Collections.sort(MidiList,Comparator.comparing(Midi::getSongName));
                    Collections.reverse(MidiList);
                }
                if(way.equals("Ascending By Name")){Collections.sort(MidiList,Comparator.comparing(Midi::getSongName));}
                if(way.equals("Recently Played")){Collections.shuffle(MidiList);}
                if(way.equals("Most Played")){Collections.shuffle(MidiList);}

                adapter = new ListAdapter(MidiList, LibraryFragment.this, LibraryFragment.this);
                view.setLayoutManager(llm);
                view.setAdapter(adapter);
                registerForContextMenu(view);


            }

            public void onNothingSelected(AdapterView<?> argO) {
                argO.setVisibility(View.VISIBLE);

            }
        });


        return root;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            //Get the Uri of the selected file
            Uri uri = data.getData();
            if (null != uri) {
                String path = ContentUriUtil.getPath(getActivity(), uri);
                if (!(path.substring(path.length()-3).equals("mid"))){
                    Toast.makeText(getActivity(), "Not a Midi File", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (Midi i :MidiList){
                    if (path.equals(i.getPath())){
                        Toast.makeText(getActivity(), "File Already Exist", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int duration = mediaPlayer.getDuration() / 1000;
                int minutes = (duration % 3600) / 60;
                int seconds = duration % 60;
                String timeString = String.format("%02d:%02d", minutes, seconds);
                Toast.makeText(getActivity(), "Successfully uploaded!", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
                Midi midi = new Midi(getFileName(path), timeString, 0, path);
                StrictMode.ThreadPolicy Policy1 = StrictMode.allowThreadDiskReads();
                StrictMode.ThreadPolicy Policy2 = StrictMode.allowThreadDiskWrites();
                SharedPreferences.Editor prefsEditor = MainActivity.getSp().edit();
                MidiList.add(midi);
                Gson gson = new Gson();
                String json = gson.toJson(MidiList);
                prefsEditor.putString("midilist", json);
                prefsEditor.commit();
                StrictMode.setThreadPolicy(Policy1);
                StrictMode.setThreadPolicy(Policy2);
                adapter.notifyItemChanged(MidiList.size() - 1);
                Log.i("filepath", " = " + path);
            } else {
                Toast.makeText(getActivity(), "Could not upload MIDI file. Please ensure that you have allow the permission to access the external storage.", Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void onCLick(int position) {
        SongActivity.setMidi(MidiList.get(position));
        Intent intent = new Intent(getActivity(), SongActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.clear();
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.selection_delete, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            Toast.makeText(getActivity().getApplicationContext(), "Successfully deleted!", Toast.LENGTH_LONG).show();
            StrictMode.ThreadPolicy Policy1 = StrictMode.allowThreadDiskReads();
            StrictMode.ThreadPolicy Policy2 = StrictMode.allowThreadDiskWrites();
            SharedPreferences.Editor prefsEditor = MainActivity.getSp().edit();
            MidiList.remove(pos);
            Gson gson = new Gson();
            String json = gson.toJson(MidiList);
            prefsEditor.putString("midilist", json);
            prefsEditor.commit();
            StrictMode.setThreadPolicy(Policy1);
            StrictMode.setThreadPolicy(Policy2);
            adapter.notifyItemRemoved(pos);
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static void setPos(int position) {
        pos = position;
    }

    public String getFileName(String pathandname) {

        int start = pathandname.lastIndexOf("/");
        int end = pathandname.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return pathandname.substring(start + 1, end);
        } else {
            return null;
        }

    }
    public static ArrayList<Midi> shuffle(ArrayList<Midi> arr) {
        Midi arrTemp[] = new Midi[arr.size()];
        int size = arr.size()-1;
        Random rd = new Random();
        for(int i = 0 ; i < arr.size() ; i++) {
            int rand = rd.nextInt(size+1);
            arrTemp[i] = arr.get(i);
            arr.set(i,arr.get(size));
            size--;
        }
        ArrayList<Midi> result = new ArrayList<>();
        for (Midi i :arrTemp){
            result.add(i);
        }
        return result;

    }


}
