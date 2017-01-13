package filewhalewebhard.defytech.wmqkem.filewhalewebhard.viewer;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import filewhalewebhard.defytech.wmqkem.filewhalewebhard.R;

public class App_audioviewer extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnErrorListener,MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener,MediaPlayer.OnPreparedListener, MediaController.MediaPlayerControl{

    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL

    ImageView iv_audioviewer;
    Button btn_back;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    MediaPlayer mp;
    MediaController mc;

    String covername, filename, filecategory, urlStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_audioviewer);

        iv_audioviewer = (ImageView) findViewById(R.id.iv_audioviewer);

        Intent intent = getIntent();
        filename = intent.getStringExtra("filename");
        filecategory = intent.getStringExtra("filecategory");
        covername = intent.getStringExtra("covername");

        urlStream = "http://115.71.238.61/android/filestorage/" + filecategory + "/" + filename; // 스트리밍 할 m3u8 주소

        mp = new MediaPlayer();
        mc = new MediaController(this);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mc.show(0);
            }
        });

        surfaceView = (SurfaceView) findViewById(R.id.sfv);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mc != null){
                    mc.show();
                }
                return false;
            }
        });

        coverImg();

        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void coverImg(){
        if(!covername.equals("")) {
            System.out.println(URLlink + "/android/filestorage/a_coverimg/" + covername);
            Glide.
                    with(App_audioviewer.this).
                    load(URLlink + "/android/filestorage/a_coverimg/" + covername).
                    fitCenter().
                    diskCacheStrategy(DiskCacheStrategy.NONE).
                    skipMemoryCache(true).
                    error(R.drawable.ic_profile).
                    placeholder(R.drawable.ic_profile).
                    into(iv_audioviewer);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mp = new MediaPlayer();
            mp.setOnErrorListener(this);
            mp.setOnBufferingUpdateListener(this);
            mp.setOnCompletionListener(this);
            mp.setOnPreparedListener(this);
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setScreenOnWhilePlaying(true);

            mp.setDisplay(surfaceView.getHolder());
            mp.setDataSource(urlStream);
            mp.prepare();

            Log.v("LOG / 영상", "Duration: ===>" + mp.getDuration());
            mp.start();
        } catch (Exception e) {
            Log.e("LOG / 영상", "error: "+ e.getMessage(), e);
            if (mp != null) {
                mp.stop();
                mp.reset();
                mp.release();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mc = new MediaController(this);
        mc.setMediaPlayer(this);
        mc.setAnchorView(surfaceView);
        mc.setEnabled(true);
        mc.show();
    }

    public void onCompletion(MediaPlayer arg0) {
    }

    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
    }

    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        return true;
    }

    @Override
    public void start() {
        mp.start();
    }

    @Override
    public void pause() {
        mp.pause();
    }

    @Override
    public int getDuration() {
        return mp.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mp.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        mp.seekTo(pos);
        System.out.println("Seek To :" + pos);
    }

    @Override
    public boolean isPlaying() {
        return mp.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        System.out.println("캔푸즈:");
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return mp.getAudioSessionId();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mp.stop();
    }
}