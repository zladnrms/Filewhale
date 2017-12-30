package filewhalewebhard.defytech.wmqkem.filewhalewebhard.main;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.victor.loading.rotate.RotateLoading;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import filewhalewebhard.defytech.wmqkem.filewhalewebhard.etc.SearchResultActivity;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.App_test;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.App_uploadfile_ffmpegtest;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.etc.UserInfoActivity;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.R;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.SQLite.DownloadListSQLHelper;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.SQLite.LoginSQLHelper;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.filejob.FileUploadActivity;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.user.LoginActivity;

public class MainFragment extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TabLayout.OnTabSelectedListener ,GoogleApiClient.OnConnectionFailedListener{

    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL

    /*
     * PHP에서 받아온 JSON Array에 대한 처리
     */
    private JSONArray jarray = null;
    private String js_error = null;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Pager adapter;

    // SQLite
    LoginSQLHelper loginSqlHelper = null;

    // 로그인 성공 시 닉네임 저장
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private String userNick = null;

    // 프로필 변경 처리
    private int serverResponseCode = 0; // 파일을 업로드 하기 위한 변수 선언
    private static final int CHANGE_PROFILE_IMAGE = 0;
    private String imgPath = null;
    private String imgName = null;
    private ImageView iv_profile;
    public static Bitmap bm_profile; // 유저 프로필 전역변수, 어플 내 로그인 유저에 대한 프로필 사용은 이 Bitmap 변수 사용

    private RotateLoading rotateLoading;

    // Navigation Menu
    LinearLayout llayout_drawer_music, llayout_drawer_music_low, llayout_drawer_ani, llayout_drawer_ani_low;
    ImageView iv_music, iv_ani;
    boolean open_music = false, open_ani = false;

    // Navigation Button
    Button btn_notice, btn_version, btn_setting, btn_logout;

    // 구글 로그인
    private Boolean GOOGLELOGIN = false;
    private static final String GOOGLETAG = "구글 로그인 시도";
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 로그인 성공 시 닉네임 저장
        pref = getSharedPreferences("nickname", 0);
        editor = pref.edit();

        userNick = pref.getString("nick", "손님");

        //Tablayout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        // SQLite
        loginSqlHelper = new LoginSQLHelper(getApplicationContext(), "LoginData.db", null, 1);

        // TabLayout (ViewPager) Custom Icon 탭 추가하기
        View view1 = getLayoutInflater().inflate(R.layout.tab_custom, null);
        view1.findViewById(R.id.icon).setBackgroundResource(R.drawable.tab_home);
        View view2 = getLayoutInflater().inflate(R.layout.tab_custom, null);
        view2.findViewById(R.id.icon).setBackgroundResource(R.drawable.tab_download);
        View view3 = getLayoutInflater().inflate(R.layout.tab_custom, null);
        view3.findViewById(R.id.icon).setBackgroundResource(R.drawable.tab_upload);

        tabLayout.addTab(tabLayout.newTab().setCustomView(view1));
        tabLayout.addTab(tabLayout.newTab().setCustomView(view2));
        tabLayout.addTab(tabLayout.newTab().setCustomView(view3));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // ViewPager
        viewPager = (ViewPager) findViewById(R.id.main_viewpager);

