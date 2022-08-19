package com.example.tenutotutor.ui.library;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tenutotutor.MainActivity;
import com.example.tenutotutor.MidiPlayer;
import com.example.tenutotutor.R;
import com.example.tenutotutor.ui.FirstPageActivity;
import com.leff.midi.util.MidiUtil;

public class LearnerPlayActivity extends AppCompatActivity {
    public static Midi midi;
    private static int num;
    private static int BPM;
    private static int duration;
    private Animation aniRotate;
    private static int num_new;
    private static int part;
    private static int part_new = 1;
    private static String starttime = "00:00";
    private static String endtime;
    private static int startmilli;
    private static int endmilli;
    private static TextView bpm;
    private static SeekBar seekBar;
    private static TextView stime;
    private static ImageView play;
    private static ImageView stop;
    private static MidiPlayer midiPlayer;
    private static ImageView img;
    private static TextView nor;
    private static TextView dop;
    private static TextView yourturn;
    private static String repeat;
    private static LayoutInflater inflater;
    private PopupWindow popupWindow;
    private View view;
    private static boolean stopped = false;
    private static Button exit;

    @SuppressLint("ResourceType")

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learner_play);

        aniRotate = AnimationUtils.loadAnimation(this, R.anim.rotation);
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        exit = findViewById(R.id.lp_button);
        img = findViewById(R.id.lp_music);
        play = findViewById(R.id.lp_play);
        stop = findViewById(R.id.lp_stop);
        ImageView ff = findViewById(R.id.lp_ff);
        ImageView fr = findViewById(R.id.lp_fr);
        TextView title = findViewById(R.id.lp_title);
        nor = findViewById(R.id.lp_nor);
        dop = findViewById(R.id.lp_dop);
        bpm = findViewById(R.id.lp_bpm);
        seekBar = findViewById(R.id.lp_bar);
        yourturn = findViewById(R.id.lp_yourturn);
        title.setText(midi.getSongName());
        midiPlayer = new MidiPlayer(midi.getPath(), duration, num, this,BPM);
        starttime = "00:00";
        String[] time = starttime.split(":");
        startmilli = Integer.parseInt(time[0])*60 + Integer.parseInt(time[1]);
        endmilli = (int) MidiUtil.ticksToMs(midiPlayer.midi.getLengthInTicks(),midiPlayer.processor.mMPQN,midiPlayer.processor.mPPQ)/1000;
        int minutes = (endmilli % 3600) / 60;
        int seconds = endmilli % 60;
        endtime = String.format("%02d:%02d", minutes, seconds);
        seekBar.setMax(endmilli);
        seekBar.setProgress(startmilli);
        stime = findViewById(R.id.lp_stime);
        stime.setText(starttime);

        TextView etime = findViewById(R.id.lp_etime);
        etime.setText(endtime);

        part = (int) midiPlayer.getNumberOfParts();
        repeat = starttime;
        part_new = 1;
        stopped = false;

        play.setOnClickListener(view -> {
            play.setVisibility(View.INVISIBLE);
            img.startAnimation(aniRotate);
            stop.setVisibility(View.VISIBLE);
            if (!stopped) {
                midiPlayer.educate(false);
            } else {
                midiPlayer.resumeEducate();
            }
            stopped = false;
            update();
        });

        stop.setOnClickListener(view -> {
            stop.setVisibility(View.INVISIBLE);
            img.clearAnimation();
            play.setVisibility(View.VISIBLE);
            midiPlayer.stop();
            stopped = true;
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

        exit.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.putExtra("finish", true);
            setResult(RESULT_OK, intent);
            midiPlayer.stop();
            midiPlayer.turnOffKeys();
            finish();
        });

        nor.setText("Repeats: " + num);

        bpm.setText("BPM: " + BPM);
        dop.setText("Parts: 1/" + part);

    }

    public static void setNumberOfRepeat(int n) {
        num = n;
        num_new = n;
    }
    public static void setBPM(int n) {
        BPM = n;
    }
    public static void setDuration(int d) {
        duration = d;
    }

    public static void setMidi(Midi midiInit)
    {
        LearnerPlayActivity.midi = midiInit;
    }

    public static void update() {
        double a = ((double)part_new/part);
        if (startmilli <= (a*endmilli) && !stopped) {
            int minutes = (startmilli % 3600) / 60;
            int seconds = startmilli % 60;
            starttime = String.format("%02d:%02d", minutes, seconds);
            stime.setText(starttime);
            seekBar.setProgress(startmilli);
            if (startmilli < a*endmilli) {
                startmilli += 1;
                new Handler().postDelayed(() -> {
                    if (!stopped) {
                        update();
                    }
                }, 1000);
            }
        }
    }

    public void nextCycle() {
        if (num_new == 1) {
            if (part_new == part) {
                img.clearAnimation();
                midiPlayer.stop();

                View popupView = inflater.inflate(R.layout.popup_edu, LearnerPlayActivity.this.findViewById(R.id.edu_popup));

                ConstraintLayout viewGroup = (ConstraintLayout) this.findViewById(R.id.edu_popup);
                boolean focusable = true; // lets taps outside the popup also dismiss it
                popupWindow = new PopupWindow(popupView, 1000, 1000, focusable);

                view = inflater.inflate(R.layout.activity_learner_play, viewGroup);
                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window token
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                View container1 = (View) popupWindow.getContentView().getParent();
                WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
                WindowManager.LayoutParams p = (WindowManager.LayoutParams) container1.getLayoutParams();

                p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                p.dimAmount = 0.5f;
                wm.updateViewLayout(container1, p);

                TextView question = popupWindow.getContentView().findViewById(R.id.popup_question);
                question.setText("Do you want to finish this practice?");

                final int[] i = {0};
                ProgressBar mProgressBar = popupWindow.getContentView().findViewById(R.id.popup_progress);
                mProgressBar.setProgress(i[0]);
                CountDownTimer mCountDownTimer = new CountDownTimer(5000,1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        Log.v("Log_tag", "Tick of Progress"+ i[0] + millisUntilFinished);
                        i[0]++;
                        mProgressBar.setProgress((int) i[0] *100/(5000/1000));
                    }

                    @Override
                    public void onFinish() {
                        //Do what you want
                        i[0]++;
                        mProgressBar.setProgress(100);
                        popupWindow.dismiss();
                        hideYourTurn();
                        Toast.makeText(LearnerPlayActivity.this, "You have finished this song!",Toast.LENGTH_SHORT).show();
                        stop.setVisibility(View.INVISIBLE);
                        play.setVisibility(View.VISIBLE);
                        starttime = "00:00";
                        String[] time = starttime.split(":");
                        startmilli = Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);
                        stime.setText(starttime);
                        seekBar.setProgress(startmilli);
                        stop.setVisibility(View.INVISIBLE);
                        img.clearAnimation();
                        play.setVisibility(View.VISIBLE);
                        nor.setText("Repeats: " + num);
                        dop.setText("Parts: 1/" + part);
                        num_new = num;
                        part_new = 1;
                        repeat = starttime;
                        MainActivity.setToLibrary(true);
                        Intent intent = new Intent(LearnerPlayActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                };
                mCountDownTimer.start();

                Button repeatb = popupWindow.getContentView().findViewById(R.id.popup_repeat);
                repeatb.setOnClickListener(view11 -> {
                    mCountDownTimer.cancel();
                    img.startAnimation(aniRotate);
                    nor.setText("Repeats: " + num);
                    starttime = repeat;
                    stime.setText(starttime);
                    String[] time = starttime.split(":");
                    startmilli = Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);
                    seekBar.setProgress(startmilli);
                    midiPlayer.educate(false);
                    update();
                    popupWindow.dismiss();
                    hideYourTurn();
                });

                Button proceed = popupWindow.getContentView().findViewById(R.id.popup_proceed);
                proceed.setText("Finish");
                proceed.setOnClickListener(view11 -> {
                    mCountDownTimer.cancel();
                    popupWindow.dismiss();
                    hideYourTurn();
                    Toast.makeText(LearnerPlayActivity.this, "You have finished this song!",Toast.LENGTH_SHORT).show();
                    stop.setVisibility(View.INVISIBLE);
                    play.setVisibility(View.VISIBLE);
                    starttime = "00:00";
                    String[] time = starttime.split(":");
                    startmilli = Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);
                    stime.setText(starttime);
                    seekBar.setProgress(startmilli);
                    stop.setVisibility(View.INVISIBLE);
                    img.clearAnimation();
                    play.setVisibility(View.VISIBLE);
                    nor.setText("Repeats: " + num);
                    dop.setText("Parts: 1/" + part);
                    num_new = num;
                    part_new = 1;
                    repeat = starttime;
                    MainActivity.setToLibrary(true);
                    Intent intent = new Intent(LearnerPlayActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
            } else {
                img.clearAnimation();
                midiPlayer.stop();

                View popupView = inflater.inflate(R.layout.popup_edu, LearnerPlayActivity.this.findViewById(R.id.edu_popup));

                ConstraintLayout viewGroup = (ConstraintLayout) this.findViewById(R.id.edu_popup);
                boolean focusable = true; // lets taps outside the popup also dismiss it
                popupWindow = new PopupWindow(popupView, 800, 1000, focusable);

                view = inflater.inflate(R.layout.activity_learner_play, viewGroup);
                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window token
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                View container1 = (View) popupWindow.getContentView().getParent();
                WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
                WindowManager.LayoutParams p = (WindowManager.LayoutParams) container1.getLayoutParams();

                p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                p.dimAmount = 0.5f;
                wm.updateViewLayout(container1, p);

                final int[] i = {0};
                ProgressBar mProgressBar = popupWindow.getContentView().findViewById(R.id.popup_progress);
                mProgressBar.setProgress(i[0]);
                CountDownTimer mCountDownTimer = new CountDownTimer(5000,1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        Log.v("Log_tag", "Tick of Progress"+ i[0] + millisUntilFinished);
                        i[0]++;
                        mProgressBar.setProgress((int) i[0] *100/(5000/1000));
                    }

                    @Override
                    public void onFinish() {
                        //Do what you want
                        i[0]++;
                        mProgressBar.setProgress(100);
                        img.startAnimation(aniRotate);
                        repeat = starttime;
                        part_new += 1;
                        dop.setText("Parts: " + part_new + "/" + part);
                        num_new = num;
                        nor.setText("Repeats: " + num_new);
                        popupWindow.dismiss();
                        hideYourTurn();
                        midiPlayer.educate(true);
                        update();
                    }
                };
                mCountDownTimer.start();

                Button repeatb = popupWindow.getContentView().findViewById(R.id.popup_repeat);
                repeatb.setOnClickListener(view11 -> {
                    mCountDownTimer.cancel();
                    img.startAnimation(aniRotate);
                    nor.setText("Repeats: " + num);
                    starttime = repeat;
                    stime.setText(starttime);
                    String[] time = starttime.split(":");
                    startmilli = Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);
                    seekBar.setProgress(startmilli);
                    midiPlayer.educate(false);
                    update();
                    popupWindow.dismiss();
                    hideYourTurn();
                });

                Button proceed = popupWindow.getContentView().findViewById(R.id.popup_proceed);
                proceed.setOnClickListener(view11 -> {
                    mCountDownTimer.cancel();
                    img.startAnimation(aniRotate);
                    repeat = starttime;
                    part_new += 1;
                    dop.setText("Parts: " + part_new + "/" + part);
                    num_new = num;
                    nor.setText("Repeats: " + num_new);
                    popupWindow.dismiss();
                    hideYourTurn();
                    midiPlayer.educate(true);
                    update();
                });

            }
        } else {
            num_new -= 1;
            img.startAnimation(aniRotate);
            nor.setText("Repeats: " + num_new);
            starttime = repeat;
            stime.setText(starttime);
            String[] time = starttime.split(":");
            startmilli = Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);
            seekBar.setProgress(startmilli);
            hideYourTurn();
            update();
        }
    }

    public static void showYourTurn() {
        yourturn.setVisibility(View.VISIBLE);
        exit.setClickable(false);
        stop.setClickable(false);
    }

    public static void hideYourTurn() {
        yourturn.setVisibility(View.INVISIBLE);
        exit.setClickable(true);
        stop.setClickable(true);
    }

    public void started() {
        showYourTurn();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                while (!midiPlayer.runEducate){

                }
                finished();
                midiPlayer.newCleverEducate();
            }
        }, midiPlayer.duration* midiPlayer.ONE_BAR_LEN_MS);
    }

    public void finished() {
        hideYourTurn();
        nextCycle();
    }

}
