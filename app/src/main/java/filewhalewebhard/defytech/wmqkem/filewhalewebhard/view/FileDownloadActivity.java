package filewhalewebhard.defytech.wmqkem.filewhalewebhard.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.mikhaellopez.circularimageview.CircularImageView;
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
import java.util.ArrayList;

import filewhalewebhard.defytech.wmqkem.filewhalewebhard.SQLite.DownloadListSQLHelper;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.R;

public class FileDownloadActivity extends AppCompatActivity {

    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL
    /*
         * PHP에서 받아온 JSON Array에 대한 처리
         */
    private JSONArray jarray = null, jarray_putcomment, jarray_getrecommendlist;
    private String str_getComment;
    private JSONObject obj;

    Button btn_see, btn_del;

    // 파일 _id 정보
    private String _id = null;

    // 파일 정보 객체 생성
    LinearLayout llayout_filecontent;
    CircularImageView iv_writer_profile;
    TextView tv_writer;
    TextView tv_filesubject;
    TextView tv_filecontent;
    TextView tv_filesize;
    TextView tv_date;
    ImageView iv_filethumbnail;

    // 다운로드 버튼
    Button btn_download;

    // 다운로드 시 Intent 전달 객체
    private String pindex = null;
    private String pwriter = null;
    private String pfilesubject = null;
    private String pfilename = null;
    private String pfilecategory = null;
    private String pfilecontent = null;
    private String pfilesize = null;

    // 댓글 / 평점 달 수 있는 회원 구분 위한 처리 (내역 有 -> 기능 사용 가능)
    DownloadListSQLHelper downloadListSQLHelper;
    ArrayList<String> str_downlist = new ArrayList<String>();
    Boolean alreadyget = false; // 다운로드 내역에 없을 경우 false, 있을 경우 true

    // 평점 상세 처리
    Boolean alreadyrcmd = false; // 평점을 이미 달았는지 달지 않았는지

    // 서버에서 받은 파일 정보
    ArrayList<ArticleInfo> ShowInfo = new ArrayList<ArticleInfo>();

    // 사용자 닉네임
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private String userNick = null;

    // 댓글 및 평점
    TextView tv_alert_comment;
    LinearLayout llayout_comment;
    CircularImageView iv_comment_profile;
    EditText et_comment;
    Button btn_comment;
    RotateLoading rotateLoading_inBtn;

    // 댓글 목록
    ScrollView scrollview;
    ListView lv_commentlist;
    CommentlistAdapter lv_adapter;
    ArrayList<CommentInfo> commentlist = new ArrayList<CommentInfo>();
    private boolean lv_lock = true;

    // 받아오는 Progress Loading
    LinearLayout llayout_progress;
    private RotateLoading rotateLoading;

