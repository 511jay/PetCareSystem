package com.jhlee.petcaremaster;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class NowAdd extends AppCompatActivity {

    String Name = "file";

    EditText BT1;
    EditText BT2 ;
    EditText BT3;

    TextView BT1_check;
    TextView BT2_check;
    TextView BT3_check;

    public static Context context_now_add;

    String BT1_saved;
    String BT2_saved;
    String BT3_saved;

    String is_auto_text;
    int is_auto;
    String BT1_data;
    String BT2_data;
    String BT3_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_add);

        context_now_add = this;

        Button canB = (Button)findViewById(R.id.NowCancelB);
        Button ModB = (Button)findViewById(R.id.NowModB);
        Button NowAutoB = (Button)findViewById(R.id.NowAutoB);
        BT1 = (EditText)findViewById(R.id.NowBottle1);
        BT2 = (EditText)findViewById(R.id.NowBottle2);
        BT3 = (EditText)findViewById(R.id.NowBottle3);
        BT1_check = (TextView)findViewById(R.id.checkboxBT1_gram);
        BT2_check = (TextView)findViewById(R.id.checkboxBT2_gram);
        BT3_check = (TextView)findViewById(R.id.checkboxBT3_gram);

        SharedPreferences sharedPreferences = getSharedPreferences(Name, 0);
        if(sharedPreferences.getInt("is_auto", 0)==1) {
            BT1_check.setText("자동");

            BT2_check.setText("자동");

            BT3_check.setText("자동");
        }
        else{
            BT1_saved = getPreferencesBT1();
            BT1_check.setText(BT1_saved);

            BT2_saved = getPreferencesBT2();
            BT2_check.setText(BT2_saved);

            BT3_saved = getPreferencesBT3();
            BT3_check.setText(BT3_saved);
        }



        ModB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(BT1.length()<=0 || BT2.length()<=0 || BT3.length()<=0){
                    Toast.makeText(getApplicationContext(), "입력하지 않은 값이 있습니다.",  Toast.LENGTH_LONG).show();
                }
                else{
                    String temp = "1번 통 배식량: " + BT1.getText().toString()+ "g\n2번 통 배식량: " + BT2.getText().toString()+ "g\n3번 통 배식량: " + BT3.getText().toString()+ "g";
                    final AlertDialog.Builder builder = new AlertDialog.Builder(NowAdd.this);
                    builder.setTitle("추가확인");
                    builder.setMessage(temp + "  이 맞나요?");
                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BT1_check.setText(BT1.getText());
                            BT2_check.setText(BT2.getText());
                            BT3_check.setText(BT3.getText());
                            finish();
                        }
                    });
                    builder.setNegativeButton("아니요", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();


                }
            }
        });
        NowAutoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(Name, 0);
                if(sharedPreferences.getInt("is_auto", 0)==1){
                    savePreferencesAuto(0);
                    BT1_saved = getPreferencesBT1();
                    BT1_check.setText(BT1_saved);

                    BT2_saved = getPreferencesBT2();
                    BT2_check.setText(BT2_saved);

                    BT3_saved = getPreferencesBT3();
                    BT3_check.setText(BT3_saved);
                }
                else{
                    savePreferencesAuto(1);
                    BT1_check.setText("자동");

                    BT2_check.setText("자동");

                    BT3_check.setText("자동");
                }

            }
        });
        canB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    //불러오기
    public String getPreferencesBT1(){
            SharedPreferences sharedPreferences = getSharedPreferences(Name, 0);
        return sharedPreferences.getString("BT1", "0");
    }
    public String getPreferencesBT2(){
        SharedPreferences sharedPreferences = getSharedPreferences(Name, 0);
        return sharedPreferences.getString("BT2", "0");
    }
    public String getPreferencesBT3(){
        SharedPreferences sharedPreferences = getSharedPreferences(Name, 0);
        return sharedPreferences.getString("BT3", "0");
    }

    public void savePreferencesAuto(int value){
        SharedPreferences sharedPreferences = getSharedPreferences(Name, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("is_auto", value);
        editor.commit();
    }
    //저장하기
    public void savePreferencesBT1(){
        SharedPreferences sharedPreferences = getSharedPreferences(Name, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String str1 = BT1_check.getText().toString();
        BT1_data = str1;
        editor.putString("BT1", str1);
        editor.commit();

    }
    public void savePreferencesBT2(){
        SharedPreferences sharedPreferences = getSharedPreferences(Name, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String str2 = BT2_check.getText().toString();
        BT2_data = str2;
        editor.putString("BT2", str2);
        editor.commit();
    }
    public void savePreferencesBT3(){
        SharedPreferences sharedPreferences = getSharedPreferences(Name, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String str3 = BT3_check.getText().toString();
        BT3_data = str3;
        editor.putString("BT3", str3);
        editor.commit();
    }
    //종료시
    public void onStop(){
        super.onStop();
        savePreferencesBT1();
        savePreferencesBT2();
        savePreferencesBT3();
    }

}
