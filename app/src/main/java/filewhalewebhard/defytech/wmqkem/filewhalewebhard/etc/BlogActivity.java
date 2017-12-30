package filewhalewebhard.defytech.wmqkem.filewhalewebhard.etc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import filewhalewebhard.defytech.wmqkem.filewhalewebhard.R;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.filejob.FileDownloadActivity;

public class BlogActivity extends AppCompatActivity {

    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL

    /*
     * PHP에서 받아온 JSON Array에 대한 처리
     */
    private JSONArray jarray = null;

    // WhaleLog 주인 정보
    String blogMaster;
    Bitmap bm_profile;

    // WhaleLog 사용 객체
    TextView tv_nick;
    CircularImageView iv_blogprofile;
    ListView lv_filelist;
    FilelistAdapter lv_adapter;

    // 파일 리스트
    ArrayList<ArticleInfo> filelist = new ArrayList<ArticleInfo>();
    private int skipnumber = 0; // 리스트뷰에 보여질 파일 갯수, 또는 추가적으로 보여질 파일 갯수
    private boolean lv_lock = true;

    // 파일 List 받아오기 Progress
    private RotateLoading rotateLoading;
    private RotateLoading rotateLoading_blog;
    private LinearLayout llayout_progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_backarrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_nick = (TextView) findViewById(R.id.tv_nick);
        iv_blogprofile = (CircularImageView) findViewById(R.id.iv_blogprofile);
        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);
        llayout_progress = (LinearLayout) findViewById(R.id.llayout_progress);
        rotateLoading_blog = (RotateLoading) findViewById(R.id.rotateloading_blog);

        Intent intent = getIntent();
        blogMaster = intent.getStringExtra("blogmaster");

        new profileLoad().execute();

        // WhaleLog 주인이 올린 게시글 보여주기
        lv_filelist = (ListView) findViewById(R.id.lv_blogfilelist);
        lv_adapter = new FilelistAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, filelist); // 데이터
        ViewGroup footer = (ViewGroup) getLayoutInflater().inflate(R.layout.listview_footer, null);
        lv_filelist.addFooterView(footer);
        lv_filelist.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int count = totalItemCount - visibleItemCount;

                if (firstVisibleItem >= count && totalItemCount != 0 && lv_lock == false) {
                    // 아이템 추가
                    skipnumber += 10;
                    new GetFileList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
        lv_filelist.setAdapter(lv_adapter);
        lv_filelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!(position == filelist.size())) { // 리스트뷰 푸터가 아니면
                    Intent intent = new Intent(getApplicationContext(), FileDownloadActivity.class);
                    intent.putExtra("_id", filelist.get(position).getId());
                    startActivity(intent);
                }
            }
        });
    }

    // 이미지 가져오기 처리
    class profileLoad extends AsyncTask<Void, Void, Void> { // 불러오기

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            llayout_progress.setVisibility(View.VISIBLE);
            rotateLoading_blog.start();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                String url = URLlink + "/android/filestorage/profile/" + "profile_" + blogMaster + ".png";

                bm_profile = Glide.
                        with(BlogActivity.this).
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

            llayout_progress.setVisibility(View.GONE);
            rotateLoading_blog.stop();

            iv_blogprofile.setImageBitmap(bm_profile);
            tv_nick.setText(blogMaster);

            // 게시물 받아오기
            new GetFileList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    // 게시물 받아오기
    private class GetFileList extends AsyncTask<Void, Void, JSONArray> { // 불러오기

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            lv_lock = true;
            rotateLoading.start();
        }

        @Override
        protected JSONArray doInBackground(Void... params) {

            try {
                URL url = new URL(URLlink + "/android/list/get_blogfilelist.php"); // 앨범 폴더의 dbname 폴더에 접근
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
                    buffer.append("blogmaster").append("=").append(blogMaster).append("&"); // 옆의 변수 대로 더 불러옴
                    buffer.append("skipnumber").append("=").append(skipnumber); // 옆의 변수 대로 더 불러옴

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
                rotateLoading.stop();
            }

            System.out.println("결과" + result);

            try {
                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject obj = jarray.getJSONObject(i);

                    String _id = null, writer = null, subject = null, filename = null, filecategory = null, state = null, date = null;
                    Double starNum = 0.0;
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
                    if (!obj.isNull("starNum")) {
                        starNum = Double.valueOf(obj.getString("starNum"));
                    }
                    if (!obj.isNull("filename")) {
                        filename = obj.getString("filename");
                    }
                    if (!obj.isNull("state")) {
                        state = obj.getString("state");
                    }
                    if (!obj.isNull("filesize")) {
                        filesize = obj.getLong("filesize");
                    }
                    if (!obj.isNull("date")) {
                        date = obj.getString("date");
                    }

                    ArticleInfo articleInfo = new ArticleInfo(_id, writer, subject, filename, filecategory, starNum, filesize, date, state);
                    filelist.add(articleInfo); // 파일 정보 객체를 리스트뷰에 뿌릴 ArrayList에 넣음
                    lv_adapter.notifyDataSetChanged(); // 정보가 바뀌었다는 것을 알림
                    lv_lock = false;
                }

            } catch (JSONException e) {
                System.out.println("JSONException : " + e);
            }
        }
    }

    // 파일리스트뷰 어댑터
    private class FilelistAdapter extends ArrayAdapter<ArticleInfo> {

        private ArrayList<ArticleInfo> items;

        public FilelistAdapter(Context context, int textViewResourceId, ArrayList<ArticleInfo> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.listview_main_list, null);

                ViewHolder holder = new ViewHolder();
                holder.subject = (TextView) v.findViewById(R.id.tv_mainlist_subject);
                holder.filename = (TextView) v.findViewById(R.id.tv_mainlist_filename);
                holder.date = (TextView) v.findViewById(R.id.tv_mainlist_date);
                holder.writerprofile = (CircularImageView) v.findViewById(R.id.iv_comment_writer_profile);
                holder.item_rotateLoading = (RotateLoading) v.findViewById(R.id.item_rotateloading);

                v.setTag(holder);
            }

            final ArticleInfo f_info = items.get(position);
            if (f_info != null) {

                ViewHolder holder = (ViewHolder) v.getTag();

                if (holder.writerprofile != null) {
                    holder.writerprofile.setImageBitmap(bm_profile);
                }

                if (holder.subject != null) {
                    holder.subject.setText(f_info.getSubject());
                }
                if (holder.filename != null) {
                    holder.filename.setText(f_info.getFilename());
                }
                if (holder.filesize != null) {
                    holder.filesize.setText(String.valueOf(setfileUnit(f_info.getFilesize())));
                }
                if(holder.starnum != null) {
                    holder.starnum.setText(f_info.getStarnum()+"점");
                }
                if(holder.date != null) {
                    holder.date.setText(compareTime(nowTime(), f_info.getDate()));
                }
            }

            return v;
        }
    }

    /*
     * 얼마 전 게시된 게시물인지 계산 nowTime(), compareTime()
     */
    private String nowTime() {
        // 현재시간을 msec 으로 구한다.
        long now = System.currentTimeMillis();
        // 현재시간을 date 변수에 저장한다.
        Date date = new Date(now);
        // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // nowDate 변수에 값을 저장한다.
        String formatDate = sdfNow.format(date);
        return formatDate;
    }

    private String compareTime(String t1, String t2) {
        SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        Date day1 = null, day2 = null;
        long diff, diffDays = 0;
        String strbefore = "";
        try {
            day1 = format.parse(t1);
            day2 = format.parse(t2);
            System.out.println("day1:" + day1 + ",day2 :"+ day2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int compare = day1.compareTo( day2 );
        if ( compare > 0 ) {
            diff = day1.getTime() - day2.getTime();
            System.out.println("비교"+diff);
            if(diff > 24 * 60 * 60 * 1000) { // 하루 이상 차이나면
                diffDays = diff / (24 * 60 * 60 * 1000);
                strbefore = diffDays + "일 전";
            } else if (diff < 24 * 60 * 60 * 1000 && diff > 60 * 60 * 1000) { // 하루 이상 안나고 몇 시간 차이나면
                diffDays = diff / (60 * 60 * 1000);
                strbefore = diffDays + "시간 전";
            } else if (diff < 60 * 60 * 1000 && diff > 60 * 1000) { // 시간 차이 안나고 몇 분 차이나면
                diffDays = diff / (60 * 1000);
                strbefore = diffDays + "분 전";
            } else if (diff < 60 * 1000) { // 방금 전
                strbefore = "방금 전";
            }
        } else {
            strbefore = "지금";
        }
        return strbefore;
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

    class ArticleInfo { // 게시물 정보 클래스

        private String _id;
        private String writer;
        private String subject;
        private String filename;
        private String filecategory;
        private Double starnum;
        private long filesize;
        private String date;
        private String state; // 업로드된 파일의 상태 (live, expire)

        public ArticleInfo(String id, String _writer, String _subject, String _filename, String _filecategory, Double _starnum, long _filesize, String _date, String _state) {
            this._id = id;
            this.writer = _writer;
            this.subject = _subject;
            this.filename = _filename;
            this.filecategory = _filecategory;
            this.starnum = _starnum;
            this.filesize = _filesize;
            this.date = _date;
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

        public Double getStarnum() {
            return starnum;
        }

        public long getFilesize() {
            return filesize;
        }

        public String getDate() {
            return date;
        }

        public String getState() {
            return state;
        }
    }

    static class ViewHolder {
        CircularImageView writerprofile;
        TextView subject;
        TextView filename;
        TextView filesize;
        TextView starnum;
        TextView date;
        RotateLoading item_rotateLoading;
    }
}
