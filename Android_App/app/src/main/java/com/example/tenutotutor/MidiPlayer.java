package com.example.tenutotutor;

import android.os.Build;
import android.os.SystemClock;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.example.tenutotutor.ui.library.LearnerPlayActivity;
import com.example.tenutotutor.ui.library.SongActivity;
import com.leff.midi.MidiFile;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.meta.Tempo;


import java.io.File;
import java.io.IOException;

public class MidiPlayer {
    public CustomMidiProcessor processor;
    public final int duration;
    private final int repeats;
    private long currentPart;
    private int currentRepeat;
    private boolean runninng;
    public MidiFile midi;
    public boolean runEducate;
    private float bpm;
    public LearnerPlayActivity LearnerPlayActivity;

    public final long ONE_BAR_LEN_MS = 5000;

    public MidiPlayer(String path) {
        this(path, 0, 0,null);
    }

    public MidiPlayer(String path, float BPM) {
        this(path, 0, 0,null, BPM);
    }

    public MidiPlayer(String path, int durationInit, int repeatsInit, LearnerPlayActivity learnerPlayActivity)
    {
        this(path, durationInit, repeatsInit, learnerPlayActivity, Tempo.DEFAULT_BPM);
    }
    public MidiPlayer(String path, int durationInit, int repeatsInit, LearnerPlayActivity learnerPlayActivity, float BPM) {
        try {
            midi = new MidiFile(new File(path));

            // Create a new MidiProcessor.
            bpm = BPM;
            processor = new CustomMidiProcessor(midi, BPM);

            boolean educ = durationInit > 0;

            // listen for all events:
            processor.registerEventListener(new EventSender(this, educ), MidiEvent.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        duration = durationInit;
        repeats = repeatsInit;
        currentPart = 0;
        currentRepeat = repeats;
        runninng = false;
        runEducate = false;
        LearnerPlayActivity = learnerPlayActivity;
    }

    public MidiPlayer(MidiPlayer other, float BPM)
    {
        midi = other.midi;
        duration = other.duration;
        repeats = other.repeats;
        currentPart = other.currentPart;
        runninng = other.runninng;
        runEducate = other.runEducate;
        currentRepeat = other.currentRepeat;
        LearnerPlayActivity = other.LearnerPlayActivity;
        bpm = other.bpm;
        boolean educ = duration > 0;
        processor = new CustomMidiProcessor(other.processor, BPM);
        processor.registerEventListener(new EventSender(this, educ), MidiEvent.class);
    }

    public void play() {
        processor.start();
    }

    public void resumeEducate() {
        runEducate = true;
        processor.start();
    }

    public void stop() {
        processor.stop();
        runEducate = false;
    }

    public void educate(boolean update) {
        runEducate = true;
        if (update) {
            currentPart = currentPart + duration;
        }
//        new Thread(new Runnable()
//        {
//            @RequiresApi(api = Build.VERSION_CODES.M)
//            public void run()
//            {
//                educateThread();
//            }
//        }).start();
        currentRepeat = repeats;
        newCleverEducate();
    }

  //  @RequiresApi(api = Build.VERSION_CODES.M)
    public void newCleverEducate() {
        processor = new CustomMidiProcessor(midi, bpm);
        // listen for all events:
        processor.registerEventListener(new EventSender(this, true), MidiEvent.class);

        long midiLen = midi.getLengthInTicks();
        if (processor.barLength * currentPart < midiLen) {
            currentRepeat = currentRepeat - 1;
            if (currentRepeat >= 0) {
                processor.reset();
                processor.loop(processor.barLength * currentPart, processor.barLength * (currentPart + duration));
                processor.start();
            }
        }
    }

//    @RequiresApi(api = Build.VERSION_CODES.M)
//    private void educateThread() {
//        long midiLen = midi.getLengthInTicks();
//        if (processor.barLength * currentPart < midiLen) {
//            for (int rep = 0; rep < repeats; rep++) {
//                if (runEducate) {
//                    processor.loop(processor.barLength * currentPart, processor.barLength * (currentPart + duration));
//                    processor.start();
//                    runninng = true;
//                    while (!processor.finishedPlay) {
//                         System.out.println("wait until finishedplay");
//                       // runninng = processor.mRunning;
//                    }
//                   // if (runEducate) {
//                        System.out.println("START SLEEP");
//                        turnOffKeys();
//                        LearnerPlayActivity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                LearnerPlayActivity.started();
//                            }
//                        });
//                        try {
//                            Thread.sleep(duration*ONE_BAR_LEN_MS);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        System.out.println("EXIT SLEEP");
//                        while (!runEducate) {
//                            System.out.println("wait until runeducate");
//                        }
//                        LearnerPlayActivity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                LearnerPlayActivity.finished();
//                            }
//                        });
//                    //}
//                }
//            }
//        }
//    }

    public long getNumberOfParts(){
        long bars = processor.getLengthInBars();
        return (bars + (duration -1)) / duration;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void turnOffKeys()
    {
        byte[] buffer = new byte[32];
        int numBytes = 0;
        buffer[numBytes++] = (byte) 0x80;
        buffer[numBytes++] = (byte) 0;
        buffer[numBytes++] = (byte) 0;
        try {
            MainActivity.getMidiInputPort().send(buffer, 0, numBytes);
        } catch (IOException e) {
            System.err.println("ERROR: Failed to send MIDI event to ESP32");
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("ERROR: Input port is not open");
        }
    }
}
