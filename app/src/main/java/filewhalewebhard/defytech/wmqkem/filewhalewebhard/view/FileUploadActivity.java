package filewhalewebhard.defytech.wmqkem.filewhalewebhard.view;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.victor.loading.rotate.RotateLoading;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import filewhalewebhard.defytech.wmqkem.filewhalewebhard.customview.Encodingsetting_custom;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.R;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.Thumbnailsetting_ffmpeg;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.SQLite.UploadListSQLHelper;

public class FileUploadActivity extends AppCompatActivity {

    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL
    /*
     * 현재 진행중 : 참고 사이트
     * http://gogorchg.tistory.com/entry/Android-ACTIONPICK-%EC%82%AC%EC%9A%A9-%EC%9D%B4%EB%AF%B8%EC%A7%80-%EA%B0%80%EC%A0%B8%EC%98%A4%EA%B8%B0
     * http://codewalkerster.blogspot.kr/2015/08/android-file-chooser-and-uri-to-file.html
     */

    /*
     * PHP에서 받아온 JSON Array에 대한 처리
     */
    private JSONArray jarray = null;
    private String js_error = null;
    private String js_result = null;
    private String js_id = null;
    private String js_m3u8name = null;

    // 업로드 시 업로드 내역 저장
    UploadListSQLHelper uploadListSQLHelper = null;

    // 게시물 객체
    Button btn_fileselect, btn_submit, btn_videoencodesetting, btn_thumbnailsetting;
    EditText et_upload_subject, et_upload_content;
    Spinner sp_category_major, sp_category_minor;
    ArrayAdapter sp_adapter_major, sp_adapter_minor;

    // 파일 정보 객체
    TextView tv_filepath, tv_filename, tv_filesize;
    ImageView iv_thumbnail; // 파일 썸네일 이미지

    // 파일 업로드 출력 정보
    private String writer = null; // 작성자
    private String fileSubject = null; // 게시물 제목
    private String fileContent = null; // 파일 설명
    private String filePath = ""; // 파일 경로
    private String fileCategory = "select"; // 파일 카테고리

    private ArrayList<String> ctgr_major_video = new ArrayList<String>(Arrays.asList("선택", "영화", "드라마", "애니", "기타"));
    private ArrayList<String> ctgr_major_image = new ArrayList<String>(Arrays.asList("선택", "만화", "자연", "게임", "영화", "기타"));
    private ArrayList<String> ctgr_major_audio = new ArrayList<String>(Arrays.asList("선택", "음악", "녹음", "기타"));
    private ArrayList<String> ctgr_major_etcfile = new ArrayList<String>(Arrays.asList("선택", "게임", "유틸", "어플", "압축파일"));
    private ArrayList<String> Category = new ArrayList<String>(Arrays.asList("파일 선택"));

    // 선택 파일 종류 (Thumbnail, 분류 설정 용도)
    private String fileType = null;

    // 파일 업로드 전송 필요 정보 (서버에 전송용)
    private int serverResponseCode = 0; // 파일을 업로드 하기 위한 변수 선언
    private String fileName = ""; // 파일 이름
    private long fileSize = 0; // 파일 용량 (byte)

    // 인코딩 시간 설정
    long hours, minutes, seconds;

    // 영상 인코딩 설정 (미리보기, 썸네일)
    String v_dateType_encode_start = "00:00:00", v_dateType_encode_end = "00:00:10"; // 미설정 시 0 ~ 10초를 미리보여주기
    String v_dateType_thumbnail = "00:00:01"; // 미설정 시 1초의 컷을 썸네일로 설정

    // 음악 커버 이미지 파일 ( JPEG 변환 위한 Bitmap 변수 )
    Bitmap a_coverBitmap = null;
    String coverPath = "";

