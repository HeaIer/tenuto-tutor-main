package com.example.tenutotutor.ui.library;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tenutotutor.MidiPlayer;
import com.example.tenutotutor.R;

public class LearnerActivity extends AppCompatActivity {

    private int norDefault = 5;
    private int dopDefault = 2;
    private int bpmDefault = 120;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learner);

        ImageView norLeft = findViewById(R.id.learner_nor_left);
        ImageView norRight = findViewById(R.id.learner_nor_right);
        TextView norValue = findViewById(R.id.learner_nor_value);
        norValue.setText(String.valueOf(this.norDefault));

        norLeft.setOnClickListener(view -> {
            if (norDefault > 1) {
                norDefault -= 1;
                norValue.setText(String.valueOf(norDefault));
            } else {
                Toast.makeText(LearnerActivity.this, "The minimum number of repeat is 1 time.", Toast.LENGTH_SHORT).show();
            }
        });

        norRight.setOnClickListener(view -> {
            if (norDefault < 10) {
                norDefault += 1;
                norValue.setText(String.valueOf(norDefault));
            } else {
                Toast.makeText(LearnerActivity.this, "The maximum number of repeat is 10 times.", Toast.LENGTH_SHORT).show();
            }
        });

        ImageView dopLeft = findViewById(R.id.learner_dop_left);
        ImageView dopRight = findViewById(R.id.learner_dop_right);
        TextView dopValue = findViewById(R.id.learner_dop_value);
        dopValue.setText(String.valueOf(this.dopDefault));
        Button proceed = findViewById(R.id.learner_proceed);
        dopLeft.setOnClickListener(view -> {
            if (dopDefault > 1) {
                dopDefault -= 1;
                dopValue.setText(String.valueOf(dopDefault));
            } else {
                Toast.makeText(LearnerActivity.this, "The minimum number of duration is 1 second.", Toast.LENGTH_SHORT).show();
            }
        });

        dopRight.setOnClickListener(view -> {
            if (dopDefault < 10) {
                dopDefault += 1;
                dopValue.setText(String.valueOf(dopDefault));
            } else {
                Toast.makeText(LearnerActivity.this, "The maximum number of duration is 10 seconds.", Toast.LENGTH_SHORT).show();
            }
        });

        ImageView bpmLeft = findViewById(R.id.setvalue_nor_left2);
        ImageView bpmRight = findViewById(R.id.setvalue_nor_right2);
        TextView bpmValue = findViewById(R.id.setvalue_nor_value3);
        bpmLeft.setOnClickListener(view -> {
            if (bpmDefault > 50) {
                bpmDefault -= 10;
                bpmValue.setText(String.valueOf(bpmDefault));
            } else {
                Toast.makeText(LearnerActivity.this, "The minimum BPM is 50.", Toast.LENGTH_SHORT).show();
            }
        });

        bpmRight.setOnClickListener(view -> {
            if (bpmDefault < 200) {
                bpmDefault += 10;
                bpmValue.setText(String.valueOf(bpmDefault));
            } else {
                Toast.makeText(LearnerActivity.this, "The maximum BPM is 200.", Toast.LENGTH_SHORT).show();
            }

        });

        proceed.setOnClickListener(view -> {
            LearnerPlayActivity.setDuration(dopDefault);
            LearnerPlayActivity.setNumberOfRepeat(norDefault);
            LearnerPlayActivity.setBPM(bpmDefault);
            Intent intent = new Intent(LearnerActivity.this, LearnerPlayActivity.class);
            //startActivity(intent);
            startActivityForResult(intent, 1);
        });


    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data.getBooleanExtra("finish",false)) {
                    finish();
                }
            }
        }
    }

}