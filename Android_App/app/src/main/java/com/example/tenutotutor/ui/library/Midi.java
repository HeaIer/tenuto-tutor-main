package com.example.tenutotutor.ui.library;

import android.content.res.AssetFileDescriptor;

public class Midi {

    private final String songName;
    private final String duration;
    private final int numberOfPlayed;
    private final String path;


    public Midi(String songName, String duration, int numberOfPlayed, String path) {
        this.songName = songName;
        this.duration = duration;
        this.numberOfPlayed = numberOfPlayed;
        this.path = path;


    }

    public String getSongName() {
        return songName;
    }

    public String getDuration() {
        return duration;
    }

    public int getNumberOfPlayed() {
        return numberOfPlayed;
    }

    public String getPath() { return path; }


}
