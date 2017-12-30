package filewhalewebhard.defytech.wmqkem.filewhalewebhard.viewer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import filewhalewebhard.defytech.wmqkem.filewhalewebhard.R;

public class App_imgviewer extends AppCompatActivity {

    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL

    Button btn_back;
    ImageView iv_imageviewer;

    String filename, filecategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        iv_imageviewer = (ImageView) findViewById(R.id.iv_imgviewer);

        // 파일의 이름을 가져옴 (확장자 제외)
        Intent intent = getIntent();
        filename = intent.getStringExtra("filename"); // 확장자 제외 이름
        filecategory = intent.getStringExtra("filecategory"); // 파일 분류 (영어)

        getPicture();

        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    void getPicture(){
        // GIF 파일 일 경우 대비
        GlideDrawableImageViewTarget ivTarget = new GlideDrawableImageViewTarget(iv_imageviewer);
        Glide.
                with(App_imgviewer.this).
                load(URLlink + "/android/filestorage/" + filecategory + "/" + filename).
                fitCenter().
                diskCacheStrategy(DiskCacheStrategy.NONE).
                skipMemoryCache(true).
                error(R.drawable.ic_profile).
                placeholder(R.drawable.ic_profile).
                into(ivTarget);
    }
}
