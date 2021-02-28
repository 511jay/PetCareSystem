package com.jhlee.petcaremaster;

import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class ReservationWeek extends AppCompatActivity {

    private TextView WeekText;
    private TextView TimeText;
    private TimePickerDialog.OnTimeSetListener Timer;

    String WeekData;
    String TimeData;
    String BT1_Weight;
    String BT2_Weight;
    String BT3_Weight;
    String sendStr = "";

    Button WeekB;
    Button BT1B;
    Button BT2B;
    Button BT3B;
    Button is_alwaysB;
    Button is_autoB;
    TextView BT1_txt;
    TextView BT2_txt;
    TextView BT3_txt;

    int is_always = 0;
    int is_auto = 0;
    int weekday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_week);


        WeekB = (Button)findViewById(R.id.ResWeekAddB);
        Button TimeB = (Button)findViewById(R.id.ResWeekTimeAddB);
        Button addB = (Button)findViewById(R.id.WeekAddB);
        BT1B = (Button)findViewById(R.id.BT1B);
        BT2B = (Button)findViewById(R.id.BT2B);
        BT3B = (Button)findViewById(R.id.BT3B);
        is_alwaysB = (Button)findViewById(R.id.is_alwaysB);
        is_autoB = (Button)findViewById(R.id.is_autoB);
        BT1_txt = (TextView)findViewById(R.id.BT1_check_txt);
        BT2_txt = (TextView)findViewById(R.id.BT2_check_txt);
        BT3_txt = (TextView)findViewById(R.id.BT3_check_txt);

        this.InitializeTimeTxt();
        this.InitializeWeekTxt();
        this.InitializeTimeListener();

        WeekB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnClickWeek(v);

            }
        });
        TimeB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnClickTime(v);
            }
        });

        is_alwaysB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = null;
                if(is_always == 1){
                    is_always = 0;
                    str = (String) WeekText.getText();
                    str = str.substring(3,str.length());
                }
                else if(is_always == 0){
                    is_always = 1;
                    str = (String) WeekText.getText();
                    str = "매주 "+ str;
                }
                WeekText.setText(str);
            }
        });

        is_autoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(is_auto == 1){
                    is_auto = 0;
                    BT1_txt.setText("");
                    BT1_Weight = "";
                    BT2_txt.setText("");
                    BT2_Weight = "";
                    BT3_txt.setText("");
                    BT3_Weight = "";
                }
                else if(is_auto == 0){
                    is_auto = 1;
                    BT1_txt.setText("자동");
                    BT1_Weight = "자동";
                    BT2_txt.setText("자동");
                    BT2_Weight = "자동";
                    BT3_txt.setText("자동");
                    BT3_Weight = "자동";
                }
            }
        });

        addB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp ;
                if(WeekData==null || TimeData==null|| BT1_Weight==null|| BT2_Weight==null|| BT3_Weight==null){
                    Toast.makeText(getApplicationContext(), "입력하지 않은 값이 있습니다.",  Toast.LENGTH_LONG).show();
                }
                else {
                    temp = WeekData + "  [" + TimeData + "]\n1번 통 배식량: " + BT1_Weight + "g\n2번 통 배식량: " + BT2_Weight + "g\n3번 통 배식량: " + BT3_Weight + "g";
                    sendStr = WeekData + " [" + TimeData + "]\n1번 통: " + BT1_Weight + "g 2번 통: " + BT2_Weight + "g 3번 통: " + BT3_Weight + "g";
                    final AlertDialog.Builder builder = new AlertDialog.Builder(ReservationWeek.this);
                    builder.setTitle("추가확인");
                    builder.setMessage(temp + "  이 맞나요?");
                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //통신할 서버의 주소
                            String url = "https://skillserver.herokuapp.com/sendCommand";
                            //POST 방식으로 보낼것이기 때문에 데이터를 담을 자료구조 생성
                            ContentValues values = new ContentValues();
                            SharedPreferences getid = getSharedPreferences("Fale",MODE_PRIVATE);
                            String id = getid.getString("ID","null");
                            //KEY와 VALUE 쌍으로 데이터를 넣는다.
                            values.put("id",id);
                            values.put("command",1);
                            values.put("weekday",weekday);
                            values.put("time",TimeData);
                            values.put("auto",is_auto);
                            values.put("always",is_always);
                            values.put("c1",BT1_Weight);
                            values.put("c2",BT2_Weight);
                            values.put("c3",BT3_Weight);


                            //아래에서 만들 NetworkTask 클래스를 인스턴스화 하는데
                            //생성자 값으로 서버주소와 전송할 데이터를 준다.
                            NetworkTask networkTask = new NetworkTask(url,values);
                            //객체의 execute 함수를 실행시키면 비동기로 백그라운드에서 지혼자 통신을 하러간다.
                            //서버로부터 돌아온 결과값으로 뭔가 해야 한다면 아래 클래스 주석 참고
                            networkTask.execute();

                            finish();
                        }
                    });
                    builder.setNegativeButton("아니요", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
        BT1B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBT1(v);
            }
        });
        BT2B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBT2(v);
            }
        });
        BT3B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBT3(v);
            }
        });


    }
    //onCreate
    public void InitializeWeekTxt(){
        WeekText = (TextView)findViewById(R.id.CheckWeekByText);
    }

    public void OnClickWeek(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] weeks = getResources().getStringArray(R.array.ListDial_array);
        final ArrayList<String> selected =  new ArrayList<String>();
        selected.add(weeks[0]);
        weekday = 1;
        builder.setTitle("요일 선택");
        builder.setSingleChoiceItems(R.array.ListDial_array, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selected.clear();
                selected.add(weeks[which]);
                weekday = which+1;
            }
        });
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                WeekText.setText(selected.get(0)+"입니다");
                WeekData = selected.get(0);

            }
        });
        builder.setNegativeButton("취소", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }



    //week
    public void InitializeTimeTxt(){
        TimeText = (TextView)findViewById(R.id.CheckTimeByText);
    }

    public void InitializeTimeListener(){
        Timer = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                TimeText.setText(hourOfDay+":"+minChange(minute));
                TimeData = ""+hourOfDay+":"+minChange(minute);
            }
        };
    }

    public void OnClickTime(View v){
        TimePickerDialog dialog2 = new TimePickerDialog(this, Timer, 12, 0, true);
        dialog2.show();
    }

    public String minChange(int a){
        String temp ="";
        if(a<10){
            temp = "0"+a;
        }
        else{
            temp += a;
        }
        return temp;
    }
    //time
    public void onClickBT1(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText BT1_et = new EditText(ReservationWeek.this);
        BT1_et.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setTitle("1번 통");
        builder.setMessage("양을 입력해주세요(g)");
        builder.setView(BT1_et);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BT1_txt.setText(BT1_et.getText().toString());
                BT1_Weight = BT1_et.getText().toString();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    public void onClickBT2(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText BT2_et = new EditText(ReservationWeek.this);
        BT2_et.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setTitle("2번 통");
        builder.setMessage("양을 입력해주세요(g)");
        builder.setView(BT2_et);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BT2_txt.setText(BT2_et.getText().toString());
                BT2_Weight = BT2_et.getText().toString();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    public void onClickBT3(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText BT3_et = new EditText(ReservationWeek.this);
        BT3_et.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setTitle("3번 통");
        builder.setMessage("양을 입력해주세요(g)");
        builder.setView(BT3_et);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BT3_txt.setText(BT3_et.getText().toString());
                BT3_Weight = BT3_et.getText().toString();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    public class NetworkTask extends AsyncTask<Void,Void,String> {
        private String url;
        private ContentValues values;

        public NetworkTask(String url,ContentValues values){
            this.url = url;
            this.values = values;
        }
        @Override
        protected  String doInBackground(Void... params){
            String result;
            //여기서는 POST를 써서 통신할것이므로 POST를 선언했지만 GET을 쓰고싶거나
            //써야한다면 RequestHttpGETURLConnection으로 바꿔준다
            RequestHttpPOSTURLConnection requestHttpPOSTURLConnection = new RequestHttpPOSTURLConnection();
            //URLconnection의 request함수를 실행시켜 서버와 통신함
            result = requestHttpPOSTURLConnection.request(url,values);

            return result;
        }

        @Override
        protected void onPostExecute(String s){
            //서버와 통신이 완료된 후 결과 값(수신 값)이 String s에 담겨 돌아온다.
            //여기서 결과 값으로 하고 싶은 작업을 하면된다.
            super.onPostExecute(s);
            if(s.equals("Schedule Already EXIST")){
                Toast.makeText(getApplicationContext(), "이미 있는 시간입니다.",  Toast.LENGTH_LONG).show();
            }
            else{
                ((ReservationList) ReservationList.test).items.add(sendStr);
                ((ReservationList) ReservationList.test).adapter.notifyDataSetChanged();
                finish();
            }
        }
    }

    public class RequestHttpPOSTURLConnection {
        public String request(String _url, ContentValues _params){
            HttpURLConnection urlConn = null;
            StringBuffer sbParams = new StringBuffer();

            if(_params == null)
                sbParams.append("");
            else{
                boolean isAnd = false;
                String key;
                String value;

                for(Map.Entry<String,Object> parameter: _params.valueSet()){
                    key = parameter.getKey();
                    value = parameter.getValue().toString();

                    if(isAnd)
                        sbParams.append("&");

                    sbParams.append(key).append("=").append(value);

                    if(!isAnd)
                        if(_params.size() >= 2)
                            isAnd = true;
                }
            }

            try{
                URL url = new URL(_url);
                urlConn = (HttpURLConnection) url.openConnection();

                urlConn.setRequestMethod("POST");
                urlConn.setRequestProperty("Accept-Charset","UTF-8");
                urlConn.setRequestProperty("Content_Type","text/html");

                String strParams = sbParams.toString();
                OutputStream os = urlConn.getOutputStream();
                os.write(strParams.getBytes("UTF-8"));
                os.flush();
                os.close();

                if(urlConn.getResponseCode()!= HttpURLConnection.HTTP_OK)
                    return null;

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(),"UTF-8"));

                String line;
                String page ="";

                while((line=reader.readLine())!=null){
                    page += line;
                }

                return page;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                if(urlConn != null)
                    urlConn.disconnect();
            }
            return null;
        }
    }




}
