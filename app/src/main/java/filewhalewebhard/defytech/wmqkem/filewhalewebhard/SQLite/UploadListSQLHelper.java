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

public class UploadListSQLHelper extends SQLiteOpenHelper {

    JSONObject JSONobj = new JSONObject();

    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public UploadListSQLHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // DB를 새로 생성할 때 호출되는 함수
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블 생성
        /* 이름은 MONEYBOOK이고, 자동으로 값이 증가하는 _id 정수형 기본키 컬럼과
        item 문자열 컬럼, price 정수형 컬럼, create_at 문자열 컬럼으로 구성된 테이블을 생성. */
        db.execSQL("CREATE TABLE IF NOT EXISTS UPLOADLIST (_id INTEGER PRIMARY KEY AUTOINCREMENT, writer VARCHAR, subject VARCHAR, filename VARCHAR, category VARCHAR, filesize VARCHAR, fileindex VARCHAR);");
    }

    // DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(String writer, String subject, String filename, String category, String filesize, String fileindex) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        db.execSQL("INSERT INTO UPLOADLIST VALUES(null, '" + writer + "', '" + subject + "', '" + filename + "', '" + category + "', '" + filesize + "', '" + fileindex + "');");
        db.close();
    }

    public void update(String id, String pw) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행의 가격 정보 수정
        db.execSQL("UPDATE LOGINLIST SET id = '" + id + "' and pw = '" + pw + "' WHERE id='" + id + "' and pw = '" + pw + "';");
        db.close();
    }

    public void delete(int id, String writer, String subject, String category) { // 로그아웃 시 데이터 삭제
        SQLiteDatabase db = getWritableDatabase();
        // 회원 정보 삭제
        db.execSQL("DELETE FROM UPLOADLIST WHERE _id = " + id + " and writer = " + writer + " and subject = '" + subject + "' and category = '" + category + "';");
        db.close();
    }

    public ArrayList<String> getUploadList() { // 업로드 내역 출력

        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();

        // 업로드 내역에 전달할 jsonArray
        ArrayList<String> uplist = new ArrayList<String>();

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM UPLOADLIST", null);

        while (cursor.moveToNext()) {

            try {
                JSONobj.put("_id", cursor.getInt(0));
                JSONobj.put("writer", cursor.getString(1));
                JSONobj.put("subject", cursor.getString(2));
                JSONobj.put("filename", cursor.getString(3));
                JSONobj.put("category", cursor.getString(4));
                JSONobj.put("filesize", cursor.getString(5));
                JSONobj.put("fileindex", cursor.getString(6));

                uplist.add(JSONobj.toString());
            } catch (JSONException e) {
            }
        }

        return uplist;
    }
}


