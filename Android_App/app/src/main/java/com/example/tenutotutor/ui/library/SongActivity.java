package com.example.tenutotutor.ui.library;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tenutotutor.MainActivity;
import com.example.tenutotutor.MidiPlayer;
import com.example.tenutotutor.R;
import com.example.tenutotutor.ui.FirstPageActivity;
import com.google.common.base.Stopwatch;
import com.leff.midi.util.MidiUtil;

import java.util.concurrent.TimeUnit;


public class SongActivity extends AppCompatActivity {
    public static Midi midi;
    public static int norDefault = 120;
    private Animation aniRotate;
    private String starttime = "00:00";
    private String endtime;
    private int startmilli;
    private int endmilli;
    private long start;
    private long end;
    boolean flag = false;
    private SeekBar seekBar;
    private TextView stime;
    private ImageView play;
    private ImageView stop;
    private MidiPlayer midiPlayer;
    private ImageView img;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        img = findViewById(R.id.song_music);
        aniRotate = AnimationUtils.loadAnimation(this, R.anim.rotation);

        midiPlayer = new MidiPlayer(midi.getPath(),norDefault);

        String[] time = starttime.split(":");
        startmilli = Integer.parseInt(time[0])*60 + Integer.parseInt(time[1]);
        endmilli = (int) MidiUtil.ticksToMs(midiPlayer.midi.getLengthInTicks(),midiPlayer.processor.mMPQN,midiPlayer.processor.mPPQ)/1000;
        int minutes = (endmilli % 3600) / 60;
        int seconds = endmilli % 60;
        endtime = String.format("%02d:%02d", minutes, seconds);

        seekBar = findViewById(R.id.song_bar);
        seekBar.setMax(endmilli);
        seekBar.setProgress(startmilli);

        TextView title = findViewById(R.id.song_title);
        title.setText(midi.getSongName());

        stime = findViewById(R.id.song_stime);
        stime.setText(starttime);

        TextView etime = findViewById(R.id.song_etime);
        etime.setText(endtime);

        play = findViewById(R.id.song_play);
        stop = findViewById(R.id.song_stop);
        ImageView ff = findViewById(R.id.song_ff);
        ImageView fr = findViewById(R.id.song_fr);

        Button bu = findViewById(R.id.song_button);

        ImageView norLeft = findViewById(R.id.setvalue_nor_left2);
        ImageView norRight = findViewById(R.id.setvalue_nor_right2);
        TextView norValue = findViewById(R.id.setvalue_nor_value2);
        norValue.setText(String.valueOf(this.norDefault));

        norLeft.setOnClickListener(view -> {
            if(flag){
                Toast.makeText(SongActivity.this, "Can't change BPM during play", Toast.LENGTH_SHORT).show();
            }else{
                if (norDefault > 50) {
                    norDefault -= 10;
                    norValue.setText(String.valueOf(norDefault));
                    midiPlayer = new MidiPlayer(midiPlayer, norDefault);
                } else {
                    Toast.makeText(SongActivity.this, "The minimum BPM is 50.", Toast.LENGTH_SHORT).show();
                }
            }

        });

        norRight.setOnClickListener(view -> {
            if(flag){
                Toast.makeText(SongActivity.this, "Can't change BPM during play", Toast.LENGTH_SHORT).show();
            }else{
                if (norDefault < 200) {
                    norDefault += 10;
                    norValue.setText(String.valueOf(norDefault));
                    midiPlayer = new MidiPlayer(midiPlayer, norDefault);
                } else {
                    Toast.makeText(SongActivity.this, "The maximum BPM is 200.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        play.setOnClickListener(view -> {
            play.setVisibility(View.INVISIBLE);
            img.startAnimation(aniRotate);
            stop.setVisibility(View.VISIBLE);
            midiPlayer.play();
            start = System.nanoTime()/1000000000;
            flag = true;
            update();
        });

        stop.setOnClickListener(view -> {
            stop.setVisibility(View.INVISIBLE);
            img.clearAnimation();
            play.setVisibility(View.VISIBLE);
            midiPlayer.stop();
            flag = false;
        });

        ff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        fr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        bu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SongActivity.this, LearnerActivity.class);
                startActivity(intent);
            }
        });

    }

    public static void setBPM(int n) {
        norDefault = n;
    }
    public static void setMidi(Midi midiInit) {
        midi = midiInit;
        LearnerPlayActivity.setMidi(midiInit);
    }

    private static String num;
    private static String duration;

    public static void setNumberOfRepeat(String n) {
        num = n;
    }

    public static void setDuration(String d) {
        duration = d;
    }

    private void update() {
        if (flag) {
            end = System.nanoTime()/1000000000;
            startmilli = (int) (end-start) + startmilli;
            start = System.nanoTime()/1000000000;
            if (startmilli >= endmilli) {
                starttime = "0:00";
                String[] time = starttime.split(":");
                startmilli = Integer.parseInt(time[0])*60 + Integer.parseInt(time[1]);
                stop.setVisibility(View.INVISIBLE);
                img.clearAnimation();
                play.setVisibility(View.VISIBLE);
                midiPlayer.stop();
                flag = false;
                stime.setText(starttime);
                seekBar.setProgress(startmilli);
            } else {
                int minutes = (startmilli % 3600) / 60;
                int seconds = startmilli % 60;
                starttime = String.format("%02d:%02d", minutes, seconds);
                stime.setText(starttime);
                seekBar.setProgress(startmilli);
                new Handler().postDelayed(() -> {
                    update();
                }, 1000);
            }
        }
    }

    private void startMessage() {

    }

}