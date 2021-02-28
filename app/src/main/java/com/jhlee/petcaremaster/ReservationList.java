package com.jhlee.petcaremaster;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

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

public class ReservationList extends AppCompatActivity {
    ArrayList<String> items= new ArrayList<>();
    ArrayAdapter<String> adapter;
    ListView listView ;
    public static Context test;
    private static final String SETTINGS_PLAYER_JSON = "settings_item_json";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_list);

        Button AddB = (Button)findViewById(R.id.ResListaddB);
        Button DelB = (Button)findViewById(R.id.ResListdelB);


        test = this;

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, items);
        listView =  (ListView)findViewById(R.id.ResListView) ;
        listView.setAdapter(adapter);

        String url = "https://skillserver.herokuapp.com/getFeedSchedule";
        ContentValues values = new ContentValues();
        ReservationList.NetworkTask networkTask = new ReservationList.NetworkTask(url,values);
        SharedPreferences getid = getSharedPreferences("Fale",MODE_PRIVATE);
        String id = getid.getString("ID","null");
        values.put("id",id);
        networkTask.execute();

        AddB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              Intent intent = new Intent(getApplicationContext(), ReservationWeek.class);
              startActivity(intent);
            }
        });

        DelB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = 0;
                int checked = 0;
                count = adapter.getCount();

                if(count>0){
                    checked = listView.getCheckedItemPosition();

                    if((checked > -1) && (checked < count)){
                        //통신할 서버의 주소
                        String url2 = "https://skillserver.herokuapp.com/sendCommand";
                        //POST 방식으로 보낼것이기 때문에 데이터를 담을 자료구조 생성
                        ContentValues values = new ContentValues();
                        SharedPreferences getid = getSharedPreferences("Fale",MODE_PRIVATE);
                        String id = getid.getString("ID","null");
                        //KEY와 VALUE 쌍으로 데이터를 넣는다.
                        values.put("id",id);
                        values.put("command",3);
                        String F = items.get(checked);
                        int T1 = GetweekData(F);
                        values.put("weekday",T1);
                        String T2 = DelWeekData(F);
                        String T3 = GettimeData(T2);
                        values.put("time",T3);

                        //아래에서 만들 NetworkTask 클래스를 인스턴스화 하는데
                        //생성자 값으로 서버주소와 전송할 데이터를 준다.
                        NetworkTask networkTask = new NetworkTask(url2,values);
                        //객체의 execute 함수를 실행시키면 비동기로 백그라운드에서 지혼자 통신을 하러간다.
                        //서버로부터 돌아온 결과값으로 뭔가 해야 한다면 아래 클래스 주석 참고
                        networkTask.execute();

                        items.remove(checked);
                        listView.clearChoices();
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });





    }//onCreate
    public int GetweekData(String str){
        int day = 0;
        if(str.charAt(0)=='월'){
            day = 1;
        }else if(str.charAt(0)=='화'){
            day = 2;
        }else if(str.charAt(0)=='수'){
            day = 3;
        }else if(str.charAt(0)=='목'){
            day = 4;
        }else if(str.charAt(0)=='금'){
            day = 5;
        }else if(str.charAt(0)=='토'){
            day = 6;
        }else if(str.charAt(0)=='일'){
            day = 7;
        }
        return day;
    }

    public String DelWeekData(String str){
        int start = str.indexOf("[")+1;
        int end = str.length();
        String result = str.substring(start, end);
        return result;
    }
    public String GettimeData(String str){
        int end = str.indexOf("]");
        String time = str.substring(0, end);
        return time;
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
            if(s.equals("EMPTY")){
                adapter.notifyDataSetChanged();
            }else {
                ArrayList<String> nin = getDBItem(s);
                items.clear();
                for (int i = 0; i < nin.size(); i++) {
                    items.add(nin.get(i));

                }
                adapter.notifyDataSetChanged();
            }
        }
    }
    public ArrayList getDBItem(String inStr){
        ArrayList<String> urls = new ArrayList<String>();

        String check = ""+inStr;

        while(true){
            if(check.charAt(0)==']'){
                break;
            }
            urls.add(getData(check, 0));
            check = cutString(check);
        }

        return urls;
    }

    public String Getweight(String str, int i) {
        String result = "";
        int count = i;
        while(str.charAt(count)!=',') {
            result+=str.charAt(count);
            count++;
        }
        return result;
    }
    public String cutString(String str) {
        String result = "";
        if(str.charAt(0)==']'){
            result = "n";
        }
        else{
            int checkpoint = str.indexOf("}")+1;
            int endpoint = str.length();
            result += str.substring(checkpoint, endpoint);

        }
        return result;
    }
    public String getData(String str, int stpoint) {

        int weekpoint = str.indexOf("weekday")+9;
        int timepoint = str.indexOf("time")+7;
        int C1point = str.indexOf("_c1")+5;
        int C2point = str.indexOf("_c2")+5;
        int C3point = str.indexOf("_c3")+5;


        String weekdata = toWeekData(Character.getNumericValue(str.charAt(weekpoint)));
        String timedata = "";
        String c1data = Getweight(str, C1point);
        String c2data = Getweight(str, C2point);
        String c3data = Getweight(str, C3point);

        int i = timepoint;
        int j= i+4;
        while(i<j+1) {
            timedata+=str.charAt(i);
            i++;
        }
        String result = weekdata + " [" + timedata + "]\n1번 통: " + c1data + "g 2번 통: " + c2data + "g 3번 통: " + c3data + "g";

        return result;

    }
    public String toWeekData(int a){
        String Temp = "";
        switch (a){
            case 1:
                Temp="월요일";
                break;
            case 2:
                Temp="화요일";
                break;
            case 3:
                Temp="수요일";
                break;
            case 4:
                Temp="목요일";
                break;
            case 5:
                Temp="금요일";
                break;
            case 6:
                Temp="토요일";
                break;
            case 7:
                Temp="일요일";
                break;
            default:
                break;
        }
        return Temp;
    }

}

