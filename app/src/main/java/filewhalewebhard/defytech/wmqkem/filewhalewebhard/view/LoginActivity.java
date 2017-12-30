package filewhalewebhard.defytech.wmqkem.filewhalewebhard.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.victor.loading.rotate.RotateLoading;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import filewhalewebhard.defytech.wmqkem.filewhalewebhard.SQLite.LoginSQLHelper;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.R;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL

    /*
     * PHP에서 받아온 JSON Array에 대한 처리
     */
    private JSONArray jarray = null;
    private String js_error = null;
    private String js_result = null;
    private String js_nick = null;
    private String js_md5pw = null;

    // 로그인 버튼
    Button btn_login;
    Button btn_join;

    // 아이디 패스워드
    ImageView iv_id, iv_pw;
    EditText et_login_id, et_login_pw;
    String login_id, login_pw;

    // 초반 인트로
    ImageView iv_whale;
    LinearLayout llayout_login;
    Animation anim_login_whale;
    Animation anim_login_llayout;

    // 로그인 성공 시 닉네임 저장
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    /*
     * 자동 로그인 (한번 로그인 시 로그아웃 전까지 어플 진입 시 자동로그인 됨) 기능 처리
     */
    LoginSQLHelper loginSqlHelper = null;
    Boolean autoLogin = false; // 자동 로그인인지 아닌지 여부

    // 로그인 Progress
    private RotateLoading rotateLoading;

    // 구글 로그인
    private String googleId, googleEmail;
    private static final String GOOGLETAG = "구글 로그인 시도";
    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 로그인 시 회원정보 저장 DB
        loginSqlHelper = new LoginSQLHelper(getApplicationContext(), "LoginData.db", null, 1);

        // 로그인 성공 시 닉네임 저장
        pref = getSharedPreferences("nickname", 0);
        editor = pref.edit();

        btn_login = (Button) findViewById(R.id.btn_login);
        btn_join = (Button) findViewById(R.id.btn_join);
        iv_id = (ImageView) findViewById(R.id.iv_id);
        iv_pw = (ImageView) findViewById(R.id.iv_pw);
        et_login_id = (EditText) findViewById(R.id.et_login_id);
        et_login_pw = (EditText) findViewById(R.id.et_login_pw);
        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);
        iv_whale = (ImageView) findViewById(R.id.iv_whale);
        llayout_login = (LinearLayout) findViewById(R.id.llayout_login);
        anim_login_whale = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_login_whale);
        anim_login_llayout = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_login_llayout);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et_login_id.getText().toString().equals("") && !et_login_pw.getText().toString().equals("")) {
                    login_id = et_login_id.getText().toString();
                    login_pw = et_login_pw.getText().toString();

                    new loginConnection().execute("");
                } else {
                    Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 모두 입력해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        // 인트로 애니메이션
        introAnim();

        // 자동 로그인 ( 한번 로그인 시 다음부터는 자동 ), 인트로 0.6초간 보여준 뒤 실행
        if (loginSqlHelper.chkIdForAuto() != null) { // 저장된 회원 정보가 있을 경우
            login_id = loginSqlHelper.chkIdForAuto();
            login_pw = loginSqlHelper.getPwForAuto();
            autoLogin = true;
            new loginConnection().execute("");
        }

        et_login_id.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View arg0, boolean gotfocus) {
                // TODO Auto-generated method stub
                if (gotfocus) {
                    iv_id.setVisibility(View.GONE);
                } else if (!gotfocus) {
                    if (et_login_id.getText().length() == 0)
                        iv_id.setVisibility(View.VISIBLE);
                }
            }
        });

        et_login_pw.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View arg0, boolean gotfocus) {
                // TODO Auto-generated method stub
                if (gotfocus) {
                    iv_pw.setVisibility(View.GONE);
                } else if (!gotfocus) {
                    if (et_login_pw.getText().length() == 0)
                        iv_pw.setVisibility(View.VISIBLE);
                }
            }
        });

        /*
         * 구글 로그인 처리
         */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton btn_googleLogin = (SignInButton) findViewById(R.id.btn_googleLogin);
        btn_googleLogin.setSize(SignInButton.SIZE_WIDE);
        btn_googleLogin.setScopes(gso.getScopeArray());
        btn_googleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    void introAnim() {
        iv_whale.setAnimation(anim_login_whale);
        llayout_login.setAnimation(anim_login_llayout);
    }

    // 로그인 처리
    private class loginConnection extends AsyncTask<String, Void, String> { // 불러오기

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            btn_login.setVisibility(View.GONE);
            rotateLoading.start();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                URL url = new URL(URLlink + "/android/member/login_member.php"); // 로그인 php 파일에 접근
                if (autoLogin) { // 자동 로그인 시
                    url = new URL(URLlink + "/android/member/login_auto_member.php"); // 자동 로그인 php 파일에 접근
                }
                if (params[0].equals("GOOGLELOGIN")) {
                    url = new URL(URLlink + "/android/member/login_googlemember.php"); // 구글 로그인 php 파일에 접근
                }
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
                    if (params[0].equals("GOOGLELOGIN")) {
                        buffer.append("googleId").append("=").append(googleId).append("&");
                        buffer.append("googleEmail").append("=").append(googleEmail);
                    } else {
                        buffer.append("id").append("=").append(login_id).append("&");
                        buffer.append("pw").append("=").append(login_pw);
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

            if (rotateLoading.isStart()) {
                btn_login.setVisibility(View.VISIBLE);
                rotateLoading.stop();
            }

            try {
                JSONObject jsonObj = new JSONObject(result);
                jarray = jsonObj.getJSONArray("result");

                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject c = jarray.getJSONObject(i);

                    if (!c.isNull("error")) { // 우선 에러를 검출함
                        js_error = c.getString("error");

                        switch (js_error) {
                            case "01":
                                Toast.makeText(LoginActivity.this, "DB 연결에 실패하였습니다", Toast.LENGTH_SHORT).show();
                                break;
                            case "02":
                                Toast.makeText(LoginActivity.this, "서버 오류입니다 (date_fail)", Toast.LENGTH_SHORT).show();
                                break;
                        }

                    } else { // 에러가 없으면 진행
                        if (!c.isNull("result")) {
                            js_result = c.getString("result");

                            switch (js_result) {
                                case "miss_id":
                                    Toast.makeText(LoginActivity.this, "가입되어 있지 않은 아이디입니다", Toast.LENGTH_SHORT).show();
                                    break;
                                case "miss_pw":
                                    Toast.makeText(LoginActivity.this, "비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show();
                                    if (!c.isNull("nick")) {
                                        js_nick = c.getString("nick");
                                        Toast.makeText(LoginActivity.this, js_nick, Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case "success":
                                    if (!c.isNull("nick")) {
                                        js_nick = c.getString("nick");
                                        Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                                        // php에서 받아온 pw의 md5 암호화값
                                        if (!c.isNull("md5pw") && !autoLogin) { // 자동 로그인이 아닐 경우
                                            js_md5pw = c.getString("md5pw");
                                            loginSqlHelper.insert(login_id, js_md5pw);
                                        }
                                        //닉네임 저장
                                        editor.putString("nick", js_nick);
                                        editor.commit();
                                        //닉네임 저장 //
                                        Intent intent = new Intent(LoginActivity.this, MainFragment.class);
                                        startActivity(intent);
                                        finish();
                                    } else { // 받아온 nick이 null이라 일어나는 문제
                                        Toast.makeText(LoginActivity.this, "로그인 실패 (네트워크 문제)", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case "google_success":
                                    if (!c.isNull("nick")) {
                                        js_nick = c.getString("nick");
                                        Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                                        //닉네임 저장
                                        editor.putString("nick", js_nick);
                                        editor.commit();
                                        Intent intent = new Intent(LoginActivity.this, MainFragment.class);
                                        intent.putExtra("GOOGLELOGIN", true);
                                        startActivity(intent);
                                        finish();
                                    } else { // 받아온 nick이 null이라 일어나는 문제
                                        Toast.makeText(LoginActivity.this, "로그인 실패 (네트워크 문제)", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case "no_exist_google_id":
                                    Toast.makeText(LoginActivity.this, "(필수) 사용하실 닉네임을 설정해주세요!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, GoogleNicknameActivity.class);
                                    intent.putExtra("googleId", googleId);
                                    intent.putExtra("googleEmail", googleEmail);
                                    startActivity(intent);
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                    finish();
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

    /*
     * 구글 로그인 연동
     */

    @Override
    protected void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            Log.d(GOOGLETAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    // 구글 API에서 정보 받아와서 handleSignInResult로 결과내기
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    // 구글 로그인 결과 ( 로그인 성공 또는 실패 )
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(GOOGLETAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();

            Log.d(GOOGLETAG, "구글 계정 Name :" + acct.getDisplayName());
            Log.d(GOOGLETAG, "구글 계정 getId" + acct.getId());
            Log.d(GOOGLETAG, "구글 계정 getIdToken" + acct.getIdToken());
            Log.d(GOOGLETAG, "구글 계정 getEmail" + acct.getEmail());

            googleEmail = acct.getEmail();
            googleId = acct.getId();

            new loginConnection().execute("GOOGLELOGIN");
        } else {
            Log.d(GOOGLETAG, "구글 로그인 연결 정보 없음");
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(GOOGLETAG, "구글 로그인 연결 실패 :" + connectionResult);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.gllogin_loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }
}