        adapter = new Pager(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        viewPager.setAdapter(adapter);

        // TabSelectedListener 달기
        tabLayout.setOnTabSelectedListener(this);

        // FAB
        final com.getbase.floatingactionbutton.FloatingActionButton actionA = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab_download);
        actionA.setTitle("다운로드");
        actionA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainFragment.this, App_uploadfile_ffmpegtest.class);
                startActivity(intent);
            }
        });

        final com.getbase.floatingactionbutton.FloatingActionButton actionB = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab_upload);
        actionB.setTitle("업로드");
        actionB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainFragment.this, FileUploadActivity.class);
                startActivity(intent);
            }
        });

        // FAB Menu
        final FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.fab_menu);

        // DrawerLayout 처리
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Navigation DrawerHeader 부분 처리
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0); // Hedaer 객체 생성
        rotateLoading = (RotateLoading) hView.findViewById(R.id.rotateloading);
        TextView nav_user = (TextView) hView.findViewById(R.id.tv_drawerheader_nickname);
        nav_user.setText(userNick); // 사용자 닉네임 Set
        TextView nav_myinfo = (TextView) hView.findViewById(R.id.tv_drawerheader_myinfo);
        nav_myinfo.setPaintFlags(nav_myinfo.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        iv_profile = (ImageView) hView.findViewById(R.id.iv_drawerheader_profile);
        iv_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, CHANGE_PROFILE_IMAGE);
            }
        });
        navigationView.setNavigationItemSelectedListener(this);

        btn_notice = (Button) findViewById(R.id.btn_notice);
        btn_version = (Button) findViewById(R.id.btn_version);
        btn_setting = (Button) findViewById(R.id.btn_setting);
        btn_logout = (Button) findViewById(R.id.btn_logout);

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(GOOGLELOGIN) {
                    signOut();
                } else {
                    loginSqlHelper.delete(); // 자동 로그인 해제
                    Intent intent = new Intent(MainFragment.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        llayout_drawer_music = (LinearLayout) hView.findViewById(R.id.llayout_drawer_music);
        llayout_drawer_music_low = (LinearLayout) hView.findViewById(R.id.llayout_drawer_music_low);
        llayout_drawer_ani = (LinearLayout) hView.findViewById(R.id.llayout_drawer_ani);
        llayout_drawer_ani_low = (LinearLayout) hView.findViewById(R.id.llayout_drawer_ani_low);
        iv_music = (ImageView) hView.findViewById(R.id.iv_music);
        iv_ani = (ImageView) hView.findViewById(R.id.iv_ani);

        llayout_drawer_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(open_music) {
                    iv_music.setImageResource(R.drawable.ic_plus);
                    open_music = false;
                    llayout_drawer_music_low.setVisibility(View.GONE);
                } else {
                    iv_music.setImageResource(R.drawable.ic_minus);
                    open_music = true;
                    llayout_drawer_music_low.setVisibility(View.VISIBLE);
                }
            }
        });

        llayout_drawer_ani.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(open_ani) {
                    iv_ani.setImageResource(R.drawable.ic_plus);
                    open_ani = false;
                    llayout_drawer_ani_low.setVisibility(View.GONE);
                } else {
                    iv_ani.setImageResource(R.drawable.ic_minus);
                    open_ani = true;
                    llayout_drawer_ani_low.setVisibility(View.VISIBLE);
                }
            }
        });
        /*
         * 다운로드 페이지에서 메인으로 넘어와 다운로드를 실행하게 됨
         * 다운로드 내역으로 Fragment를 넘기며(setCurrentItem(1))
         * SQLite에 저장할 내역들을 받아오고, SQLite에 저장한다
         * 이후는 App_downlist의 DownloadFile에서 처리한다
         */
        // getIntent 내 데이터 유무에 따른 다운로드 처리
        Intent intent = getIntent();
        GOOGLELOGIN = intent.getBooleanExtra("GOOGLELOGIN", false);
        if (!(intent.getStringExtra("writer") == null)) {
            String dindex = intent.getStringExtra("fileindex");
            String dwriter = intent.getStringExtra("writer");
            String dsubject = intent.getStringExtra("filesubject");
            String dfilename = intent.getStringExtra("filename");
            String dfilecategory = intent.getStringExtra("filecategory");
            String dfilesize = intent.getStringExtra("filesize");

            if (dwriter.equals("null")) { // 데이터 없음
                // 그냥 넘어감
            } else { // 데이터 존재, 다운로드함
                // FileWhale 폴더 내에 저장 위해 폴더 체크
                String fileFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FileWhale";
                File file = new File(fileFolderPath);
                file.mkdir();// 디렉토리 없으면 생성, 있으면 통과

                String filepath = fileFolderPath + "/" + dfilename;
                Log.d("로그","sql 저장 경로 :" + filepath);
                DownloadListSQLHelper downloadListSQLHelper = new DownloadListSQLHelper(getApplicationContext(), "DownloadHistory.db", null, 1);
                downloadListSQLHelper.insert(dwriter, dsubject, dfilename, dfilecategory, filepath, dfilesize, "Nothing", dindex);

                viewPager.setCurrentItem(1);
            }
        }


        // 프로필 설정
        new getProfile().execute();

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
    }

    // BackPressed 시 DrawerLayout 처리
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // DrawerLayout 버튼 처리
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Intent intent = new Intent(MainFragment.this, UserInfoActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_apkinfo) {

        } else if (id == R.id.nav_cpptest) {
            Intent intent = new Intent(MainFragment.this, App_test.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            if(GOOGLELOGIN) {
                signOut();
            } else {
                loginSqlHelper.delete(); // 자동 로그인 해제
                Intent intent = new Intent(MainFragment.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.searchview, menu);


        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String word) {
                Intent intent = new Intent(MainFragment.this, SearchResultActivity.class);
                intent.putExtra("searchword", word);
                startActivity(intent);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String word) {
                return false;
            }
        });

        return true;
    }


    // ViewPager 처리
    public class Pager extends FragmentStatePagerAdapter {

        int tabCount;

        public Pager(android.support.v4.app.FragmentManager fm, int tabCount) {
            super(fm);
            this.tabCount = tabCount;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    HomeFragment homeTab = new HomeFragment();
                    return homeTab;
                case 1:
                    DownloadListFragment downlistTab = new DownloadListFragment();
                    return downlistTab;
                case 2:
                    UploadListFragment uplistTab = new UploadListFragment();
                    return uplistTab;
                default:
                    return null;
            }
        }

        public void destroyFragment(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return tabCount;
        }
    }

    // 이미지 가져오기 처리
    private class getProfile extends AsyncTask<Void, Void, Void> { // 불러오기

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            rotateLoading.start();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                String url = URLlink + "/android/filestorage/profile/" + "profile_" + userNick + ".png";

                bm_profile = Glide.
                        with(MainFragment.this).
                        load(url).
                        asBitmap().
                        diskCacheStrategy(DiskCacheStrategy.NONE).
                        skipMemoryCache(true).
                        error(R.drawable.ic_profile).
                        placeholder(R.drawable.ic_profile).
                        into(-1, -1).
                        get();

            } catch (final Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (rotateLoading.isStart()) {
                rotateLoading.stop();
            }

            iv_profile.setImageBitmap(bm_profile);

            if(bm_profile == null){
                iv_profile.setImageResource(R.drawable.ic_profile);
            }
        }
    }

    // 이미지 업로드 처리
    private class uploadImage extends AsyncTask<Void, String, String> {

        String fileName = imgName; // 서버에 올려질 파일의 이름 : 유저의 분류명_파일원래이름.확장자
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer; // 바이트 버퍼 저장
        int maxBufferSize = 1 * 10240 * 10240; // 최대 크기
        File sourceFile = new File(imgPath); // 업로드 할 파일 그 자체

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            rotateLoading.start();
        }

        @Override
        protected String doInBackground(Void... s) {

            if (!sourceFile.isFile()) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.i("WRITELOG", "[UploadImageToServer] Source File not exist :" + imgPath);
                    }
                });
                return null;
            } else {
                try {
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(URLlink + "/android/filestorage/profileupload.php");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    if (conn != null) {
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("uploaded_file", fileName); // 업로드 되는 파일명
                        Log.i("WRITELOG", "fileName: " + fileName);

                        dos = new DataOutputStream(conn.getOutputStream());

                        // 일반 정보 전송, 회원의 ID, PW ,DBname 을 먼저 전송한 뒤 파일 전송함
                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"nickname\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(URLEncoder.encode(userNick, "utf-8"));
                        dos.writeBytes(lineEnd);

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"filename\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(URLEncoder.encode(fileName, "utf-8"));
                        dos.writeBytes(lineEnd);

                        //위의 문장 실제 전송깂
                        //--*****
                        //Content-Disposition: form-data; name="foldername"
                        //
                        //
                        //newImage
                        //

                        // 이미지 전송
                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\"; filename=\"" + fileName + "\"" + lineEnd);
                        dos.writeBytes(lineEnd);

                        //위의 문장 실제 전송값
                        //--*****
                        //Content-Disposition: form-data; name="uploaded_file"; filename="파일경로"
                        //

                        // create a buffer of  maximum size
                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {
                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        }

                        // send multipart form data necesssary after file data...
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                        // boundary 뒤에 -- 이 붙음으로써 인자 나열이 끝낫음을 의미함. 뒤의 flush()호출로 전송 마무리

                        // Responses from the server (code and message)
                        serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn.getResponseMessage();

                        Log.i("WRITELOG", "[UploadImageToServer] HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

                        if (serverResponseCode == 200) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(MainFragment.this, "File Upload Completed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        //close the streams //
                        fileInputStream.close();
                        dos.flush();
                        dos.close();

                        // 전송 && echo 받기

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
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MainFragment.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.i("WRITELog", "[UploadImageToServer] error: " + ex.getMessage(), ex);
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MainFragment.this, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.i("WRITELOG", "[UploadImageToServer] Upload file to server Exception Exception : " + e.getMessage(), e);
                }
                Log.i("WRITELOG", "[UploadImageToServer] Finish");
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (rotateLoading.isStart()) {
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
                                Toast.makeText(MainFragment.this, "게시물 정보를 모두 채워주세요 (not_full_content)", Toast.LENGTH_SHORT).show();
                                break;
                            case "02":
                                Toast.makeText(MainFragment.this, "서버 오류입니다 (move_fail)", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    } else { // 에러가 없으면 진행
                        if (!c.isNull("result")) {
                            Toast.makeText(MainFragment.this, "프로필 사진이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                            iv_profile.setImageBitmap(bm_profile);  //배치해놓은 ImageView에 set
                        }
                    }
                }

            } catch (JSONException e) {
                System.out.println("JSONException : " + e);
            }
        }
    }

    // 프로필 변경
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case CHANGE_PROFILE_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    try {

                        Uri imgURI = data.getData();
                        imgPath = getPath(imgURI); // 이미지 절대경로 얻기 //
                        imgName = getName(imgURI); // 이미지 절대경로 얻기 //

                        bm_profile = BitmapFactory.decodeFile(imgPath);

                        new uploadImage().execute();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private String getPath(Uri uri) {

        Cursor c = getContentResolver().query(Uri.parse(uri.toString()), null, null, null, null);
        c.moveToNext();
        String absolutePath = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
        return absolutePath;
    }

    // 파일명 찾기
    private String getName(Uri uri) {
        Cursor c = getContentResolver().query(Uri.parse(uri.toString()), null, null, null, null);
        int column_index = c.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME);
        c.moveToNext();
        return c.getString(column_index);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Intent intent = new Intent(MainFragment.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(GOOGLETAG, "구글 로그인 연결 실패 :" + connectionResult);
    }
}
