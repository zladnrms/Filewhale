package filewhalewebhard.defytech.wmqkem.filewhalewebhard.user;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import filewhalewebhard.defytech.wmqkem.filewhalewebhard.R;

public class JoinActivity extends AppCompatActivity {

    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL
    /*
     * PHP에서 받아온 JSON Array에 대한 처리
     */
    private JSONArray jarray = null;
    private String js_error = null;
    private String js_result = null;

    // 중복체크 / 가입 버튼, 회원정보 입력칸
    Button btn_chk_id, btn_chk_nick, btn_join;
    EditText et_join_id, et_join_pw, et_join_nick;

    // 중복체크 필요 변수, 회원정보 변수
    private int checkNum = 0;
    private Boolean checkId = false, checkNick = false;
    private String join_id, join_pw, join_nick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        btn_chk_id = (Button) findViewById(R.id.btn_chk_id);
        btn_chk_nick = (Button) findViewById(R.id.btn_chk_nick);
        btn_join = (Button) findViewById(R.id.btn_join);
        et_join_id = (EditText) findViewById(R.id.et_join_id);
        et_join_pw = (EditText) findViewById(R.id.et_join_pw);
        et_join_nick = (EditText) findViewById(R.id.et_join_nick);

        btn_chk_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et_join_id.getText().toString().equals("")) {
                    checkNum = 0;
                    join_id = et_join_id.getText().toString().toLowerCase();
                    new checkJoinInfo().execute();
                } else {
                    Toast.makeText(JoinActivity.this, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show();
                    et_join_id.requestFocus();
                }
            }
        });

        btn_chk_nick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et_join_nick.getText().toString().equals("")) {
                    checkNum = 1;
                    join_nick = et_join_nick.getText().toString();
                    new checkJoinInfo().execute();
                } else {
                    Toast.makeText(JoinActivity.this, "별명을 입력해주세요", Toast.LENGTH_SHORT).show();
                    et_join_nick.requestFocus();
                }
            }
        });

        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                join_pw = et_join_pw.getText().toString();

                if(join_pw.length() >= 8) {
                    if (checkId && checkNick) {

                        new joinConnection().execute();
                    } else {
                        Toast.makeText(JoinActivity.this, "전부 중복체크 해주세요", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(JoinActivity.this, "비밀번호는 8자 이상이여야 합니다", Toast.LENGTH_SHORT).show();
                    et_join_pw.requestFocus();
                }
            }
        });

        et_join_id.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_chk_id.setClickable(true);
                checkId = false;
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전에
            }
        });

        et_join_nick.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_chk_nick.setClickable(true);
                checkNick = false;
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전에
            }
        });

    }

    // 중복체크 처리
    private class checkJoinInfo extends AsyncTask<Void, Void, String> { // 불러오기

        @Override
        protected String doInBackground(Void... params) {

            try {
                URL url = new URL(URLlink + "/android/member/join_check.php"); // 앨범 폴더의 dbname 폴더에 접근
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                if (conn != null) {
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setInstanceFollowRedirects(false); // 추가됨
                    conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");

                    StringBuffer buffer = new StringBuffer();
                    if (checkNum == 0) {
                        buffer.append("join_id").append("=").append(join_id);
                    } else if (checkNum == 1) {
                        buffer.append("join_nick").append("=").append(join_nick);
                    }

                    OutputStreamWriter outStream = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                    PrintWriter writer = new PrintWriter(outStream);
                    writer.write(buffer.toString());
                    writer.flush();

                    // 보내기  &&  받기
                    //헤더 받는 부분

                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuilder builder = new StringBuilder();
                    String json;
                    while ((json = reader.readLine()) != null) {       // 서버에서 라인단위로 보내줄 것이므로 라인단위로 읽는다
                        builder.append(json + "\n");
                        System.out.println("json + " + json);
                    }

                    return builder.toString().trim();

                }

            } catch (final Exception e) {

                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject jsonObj = new JSONObject(result);
                jarray = jsonObj.getJSONArray("result");

                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject c = jarray.getJSONObject(i);

                    if (!c.isNull("error")) { // 우선 에러를 검출함
                        js_error = c.getString("error");

                        switch (js_error) {
                            case "01":
                                Toast.makeText(JoinActivity.this, "DB 연결에 실패하였습니다", Toast.LENGTH_SHORT).show();
                                break;
                        }

                    } else { // 에러가 없으면 진행
                        if (!c.isNull("result")) {
                            js_result = c.getString("result");

                            switch (js_result) {
                                case "chk_success":
                                    if (checkNum == 0) {
                                        Toast.makeText(JoinActivity.this, "사용가능한 아이디 입니다.", Toast.LENGTH_SHORT).show();
                                        checkId = true;
                                        et_join_id.setClickable(false);
                                        et_join_id.setEnabled(false);
                                        et_join_id.setFocusable(false);
                                        et_join_id.setFocusableInTouchMode(false);
                                        btn_chk_id.setClickable(false);
                                    } else if (checkNum == 1) {
                                        Toast.makeText(JoinActivity.this, "사용가능한 닉네임 입니다.", Toast.LENGTH_SHORT).show();
                                        checkNick = true;
                                        et_join_nick.setClickable(false);
                                        et_join_nick.setEnabled(false);
                                        et_join_nick.setFocusable(false);
                                        et_join_nick.setFocusableInTouchMode(false);
                                        btn_chk_nick.setClickable(false);
                                    }

                                    break;
                                case "chk_failure":
                                    Toast.makeText(JoinActivity.this, "중복입니다. 다시 입력해주세요", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }
                }

            } catch (JSONException e) {
                System.out.println("JSONException : " + e);
            }
        }
    }

    // 회원가입 처리
    private class joinConnection extends AsyncTask<Void, Void, String> { // 불러오기

        @Override
        protected String doInBackground(Void... params) {

            try {
                URL url = new URL(URLlink + "/android/member/join_member.php"); // 앨범 폴더의 dbname 폴더에 접근
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                if (conn != null) {
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setInstanceFollowRedirects(false); // 추가됨
                    conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");

                    StringBuffer buffer = new StringBuffer();
                    buffer.append("join_id").append("=").append(join_id).append("&");
                    buffer.append("join_pw").append("=").append(join_pw).append("&");
                    buffer.append("join_nick").append("=").append(join_nick);

                    OutputStreamWriter outStream = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                    PrintWriter writer = new PrintWriter(outStream);
                    writer.write(buffer.toString());
                    writer.flush();

                    // 보내기  &&  받기
                    //헤더 받는 부분

                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuilder builder = new StringBuilder();
                    String json;
                    while ((json = reader.readLine()) != null) {       // 서버에서 라인단위로 보내줄 것이므로 라인단위로 읽는다
                        builder.append(json + "\n");
                        System.out.println("json + " + json);
                    }

                    System.out.println("dd" + builder.toString().trim());

                    return builder.toString().trim();

                }

            } catch (final Exception e) {

                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject jsonObj = new JSONObject(result);
                jarray = jsonObj.getJSONArray("result");

                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject c = jarray.getJSONObject(i);

                    if (!c.isNull("error")) { // 우선 에러를 검출함
                        js_error = c.getString("error");

                        switch (js_error) {
                            case "01":
                                Toast.makeText(JoinActivity.this, "DB 연결에 실패하였습니다", Toast.LENGTH_SHORT).show();
                                break;
                        }

                    } else { // 에러가 없으면 진행
                        if (!c.isNull("result")) {
                            js_result = c.getString("result");

                            switch (js_result) {
                                case "success":
                                    Toast.makeText(JoinActivity.this, "가입성공", Toast.LENGTH_SHORT).show();
                                    // 가입 성공 시 로그인 화면으로
                                    finish();
                                    //Intent intent = new Intent(JoinActivity.this, LoginActivity.class);
                                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    //startActivity(intent);
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                    break;
                                case "already_id":
                                    Toast.makeText(JoinActivity.this, "이미 있는 아이디입니다", Toast.LENGTH_SHORT).show();
                                    break;
                                case "already_nick":
                                    Toast.makeText(JoinActivity.this, "이미 있는 닉네임입니다", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }
                }

            } catch (JSONException e) {
                System.out.println("JSONException : " + e);
            }
        }
    }

}

