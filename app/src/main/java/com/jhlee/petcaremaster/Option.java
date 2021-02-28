package com.jhlee.petcaremaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Option extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        Button NowAddB = (Button)findViewById(R.id.MoveNowAddB);
        final Button AduinoB = (Button)findViewById(R.id.AduinoB);
        final Button PetinfoB = (Button)findViewById(R.id.PetinfoB);
        NowAddB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NowAdd.class);
                startActivity(intent);
            }
        });
        AduinoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AduinoOption.class);
                startActivity(intent);
            }
        });
        PetinfoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PetInfoOption.class);
                startActivity(intent);
            }
        });



    }


}

