package com.jhlee.petcaremaster;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class PetInfoOption extends AppCompatActivity {
    TextView brdtxt;
    String myPETBreed = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_info_option);

        brdtxt=(TextView)findViewById(R.id.breedtxt);
        Button breedB = (Button)findViewById(R.id.breedB);
        Button sendB = (Button)findViewById(R.id.PetInfoSendB);

        breedB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnClickBreed(v);
            }
        });

        sendB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(PetInfoOption.this);
                builder.setTitle("펫 정보 전송");
                builder.setMessage("펫정보를 전송하시겠습니까?");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(PetInfoOption.this);
                        //정준
                        //통신할 서버의 주소
                        String url = "https://skillserver.herokuapp.com/registerPET";
                        //POST 방식으로 보낼것이기 때문에 데이터를 담을 자료구조 생성
                        ContentValues values = new ContentValues();
                        SharedPreferences getid = getSharedPreferences("Fale",MODE_PRIVATE);
                        String id = getid.getString("ID",null);
                        //KEY와 VALUE 쌍으로 데이터를 넣는다.
                        values.put("id",id);
                        values.put("cat_dog",myPETBreed);

                        //아래에서 만들 NetworkTask 클래스를 인스턴스화 하는데
                        //생성자 값으로 서버주소와 전송할 데이터를 준다.
                        PetInfoOption.NetworkTask networkTask = new PetInfoOption.NetworkTask(url,values);
                        //객체의 execute 함수를 실행시키면 비동기로 백그라운드에서 지혼자 통신을 하러간다.
                        //서버로부터 돌아온 결과값으로 뭔가 해야 한다면 아래 클래스 주석 참고
                        networkTask.execute();
                        //정준
                        builder2.setMessage("전송되었습니다");
                        builder2.setPositiveButton("확인", null);
                        AlertDialog dialog2 = builder2.create();
                        dialog2.show();
                    }
                });
                builder.setNegativeButton("아니요", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }
    public void OnClickBreed(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] breeds = getResources().getStringArray(R.array.breed);
        final ArrayList<String> selected =  new ArrayList<String>();
        selected.add(breeds[0]);

        builder.setTitle("종 선택");
        builder.setSingleChoiceItems(R.array.breed, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selected.clear();
                selected.add(breeds[which]);
            }
        });
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                brdtxt.setText(selected.get(0));
                myPETBreed = selected.get(0);
            }
        });
        builder.setNegativeButton("취소", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
            PetInfoOption.RequestHttpPOSTURLConnection requestHttpPOSTURLConnection = new PetInfoOption.RequestHttpPOSTURLConnection();
            //URLconnection의 request함수를 실행시켜 서버와 통신함
            result = requestHttpPOSTURLConnection.request(url,values);

            return result;
        }

        @Override
        protected void onPostExecute(String s){
            //서버와 통신이 완료된 후 결과 값(수신 값)이 String s에 담겨 돌아온다.
            //여기서 결과 값으로 하고 싶은 작업을 하면된다.
            super.onPostExecute(s);
        }
    }
}