    // 닉네임 받아오기
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    // 업로드 시 Progress 출력
    fileUpload fileupload;
    LinearLayout llayout_progress;
    private RotateLoading rotateLoading;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_upload);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_backarrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 닉네임 받아오기용
        pref = getSharedPreferences("nickname", 0);
        editor = pref.edit();

        writer = pref.getString("nick", "사용자");

        btn_fileselect = (Button) findViewById(R.id.btn_fileselect);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        tv_filename = (TextView) findViewById(R.id.tv_filename);
        tv_filepath = (TextView) findViewById(R.id.tv_filepath);
        tv_filesize = (TextView) findViewById(R.id.tv_filesize);
        iv_thumbnail = (ImageView) findViewById(R.id.iv_thumbnail);
        btn_videoencodesetting = (Button) findViewById(R.id.btn_videoencodesetting);
        btn_thumbnailsetting = (Button) findViewById(R.id.btn_thumbnailsetting);
        et_upload_subject = (EditText) findViewById(R.id.et_upload_subject);
        et_upload_content = (EditText) findViewById(R.id.et_upload_content);
        sp_category_major = (Spinner) findViewById(R.id.sp_category_major);
        sp_category_minor = (Spinner) findViewById(R.id.sp_category_minor);
        llayout_progress = (LinearLayout) findViewById(R.id.llayout_progress);
        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);

        // SQLite
        uploadListSQLHelper = new UploadListSQLHelper(getApplicationContext(), "UploadHistory.db", null, 1);

        // 대분류 설정
        sp_adapter_major = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, Category); // 데이터
        sp_adapter_major.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_category_major.setAdapter(sp_adapter_major);
        sp_category_major.setClickable(false);
        sp_category_major.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (fileType != null)
                    switch (fileType) {
                        case "video":
                            switch (position) {
                                case 0: // 선택
                                    fileCategory = "select";
                                    break;
                                case 1: // 영화
                                    fileCategory = "v_movie";
                                    break;
                                case 2: // 드라마
                                    fileCategory = "v_drama";
                                    break;
                                case 3: // 애니
                                    fileCategory = "v_ani";
                                    break;
                                case 4: // 기타
                                    fileCategory = "v_etc";
                                    break;
                            }
                            break;
                        case "audio":
                            switch (position) {
                                case 0: // 선택
                                    fileCategory = "select";
                                    break;
                                case 1: // 음악
                                    fileCategory = "a_music";
                                    break;
                                case 2: // 녹음
                                    fileCategory = "a_record";
                                    break;
                                case 3: // 기타
                                    fileCategory = "a_etc";
                                    break;
                            }
                            break;
                        case "image":
                            switch (position) {
                                case 0: // 선택
                                    fileCategory = "select";
                                    break;
                                case 1: // 만화
                                    fileCategory = "i_comic";
                                    break;
                                case 2: // 자연
                                    fileCategory = "i_nature";
                                    break;
                                case 3: // 게임
                                    fileCategory = "i_game";
                                    break;
                                case 4: // 영화
                                    fileCategory = "i_movie";
                                    break;
                                case 5: // 기타
                                    fileCategory = "i_etc";
                                    break;
                            }
                            break;
                        case "etcfile":
                            switch (position) {
                                case 0: // 선택
                                    fileCategory = "select";
                                    break;
                                case 1: // 게임
                                    fileCategory = "e_game";
                                    break;
                                case 2: // 유틸
                                    fileCategory = "e_util";
                                    break;
                                case 3: // 어플
                                    fileCategory = "e_app";
                                    break;
                                case 4: // 압축파일
                                    fileCategory = "e_zip";
                                    break;
                            }
                            break;
                    }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // 대분류 처리 //

        btn_fileselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final CharSequence[] items = {"이미지", "동영상", "오디오", "기타 파일"};

                AlertDialog.Builder builder = new AlertDialog.Builder(FileUploadActivity.this);

                builder.setTitle("업로드할 파일 종류")        // 제목 설정
                        .setItems(items, new DialogInterface.OnClickListener() {    // 목록 클릭시 설정
                            public void onClick(DialogInterface dialog, int index) {

                                switch (index) {
                                    case 0: // 이미지
                                        fileType = "image";
                                        startActivityForResult(
                                                Intent.createChooser(
                                                        new Intent(Intent.ACTION_GET_CONTENT)
                                                                .setType("image/*"), "이미지 파일 선택"),
                                                0);
                                        break;
                                    case 1: // 동영상
                                        fileType = "video";
                                        startActivityForResult(
                                                Intent.createChooser(
                                                        new Intent(Intent.ACTION_GET_CONTENT)
                                                                .setType("video/*"), "비디오 파일 선택"),
                                                0);
                                        break;
                                    case 2: // 오디오
                                        fileType = "audio";
                                        startActivityForResult(
                                                Intent.createChooser(
                                                        new Intent(Intent.ACTION_GET_CONTENT)
                                                                .setType("audio/*"), "오디오 파일 선택"),
                                                0);
                                        break;
                                    case 3: // 기타 파일
                                        fileType = "etcfile";
                                        try {
                                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                            intent.setType("application/zip");
                                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                                            try {
                                                startActivityForResult(
                                                        Intent.createChooser(intent, "기타 파일 선택"), 0);
                                            } catch (android.content.ActivityNotFoundException ex) {
                                                // Potentially direct the user to the Market with a Dialog
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        break;

                                }
                            }
                        });

                AlertDialog dialog = builder.create();    // 알림창 객체 생성
                dialog.show();    // 알림창 띄우기
            }
        });

        iv_thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fileName.equals("") && !filePath.equals(""))
                    showFile();
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et_upload_subject.getText().toString().equals("") && !et_upload_content.getText().toString().equals("") && !fileName.equals("") && !filePath.equals("") && !fileCategory.equals("select")) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(FileUploadActivity.this);
                    // 팝업Dialog
                    // Dialog 기본설정
                    builder.setTitle("파일 업로드");
                    builder.setMessage("이대로 업로드 하시겠습니까?");

                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            fileSubject = et_upload_subject.getText().toString();
                            fileContent = et_upload_content.getText().toString();
                            if(fileType.equals("audio")){
                                if(a_coverBitmap != null){
                                    saveBitmapToJpeg(getApplicationContext(), a_coverBitmap, fileName);
                                }
                                new audiofileUpload().execute();
                            } else {
                                fileupload = new fileUpload();
                                fileupload.execute();
                            }
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    });

                    AlertDialog dialog = builder.create();    // 알림창 객체 생성
                    dialog.show();

                } else {
                    Toast.makeText(FileUploadActivity.this, "모든 업로드 정보를 설정해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_videoencodesetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Encodingsetting_custom customdialog = new Encodingsetting_custom(FileUploadActivity.this);

                customdialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        customdialog.getFileLength(hours, minutes, seconds);
                        customdialog.setPath(filePath);
                    }
                });
                customdialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        int[] time = new int[6];
                        time = customdialog.getTime();

                        // 00:01:00 ~ 00:01:05

                        String hours_start, hours_end, minutes_start, minutes_end, seconds_start, seconds_end;
                        if (time[0] >= 10) {
                            hours_start = String.valueOf(time[0]);
                        } else {
                            hours_start = "0" + String.valueOf(time[0]);
                        }
                        if (time[1] >= 10) {
                            hours_end = String.valueOf(time[1]);
                        } else {
                            hours_end = "0" + String.valueOf(time[1]);
                        }
                        if (time[2] >= 10) {
                            minutes_start = String.valueOf(time[2]);
                        } else {
                            minutes_start = "0" + String.valueOf(time[2]);
                        }
                        if (time[3] >= 10) {
                            minutes_end = String.valueOf(time[3]);
                        } else {
                            minutes_end = "0" + String.valueOf(time[3]);
                        }
                        if (time[4] >= 10) {
                            seconds_start = String.valueOf(time[4]);
                        } else {
                            seconds_start = "0" + String.valueOf(time[4]);
                        }
                        if (time[5] >= 10) {
                            seconds_end = String.valueOf(time[5]);
                        } else {
                            seconds_end = "0" + String.valueOf(time[5]);
                        }

                        v_dateType_encode_start = hours_start + ":" + minutes_start + ":" + seconds_start;
                        v_dateType_encode_end = hours_end + ":" + minutes_end + ":" + seconds_end;
                    }
                });
                customdialog.show();
            }
        });

        btn_thumbnailsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Thumbnailsetting_ffmpeg customdialog = new Thumbnailsetting_ffmpeg(FileUploadActivity.this);

                customdialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        customdialog.getFileLength(hours, minutes, seconds);
                        customdialog.setPath(filePath);
                    }
                });
                customdialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        int[] time = new int[3];
                        time = customdialog.getTime();

                        String hours, minutes, seconds;
                        if (time[0] >= 10) {
                            hours = String.valueOf(time[0]);
                        } else {
                            hours = "0" + String.valueOf(time[0]);
                        }
                        if (time[1] >= 10) {
                            minutes = String.valueOf(time[1]);
                        } else {
                            minutes = "0" + String.valueOf(time[1]);
                        }
                        if (time[2] >= 10) {
                            seconds = String.valueOf(time[2]);
                        } else {
                            seconds = "0" + String.valueOf(time[2]);
                        }

                        v_dateType_thumbnail = hours + ":" + minutes + ":" + seconds;
                    }
                });
                customdialog.show();
            }
        });
    }

    // 파일 업로드 처리
    private class fileUpload extends AsyncTask<Void, Integer, String> {

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer; // 바이트 버퍼 저장
        int maxBufferSize = 1 * 40960 * 40960; // 최대 크기
        File sourceFile = new File(filePath); // 업로드 할 파일 그 자체

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            llayout_progress.setVisibility(View.VISIBLE);
            rotateLoading.start();

        }

        @Override
        protected String doInBackground(Void... params) {

            if (!sourceFile.isFile()) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.i("WRITELOG", "[UploadImageToServer] Source File not exist :" + filePath);
                    }
                });
                return null;
            } else {
                try {
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(URLlink + "/android/filestorage/fileupload.php");

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

                        // 게시물 정보 전송
                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"writer\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(URLEncoder.encode(writer, "utf-8"));
                        dos.writeBytes(lineEnd);

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"subject\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(URLEncoder.encode(fileSubject, "utf-8"));
                        dos.writeBytes(lineEnd);

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"filename\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(URLEncoder.encode(fileName, "utf-8"));
                        dos.writeBytes(lineEnd);

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"filesize\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(String.valueOf(fileSize));
                        dos.writeBytes(lineEnd);

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"filecategory\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(URLEncoder.encode(fileCategory, "utf-8"));
                        dos.writeBytes(lineEnd);

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"filecontent\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(URLEncoder.encode(fileContent, "utf-8"));
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
                                    Toast.makeText(FileUploadActivity.this, "File Upload Completed", Toast.LENGTH_SHORT).show();
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
                    Log.i("업로드 LOG", "에러 : " + ex.getMessage(), ex);
                } catch (Exception e) {
                    Log.i("업로드 LOG", "에러 : " + e.getMessage(), e);
                }
                Log.i("업로드 LOG", "완료");
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (rotateLoading.isStart()) {
                llayout_progress.setVisibility(View.GONE);
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
                                Toast.makeText(FileUploadActivity.this, "게시물 정보를 모두 채워주세요 (not_full_content)", Toast.LENGTH_SHORT).show();
                                break;
                            case "02":
                                Toast.makeText(FileUploadActivity.this, "서버 오류입니다 (move_fail)", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    } else { // 에러가 없으면 진행
                        if (!c.isNull("result")) {
                            js_result = c.getString("result");

                            if (js_result.equals("success")) {

                                if (!c.isNull("_id")) {
                                    js_id = c.getString("_id");
                                }

                                if (!c.isNull("m3u8name")) {
                                    js_m3u8name = c.getString("m3u8name");
                                }

                                Toast.makeText(FileUploadActivity.this, "업로드 성공", Toast.LENGTH_SHORT).show();
                                uploadListSQLHelper.insert(writer, fileSubject, fileName, fileCategory, String.valueOf(setfileUnit()), js_id);

                                /*
                                 * Video 파일 일 시 인코딩한다
                                 */
                                if (fileType.equals("video")) {
                                    startVideoEncoding();
                                }

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

    // 다중 파일 업로드 처리
    private class audiofileUpload extends AsyncTask<Void, Integer, String> {

        // 기본 세팅 //
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer; // 바이트 버퍼 저장
        int maxBufferSize = 1 * 40960 * 40960; // 최대 크기
        // 업로드 정보 //
        File audioFile, coverFile; // 보낼 파일
        int coverUsed = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            audioFile = new File(filePath); // 업로드 할 오디오 파일
            if(!coverPath.equals("")){
                coverUsed = 1;
                coverFile = new File(coverPath); // 업로드 할 파일 그 자체
            }

            llayout_progress.setVisibility(View.VISIBLE);
            rotateLoading.start();
        }

        @Override
        protected String doInBackground(Void... params) {

            if (!audioFile.isFile()) {
                Log.i("업로드 LOG", "소스 파일을 찾을 수 없음 :" + filePath);
                return null;
            } else {
                try {

                    URL url = new URL(URLlink + "/android/filestorage/audiofileupload.php");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    if (conn != null) {
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("audio_file", fileName); // 업로드 되는 파일명
                        conn.setRequestProperty("cover_file", fileName+".jpg"); // 업로드 되는 파일명
                        conn.connect();

                        dos = new DataOutputStream(conn.getOutputStream());
                        FileInputStream mFileInputStream1 = new FileInputStream(audioFile);

                        // 게시물 정보 전송
                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"writer\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(URLEncoder.encode(writer, "utf-8"));
                        dos.writeBytes(lineEnd);

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"subject\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(URLEncoder.encode(fileSubject, "utf-8"));
                        dos.writeBytes(lineEnd);

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"filesize\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(String.valueOf(fileSize));
                        dos.writeBytes(lineEnd);

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"filecategory\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(URLEncoder.encode(fileCategory, "utf-8"));
                        dos.writeBytes(lineEnd);

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"filecontent\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(URLEncoder.encode(fileContent, "utf-8"));
                        dos.writeBytes(lineEnd);

                        // 보내는 파일 정보
                        dos.writeBytes("\r\n--" + boundary + "\r\n");
                        dos.writeBytes("Content-Disposition: form-data; name=\"coverUsed\"\r\n\r\n" + coverUsed); // 앨범 커버 파일이 있는지 없는지

                        dos.writeBytes("\r\n--" + boundary + "\r\n");
                        dos.writeBytes("Content-Disposition: form-data; name=\"audio_fname\"\r\n\r\n" + URLEncoder.encode(fileName, "utf-8"));

                        dos.writeBytes("\r\n--" + boundary + "\r\n");
                        dos.writeBytes("Content-Disposition: form-data; name=\"audio_file\";filename=\"" + fileName + "\"" + lineEnd);
                        dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);
                        dos.writeBytes(lineEnd);
                        int bytesAvailable1 = mFileInputStream1.available();
                        bufferSize = Math.min(bytesAvailable1, maxBufferSize);
                        byte[] buffer1 = new byte[bufferSize];
                        int bytesRead1 = mFileInputStream1.read(buffer1, 0, bufferSize);
                        while (bytesRead1 > 0) {
                            dos.write(buffer1, 0, bufferSize);
                            bytesAvailable1 = mFileInputStream1.available();
                            bufferSize = Math.min(bytesAvailable1, maxBufferSize);
                            bytesRead1 = mFileInputStream1.read(buffer1, 0, bufferSize);
                        }
                        mFileInputStream1.close();

                        if(coverUsed == 1){
                            dos = new DataOutputStream(conn.getOutputStream());
                            FileInputStream mFileInputStream2 = new FileInputStream(coverFile);

                            dos.writeBytes("\r\n--" + boundary + "\r\n");
                            dos.writeBytes("Content-Disposition: form-data; name=\"cover_fname\"\r\n\r\n" + URLEncoder.encode(fileName+".jpg", "utf-8"));

                            dos.writeBytes("\r\n--" + boundary + "\r\n");
                            dos.writeBytes("Content-Disposition: form-data; name=\"cover_file\";filename=\"" + fileName+".jpg" + "\"" + lineEnd);
                            dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);
                            dos.writeBytes(lineEnd);
                            int bytesAvailable2 = mFileInputStream2.available();
                            bufferSize = Math.min(bytesAvailable2, maxBufferSize);
                            byte[] buffer2 = new byte[bufferSize];
                            int bytesRead2 = mFileInputStream2.read(buffer2, 0, bufferSize);
                            while (bytesRead2 > 0) {
                                dos.write(buffer2, 0, bufferSize);
                                bytesAvailable2 = mFileInputStream2.available();
                                bufferSize = Math.min(bytesAvailable2, maxBufferSize);
                                bytesRead2 = mFileInputStream2.read(buffer2, 0, bufferSize);
                            }
                            mFileInputStream2.close();
                        }

                        // send multipart form data necesssary after file data...
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                        // boundary 뒤에 -- 이 붙음으로써 인자 나열이 끝낫음을 의미함. 뒤의 flush()호출로 전송 마무리

                        // Responses from the server (code and message)
                        serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn.getResponseMessage();

                        Log.i("업로드 LOG", "HTTP Response : " + serverResponseMessage + ": " + serverResponseCode);

                        if (serverResponseCode == 200) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(FileUploadActivity.this, "File Upload Completed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
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
                    Log.i("업로드 LOG", "에러 : " + ex.getMessage(), ex);
                } catch (Exception e) {
                    Log.i("업로드 LOG", "에러 : " + e.getMessage(), e);
                }
                Log.i("업로드 LOG", "완료");
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (rotateLoading.isStart()) {
                llayout_progress.setVisibility(View.GONE);
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
                            case "02":
                                Toast.makeText(FileUploadActivity.this, "서버 오류입니다 (move_fail)", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    } else { // 에러가 없으면 진행
                        if (!c.isNull("result")) {
                            js_result = c.getString("result");

                            if (js_result.equals("success")) {

                                if (!c.isNull("_id")) {
                                    js_id = c.getString("_id");
                                }

                                Toast.makeText(FileUploadActivity.this, "업로드 성공", Toast.LENGTH_SHORT).show();
                                uploadListSQLHelper.insert(writer, fileSubject, fileName, fileCategory, String.valueOf(setfileUnit()), js_id);

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


    // ffmpeg 실행시켜 비디오 파일 인코딩하기 (액티비티 넘어가며 백그라운드에서 처리함), AsyncTask와의 병행을 위해 우선적 Volley 사용
    void startVideoEncoding() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLlink + "/android/filecnt/set_videoencoding.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(FileUploadActivity.this,response,Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(FileUploadActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("filecategory", fileCategory);
                params.put("filename", fileName);
                params.put("m3u8name", js_m3u8name);
                params.put("begintime", v_dateType_encode_start);
                params.put("endtime", v_dateType_encode_end);
                params.put("thumbnailtime", v_dateType_thumbnail);

                Log.d("설정 시간", "시작구간 : " + v_dateType_encode_start + ", 끝구간 : " + v_dateType_encode_end);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    // 파일 선택 시 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK) {
                        // 파일을 고른 뒤 URI 받기
                        Uri uri = data.getData(); // URI
                        filePath = getPath(getApplicationContext(), uri); // filePath
                        Log.d("업로딩 로그","경로 :" + filePath);
                        fileName = new File(filePath).getName(); // fileName
                        // 서버에 유효한 파일 형식 갖추기 (테스트)
                        fileName = fileName.replaceAll(" ", "");
                        fileName = fileName.replaceAll("\\(", "_");
                        fileName = fileName.replaceAll("\\)", "_");
                        fileName = fileName.replaceAll("'", "_");
                        fileName = fileName.replaceAll("'", "_");
                        // 서버에 유효한 파일 형식 갖추기 //
                        Log.d("업로딩 로그","이름 :" + fileName);
                        fileSize = getfileSize(filePath); // fileSize

                        tv_filename.setText(fileName); // 파일 이름 출력
                        tv_filepath.setText(filePath); // 파일 경로 충력
                        tv_filesize.setText(String.valueOf(setfileUnit())); // 파일 사이즈 단위별 출력

                        getThumbnail();
                        setCategoryMajor();
                    }
                    break;
            }


        } catch (NullPointerException e) {
            Toast.makeText(getApplicationContext(), "해당 파일은 기기 내 저장된 파일이 아닙니다",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // 파일 경로찾기
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    // 파일 용량 구하기
    private long getfileSize(String path) {

        File oFile = new File(path);

        if (oFile.exists()) {
            fileSize = oFile.length();
        } else {
            System.out.println("파일없음");
        }

        return fileSize;
    }

    // 파일 용량 단위 변환
    private String setfileUnit() {

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

    // 썸네일 구하기
    private void getThumbnail() {

        MediaMetadataRetriever retriver = new MediaMetadataRetriever();
        Bitmap bmThumbnail;
        String time;
        long timeInmillisec;
        long duration;

        switch (fileType) {
            case "image":
                // 이미지 크기 클 때 줄이기 위한 수단
                int n = 1; // 2048 * n
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                n = 2;

                bmThumbnail = BitmapFactory.decodeFile(filePath, options);

                //  이미지 크기 클 때 줄이기 위한 수단 //

                iv_thumbnail.setImageBitmap(bmThumbnail);  //배치해놓은 ImageView에 set
                btn_videoencodesetting.setVisibility(View.GONE);
                btn_thumbnailsetting.setVisibility(View.GONE);
                break;
            case "video":
                bmThumbnail = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MICRO_KIND);
                iv_thumbnail.setImageBitmap(bmThumbnail);

                // 동영상 재생시간 구하기
                retriver.setDataSource(filePath);
                time = retriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                timeInmillisec = Long.parseLong(time);
                duration = timeInmillisec / 1000;
                hours = duration / 3600;
                minutes = (duration - hours * 3600) / 60;
                seconds = duration - (hours * 3600 + minutes * 60);

                Log.d("업로드 영상 길이:", "총 기간 : " + duration + ",총 시간 :" + hours + ",총 분 : " + minutes + ",총 초:" + seconds);
                btn_videoencodesetting.setVisibility(View.VISIBLE);
                btn_thumbnailsetting.setVisibility(View.VISIBLE);
                break;
            case "audio":
                retriver.setDataSource(filePath);
                byte[] data = retriver.getEmbeddedPicture();

                if (data != null) {
                    a_coverBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    iv_thumbnail.setImageBitmap(a_coverBitmap);
                } else {
                    a_coverBitmap = null;
                    iv_thumbnail.setImageResource(R.drawable.ic_musicfile);
                }

                btn_videoencodesetting.setVisibility(View.GONE);
                btn_thumbnailsetting.setVisibility(View.GONE);
                break;
            case "etcfile":
                iv_thumbnail.setImageResource(R.drawable.ic_file);
                btn_videoencodesetting.setVisibility(View.GONE);
                btn_thumbnailsetting.setVisibility(View.GONE);
                break;
        }
    }

    // 업로드 위한 오디오 커버 이미지 파일 임시 생성
    public void saveBitmapToJpeg(Context context, Bitmap bitmap, String _name){
        File storage = context.getCacheDir(); // 임시파일 저장 경로
        Log.d("비트맵 -> JPEG 변환 LOG", "임시 저장 경로 :" + storage);
        String fileName = _name + ".jpg";  // 파일이름
        File tempFile = new File(storage, fileName);
        try{
            tempFile.createNewFile();  // 파일을 생성
            FileOutputStream out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90 , out);  // 넘거 받은 bitmap을 jpeg(손실압축)으로 저장함
            out.close(); // 마무리로 닫아줍니다.
        } catch (FileNotFoundException e) {
            Log.d("비트맵 -> JPEG 변환 LOG", "에러 :" + e.getMessage());
        } catch (IOException e) {
            Log.d("비트맵 -> JPEG 변환 LOG", "에러 :" + e.getMessage());
        }
        coverPath = tempFile.getAbsolutePath();   // 임시파일 저장경로를 리턴
    }

    // 대분류 설정하기
    private void setCategoryMajor() {

        Category.clear();

        switch (fileType) {
            case "image":
                Category.addAll(ctgr_major_image);
                break;
            case "video":
                Category.addAll(ctgr_major_video);
                break;
            case "audio":
                Category.addAll(ctgr_major_audio);
                break;
            case "etcfile":
                Category.addAll(ctgr_major_etcfile);
                break;
        }

        fileCategory = "select";
        sp_category_major.setSelection(0);
        sp_adapter_major.notifyDataSetChanged();
        sp_category_major.setClickable(true);
    }

    // 파일 보기, 확장자에 따른 처리
    private void showFile() {
        // TODO Auto-generated method stub
        Intent fileLinkIntent = new Intent(Intent.ACTION_VIEW);
        fileLinkIntent.addCategory(Intent.CATEGORY_DEFAULT);
        File file = new File(filePath);
        Uri uri = Uri.fromFile(file);
        //확장자 구하기
        String fileExtend = getExtension(file.getAbsolutePath());
        // 파일 확장자 별로 mime type 지정해 준다.
        if (fileExtend.equalsIgnoreCase("mp3")
                || fileExtend.equalsIgnoreCase("m4a")
                || fileExtend.equalsIgnoreCase("wav")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file), "audio/*");
        } else if (fileExtend.equalsIgnoreCase("mp4")
                || fileExtend.equalsIgnoreCase("mpeg")
                || fileExtend.equalsIgnoreCase("mpg")
                || fileExtend.equalsIgnoreCase("mpe")
                || fileExtend.equalsIgnoreCase("avi")
                || fileExtend.equalsIgnoreCase("mov")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file), "video/*");
        } else if (fileExtend.equalsIgnoreCase("jpg")
                || fileExtend.equalsIgnoreCase("jpeg")
                || fileExtend.equalsIgnoreCase("gif")
                || fileExtend.equalsIgnoreCase("png")
                || fileExtend.equalsIgnoreCase("bmp")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file), "image/*");
        } else if (fileExtend.equalsIgnoreCase("txt")
                || fileExtend.equalsIgnoreCase("css")
                || fileExtend.equalsIgnoreCase("html")
                || fileExtend.equalsIgnoreCase("htm")
                || fileExtend.equalsIgnoreCase("rtx")
                || fileExtend.equalsIgnoreCase("xml")
                || fileExtend.equalsIgnoreCase("tsv")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file), "text/*");
        } else if (fileExtend.equalsIgnoreCase("doc")
                || fileExtend.equalsIgnoreCase("docx")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file), "application/msword");
        } else if (fileExtend.equalsIgnoreCase("xls")
                || fileExtend.equalsIgnoreCase("xlsx")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file),
                    "application/vnd.ms-excel");
        } else if (fileExtend.equalsIgnoreCase("ppt")
                || fileExtend.equalsIgnoreCase("pptx")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file),
                    "application/vnd.ms-powerpoint");
        } else if (fileExtend.equalsIgnoreCase("pdf")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file), "application/pdf");
        } else if (fileExtend.equalsIgnoreCase("hwp")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file),
                    "application/haansofthwp");
        } else if (fileExtend.equalsIgnoreCase("zip")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file),
                    "application/zip");
        }
        PackageManager pm = getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(fileLinkIntent,
                PackageManager.GET_META_DATA);
        if (list.size() == 0) {
            Toast.makeText(getApplicationContext(), fileName + "을 확인할 수 있는 앱이 설치되지 않았습니다.",
                    Toast.LENGTH_SHORT).show();
        } else {
            startActivity(fileLinkIntent);
        }
    }

    public static String getExtension(String fileStr) {
        return fileStr.substring(fileStr.lastIndexOf(".") + 1, fileStr.length());
    }
}