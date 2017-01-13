package filewhalewebhard.defytech.wmqkem.filewhalewebhard.filejob;

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

import filewhalewebhard.defytech.wmqkem.filewhalewebhard.etc.App_filecontentdetail;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.main.App_main;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.SQLite.DownloadListSQLHelper;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.customview.GraphView;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.R;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.viewer.App_audioviewer;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.viewer.App_imgviewer;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.viewer.App_videoviewer;

public class App_downloadfile extends AppCompatActivity {

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
    TextView tv_filesubject;
    TextView tv_filecontent;
    TextView tv_filename;
    TextView tv_filecategory;
    TextView tv_filesize;
    TextView tv_state;
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
    TextView tv_alert_comment, tv_starnum, tv_rcmdcount;
    LinearLayout llayout_comment, llayout_starnum;
    CircularImageView iv_comment_profile;
    double inst_starNum = 0.0;
    ImageView iv_star1, iv_star2, iv_star3, iv_star4, iv_star5;
    private double rcmd_starNum = 0.0, rcmd_originstarNum;
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
        setContentView(R.layout.app_downloadfile);

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
        tv_filesubject = (TextView) findViewById(R.id.tv_filesubject);
        tv_filecontent = (TextView) findViewById(R.id.tv_filecontent);
        tv_filename = (TextView) findViewById(R.id.tv_filename);
        tv_filecategory = (TextView) findViewById(R.id.tv_filecategory);
        tv_filesize = (TextView) findViewById(R.id.tv_filesize);
        tv_state = (TextView) findViewById(R.id.tv_state);
        iv_filethumbnail = (ImageView) findViewById(R.id.iv_filethumbnail);
        llayout_progress = (LinearLayout) findViewById(R.id.llayout_progress);
        btn_download = (Button) findViewById(R.id.btn_download);
        btn_see = (Button) findViewById(R.id.btn_see);
        btn_del = (Button) findViewById(R.id.btn_del);
        scrollview = (ScrollView) findViewById(R.id.scrollview);
        llayout_comment = (LinearLayout) findViewById(R.id.llayout_comment);
        llayout_starnum = (LinearLayout) findViewById(R.id.llayout_starnum);
        tv_alert_comment = (TextView) findViewById(R.id.tv_alert_comment);
        iv_comment_profile = (CircularImageView) findViewById(R.id.iv_comment_profile);
        et_comment = (EditText) findViewById(R.id.et_comment);
        btn_comment = (Button) findViewById(R.id.btn_comment);
        tv_starnum = (TextView) findViewById(R.id.tv_starnum);
        tv_rcmdcount = (TextView) findViewById(R.id.tv_rcmdcount);
        iv_star1 = (ImageView) findViewById(R.id.iv_star_1);
        iv_star2 = (ImageView) findViewById(R.id.iv_star_2);
        iv_star3 = (ImageView) findViewById(R.id.iv_star_3);
        iv_star4 = (ImageView) findViewById(R.id.iv_star_4);
        iv_star5 = (ImageView) findViewById(R.id.iv_star_5);
        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);
        rotateLoading_inBtn = (RotateLoading) findViewById(R.id.rotateloading_inBtn);

        Intent intent = getIntent();
        _id = intent.getStringExtra("_id");

        new getArticle().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); // 파일 정보 가져오기

        // 댓글란 유저 프로필 설정
        if (App_main.bm_profile != null) {
            iv_comment_profile.setImageBitmap(App_main.bm_profile);
        }

        // 평점 매기기 부분
        iv_star1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(1.0);
            }
        });
        iv_star2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(2.0);
            }
        });
        iv_star3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(3.0);
            }
        });
        iv_star4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(4.0);
            }
        });
        iv_star5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(5.0);
            }
        });

        btn_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rcmd_starNum == 0.0 && !userNick.equals(pwriter)) {
                    Toast.makeText(App_downloadfile.this, "평점을 매기셔야 댓글을 남기실 수 있습니다.", Toast.LENGTH_SHORT).show();
                } else if (et_comment.getText().toString().equals("")) {
                    Toast.makeText(App_downloadfile.this, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    new putComment().execute();
                }
            }
        });

        // 댓글 보여주기
        lv_commentlist = (ListView) findViewById(R.id.lv_commentlist);
        lv_adapter = new CommentlistAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, commentlist); // 데이터
        ViewGroup footer = (ViewGroup) getLayoutInflater().inflate(R.layout.lv_footer, null);
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
        new getRecommendCount().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
                    Intent intent = new Intent(App_downloadfile.this, App_videoviewer.class);
                    intent.putExtra("notcolumnfilename", notColumnFileName);
                    intent.putExtra("filecategory", EngCategory);
                    startActivity(intent);
                }
                else if(pfilecategory.matches("이미지_"+".*")){
                    Intent intent = new Intent(App_downloadfile.this, App_imgviewer.class);
                    intent.putExtra("filename", pfilename);
                    intent.putExtra("filecategory", EngCategory);
                    startActivity(intent);
                }
                else if(pfilecategory.matches("오디오_"+".*")){
                    Intent intent = new Intent(App_downloadfile.this, App_audioviewer.class);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(App_downloadfile.this);
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
                Intent intent = new Intent(App_downloadfile.this, App_filecontentdetail.class);
                intent.putExtra("filecontent", pfilecontent);
                startActivity(intent);
            }
        });
    }

    // 평점 버튼 클릭 시 나타나는 Dialog
    void showDialog(final double starNum) {
        if(alreadyget){
            AlertDialog.Builder builder = new AlertDialog.Builder(App_downloadfile.this);
            // 팝업Dialog
            // Dialog 기본설정
            builder.setTitle("파일웨어");
            builder.setMessage("이대로 평가하시겠습니까?");

            builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    if (rcmd_starNum == 0.0) {
                        inst_starNum = starNum;
                        new putRecommend().execute("put"); // 평점 넣기
                    } else {
                        inst_starNum = starNum;
                        rcmd_originstarNum = rcmd_starNum; // 기존 평점을 저장
                        new putRecommend().execute("update"); // 평점 재설정
                    }

                }
            });
            builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            AlertDialog dialog = builder.create();    // 알림창 객체 생성=
            dialog.show();
        } else {
            Toast.makeText(App_downloadfile.this, "다운로드 내역이 있어야 평점이 가능합니다.", Toast.LENGTH_SHORT).show();
        }

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
                    Toast.makeText(App_downloadfile.this, "서버에 없는 파일 정보입니다. 목록을 갱신해주세요.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    for (int i = 0; i < jarray.length(); i++) {
                        JSONObject obj = jarray.getJSONObject(i);

                        String writer = null, _id = null, subject = null, filename = null, filecategory = null, state = null, filecontent = null;
                        double starNum = 0.0;
                        long filesize = 0;

                        if (!obj.isNull("_id")) {
                            _id = obj.getString("_id");
                        }
                        if (!obj.isNull("writer")) {
                            writer = obj.getString("writer");
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
                        if (!obj.isNull("starNum")) {
                            starNum = Double.valueOf(obj.getString("starNum"));
                            tv_starnum.setText(obj.getString("starNum"));
                        }
                        if (!obj.isNull("state")) {
                            state = obj.getString("state");
                        }
                        if (!obj.isNull("filesize")) {
                            filesize = obj.getLong("filesize");
                        }
                        // -- 댓글 처리 -- //
                        if (!obj.isNull("comments")) {
                            str_getComment = obj.getString("comments");
                        }

                        ArticleInfo articleInfo = new ArticleInfo(_id, writer, subject, filename, filecategory, filecontent, filesize, starNum, state);
                        setInfo(articleInfo);

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

    void setInfo(ArticleInfo mInfo) {
        pindex = mInfo.getId();
        pwriter = mInfo.getWriter();
        pfilesubject = mInfo.getSubject();
        pfilename = mInfo.getFilename();
        pfilecategory = mInfo.getFilecategory();
        pfilecontent = mInfo.getFilecontent();
        pfilesize = setfileUnit(mInfo.getFilesize());

        tv_filecategory.setText(pfilecategory);
        tv_filename.setText(pfilename);
        tv_filesubject.setText(pfilesubject);
        tv_filesubject.setSelected(true);
        tv_filesize.setText(pfilesize);
        tv_filecontent.setText(pfilecontent);
        tv_state.setText(mInfo.getState());

        setCategoryKorToEng(pfilecategory);

        // 미리보기 썸네일
        if (pfilecategory.matches("비디오_"+".*")) {
            btn_see.setText("영상 미리보기");

            Glide.
                    with(App_downloadfile.this).
                    load(URLlink + "/HLS/thumbnail/" + EngCategory + "_" + pfilename + ".png").
                    fitCenter().
                    diskCacheStrategy(DiskCacheStrategy.NONE).
                    skipMemoryCache(true).
                    error(R.drawable.ic_profile).
                    placeholder(R.drawable.ic_profile).
                    into(iv_filethumbnail);
        } else if(pfilecategory.matches("오디오_"+".*")){
            if(!covername.equals("")) {
                btn_see.setText("음악 미리듣기");

                Glide.
                        with(App_downloadfile.this).
                        load(URLlink + "/android/filestorage/a_coverimg/" + covername).
                        fitCenter().
                        diskCacheStrategy(DiskCacheStrategy.NONE).
                        skipMemoryCache(true).
                        error(R.drawable.ic_profile).
                        placeholder(R.drawable.ic_profile).
                        into(iv_filethumbnail);
            }
        } else if(pfilecategory.matches("이미지_"+".*")){
            btn_see.setText("사진 미리보기");

            // GIF 파일 일 경우 대비
            GlideDrawableImageViewTarget ivTarget = new GlideDrawableImageViewTarget(iv_filethumbnail);
            Glide.
                    with(App_downloadfile.this).
                    load(URLlink + "/android/filestorage/" + EngCategory + "/" + pfilename).
                    fitCenter().
                    diskCacheStrategy(DiskCacheStrategy.NONE).
                    skipMemoryCache(true).
                    error(R.drawable.ic_profile).
                    placeholder(R.drawable.ic_profile).
                    into(ivTarget);
        } else if(pfilecategory.matches("기타_"+".*")){
            btn_see.setText("미리보기 지원하지 않음");
        }

        if(userNick.equals(pwriter)){
            btn_del.setVisibility(View.VISIBLE);
            tv_alert_comment.setVisibility(View.GONE);
            llayout_starnum.setVisibility(View.GONE);
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
        private double starNum;
        private String state; // 업로드된 파일의 상태 (live, expire)

        public ArticleInfo(String id, String _writer, String _subject, String _filename, String _filecategory, String _filecontent, long _filesize, double _starNum, String _state) {
            this._id = id;
            this.writer = _writer;
            this.subject = _subject;
            this.filename = _filename;
            this.filecategory = _filecategory;
            this.filecontent = _filecontent;
            this.filesize = _filesize;
            this.starNum = _starNum;
            this.state = _state;
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

        public double getStarnum() {
            return starNum;
        }

        public String getState() {
            return state;
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
                            Toast.makeText(App_downloadfile.this, "게시물을 삭제하셨습니다", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(App_downloadfile.this, App_main.class);
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

                                if (!obj.isNull("starNum")) {
                                    System.out.println("평점 : " + obj.getString("starNum"));
                                    rcmd_starNum = Double.valueOf(obj.getString("starNum"));
                                    setStarView(rcmd_starNum);
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

    void setStarView(double starNum) { // 액티비티 평점View 세팅
        if (starNum == 1.0) {
            iv_star1.setImageResource(R.drawable.star_fill);
            iv_star2.setImageResource(R.drawable.star_blank);
            iv_star3.setImageResource(R.drawable.star_blank);
            iv_star4.setImageResource(R.drawable.star_blank);
            iv_star5.setImageResource(R.drawable.star_blank);
            rcmd_starNum = 1.0;
        } else if (starNum == 2.0) {
            iv_star1.setImageResource(R.drawable.star_fill);
            iv_star2.setImageResource(R.drawable.star_fill);
            iv_star3.setImageResource(R.drawable.star_blank);
            iv_star4.setImageResource(R.drawable.star_blank);
            iv_star5.setImageResource(R.drawable.star_blank);
            rcmd_starNum = 2.0;
        } else if (starNum == 3.0) {
            iv_star1.setImageResource(R.drawable.star_fill);
            iv_star2.setImageResource(R.drawable.star_fill);
            iv_star3.setImageResource(R.drawable.star_fill);
            iv_star4.setImageResource(R.drawable.star_blank);
            iv_star5.setImageResource(R.drawable.star_blank);
            rcmd_starNum = 3.0;
        } else if (starNum == 4.0) {
            iv_star1.setImageResource(R.drawable.star_fill);
            iv_star2.setImageResource(R.drawable.star_fill);
            iv_star3.setImageResource(R.drawable.star_fill);
            iv_star4.setImageResource(R.drawable.star_fill);
            iv_star5.setImageResource(R.drawable.star_blank);
            rcmd_starNum = 4.0;
        } else if (starNum == 5.0) {
            iv_star1.setImageResource(R.drawable.star_fill);
            iv_star2.setImageResource(R.drawable.star_fill);
            iv_star3.setImageResource(R.drawable.star_fill);
            iv_star4.setImageResource(R.drawable.star_fill);
            iv_star5.setImageResource(R.drawable.star_fill);
            rcmd_starNum = 5.0;
        }
    }

    // 게시물 평점 갯수 가져오기 처리 시작 -----------------------------------------------------------//
    private class getRecommendCount extends AsyncTask<Void, Void, String> { // 불러오기

        @Override
        protected String doInBackground(Void... kinds) {

            try {
                URL url = new URL(URLlink + "/android/filecnt/get_rcmdcount.php");

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

                    int total, countOne, countTwo, countThree, countFour, countFive;

                    if (!c.isNull("starNum")) {
                        tv_starnum.setText(c.getString("starNum"));
                    }

                    if (!c.isNull("total")) {
                        total = Integer.valueOf(c.getString("total"));
                        tv_rcmdcount.setText(c.getString("total"));

                        if (!c.isNull("one")) {
                            countOne = Integer.valueOf(c.getString("one"));
                            double part = countOne / (double) total;
                            GraphView graphView = graphView = (GraphView) findViewById(R.id.gv_one);
                            graphView.setColor("#FFA648");
                            graphView.setWidth(part);
                        }
                        if (!c.isNull("two")) {
                            countTwo = Integer.valueOf(c.getString("two"));
                            double part = countTwo / (double) total;
                            GraphView graphView = (GraphView) findViewById(R.id.gv_two);
                            graphView.setColor("#FFCD12");
                            graphView.setWidth(part);
                        }
                        if (!c.isNull("three")) {
                            countThree = Integer.valueOf(c.getString("three"));
                            double part = countThree / (double) total;
                            GraphView graphView = (GraphView) findViewById(R.id.gv_three);
                            graphView.setColor("#FFFC80");
                            graphView.setWidth(part);
                        }
                        if (!c.isNull("four")) {
                            countFour = Integer.valueOf(c.getString("four"));
                            double part = countFour / (double) total;
                            GraphView graphView = (GraphView) findViewById(R.id.gv_four);
                            graphView.setColor("#98F791");
                            graphView.setWidth(part);
                        }
                        if (!c.isNull("five")) {
                            countFive = Integer.valueOf(c.getString("five"));
                            double part = countFive / (double) total;
                            GraphView graphView = (GraphView) findViewById(R.id.gv_five);
                            graphView.setColor("#47C83E");
                            graphView.setWidth(part);
                        }
                    }
                }

            } catch (JSONException e) {
                System.out.println("JSONException : " + e);
            }
        }
    }

    // 평점 처리 시작 -----------------------------------------------------------//
    private class putRecommend extends AsyncTask<String, Void, String> { // 불러오기

        @Override
        protected String doInBackground(String... kinds) {

            try {
                URL url = null;

                if (kinds[0].equals("put")) {
                    url = new URL(URLlink + "/android/filecnt/put_recommend.php"); // 앨범 폴더의 dbname 폴더에 접근
                } else if (kinds[0].equals("update")) {
                    url = new URL(URLlink + "/android/filecnt/update_recommend.php"); // 앨범 폴더의 dbname 폴더에 접근
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
                    buffer.append("fileindex").append("=").append(_id).append("&");
                    buffer.append("userNick").append("=").append(userNick).append("&");
                    if (kinds[0].equals("update")) {
                        buffer.append("originstarNum").append("=").append(rcmd_originstarNum).append("&");
                    }
                    buffer.append("starNum").append("=").append(inst_starNum);

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
                        System.out.println(json);
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

            JSONArray jsonArray;

            try {
                JSONObject jsonObj = new JSONObject(result);
                jsonArray = jsonObj.getJSONArray("result");

                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject c = jsonArray.getJSONObject(i);

                    if (!c.isNull("error")) {
                        String error = c.getString("error");

                        switch (error) {
                            case "01":
                                Toast.makeText(App_downloadfile.this, "모든 값이 전달 되지 않음(Error : 01)", Toast.LENGTH_SHORT).show();
                                break;
                            case "02":
                                Toast.makeText(App_downloadfile.this, "평점을 매기신지 24시간이 되지 않았습니다", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    } else {
                        setStarView(inst_starNum);
                        new getRecommendCount().execute();
                    }
                }

            } catch (JSONException e) {
                System.out.println("JSONException : " + e);
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

    // 게시물 받아오기
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
                v = vi.inflate(R.layout.lv_commentlist, null);

                ViewHolder holder = new ViewHolder();
                holder.llayout_commentlist = (LinearLayout) v.findViewById(R.id.llayout_commentlist);
                holder.writerprofile = (CircularImageView) v.findViewById(R.id.iv_writerprofile);
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
                if (holder.writerprofile != null) {
                    if(!f_info.getListed()){
                        new getProfile(f_info.getWriter(), holder.writerprofile, holder.item_rotateLoading).execute();
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
        private CircularImageView iv_writerprofile = null;
        private Bitmap bm_profile = null;
        private RotateLoading item_rotateLoading;

        private getProfile(String _userNick, CircularImageView _iv_writerprofile, RotateLoading _item_rotateLoading) {
            userNick = _userNick;
            iv_writerprofile = _iv_writerprofile;
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
                        with(App_downloadfile.this).
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

            iv_writerprofile.setImageBitmap(bm_profile);
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
        CircularImageView writerprofile;
        RotateLoading item_rotateLoading;
        TextView writer;
        TextView comment;
    }
}
