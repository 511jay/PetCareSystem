package com.jhlee.petcaremaster;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class State extends AppCompatActivity {

    BarChart Bchart;
    ArrayList date = new ArrayList();
    ArrayList weight = new ArrayList();
    ArrayList leftover = new ArrayList();
    ArrayList c1= new ArrayList();
    ArrayList c2 = new ArrayList();
    ArrayList c3 = new ArrayList();
    //ArrayList NoOfEmp = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state);

        Bchart = (BarChart)findViewById(R.id.chart);
        final Button weightB = (Button)findViewById(R.id.weightB);
        final Button remainB = (Button)findViewById(R.id.remainB);
        Button feeded1 = (Button)findViewById(R.id.feededB1);
        Button f2 = (Button)findViewById(R.id.feededB2);
        Button f3 = (Button)findViewById(R.id.feededB3);
        //정준
        //통신할 서버의 주소
        String url = "https://skillserver.herokuapp.com/getStatus";
        //POST 방식으로 보낼것이기 때문에 데이터를 담을 자료구조 생성
        ContentValues values = new ContentValues();
        SharedPreferences getid = getSharedPreferences("Fale",MODE_PRIVATE);
        String id = getid.getString("ID","null");
        //KEY와 VALUE 쌍으로 데이터를 넣는다.
        values.put("id",id);
        //아래에서 만들 NetworkTask 클래스를 인스턴스화 하는데
        //생성자 값으로 서버주소와 전송할 데이터를 준다.
        State.NetworkTask networkTask = new State.NetworkTask(url,values);
        //객체의 execute 함수를 실행시키면 비동기로 백그라운드에서 지혼자 통신을 하러간다.
        //서버로부터 돌아온 결과값으로 뭔가 해야 한다면 아래 클래스 주석 참고
        networkTask.execute();
        //정준


        weightB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BarDataSet bardataset = new BarDataSet(weight, "Weight");
                Bchart.animateY(500);
                BarData data = new BarData(date, bardataset);
                bardataset.setColors(ColorTemplate.COLORFUL_COLORS);
                Bchart.setData(data);


            }
        });

        remainB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BarDataSet bardataset = new BarDataSet(leftover, "남은량");
                Bchart.animateY(500);
                BarData data = new BarData(date, bardataset);
                bardataset.setColors(ColorTemplate.COLORFUL_COLORS);
                Bchart.setData(data);
            }
        });

        feeded1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BarDataSet bardataset = new BarDataSet(c1, "1번 통 급식량");
                Bchart.animateY(1000);
                BarData data = new BarData(date, bardataset);
                bardataset.setColors(ColorTemplate.COLORFUL_COLORS);
                Bchart.setData(data);

            }
        });
        f2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BarDataSet bardataset2 = new BarDataSet(c2, "2번 통 급식량");
                Bchart.animateY(1000);
                BarData data = new BarData(date, bardataset2);
                bardataset2.setColors(ColorTemplate.COLORFUL_COLORS);
                Bchart.setData(data);

            }
        });
        f3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BarDataSet bardataset3 = new BarDataSet(c3, "3번 통 급식량");
                Bchart.animateY(1000);
                BarData data = new BarData(date, bardataset3);
                bardataset3.setColors(ColorTemplate.COLORFUL_COLORS);
                Bchart.setData(data);

            }
        });

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
            State.RequestHttpPOSTURLConnection requestHttpPOSTURLConnection = new State.RequestHttpPOSTURLConnection();
            //URLconnection의 request함수를 실행시켜 서버와 통신함
            result = requestHttpPOSTURLConnection.request(url,values);

            return result;
        }

        @Override
        protected void onPostExecute(String s){
            //서버와 통신이 완료된 후 결과 값(수신 값)이 String s에 담겨 돌아온다.
            //여기서 결과 값으로 하고 싶은 작업을 하면된다.
            super.onPostExecute(s);
            //Toast.makeText(getApplicationContext(), s,  Toast.LENGTH_LONG).show();

            String check = s;
            int count = 0;
            while(true){

                int start_date = check.indexOf("date")+12;
                int start_weight = check.indexOf("weight\":")+8;
                int start_leftover = check.indexOf("leftover")+10;
                int start_c1 = check.indexOf("_c1")+5;
                int start_c2 = check.indexOf("_c2")+5;
                int start_c3 = check.indexOf("_c3")+5;
                if(check.charAt(0)==']'){
                    break;
                }
                c1.add(new BarEntry(GetC_info(check, start_c1), count));
                c2.add(new BarEntry(GetC_info(check, start_c2), count));
                c3.add(new BarEntry(GetC_info(check, start_c3), count));
                weight.add(new BarEntry(Get_weight_leftover(check, start_weight), count));

                leftover.add(new BarEntry(Get_weight_leftover(check, start_leftover),  count));
                date.add(Get_date(check, start_date));
                check = cutString(check);
                count++;
            }





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

        public float GetC_info(String str, int i) {
            String result = "";

            int count = i;
            while(true) {
                if(str.charAt(count)==','||str.charAt(count)=='}') {
                    break;
                }
                else {
                    result+=str.charAt(count);
                    count++;
                }

            }

            float strflt = Float.parseFloat(result);
            return strflt;
        }
        public String Get_date(String str, int i) {
            String result = "";
            int count = i;
            while(true) {
                if(str.charAt(count)=='T') {
                    break;
                }
                result+=str.charAt(count);
                count++;
            }
            return result;
        }
        public float Get_weight_leftover(String str, int i) {
            String result = "";
            int count = i;
            while(true) {
                if(str.charAt(count)==',') {
                    break;
                }
                else{
                    result+=str.charAt(count);
                    count++;
                }
            }
            float strflt = Float.parseFloat(result);
            return strflt;
        }
    }
}
