package filewhalewebhard.defytech.wmqkem.filewhalewebhard.main;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import filewhalewebhard.defytech.wmqkem.filewhalewebhard.R;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.SQLite.DownloadListSQLHelper;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.filejob.FileDownloadActivity;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.user.LoginActivity;

public class DownloadListFragment extends Fragment {

    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL
    static String DOWNLOAD_FILENAME = null;
    View view; // Fragment View 처리

    /*
     * PHP에서 받아온 JSON Array에 대한 처리
     */
    private JSONObject obj;

    // SQLite에서 받아온 다운로드 내역
    ArrayList<String> str_downlist = new ArrayList<String>();
    ArrayList<FileInfo> downlist = new ArrayList<FileInfo>();

    // 다운로드 내역
    ListView lv_downlist;
    DownlistAdapter lv_adapter;

    // 다운로드 처리
    // 다운로드 필요 변수
    private int listId;
    private String dirPath;
    DownloadManager dm;
    long downloadId;
    DownloadListSQLHelper downloadListSQLHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_downloadlist, null);

        System.out.println("onCreateView");

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dm = (DownloadManager) getActivity().getSystemService(getActivity().DOWNLOAD_SERVICE);

        // SQLite
        downloadListSQLHelper = new DownloadListSQLHelper(getActivity(), "DownloadHistory.db", null, 1);

        lv_downlist = (ListView) view.findViewById(R.id.lv_downlist);
        lv_adapter = new DownlistAdapter(getActivity(), android.R.layout.simple_list_item_1, downlist); // 데이터
        lv_downlist.setAdapter(lv_adapter);

        lv_downlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    // 다운로드 내역 리스트뷰 어뎁터
    private class DownlistAdapter extends ArrayAdapter<FileInfo> {

        private ArrayList<FileInfo> items;

        public DownlistAdapter(Context context, int textViewResourceId, ArrayList<FileInfo> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.listview_down_list, null);
            }

            final FileInfo f_info = items.get(position);
            if (f_info != null) {
                ImageView icon = (ImageView) v.findViewById(R.id.iv_downlist);
                TextView subject = (TextView) v.findViewById(R.id.tv_downlist_subject);
                TextView filename = (TextView) v.findViewById(R.id.tv_downlist_filename);
                TextView filesize = (TextView) v.findViewById(R.id.tv_downlist_size);
                LinearLayout llayout_downlist_loading = (LinearLayout) v.findViewById(R.id.llayout_downlist_loading);
                AVLoadingIndicatorView loadingview = (AVLoadingIndicatorView) v.findViewById(R.id.loadingview);
                final LinearLayout llayout_downlist_menu = (LinearLayout) v.findViewById(R.id.llayout_downlist_menu);

                if (icon != null && f_info.getFilecategory() != null) {
                    icon.setImageResource(R.drawable.ic_file);
                }
                if (subject != null) {
                    subject.setText(f_info.getSubject());
                }
                if (filesize != null) {
                    filesize.setText(f_info.getFilesize());
                }
                if (filename != null) {
                    filename.setText(f_info.getFilename());
                }
                if (!f_info.getState().equals("")) {
                    String state = f_info.getState();

                    switch (state) { // 파일의 다운로드 상태
                        case "Nothing": // 안 받아짐, 경로에 파일 유무 확인 후 없으면 다운로드

                            File files = new File(f_info.getFilepath());
                            //파일 유무를 확인합니다.
                            if(files.exists()==true) {
                                if (loadingview != null && llayout_downlist_loading != null) {
                                    llayout_downlist_loading.setVisibility(View.GONE);
                                    loadingview.hide();
                                }
                                downloadListSQLHelper.update(position, "Complete");
                            } else {
                                if (loadingview != null && llayout_downlist_loading != null) {
                                    llayout_downlist_loading.setVisibility(View.VISIBLE);
                                    loadingview.show();
                                }
                                listId = f_info.getNum();
                                DownloadFile(f_info.getFilecategory(), f_info.getFilename());
                            }

                            break;
                        case "Complete": // 받아짐
                            if (loadingview != null && llayout_downlist_loading != null) {
                                llayout_downlist_loading.setVisibility(View.GONE);
                                loadingview.hide();
                            }
                            downloadListSQLHelper.update(position, "Complete");
                            break;
                    }
                }

                LinearLayout llayout_downlist = (LinearLayout) v.findViewById(R.id.llayout_downlist);
                llayout_downlist.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (llayout_downlist_menu.getVisibility() == View.GONE) {
                            llayout_downlist_menu.setVisibility(View.VISIBLE);
                        } else {
                            llayout_downlist_menu.setVisibility(View.GONE);
                        }

                    }
                });

                LinearLayout llayout_downlist_file = (LinearLayout) v.findViewById(R.id.llayout_downlist_file);
                llayout_downlist_file.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (downlist.get(position).getState().equals("Complete")) {
                            showFile(position);
                        } else {
                            Toast.makeText(getActivity(), "다운로드 중인 파일입니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                LinearLayout llayout_downlist_article = (LinearLayout) v.findViewById(R.id.llayout_downlist_article);
                llayout_downlist_article.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), FileDownloadActivity.class);
                        intent.putExtra("_id", f_info.getFileIndex());
                        startActivity(intent);
                    }
                });

                LinearLayout llayout_downlist_namechange = (LinearLayout) v.findViewById(R.id.llayout_downlist_namechange);
                llayout_downlist_namechange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        // 팝업Dialog
                        // Dialog 기본설정
                        builder.setTitle("파일웨어");
                        builder.setMessage("파일명 변경");
                        final EditText et = new EditText(getActivity());
                        et.setText(f_info.getFilename());
                        builder.setView(et);

                        builder.setPositiveButton("변경", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String name = et.getText().toString();

                                File filePre = new File(f_info.getFilepath());
                                File fileNow = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FileWhale/" + name);

                                if (filePre.renameTo(fileNow)) {
                                    int renameid = downlist.get(position).getNum();
                                    downloadListSQLHelper.namechange(renameid, name, Environment.getExternalStorageDirectory().getAbsolutePath() + "/FileWhale/" + name);
                                    getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(fileNow))); // 갤러리에 바로 올라오도록함
                                    filePre.delete();
                                    Toast.makeText(getActivity(), "변경 성공", Toast.LENGTH_SHORT).show();

                                    getDownlist(); // 리스트 바로 갱신하여 파일 이름 뜨도록 하기
                                    lv_adapter.notifyDataSetChanged(); // 리스트 바로 갱신
                                } else {
                                    Toast.makeText(getActivity(), "변경 실패", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });

                        AlertDialog dialog = builder.create();    // 알림창 객체 생성=
                        dialog.show();
                    }
                });

                LinearLayout llayout_downlist_delete = (LinearLayout) v.findViewById(R.id.llayout_downlist_delete);
                llayout_downlist_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int del_id = downlist.get(position).getNum();

                        downloadListSQLHelper.delete(del_id);
                        getDownlist(); // 리스트 바로 갱신하여 파일 이름 뜨도록 하기
                        lv_adapter.notifyDataSetChanged(); // 리스트 바로 갱신
                    }
                });
            }
            return v;
        }
    }

    // List 갱신 메소드
    void getDownlist() {
        downlist.clear(); // 우선 비우기
        // SQLite에 저장되어 있던 업로드 내역 받아오기
        str_downlist = downloadListSQLHelper.getDownloadList(); // JSONArray로 받아옴

        try {
            if (str_downlist != null) { // 받아온 ArrayList<String> 이 비지 않았다면
                for (int i = 0; i < str_downlist.size(); i++) {
                    obj = new JSONObject(str_downlist.get(i)); // String -> JSONObject화

                    int _id = 0;
                    String subject = null, filename = null, filecategory = null, filepath = null, filesize = null, state = null, fileindex = null;

                    // JSON Key 값대로 꺼냄
                    if (!obj.isNull("_id")) {
                        _id = obj.getInt("_id"); // SQL _id 값
                    }
                    if (!obj.isNull("subject")) {
                        subject = obj.getString("subject"); // 제목
                    }
                    if (!obj.isNull("filename")) {
                        filename = obj.getString("filename"); // 파일 이름
                    }
                    if (!obj.isNull("category")) {
                        filecategory = obj.getString("category");
                    }
                    if (!obj.isNull("filepath")) {
                        filepath = obj.getString("filepath"); // 경로
                    }
                    if (!obj.isNull("filesize")) {
                        filesize = obj.getString("filesize"); // 파일 용량
                    }
                    if (!obj.isNull("state")) {
                        state = obj.getString("state"); // 상태 ( Nothing : 다운 시작 안했거나 끊켰을 경우 , Complete : 다운로드 완료 )
                    }
                    if (!obj.isNull("fileindex")) {
                        fileindex = obj.getString("fileindex"); // 상태 ( Nothing : 다운 시작 안했거나 끊켰을 경우 , Complete : 다운로드 완료 )
                    }

                    FileInfo fileinfo = new FileInfo(_id, subject, filename, filecategory, filepath, filesize, 0, state, fileindex); // 파일 정보 객체에 담음
                    downlist.add(fileinfo); // 파일 정보 객체를 리스트뷰에 뿌릴 ArrayList에 넣음
                    lv_adapter.notifyDataSetChanged(); // 정보가 바뀌었다는 것을 알림
                }
            } else {

            }
        } catch (JSONException e) {

        }
    }

    private void DownloadFile(String _category, String _name) {
        try {
            URI uri = new URI(URLlink + "/android/filestorage" + "/" + _category + "/" + _name);
            Uri finaluri = Uri.parse(uri.toASCIIString()); // 한국어 처리 위함

            Log.d("다운로드 LOG", "옆 url에서 다운로드 시작 :" + finaluri);

            String fileFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FileWhale";
            File file = new File(fileFolderPath);
            file.mkdir();// 디렉토리 없으면 생성, 있으면 통과

            dirPath = fileFolderPath + "/" + _name;

            // Make a request
            DownloadManager.Request request
                    = new DownloadManager.Request(finaluri)
                    .setAllowedOverRoaming(true)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setTitle("파일웨어 다운로드")
                    .setDescription(_name);

            if (_category.matches("오디오_" + ".*")) {
                request.setMimeType("audio/MP3");
            }

            if (android.os.Environment.getExternalStorageState()
                    .equals(android.os.Environment.MEDIA_MOUNTED)) {
                request.setDestinationInExternalPublicDir("FileWhale", _name); // 폴더명과 파일 이름만 값으로 넣는다
            }


            // 다운로드 상태 숨기는 방법
            // request.setVisibleInDownloadsUi(false);
            // request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

            // Queue에 넣기
            dm = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            downloadId = dm.enqueue(request);
        } catch (URISyntaxException e) {

        }
    }

    class FileInfo { // 파일 정보 클래스, 또한 Download와 ArrayAdapter의 Progress 진행도 공유 클래스

        private int _id;
        private String subject;
        private String filename;
        private String filecategory;
        private String filepath;
        private String filesize;
        private int percent; // Progress 진행도 공유
        private String state; // 저장된 파일의 상태 (Nothing, Cancel, Complete), 다운로드 대기, 다운로드 중 취소됨, 다운로드 완료
        private String fileindex;

        public FileInfo(int num, String _subject, String _filename, String _filecategory, String _filepath, String _filesize, int _percent, String _state, String _fileindex) {
            this._id = num;
            this.subject = _subject;
            this.filename = _filename;
            this.filecategory = _filecategory;
            this.filepath = _filepath;
            this.filesize = _filesize;
            this.percent = _percent;
            this.state = _state;
            this.fileindex = _fileindex;
        }

        public int getNum() {
            return _id;
        }

        public String getSubject() {
            return subject;
        }

        public String getFilename() {
            return filename;
        }

        public String getFilepath() {
            return filepath;
        }

        public String getFilecategory() {
            return filecategory;
        }

        public String getFilesize() {
            return filesize;
        }

        public int getProgress() {
            return percent;
        }

        public String getState() {
            return state;
        }

        public String getFileIndex() {
            return fileindex;
        }
    }

    // 파일 보기, 확장자에 따른 처리
    private void showFile(int position) {
        // TODO Auto-generated method stub
        Intent fileLinkIntent = new Intent(Intent.ACTION_VIEW);
        fileLinkIntent.addCategory(Intent.CATEGORY_DEFAULT);
        String filePath = downlist.get(position).getFilepath();
        Log.d("로그", "경로 :" + filePath);
        String fileName = downlist.get(position).getFilename();
        Log.d("로그", "이름 :" + fileName);
        File file = new File(filePath);
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
        PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(fileLinkIntent,
                PackageManager.GET_META_DATA);
        if (list.size() == 0) {
            Toast.makeText(getActivity(), fileName + "을 확인할 수 있는 앱이 설치되지 않았습니다.",
                    Toast.LENGTH_SHORT).show();
        } else {
            startActivity(fileLinkIntent);
        }
    }

    public static String getExtension(String fileStr) {
        return fileStr.substring(fileStr.lastIndexOf(".") + 1, fileStr.length());
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        getDownlist(); // List 갱신
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(downloadReceiver);
    }

    private final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            //ACTION_VIEW_DOWNLOADS 액션은 '다운로드' 액티비티를 호출할 때 사용한다
            //startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));

            //Query 객체를 생성하고 다운로드한 데이터를 찾기 위한 조건을 넣는다
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);

            //다운로드한 데이터의 정보를 얻어 커서로 만든다
            Cursor cursor = dm.query(query);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(columnIndex);
                int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                int reason = cursor.getInt(columnReason);
                String statusText, reasonText = null;

                // 데이터의 상태를 읽어 정상적으로 다운로드되었는지 파악한다
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    try {
                        //다운로드되어 있는 파일을 처리한다
                        ParcelFileDescriptor file = dm.openDownloadedFile(downloadId);
                        FileInputStream fileInputStream = new ParcelFileDescriptor.AutoCloseInputStream(file);
                        downloadListSQLHelper.update(listId, "Complete"); // SQLite에 파일 상태 ( Complete로 변경)
                        getDownlist(); // List 갱신
                        getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(dirPath)))); // 갤러리에 바로 올라오도록함
                        notifycation();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    switch (status) {

                        case DownloadManager.STATUS_FAILED:
                            System.out.println("STATUS_FAILED");
                            System.out.println(reason);
                            switch (reason) {
                                case DownloadManager.ERROR_CANNOT_RESUME:
                                    reasonText = "ERROR_CANNOT_RESUME";
                                    System.out.println(reasonText);
                                    break;
                                case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                                    reasonText = "ERROR_DEVICE_NOT_FOUND";
                                    System.out.println(reasonText);
                                    break;
                                case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                                    reasonText = "ERROR_FILE_ALREADY_EXISTS";
                                    System.out.println(reasonText);
                                    break;
                                case DownloadManager.ERROR_FILE_ERROR:
                                    reasonText = "ERROR_FILE_ERROR";
                                    System.out.println(reasonText);
                                    break;
                                case DownloadManager.ERROR_HTTP_DATA_ERROR:
                                    reasonText = "ERROR_HTTP_DATA_ERROR";
                                    System.out.println(reasonText);
                                    break;
                                case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                                    reasonText = "ERROR_INSUFFICIENT_SPACE";
                                    System.out.println(reasonText);
                                    break;
                                case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                                    reasonText = "ERROR_TOO_MANY_REDIRECTS";
                                    System.out.println(reasonText);
                                    break;
                                case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                                    reasonText = "ERROR_UNHANDLED_HTTP_CODE";
                                    System.out.println(reasonText);
                                    break;
                                case DownloadManager.ERROR_UNKNOWN:
                                    reasonText = "ERROR_UNKNOWN";
                                    System.out.println(reasonText);
                                    break;
                                default:
                                    System.out.println(reason);
                                    break;
                            }
                            break;
                        case DownloadManager.STATUS_PAUSED:
                            statusText = "STATUS_PAUSED";
                            switch (reason) {
                                case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                                    reasonText = "PAUSED_QUEUED_FOR_WIFI";
                                    System.out.println(statusText);
                                    System.out.println(reasonText);

                                    break;
                                case DownloadManager.PAUSED_UNKNOWN:
                                    reasonText = "PAUSED_UNKNOWN";
                                    System.out.println(statusText);
                                    System.out.println(reasonText);
                                    break;
                                case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                                    reasonText = "PAUSED_WAITING_FOR_NETWORK";
                                    System.out.println(statusText);
                                    System.out.println(reasonText);
                                    break;
                                case DownloadManager.PAUSED_WAITING_TO_RETRY:
                                    reasonText = "PAUSED_WAITING_TO_RETRY";
                                    System.out.println(statusText);
                                    System.out.println(reasonText);
                                    break;
                            }
                            break;
                        case DownloadManager.STATUS_PENDING:
                            statusText = "STATUS_PENDING";
                            System.out.println(statusText);
                            break;
                        case DownloadManager.STATUS_RUNNING:
                            statusText = "STATUS_RUNNING";
                            System.out.println(statusText);
                            break;
                        case DownloadManager.STATUS_SUCCESSFUL:
                            statusText = "STATUS_SUCCESSFUL";
                            System.out.println(statusText);
                            break;
                    }

                }
            }
        }
    };

    void notifycation() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);  // 노티 클릭 시
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        // 노티 띄우기
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.drawable.ic_download)
                .setContentTitle("파일웨어")
                .setContentText("다운로드 완료!")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
