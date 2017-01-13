package filewhalewebhard.defytech.wmqkem.filewhalewebhard.SQLite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by kim on 2016-08-30.
 */

public class DownloadListSQLHelper extends SQLiteOpenHelper {

    JSONObject JSONobj = new JSONObject();

    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public DownloadListSQLHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // DB를 새로 생성할 때 호출되는 함수
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블 생성
        db.execSQL("CREATE TABLE IF NOT EXISTS DOWNLOADLIST (_id INTEGER PRIMARY KEY AUTOINCREMENT, writer VARCHAR, subject VARCHAR, filename VARCHAR, category VARCHAR, filepath VARCHAR, filesize VARCHAR, state VARCHAR, fileindex VARCHAR);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(String writer, String subject, String filename, String category, String filepath,String filesize, String state, String fileindex) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        db.execSQL("INSERT INTO DOWNLOADLIST VALUES(null, '" + writer + "', '" + subject + "', '" + filename + "', '" + category + "', '" + filepath + "', '" + filesize + "', '" + state + "', '" + fileindex + "');");
        db.close();
    }

    public void update(int _id, String state) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행의 정보 수정
        db.execSQL("UPDATE DOWNLOADLIST SET state = '" + state + "' WHERE _id='" + _id + "';");
        db.close();
    }

    public void namechange(int _id, String filename, String filepath) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행의 정보 수정
        db.execSQL("UPDATE DOWNLOADLIST SET filename = '" + filename + "' WHERE _id='" + _id + "';");
        db.close();

        SQLiteDatabase db2 = getWritableDatabase();
        // 입력한 항목과 일치하는 행의 정보 수정
        db2.execSQL("UPDATE DOWNLOADLIST SET filepath = '" + filepath + "' WHERE _id='" + _id + "';");
        db2.close();
    }

    public void delete(int id) { // 로그아웃 시 데이터 삭제
        SQLiteDatabase db = getWritableDatabase();
        // 회원 정보 삭제
        db.execSQL("DELETE FROM DOWNLOADLIST WHERE _id = " + id + ";");
        db.close();
    }

    public String getState(int id){
        SQLiteDatabase db = getReadableDatabase();
        // 회원 정보 삭제
        Cursor cursor = db.rawQuery("SELECT * FROM DOWNLOADLIST  WHERE _id = " + id + "", null);

        String state = "Nothing";

        while (cursor.moveToNext()) {
                state = cursor.getString(7);
        }

        return state;
    }

    public ArrayList<String> getDownloadList() { // 업로드 내역 출력

        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();

        // 업로드 내역에 전달할 jsonArray
        ArrayList<String> uplist = new ArrayList<String>();

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM DOWNLOADLIST", null);

        while (cursor.moveToNext()) {

            try {
                JSONobj.put("_id", cursor.getInt(0));
                JSONobj.put("writer", cursor.getString(1));
                JSONobj.put("subject", cursor.getString(2));
                JSONobj.put("filename", cursor.getString(3));
                JSONobj.put("category", cursor.getString(4));
                JSONobj.put("filepath", cursor.getString(5));
                JSONobj.put("filesize", cursor.getString(6));
                JSONobj.put("state", cursor.getString(7));
                JSONobj.put("fileindex", cursor.getString(8));

                uplist.add(JSONobj.toString());
            } catch (JSONException e) {
            }
        }

        return uplist;
    }
}


