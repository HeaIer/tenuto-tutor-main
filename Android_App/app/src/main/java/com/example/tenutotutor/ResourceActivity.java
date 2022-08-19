package com.example.tenutotutor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class ResourceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource);
    }

    public void goToOne(View view){
        goToUrl ( "https://www.musikalessons.com/blog/2017/09/piano-tutorial/");
    }

    public void goToTwo(View view){
        goToUrl ( "https://www.pianistmagazine.com/blogs/14-piano-lessons-on-technique-for-beginners/");
    }

    public void goToThree(View view){
        goToUrl ( "https://www.piano-keyboard-guide.com/free-piano-lessons.html ");
    }

    public void goToFour(View view){
        goToUrl ( "https://www.merriammusic.com/school-of-music/teach-yourself-piano/");
    }

    public void goToFive(View view){
        goToUrl ( "https://www.musicnotes.com/now/tips/how-to-read-sheet-music/");
    }

    public void goToSix(View view){
        goToUrl ( "https://www.ipr.edu/blogs/audio-production/what-are-the-basics-of-music-theory/#:~:text=Some%20definitions%20of%20the%20basic,harmony%2C%20chords%20and%20chord%20progressions.&text=Music%20notes%20can%20be%20a,enjoy%20music%20and%20music%20making");
    }

    public void goToSeven(View view){
        goToUrl ( "https://blog.landr.com/music-theory/");
    }

    public void goToEight(View view){
        goToUrl ( "https://www.musictheory.net/lessons ");
    }
    public void goToNine(View view){
        goToUrl ( "https://www.youtube.com/watch?v=4Uefz3CaTEk");
    }

    private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }
}