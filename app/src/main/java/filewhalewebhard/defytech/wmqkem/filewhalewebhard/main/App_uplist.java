package filewhalewebhard.defytech.wmqkem.filewhalewebhard.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import filewhalewebhard.defytech.wmqkem.filewhalewebhard.R;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.SQLite.UploadListSQLHelper;
import filewhalewebhard.defytech.wmqkem.filewhalewebhard.filejob.App_downloadfile;

public class App_uplist extends Fragment {

    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL
    View view; // Fragment View 처리

    /*
     * PHP에서 받아온 JSON Array에 대한 처리
     */
    private JSONObject obj;

    // SQLite에서 받아온 업로드 내역
    ArrayList<String> str_uplist = new ArrayList<String>();
    ArrayList<FileInfo> uplist = new ArrayList<FileInfo>();

    // 업로드 내역
    ListView lv_uplist;
    UplistAdapter lv_adapter;

    // 업로드 시 업로드 내역 저장
    UploadListSQLHelper uploadListSQLHelper = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.app_uplist, null);

        System.out.println("onCreateView");

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // SQLite
        uploadListSQLHelper = new UploadListSQLHelper(getActivity(), "UploadHistory.db", null, 1);

        lv_uplist = (ListView) view.findViewById(R.id.lv_uplist);
        lv_adapter = new UplistAdapter(getActivity(), android.R.layout.simple_list_item_1, uplist); // 데이터
        lv_uplist.setAdapter(lv_adapter);
        lv_uplist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), App_downloadfile.class);
                intent.putExtra("_id", uplist.get(position).getfileIndex());
                startActivity(intent);
            }
        });

    }

    // 업로드 내역 리스트뷰 어뎁터
    private class UplistAdapter extends ArrayAdapter<FileInfo> {

        private ArrayList<FileInfo> items;

        public UplistAdapter(Context context, int textViewResourceId, ArrayList<FileInfo> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.lv_uplist, null);
            }

            FileInfo f_info = items.get(position);
            if (f_info != null) {
                ImageView icon = (ImageView) v.findViewById(R.id.iv_uplist);
                TextView subject = (TextView) v.findViewById(R.id.tv_uplist_subject);
                TextView filename = (TextView) v.findViewById(R.id.tv_uplist_filename);
                TextView filesize = (TextView) v.findViewById(R.id.tv_uplist_size);

                if (icon != null) {
                    // 분류별 아이콘 다르게 설정할지 고민
                    icon.setImageResource(R.drawable.ic_file);
                }
                if (subject != null) {
                    subject.setText(f_info.getSubject());
                }
                if (filename != null) {
                    filename.setText(f_info.getFilename());
                }
                if (filesize != null) {
                    filesize.setText(f_info.getFilesize());
                }
            }

            return v;
        }
    }

    // List 갱신 메소드
    void getUplist(){
        uplist.clear(); // 우선 비우기
        // SQLite에 저장되어 있던 업로드 내역 받아오기
        str_uplist = uploadListSQLHelper.getUploadList(); // JSONArray로 받아옴

        try {
            if (str_uplist != null) { // 받아온 ArrayList<String> 이 비지 않았다면
                for (int i = 0; i < str_uplist.size(); i++) {
                    obj = new JSONObject(str_uplist.get(i)); // String -> JSONObject화

                    int _id = 0;
                    String subject = null, filename = null, filecategory = null, filesize = null, fileindex=  null;

                    // JSON Key 값대로 꺼냄
                    _id = obj.getInt("_id");
                    subject = obj.getString("subject");
                    filename = obj.getString("filename");
                    filecategory = obj.getString("category");
                    filesize = obj.getString("filesize");
                    fileindex = obj.getString("fileindex");

                    FileInfo fileinfo = new FileInfo(_id, subject, filename, filecategory, filesize, fileindex);
                    uplist.add(fileinfo);
                    lv_adapter.notifyDataSetChanged();
                }
            } else {

            }
        } catch (JSONException e) {

        }
    }

    class FileInfo {

        private int _id;
        private String subject;
        private String filename;
        private String filecategory;
        private String filesize;
        private String fileindex;

        public FileInfo(int num, String _subject, String _filename, String _filecategory, String _filesize, String _fileindex) {
            this._id = num;
            this.subject = _subject;
            this.filename = _filename;
            this.filecategory = _filecategory;
            this.filesize = _filesize;
            this.fileindex = _fileindex;
        }

        public int getNum(){
            return _id;
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

        public String getFilesize() {
            return filesize;
        }

        public String getfileIndex() {
            return fileindex;
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        getUplist(); // List 갱신
    }
}
