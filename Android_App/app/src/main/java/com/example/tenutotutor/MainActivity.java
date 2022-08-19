package com.example.tenutotutor;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.midi.MidiInputPort;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.tenutotutor.ui.library.ContentUriUtil;
import com.example.tenutotutor.ui.library.Midi;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 100;
    public static boolean success = false;
    public static SharedPreferences sp;
    public static MidiInputPort midiInputPort;
    public static boolean meFragSet = false;
    public static ArrayList<Integer> randIntMe;
    public static boolean ticked = false;
    public static boolean toLibrary = false;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        checkBluetoothAndLocationPermission();
        requestPermissions(
                new String[]{Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_MEDIA_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
                .build());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        NavController navController;
        if (navHostFragment != null)
            navController = navHostFragment.getNavController();
        else
            navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        NavigationUI.setupWithNavController(navView, navController);

        StrictMode.ThreadPolicy Policy1 = StrictMode.allowThreadDiskReads();
        StrictMode.ThreadPolicy Policy2 = StrictMode.allowThreadDiskWrites();
        sp = this.getSharedPreferences("midi_data", Context.MODE_PRIVATE);
        StrictMode.setThreadPolicy(Policy1);
        StrictMode.setThreadPolicy(Policy2);

        String CHANNEL_ID = "com.android.androidosample.channel_1";

        isGrantExternalRW(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.robot)
                .setContentTitle("Time to practice!"+ new String(Character.toChars(0x1F605)))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Don't forget to practice today! Even 5 minutes a day can boost your skill!"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        int notificationId = 122;
        notificationManager.notify(notificationId, builder.build());

        if (toLibrary) {
            navController.navigate(R.id.navigation_library);
            toLibrary = false;
        }
    }
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library



    public static boolean getSuccess() {
        return success;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkBluetoothAndLocationPermission() {

        if ((checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                || (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean grantedLocation = true;
        if (requestCode == LOCATION_PERMISSION_CODE) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    grantedLocation = false;
                }
            }
        }

        if (!grantedLocation) {
            Toast.makeText(this, "Permission error !!!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
            return false;
        }
        return true;
    }

    public static MidiInputPort getMidiInputPort() {
        return midiInputPort;
    }

    public static void setMidiInputPort(MidiInputPort mdPort) {
        midiInputPort = mdPort;
    }

    public static SharedPreferences getSp() {
        return sp;
    }
    public void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }
    public void goToMidi(View view){
        goToUrl ( "https://www.midiworld.com/files/");
    }
    public void gotoFace(View view){
        goToUrl("https://www.facebook.com/Tenuto-Tutor-100548972604954");
    }
    public void gotoIns(View view){
        goToUrl("https://www.instagram.com/invites/contact/?i=1fepo2itrz08q&utm_content=o0dgyik");
    }

    public void gotoYoutube(View view) {
        goToUrl("https://youtu.be/9E22OFXq9Dc");
    }
    public void ticked(View view){
        ticked=true;
    }

    public static void setToLibrary(boolean b) {
        toLibrary = b;
    }

//    public String getDuration(Uri uri) {
//        String path = ContentUriUtil.getPath(this.getApplicationContext(), uri);
//        MediaPlayer mediaPlayer = new MediaPlayer();
//        try {
//            mediaPlayer.setDataSource(path);
//            mediaPlayer.prepare();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        int duration = mediaPlayer.getDuration() / 1000;
//        int minutes = (duration % 3600) / 60;
//        int seconds = duration % 60;
//        String timeString = String.format("%02d:%02d", minutes, seconds);
//        return timeString;
//    }

}