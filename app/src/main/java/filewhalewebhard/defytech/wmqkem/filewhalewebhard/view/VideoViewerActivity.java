package filewhalewebhard.defytech.wmqkem.filewhalewebhard.view;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;

import filewhalewebhard.defytech.wmqkem.filewhalewebhard.R;

public class VideoViewerActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnErrorListener,MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaController.MediaPlayerControl{

    Button btn_back;

    MediaController mediaController;
    private int position = 0;

    // MediaPlayer
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    MediaPlayer mp;

    String notcolumnfilename, filecategory, urlStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_viewer);

        // 파일의 이름을 가져옴 (확장자 제외)
        Intent intent = getIntent();
        notcolumnfilename = intent.getStringExtra("notcolumnfilename"); // 확장자 제외 이름
        filecategory = intent.getStringExtra("filecategory"); // 파일 분류 (영어)

        urlStream = "http://115.71.238.61/HLS/mobile/" + filecategory + "_" +notcolumnfilename + "_playlist.m3u8"; // 스트리밍 할 m3u8 주소

        surfaceView = (SurfaceView) findViewById(R.id.sfv);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mediaController != null){
                    mediaController.show();
                }
                return false;
            }
        });

        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
        mediaController = new MediaController(this);
        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(surfaceView);
        mediaController.setEnabled(true);
        mediaController.show();
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
}
