package com.example.tenutotutor;

import android.os.Build;
import android.os.Handler;

import androidx.annotation.RequiresApi;

import com.example.tenutotutor.ui.library.LearnerPlayActivity;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.util.MidiEventListener;

import java.io.IOException;


// This class will print any event it receives to the console
public class EventSender implements MidiEventListener {
    MidiPlayer midiPlayer;
    boolean educational = false;

    public EventSender(MidiPlayer initMidiPlayer, boolean educ) {
        midiPlayer = initMidiPlayer;
        educational = educ;
    }

    @Override
    public void onStart(boolean fromBeginning) {
        if (fromBeginning) {
            System.out.println("Started!");
        } else {
            System.out.println("Resumed");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onEvent(MidiEvent event, long ms) {
        System.out.println("Received event: " + event);

        byte[] buffer = new byte[32];
        int numBytes = 0;

        if (event instanceof NoteOn) {
            NoteOn noteOn = (NoteOn) event;

            buffer[numBytes++] = (byte) 0x90;
            buffer[numBytes++] = (byte) noteOn.getNoteValue();
            buffer[numBytes++] = (byte) noteOn.getVelocity();
        } else if (event instanceof NoteOff) {
            NoteOff noteOff = (NoteOff) event;

            buffer[numBytes++] = (byte) 0x80;
            buffer[numBytes++] = (byte) noteOff.getNoteValue();
            buffer[numBytes++] = (byte) noteOff.getVelocity();
        }

        try {
            MainActivity.getMidiInputPort().send(buffer, 0, numBytes);
        } catch (IOException e) {
            System.err.println("ERROR: Failed to send MIDI event to ESP32");
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("ERROR: Input port is not open");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onStop(boolean finished) {
        if (!educational) {
            return;
        }
        if (finished) {
            System.out.println("Finished!");
            midiPlayer.turnOffKeys();
            midiPlayer.LearnerPlayActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    midiPlayer.LearnerPlayActivity.started();
                }
            });
        } else {
            System.out.println("Paused");
        }
    }
}