    // 미리보기 처리
    String covername = "";
    String EngCategory;
    String notColumnFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_download);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_backarrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // SQLite
        downloadListSQLHelper = new DownloadListSQLHelper(getApplicationContext(), "DownloadHistory.db", null, 1);

        // 로그인 성공 시 닉네임 저장
        pref = getSharedPreferences("nickname", 0);
        editor = pref.edit();

        userNick = pref.getString("nick", "손님");

        llayout_filecontent = (LinearLayout) findViewById(R.id.llayout_filecontent);
        iv_writer_profile = (CircularImageView) findViewById(R.id.iv_writer_profile);
        tv_writer = (TextView) findViewById(R.id.tv_writer);
        tv_filesubject = (TextView) findViewById(R.id.tv_filesubject);
        tv_filecontent = (TextView) findViewById(R.id.tv_filecontent);
        tv_filesize = (TextView) findViewById(R.id.tv_filesize);
        tv_date = (TextView) findViewById(R.id.tv_date);
        iv_filethumbnail = (ImageView) findViewById(R.id.iv_filethumbnail);
        llayout_progress = (LinearLayout) findViewById(R.id.llayout_progress);
        btn_download = (Button) findViewById(R.id.btn_download);
        btn_see = (Button) findViewById(R.id.btn_see);
        btn_del = (Button) findViewById(R.id.btn_del);
        scrollview = (ScrollView) findViewById(R.id.scrollview);
        llayout_comment = (LinearLayout) findViewById(R.id.llayout_comment);
        tv_alert_comment = (TextView) findViewById(R.id.tv_alert_comment);
        et_comment = (EditText) findViewById(R.id.et_comment);
        btn_comment = (Button) findViewById(R.id.btn_comment);
        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);
        rotateLoading_inBtn = (RotateLoading) findViewById(R.id.rotateloading_inBtn);

        Intent intent = getIntent();
        _id = intent.getStringExtra("_id");

        new getArticle().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); // 파일 정보 가져오기

        btn_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (et_comment.getText().toString().equals("")) {
                    Toast.makeText(FileDownloadActivity.this, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    new putComment().execute();
                }
            }
        });

        // 댓글 보여주기
        lv_commentlist = (ListView) findViewById(R.id.lv_commentlist);
        lv_adapter = new CommentlistAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, commentlist); // 데이터
        ViewGroup footer = (ViewGroup) getLayoutInflater().inflate(R.layout.listview_footer, null);
        lv_commentlist.setAdapter(lv_adapter);
        lv_commentlist.setOnTouchListener(new View.OnTouchListener() { // 댓글 리스트를 스크롤할 때는 게시물 액티비티 스크롤 무시
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollview.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        // 게시물에 대한 회원 정보 확인 처리
        getDownlist(); // 다운로드 내역 가져오기
        new getRecommendList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); // 게시물의 추천 목록 가져와서 회원의 내역이 있나 확인

        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new putUserLog().execute();
            }
        });

        btn_see.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pfilecategory.matches("비디오_"+".*")) {
                    Intent intent = new Intent(FileDownloadActivity.this, VideoViewerActivity.class);
                    intent.putExtra("notcolumnfilename", notColumnFileName);
                    intent.putExtra("filecategory", EngCategory);
                    startActivity(intent);
                }
                else if(pfilecategory.matches("이미지_"+".*")){
                    Intent intent = new Intent(FileDownloadActivity.this, ImageViewerActivity.class);
                    intent.putExtra("filename", pfilename);
                    intent.putExtra("filecategory", EngCategory);
                    startActivity(intent);
                }
                else if(pfilecategory.matches("오디오_"+".*")){
                    Intent intent = new Intent(FileDownloadActivity.this, AudioViewerActivity.class);
                    intent.putExtra("filecategory", EngCategory);
                    intent.putExtra("filename", pfilename);
                    intent.putExtra("covername", covername);
                    startActivity(intent);
                }
                else if(pfilecategory.matches("기타_"+".*")){
                }
            }
        });

        btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FileDownloadActivity.this);
                // 팝업Dialog
                // Dialog 기본설정
                builder.setTitle("파일웨어");
                builder.setMessage("이 게시물을 삭제하시겠습니까?");

                builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        new deleteArticle().execute();
                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //
                    }
                });
                AlertDialog dialog = builder.create();    // 알림창 객체 생성=
                dialog.show();
            }
        });

        llayout_filecontent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FileDownloadActivity.this, FileDetailActivity.class);
                intent.putExtra("filecontent", pfilecontent);
                startActivity(intent);
            }
        });
    }

    // 파일 정보 받아옴 처리 시작 ----------------------------------------------------------------//

    // 검색
    private class getArticle extends AsyncTask<Void, Void, JSONArray> { // 불러오기

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            llayout_progress.setVisibility(View.VISIBLE);
            rotateLoading.start();
        }

        @Override
        protected JSONArray doInBackground(Void... params) {

            try {
                URL url = new URL(URLlink + "/android/filecnt/get_fileinfo.php"); // 앨범 폴더의 dbname 폴더에 접근
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
                    buffer.append("index").append("=").append(_id);

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
                    jarray = new JSONArray();
                    while ((json = reader.readLine()) != null) {       // 서버에서 라인단위로 보내줄 것이므로 라인단위로 읽는다
                        builder.append(json + "\n");
                        System.out.println("json + " + json);
                        if (!json.equals("")) {
                            JSONObject obj = new JSONObject(json);
                            jarray.put(obj);
                        }
                    }

                    return jarray;

                }

            } catch (final Exception e) {

                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray result) {

            if (rotateLoading.isStart()) {
                llayout_progress.setVisibility(View.GONE);
                rotateLoading.stop();
            }

            System.out.println("파일정보 받기 결과 " + result);


            try {
                if (jarray.length() == 0) {
                    Toast.makeText(FileDownloadActivity.this, "서버에 없는 파일 정보입니다. 목록을 갱신해주세요.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    for (int i = 0; i < jarray.length(); i++) {
                        JSONObject obj = jarray.getJSONObject(i);

                        String writer = null, _id = null, subject = null, filename = null, filecategory = null, date = null, filecontent = null;
                        long filesize = 0;

                        if (!obj.isNull("_id")) {
                            _id = obj.getString("_id");
                        }
                        if (!obj.isNull("writer")) {
                            writer = obj.getString("writer");
                            tv_writer.setText(writer);
                        }
                        if (!obj.isNull("subject")) {
                            subject = obj.getString("subject");
                        }
                        if (!obj.isNull("filecategory")) {
                            filecategory = obj.getString("filecategory");
                        }
                        if (!obj.isNull("filename")) {
                            filename = obj.getString("filename");

                            int columnCount = filename.indexOf('.'); // 맨 처음값의 위치를 찾음
                            notColumnFileName = filename.substring(0, columnCount); // 시작값만 주어지면 그 위치부터 끝까지 추출
                        }
                        if (!obj.isNull("covername")) {
                            covername = obj.getString("covername");
                        }
                        if (!obj.isNull("filecontent")) {
                            filecontent = obj.getString("filecontent");
                        }
                        if (!obj.isNull("date")) {
                            date = obj.getString("date");
                        }
                        if (!obj.isNull("filesize")) {
                            filesize = obj.getLong("filesize");
                        }
                        // -- 댓글 처리 -- //
                        if (!obj.isNull("comments")) {
                            str_getComment = obj.getString("comments");
                        }

                        ArticleInfo articleInfo = new ArticleInfo(_id, writer, subject, filename, filecategory, filecontent, filesize, date);
                        setInfo(articleInfo);

                        if (!writer.equals("")) {
                            new getWriterProfile().execute();
                        }

                        if (str_getComment != null) {
                            GetCommentList(str_getComment);
                        }
                    }
                }
            } catch (JSONException e) {
                System.out.println("JSONException : " + e);
            }
        }
    }

    // 이미지 가져오기 처리
    private class getWriterProfile extends AsyncTask<Void, Void, Void> { // 불러오기

        Bitmap bm_writer_profile;

        @Override
        protected Void doInBackground(Void... params) {

            try {
                String url = URLlink + "/android/filestorage/profile/" + "profile_" + pwriter + ".png";

                bm_writer_profile = Glide.
                        with(FileDownloadActivity.this).
                        load(url).
                        asBitmap().
                        diskCacheStrategy(DiskCacheStrategy.NONE).
                        skipMemoryCache(true).
                        error(R.drawable.ic_profile).
                        fallback(R.drawable.ic_profile).
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

            iv_writer_profile.setImageBitmap(bm_writer_profile);
        }
    }

    void setInfo(ArticleInfo mInfo) {
        pindex = mInfo.getId();
        pwriter = mInfo.getWriter();
        pfilesubject = mInfo.getSubject();
        pfilename = mInfo.getFilename();
        pfilecategory = mInfo.getFilecategory();
        pfilecontent = mInfo.getFilecontent();
        pfilesize = setfileUnit(mInfo.getFilesize());

        tv_filesubject.setText(pfilesubject);
        tv_filesubject.setSelected(true);
        tv_filesize.setText(pfilesize);
        tv_filecontent.setText(pfilecontent);
        tv_date.setText(mInfo.getDate());

        setCategoryKorToEng(pfilecategory);

        // 미리보기 썸네일
        if (pfilecategory.matches("비디오_"+".*")) {

            Glide.
                    with(FileDownloadActivity.this).
                    load(URLlink + "/HLS/thumbnail/" + EngCategory + "_" + pfilename + ".png").
                    fitCenter().
                    diskCacheStrategy(DiskCacheStrategy.NONE).
                    skipMemoryCache(true).
                    error(R.drawable.ic_profile).
                    placeholder(R.drawable.ic_profile).
                    into(iv_filethumbnail);
        } else if(pfilecategory.matches("오디오_"+".*")){
            if(!covername.equals("")) {

                Glide.
                        with(FileDownloadActivity.this).
                        load(URLlink + "/android/filestorage/a_coverimg/" + covername).
                        fitCenter().
                        diskCacheStrategy(DiskCacheStrategy.NONE).
                        skipMemoryCache(true).
                        error(R.drawable.ic_profile).
                        placeholder(R.drawable.ic_profile).
                        into(iv_filethumbnail);
            }
        } else if(pfilecategory.matches("이미지_"+".*")){
            btn_see.setText("확대보기");
            btn_see.setTextColor(Color.parseColor("#FFFFFF"));
            btn_see.setBackground(null);

            // GIF 파일 일 경우 대비
            GlideDrawableImageViewTarget ivTarget = new GlideDrawableImageViewTarget(iv_filethumbnail);
            Glide.
                    with(FileDownloadActivity.this).
                    load(URLlink + "/android/filestorage/" + EngCategory + "/" + pfilename).
                    fitCenter().
                    diskCacheStrategy(DiskCacheStrategy.NONE).
                    skipMemoryCache(true).
                    error(R.drawable.ic_profile).
                    placeholder(R.drawable.ic_profile).
                    into(ivTarget);
        } else if(pfilecategory.matches("기타_"+".*")){
            btn_see.setText("미리보기 지원하지 않음");
            btn_see.setTextColor(Color.parseColor("#FFFFFF"));
            btn_see.setBackground(null);

        }

        if(userNick.equals(pwriter)){
            btn_del.setVisibility(View.VISIBLE);
            tv_alert_comment.setVisibility(View.GONE);
            llayout_comment.setVisibility(View.VISIBLE);
        }
    }

    // 파일 용량 단위 변환
    private String setfileUnit(long fileSize) {

        String cutNumber = null;

        double fileSize_print = 0.0;
        cutNumber = fileSize + " Byte";

        if (fileSize > 1024) {
            double fileSize_KB = (double) fileSize / 1024;
            fileSize_print = fileSize_KB;

            cutNumber = String.format("%.1f", fileSize_print) + " KB";

            if (fileSize_KB > 1024.0) {
                double fileSize_MB = (double) fileSize_KB / 1024;
                fileSize_print = fileSize_MB;

                cutNumber = String.format("%.1f", fileSize_print) + " MB";

                if (fileSize_MB > 1024.0) {
                    double fileSize_GB = (double) fileSize_MB / 1024;
                    fileSize_print = fileSize_GB;

                    cutNumber = String.format("%.1f", fileSize_print) + " GB";
                }
            }
        }

        return cutNumber;
    }

    void setCategoryKorToEng(String Korcategory){
        switch (Korcategory) {
            case "비디오_영화": // 선택
                EngCategory = "v_movie";
                break;
            case "비디오_게임": // 영화
                EngCategory = "v_game";
                break;
            case "비디오_드라마": // 드라마
                EngCategory = "v_drama";
                break;
            case "비디오_애니메이션": // 애니
                EngCategory = "v_ani";
                break;
            case "비디오_기타": // 기타
                EngCategory = "v_etc";
                break;
            case "오디오_음악": // 기타
                EngCategory = "a_music";
                break;
            case "오디오_녹음": // 기타
                EngCategory = "a_record";
                break;
            case "이미지_만화": // 기타
                EngCategory = "i_comic";
                break;
            case "이미지_자연": // 기타
                EngCategory = "i_nature";
                break;
            case "이미지_게임": // 기타
                EngCategory = "i_game";
                break;
            case "이미지_영화": // 기타
                EngCategory = "i_movie";
                break;
            case "이미지_기타": // 기타
                EngCategory = "i_etc";
                break;
            case "기타_게임": // 기타
                EngCategory = "e_game";
                break;
            case "기타_유틸리티": // 기타
                EngCategory = "e_util";
                break;
            case "기타_어플": // 기타
                EngCategory = "e_app";
                break;
            case "기타_압축파일": // 기타
                EngCategory = "e_zip";
                break;
        }
    }

    class ArticleInfo { // 게시물 정보 클래스

        private String _id;
        private String writer;
        private String subject;
        private String filename;
        private String filecategory;
        private String filecontent;
        private long filesize;
        private String date;

        public ArticleInfo(String id, String _writer, String _subject, String _filename, String _filecategory, String _filecontent, long _filesize, String _date) {
            this._id = id;
            this.writer = _writer;
            this.subject = _subject;
            this.filename = _filename;
            this.filecategory = _filecategory;
            this.filecontent = _filecontent;
            this.filesize = _filesize;
            this.date = _date;
        }

        public String getId() {
            return _id;
        }

        public String getWriter() {
            return writer;
        }

        public String getSubject() {
            return subject;
        }

        public String getFilename() {
            return filename;
        }

        public String getFilecategory() {
            return filecategory;
        }

        public String getFilecontent() {
            return filecontent;
        }

        public long getFilesize() {
            return filesize;
        }

        public String getDate() {
            return date;
        }
    }

    // 게시물 삭제 처리 시작 -----------------------------------------------------------//
    private class deleteArticle extends AsyncTask<Void, Void, String> { // 불러오기

        @Override
        protected String doInBackground(Void... kinds) {

            try {
                URL url = new URL(URLlink + "/android/filecnt/del_article.php");

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
                    buffer.append("fileindex").append("=").append(_id);

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

            JSONArray jsonArray = new JSONArray();

            try {
                JSONObject jsonObj = new JSONObject(result);
                jsonArray = jsonObj.getJSONArray("result");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject c = jsonArray.getJSONObject(i);

                    if (!c.isNull("result")) {
                        if(c.getString("result").equals("success")){
                            Toast.makeText(FileDownloadActivity.this, "게시물을 삭제하셨습니다", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }

            } catch (JSONException e) {
                System.out.println("JSONException : " + e);
            }
        }
    }

    // 다운로드 시 LOG 보내기 처리 시작
    private class putUserLog extends AsyncTask<Void, Void, Void> { // 불러오기

        @Override
        protected Void doInBackground(Void... params) {

            try {
                URL url = new URL(URLlink + "/android/filecnt/put_userlog.php"); // 앨범 폴더의 dbname 폴더에 접근
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
                    buffer.append("nick").append("=").append(userNick).append("&");
                    buffer.append("filesubject").append("=").append(pfilesubject);

                    OutputStreamWriter outStream = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                    PrintWriter writer = new PrintWriter(outStream);
                    writer.write(buffer.toString());
                    writer.flush();

                    // 보내기  &&  받기
                    //헤더 받는 부분

                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    String json;
                    while ((json = reader.readLine()) != null) {       // 서버에서 라인단위로 보내줄 것이므로 라인단위로 읽는다
                        Log.d("putUserLog JSON", "JSON :" + json);
                    }

                    return null;

                }

            } catch (final Exception e) {

                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            Intent intent = new Intent(FileDownloadActivity.this, MainFragment.class);
            intent.putExtra("fileindex", pindex);
            intent.putExtra("writer", pwriter);
            intent.putExtra("filesubject", pfilesubject);
            intent.putExtra("filename", pfilename);
            intent.putExtra("filecategory", EngCategory);
            intent.putExtra("filesize", pfilesize);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    // 사용자 다운 내역 확인 처리 시작 ( 내역 有 -> 댓글 및 평점 가능 ), 작성자 일 시 처리 --------------//
    void getDownlist() {
        // SQLite에 저장되어 있던 업로드 내역 받아오기
        str_downlist = downloadListSQLHelper.getDownloadList(); // JSONArray로 받아옴

        try {
            if (str_downlist != null) { // 받아온 ArrayList<String> 이 비지 않았다면
                for (int i = 0; i < str_downlist.size(); i++) {
                    obj = new JSONObject(str_downlist.get(i)); // String -> JSONObject화

                    //내역에서 fileindex만 꺼내 현 다운로드 파일 게시물의 fileindex와 같은지 확인한다. 그래서 같은 것이 있으면 OK
                    String fileindex = null;

                    // JSON Key 값대로 꺼냄
                    if (!obj.isNull("fileindex")) {
                        fileindex = obj.getString("fileindex");

                        { // 받은 내역이 있는 파일 => 평점 달기 및 댓글 쓰기 가능
                        if (fileindex.equals(_id))
                            alreadyget = true;
                            tv_alert_comment.setVisibility(View.GONE);
                            llayout_comment.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        } catch (JSONException e) {

        }
    }

    // 게시물 평점 정보 가져오기 처리 시작 -----------------------------------------------------------//
    private class getRecommendList extends AsyncTask<Void, Void, Void> { // 불러오기

        @Override
        protected Void doInBackground(Void... kinds) {

            try {
                URL url = new URL(URLlink + "/android/filecnt/get_rcmdlist.php");

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
                    buffer.append("fileindex").append("=").append(_id).append("&");
                    buffer.append("userNick").append("=").append(userNick);

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
                    jarray_getrecommendlist = new JSONArray();
                    while ((json = reader.readLine()) != null) {       // 서버에서 라인단위로 보내줄 것이므로 라인단위로 읽는다
                        builder.append(json + "\n");
                        if (!json.equals("")) {
                            JSONObject obj = new JSONObject(json);
                            jarray_getrecommendlist.put(obj);
                        }
                    }

                    return null;
                }

            } catch (final Exception e) {

                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {

            if (jarray_getrecommendlist != null) {

                try {
                    for (int i = 0; i < jarray_getrecommendlist.length(); i++) {
                        JSONObject obj = jarray_getrecommendlist.getJSONObject(i);

                        if (!obj.isNull("userNick")) {
                            if (userNick.equals(obj.getString("userNick"))) {
                                alreadyrcmd = true;

                            }
                        }
                    }
                } catch (JSONException e) {
                    System.out.println("JSONException : " + e);
                }
            }
        }
    }

    // 댓글 달기 처리 시작 -----------------------------------------------------------//
    private class putComment extends AsyncTask<Void, Void, Void> { // 불러오기

        String comment = et_comment.getText().toString();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            btn_comment.setVisibility(View.INVISIBLE);
            rotateLoading_inBtn.start();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                URL url = new URL(URLlink + "/android/filecnt/put_comment.php"); // 앨범 폴더의 dbname 폴더에 접근
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
                    buffer.append("fileindex").append("=").append(_id).append("&");
                    buffer.append("writer").append("=").append(userNick).append("&");
                    buffer.append("comment").append("=").append(comment);

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
                    jarray_putcomment = new JSONArray();
                    while ((json = reader.readLine()) != null) {       // 서버에서 라인단위로 보내줄 것이므로 라인단위로 읽는다
                        builder.append(json + "\n");
                        System.out.println("json + " + json);
                        if (!json.equals("")) {
                            JSONObject obj = new JSONObject(json);
                            jarray_putcomment.put(obj);
                        }
                    }
                    return null;
                }

            } catch (final Exception e) {

                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {

            try {
                for (int i = 0; i < jarray_putcomment.length(); i++) {
                    JSONObject obj = jarray_putcomment.getJSONObject(i);

                    if (!obj.isNull("result")) {

                    }
                    if (!obj.isNull("error")) {

                    }
                }
            } catch (JSONException e) {
                System.out.println("JSONException : " + e);
            }

            refreshActivity(); // 댓글 리스트뷰 초기화 위함
            et_comment.setText("");

            btn_comment.setVisibility(View.VISIBLE);
            if (rotateLoading_inBtn.isStart()) {
                rotateLoading_inBtn.stop();
            }
        }
    }

    void refreshActivity() {
        commentlist.clear();
        new getArticle().execute(); // 파일 정보 가져오기
    }

    // 댓글 받아오기 처리 시작 ----------------------------------------------------------------//

    // 댓글 받아오기
    void GetCommentList(String commentList) {

        try {
            JSONArray jarray_getcomment = new JSONArray(commentList);

            for (int i = 0; i < jarray_getcomment.length(); i++) {
                JSONObject obj = jarray_getcomment.getJSONObject(i);

                String _id = null, writer = null, comment = null, date = null;

                if (!obj.isNull("_id")) {
                    _id = obj.getString("_id");
                }
                if (!obj.isNull("writer")) {
                    writer = obj.getString("writer");
                }
                if (!obj.isNull("comment")) {
                    comment = obj.getString("comment");
                }
                if (!obj.isNull("date")) {
                    date = obj.getString("date");
                }

                CommentInfo commentInfo = new CommentInfo(_id, writer, comment, date);
                commentlist.add(commentInfo); // 파일 정보 객체를 리스트뷰에 뿌릴 ArrayList에 넣음
                lv_adapter.notifyDataSetChanged(); // 정보가 바뀌었다는 것을 알림
                lv_lock = false;
            }

        } catch (JSONException e) {
            System.out.println("JSONException : " + e);
        }
    }

    // 코멘트리스트뷰 어댑터
    private class CommentlistAdapter extends ArrayAdapter<CommentInfo> {

        private ArrayList<CommentInfo> items;

        public CommentlistAdapter(Context context, int textViewResourceId, ArrayList<CommentInfo> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.listview_commentlist, null);

                ViewHolder holder = new ViewHolder();
                holder.llayout_commentlist = (LinearLayout) v.findViewById(R.id.llayout_commentlist);
                holder.iv_comment_writer_profile = (CircularImageView) v.findViewById(R.id.iv_comment_writer_profile);
                holder.item_rotateLoading = (RotateLoading) v.findViewById(R.id.item_rotateloading);
                holder.writer = (TextView) v.findViewById(R.id.tv_commentlist_writer);
                holder.comment = (TextView) v.findViewById(R.id.tv_commentlist_comment);

                v.setTag(holder);
            }

            CommentInfo f_info = items.get(position);
            if (f_info != null) {

                ViewHolder holder = (ViewHolder) v.getTag();

                if(holder.llayout_commentlist != null) {
                    if(f_info.getWriter().equals(pwriter)){
                        holder.llayout_commentlist.setBackgroundColor(Color.parseColor("#FAF4C0"));
                    } else {
                        holder.llayout_commentlist.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    }
                }
                if (holder.iv_comment_writer_profile != null) {
                    if(!f_info.getListed()){
                        new getProfile(f_info.getWriter(), holder.iv_comment_writer_profile, holder.item_rotateLoading).execute();
                        f_info.setListed(true);
                    }
                }
                if (holder.writer != null) {
                    holder.writer.setText(f_info.getWriter());
                }
                if (holder.comment != null) {
                    holder.comment.setText(f_info.getComment());
                }
            }

            return v;
        }
    }

    // 이미지 가져오기 처리
    private class getProfile extends AsyncTask<Void, Void, Void> { // 불러오기

        private String userNick = null;
        private CircularImageView iv_comment_writer_profile = null;
        private Bitmap bm_profile = null;
        private RotateLoading item_rotateLoading;

        private getProfile(String _userNick, CircularImageView _iv_writerprofile, RotateLoading _item_rotateLoading) {
            userNick = _userNick;
            iv_comment_writer_profile = _iv_writerprofile;
            item_rotateLoading = _item_rotateLoading;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            item_rotateLoading.start();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                String url = URLlink + "/android/filestorage/profile/" + "profile_" + userNick + ".png";

                bm_profile = Glide.
                        with(FileDownloadActivity.this).
                        load(url).
                        asBitmap().
                        diskCacheStrategy(DiskCacheStrategy.NONE).
                        skipMemoryCache(true).
                        error(R.drawable.ic_profile).
                        fallback(R.drawable.ic_profile).
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

            if (item_rotateLoading.isStart()) {
                item_rotateLoading.stop();
            }

            iv_comment_writer_profile.setImageBitmap(bm_profile);
        }
    }

    class CommentInfo { // 댓글물 정보 클래스

        private String _id;
        private String writer;
        private String comment;
        private String date;
        private Boolean listed = false;

        public CommentInfo(String id, String _writer, String _comment, String _date) {
            this._id = id;
            this.writer = _writer;
            this.comment = _comment;
            this.date = _date;
        }

        public String getId() {
            return _id;
        }

        public String getWriter() {
            return writer;
        }

        public String getComment() {
            return comment;
        }

        public String getDate() {
            return date;
        }

        public Boolean getListed(){
            return listed;
        }

        public void setListed(Boolean _bool){
            listed = _bool;
        }
    }

    static class ViewHolder {
        LinearLayout llayout_commentlist;
        CircularImageView iv_comment_writer_profile;
        RotateLoading item_rotateLoading;
        TextView writer;
        TextView comment;
    }
}
