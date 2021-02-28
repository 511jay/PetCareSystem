package com.jhlee.petcaremaster;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class AduinoOption extends AppCompatActivity {
    EditText URLed;
    EditText IDed;
    TextView txtID;
    TextView txtIP;
    TextView txtBot;
    TextView txtTok;
    String Name = "Fale";
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aduino_option);
        getToken();
        Button aduB = (Button)findViewById(R.id.AduinoRegiB);
        Button appB = (Button)findViewById(R.id.appB);

        //아두이노가 받은 값으로 연결을 하면서 연결이 끊어지므로 오류 발생, 버튼 추가
        Button getResultB = (Button)findViewById(R.id.getResultB);

        URLed = (EditText)findViewById(R.id.URL_ed);
        IDed = (EditText)findViewById(R.id.ID_ed);
        txtID = (TextView)findViewById(R.id.ID);
        txtIP = (TextView)findViewById(R.id.IP);
        txtBot = (TextView)findViewById(R.id.returnApp);
        txtTok = (TextView)findViewById(R.id.tokentxt);

        txtID.setText(getPreferencesID());
        txtIP.setText(getPreferencesIP());

        SharedPreferences sharedPreferences = getSharedPreferences(Name, 0);
        txtTok.setText(sharedPreferences.getString("Token", token));

        aduB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((URLed.length()!=0) && (IDed.length()!=0)){
                    String id = URLed.getText().toString();
                    String pw = IDed.getText().toString();
                    //특수문자는 url로 보낼때 인코딩해야됨
                    id = URLEncoder.encode(id);
                    pw = URLEncoder.encode(pw);
                    String url = "http://192.168.4.1/Config?id="+id+"&pw="+pw;
                    AduinoOption.NetworkTask2 networkTask2 = new AduinoOption.NetworkTask2(url,null);
                    networkTask2.execute();

                }
                else{
                    Toast.makeText(getApplicationContext(), "입력하지 않은 값이 있습니다.",  Toast.LENGTH_LONG).show();
                }
            }
        });

        appB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //정준
                //통신할 서버의 주소
                String url = "https://skillserver.herokuapp.com/registerAPP";
                //POST 방식으로 보낼것이기 때문에 데이터를 담을 자료구조 생성
                ContentValues values = new ContentValues();

                //KEY와 VALUE 쌍으로 데이터를 넣는다.
                values.put("id",txtID.getText().toString());
                //토큰 값을 shared preference에서 가져옴
                SharedPreferences prefs = getSharedPreferences(Name,0);
                String token = prefs.getString("Token",null);
                values.put("token",token);
                //아래에서 만들 NetworkTask 클래스를 인스턴스화 하는데
                //생성자 값으로 서버주소와 전송할 데이터를 준다.
                AduinoOption.NetworkTask networkTask = new AduinoOption.NetworkTask(url,values);
                //객체의 execute 함수를 실행시키면 비동기로 백그라운드에서 지혼자 통신을 하러간다.
                //서버로부터 돌아온 결과값으로 뭔가 해야 한다면 아래 클래스 주석 참고
                networkTask.execute();
                //정준
            }
        });

        //새로운 버튼은 누르면 해당 url로 get 요청을 보내는 간단한 작업을 한다. networkTask도 그대로 사용
        getResultB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://192.168.4.1/getResult";
                AduinoOption.NetworkTask2 networkTask2 = new AduinoOption.NetworkTask2(url,null);
                networkTask2.execute();
            }
        });
    }
    //onCreate 끝
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
            AduinoOption.RequestHttpPOSTURLConnection requestHttpPOSTURLConnection = new AduinoOption.RequestHttpPOSTURLConnection();
            //URLconnection의 request함수를 실행시켜 서버와 통신함
            result = requestHttpPOSTURLConnection.request(url,values);

            return result;
        }

        @Override
        protected void onPostExecute(String s){
            //서버와 통신이 완료된 후 결과 값(수신 값)이 String s에 담겨 돌아온다.
            //여기서 결과 값으로 하고 싶은 작업을 하면된다.
            super.onPostExecute(s);
            txtBot.setText(s);
        }
    }

    public class RequestHttpGETURLConnection {
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

                urlConn.setRequestMethod("GET");
                urlConn.setRequestProperty("Accept-Charset","UTF-8");
                urlConn.setRequestProperty("Content_Type","text/html");
                urlConn.setConnectTimeout(10*1000);
                urlConn.setReadTimeout(10*1000);
            /*
            String strParams = sbParams.toString();
            OutputStream os = urlConn.getOutputStream();
            os.write(strParams.getBytes("UTF-8"));
            os.flush();
            os.close();*/

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

    public class NetworkTask2 extends AsyncTask<Void,Void,String> {
        private String url;
        private ContentValues values;

        public NetworkTask2(String url,ContentValues values){
            this.url = url;
            this.values = values;
        }
        @Override
        protected  String doInBackground(Void... params){
            String result;
            //여기서는 POST를 써서 통신할것이므로 POST를 선언했지만 GET을 쓰고싶거나
            //써야한다면 RequestHttpGETURLConnection으로 바꿔준다
            AduinoOption.RequestHttpGETURLConnection requestHttpGETURLConnection = new AduinoOption.RequestHttpGETURLConnection();
            //URLconnection의 request함수를 실행시켜 서버와 통신함
            result = requestHttpGETURLConnection.request(url,values);

            return result;
        }

        @Override
        protected void onPostExecute(String s){
            //서버와 통신이 완료된 후 결과 값(수신 값)이 String s에 담겨 돌아온다.
            //여기서 결과 값으로 하고 싶은 작업을 하면된다.
            super.onPostExecute(s);
            String str = s;
            String ID = "";
            String IP = "";
            int startOfID = str.indexOf("ID");
            int startOfIP = str.indexOf("IP=");
            //해당 값이 없는데도 (-1)뒤의 +로 더해주는 값에 의해 실행되어 이상한 값이 나오므로
            //if 조건문으로 값이 없을 경우 PASS
            if(startOfID!=-1){
                startOfID+=3;
                for(int i=startOfID; i<startOfID+8; i++) {
                    ID+=str.charAt(i);
                }
            }
            if(startOfIP!=-1){
                startOfIP+=3;
                for(int j=startOfIP; j<str.length(); j++) {
                    IP+=str.charAt(j);
                }
            }

            txtID.setText(ID);
            savePreferencesID();
            txtIP.setText(IP);
        }
    }
    public void savePreferencesID(){
        SharedPreferences sharedPreferences = getSharedPreferences(Name, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String str1 = txtID.getText().toString();
        editor.putString("ID", str1);
        editor.commit();
    }
    public String getPreferencesID(){
        SharedPreferences sharedPreferences = getSharedPreferences(Name, 0);
        return sharedPreferences.getString("ID", "0");
    }
    public void savePreferencesIP(){
        SharedPreferences sharedPreferences = getSharedPreferences(Name, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String str1 = txtIP.getText().toString();
        editor.putString("IP", str1);
        editor.commit();
    }
    public String getPreferencesIP(){
        SharedPreferences sharedPreferences = getSharedPreferences(Name, 0);
        return sharedPreferences.getString("IP", "0");
    }
    public void onStop(){
        super.onStop();
        savePreferencesID();
        savePreferencesIP();
        savePreferencesTokenforStop();
    }
    public void getToken(){
        //토큰값을 받아옵니다.
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        token = task.getResult().getToken();
                        txtTok.setText(token);
                        savePreferencesToken(token);
                    }
                });
    }
    public void savePreferencesToken(String task){
        SharedPreferences sharedPreferences = getSharedPreferences(Name, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        token = task;
        editor.putString("Token", token);
        editor.commit();
    }
    public void savePreferencesTokenforStop(){
        SharedPreferences sharedPreferences = getSharedPreferences(Name, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        token = txtTok.getText().toString();
        editor.putString("Token", token);
        editor.commit();
    }
}