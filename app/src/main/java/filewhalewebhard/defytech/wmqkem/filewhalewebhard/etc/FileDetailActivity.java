package filewhalewebhard.defytech.wmqkem.filewhalewebhard.etc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import filewhalewebhard.defytech.wmqkem.filewhalewebhard.R;

public class FileDetailActivity extends AppCompatActivity {

    String filecontent;

    TextView tv_filecontent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_detail);

        tv_filecontent = (TextView) findViewById(R.id.tv_filecontent);

        Intent intent = getIntent();
        filecontent = intent.getStringExtra("filecontent");

        tv_filecontent.setText(filecontent);

        Button btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
