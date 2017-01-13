package filewhalewebhard.defytech.wmqkem.filewhalewebhard.customview;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import filewhalewebhard.defytech.wmqkem.filewhalewebhard.R;

/**
 * Created by kim on 2016-10-07.
 */

public class Encodingsetting_custom extends Dialog {

    DiscreteSeekBar seekBar_hours_start, seekBar_minutes_start, seekBar_seconds_start;
    DiscreteSeekBar seekBar_hours_end, seekBar_minutes_end, seekBar_seconds_end;
    String filePath = "";
    ImageView iv_thumb_start, iv_thumb_end;

    int max_hours;
    int max_minutes;
    int max_seconds;
    int [] timestorage = new int [6];

    public Encodingsetting_custom(Context context){
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.encodesetting_custom);

        seekBar_hours_start = (DiscreteSeekBar) findViewById(R.id.seekbar_hours_start);
        seekBar_hours_end = (DiscreteSeekBar) findViewById(R.id.seekbar_hours_end);
        seekBar_minutes_start = (DiscreteSeekBar) findViewById(R.id.seekbar_minutes_start);
        seekBar_minutes_end = (DiscreteSeekBar) findViewById(R.id.seekbar_minutes_end);
        seekBar_seconds_start = (DiscreteSeekBar) findViewById(R.id.seekbar_seconds_start);
        seekBar_seconds_end = (DiscreteSeekBar) findViewById(R.id.seekbar_seconds_end);
        iv_thumb_start = (ImageView) findViewById(R.id.iv_thumb_start);
        iv_thumb_end = (ImageView) findViewById(R.id.iv_thumb_end);
        Button btn_dissmiss = (Button) findViewById(R.id.btn_dismiss);

        seekBar_hours_start.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                if(seekBar_hours_start.getProgress() > seekBar_hours_end.getProgress()){
                    seekBar_hours_end.setProgress(seekBar_hours_start.getProgress());
                    seekBar_minutes_end.setProgress(0);
                    seekBar_seconds_end.setProgress(0);
                }
            }
            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                setVideoTimeAdj(0);
                iv_thumb_start.setImageBitmap(setThumbnailAtTime(0));
                iv_thumb_end.setImageBitmap(setThumbnailAtTime(1));
            }
        });

        seekBar_hours_end.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                if(seekBar_hours_end.getProgress() < seekBar_hours_start.getProgress()){
                    seekBar_hours_start.setProgress(seekBar_hours_end.getProgress());
                    seekBar_minutes_start.setProgress(0);
                    seekBar_seconds_start.setProgress(0);
                }
            }
            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                setVideoTimeAdj(1);
                iv_thumb_start.setImageBitmap(setThumbnailAtTime(0));
                iv_thumb_end.setImageBitmap(setThumbnailAtTime(1));
            }
        });

        seekBar_minutes_start.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                if(seekBar_minutes_start.getProgress() > seekBar_minutes_end.getProgress() && seekBar_hours_start.getProgress() <= seekBar_hours_end.getProgress()){
                    seekBar_minutes_end.setProgress(seekBar_minutes_start.getProgress());
                    seekBar_seconds_end.setProgress(0);
                }
            }
            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                setVideoTimeAdj(0);
                iv_thumb_start.setImageBitmap(setThumbnailAtTime(0));
                iv_thumb_end.setImageBitmap(setThumbnailAtTime(1));
            }
        });

        seekBar_minutes_end.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                if(seekBar_minutes_end.getProgress() < seekBar_minutes_start.getProgress() && seekBar_hours_start.getProgress() <= seekBar_hours_end.getProgress()){
                    seekBar_minutes_start.setProgress(seekBar_minutes_end.getProgress());
                    seekBar_seconds_start.setProgress(0);
                }
            }
            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                setVideoTimeAdj(1);
                iv_thumb_start.setImageBitmap(setThumbnailAtTime(0));
                iv_thumb_end.setImageBitmap(setThumbnailAtTime(1));
            }
        });

        seekBar_seconds_start.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                if(seekBar_seconds_start.getProgress() > seekBar_seconds_end.getProgress() && seekBar_minutes_start.getProgress() <= seekBar_minutes_end.getProgress()){
                    seekBar_seconds_end.setProgress(seekBar_seconds_start.getProgress());
                }
            }
            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                setVideoTimeAdj(0);
                iv_thumb_start.setImageBitmap(setThumbnailAtTime(0));
                iv_thumb_end.setImageBitmap(setThumbnailAtTime(1));
            }
        });

        seekBar_seconds_end.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                if(seekBar_seconds_end.getProgress() < seekBar_seconds_start.getProgress() && seekBar_minutes_start.getProgress() <= seekBar_minutes_end.getProgress()){
                    seekBar_seconds_start.setProgress(seekBar_seconds_end.getProgress());
                }
            }
            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                setVideoTimeAdj(1);
                iv_thumb_start.setImageBitmap(setThumbnailAtTime(0));
                iv_thumb_end.setImageBitmap(setThumbnailAtTime(1));
            }
        });

        btn_dissmiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void getFileLength(long _hours, long _minutes, long _seconds){
        max_hours = (int) _hours;
        max_minutes = (int) _minutes;
        max_seconds = (int) _seconds;

        Log.d("넘어온 시간 값", max_hours+"시간, " +max_minutes+"분, "+max_seconds+"초");

        setSetting();
    }

    public void setSetting(){
        if(max_hours == 0){
            seekBar_hours_start.setVisibility(View.GONE);
            seekBar_hours_end.setVisibility(View.GONE);
        }
        if(max_minutes == 0){
            seekBar_minutes_start.setVisibility(View.GONE);
            seekBar_minutes_end.setVisibility(View.GONE);
        }
        if(max_seconds == 0){
            seekBar_seconds_start.setVisibility(View.GONE);
            seekBar_seconds_end.setVisibility(View.GONE);
        }
        seekBar_hours_start.setMax(max_hours);
        seekBar_hours_end.setMax(max_hours);
        seekBar_minutes_start.setMax(max_minutes);
        seekBar_minutes_end.setMax(max_minutes);
        seekBar_seconds_start.setMax(max_seconds);
        seekBar_seconds_end.setMax(max_seconds);
    }

    public void setPath(String path){
        filePath = path;
    }

    public int[] getTime(){
        timestorage[0] = seekBar_hours_start.getProgress();
        timestorage[1] = seekBar_hours_end.getProgress();
        timestorage[2] = seekBar_minutes_start.getProgress();
        timestorage[3] = seekBar_minutes_end.getProgress();
        timestorage[4] = seekBar_seconds_start.getProgress();
        timestorage[5] = seekBar_seconds_end.getProgress();
        return timestorage;
    }

    private Bitmap setThumbnailAtTime(int kinds){
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(filePath);

        long hours = 0, minutes = 0, seconds = 0;
        int[] nowTime = getTime();
        switch (kinds) {
            case 0: //시작 시
                hours = nowTime[0] * 1000000 * 60 * 60;
                minutes = nowTime[2] * 1000000 * 60;
                seconds = nowTime[4] * 1000000;
                break;
            case 1: // 끝 시
                hours = nowTime[1] * 1000000 * 60 * 60;
                minutes = nowTime[3] * 1000000 * 60;
                seconds = nowTime[5] * 1000000;
                break;
        }

        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(hours + minutes + seconds);
        return bitmap;
    }

    /*
     * 예시) 1시간 34분 6초 짜리, 지금 1시간 13분 50초로 설정되어있다면
     */

    private void setVideoTimeAdj(int kinds){
        switch (kinds) {
            case 0: // 시작 시(Hour)
                if(max_hours > seekBar_hours_start.getProgress()) { //  현재 설정된 시가 영상의 시(hour)보다 작으면
                    seekBar_minutes_start.setMax(59); // 분은 max가 59
                    seekBar_seconds_start.setMax(59); // 초는 max가 59
                } else { // 현재 설정된 시가 영상의 시(hour)와 같으면
                    seekBar_minutes_start.setMax(max_minutes); // 분은 max가 영상의 분(minute)

                    if(seekBar_minutes_start.getProgress() > max_minutes){ // 설정의 분 max가 바뀌었는데 minute의 max가 안바뀌었으니 재설정
                        seekBar_minutes_start.setProgress(max_minutes);
                    }
                    if(max_minutes > seekBar_minutes_start.getProgress()){ // 현재 설정된 분이 영상의 분(minute) 보다 작으면
                        seekBar_seconds_start.setMax(59); // 초의 max가 59
                    } else { // 현재 설정된 분이 영상의 분(minute)과 같으면
                        seekBar_seconds_start.setMax(max_seconds); // 초의 max는 영상의 초(second)

                        if(seekBar_seconds_start.getProgress() > max_seconds){ // 설정의 초 max가 바뀌었는데 second의 max가 안바뀌었으니 재설정
                            seekBar_seconds_start.setProgress(max_seconds);
                        }
                    }
                }
                break;

            case 1:
                if(max_hours > seekBar_hours_end.getProgress()) { //  현재 설정된 시가 영상의 시(hour)보다 작으면
                    seekBar_minutes_end.setMax(59); // 분은 max가 59
                    seekBar_seconds_end.setMax(59); // 초는 max가 59
                } else { // 현재 설정된 시가 영상의 시(hour)와 같으면
                    seekBar_minutes_end.setMax(max_minutes); // 분은 max가 영상의 분(minute)

                    if(seekBar_minutes_end.getProgress() > max_minutes){ // 설정의 분 max가 바뀌었는데 minute의 max가 안바뀌었으니 재설정
                        seekBar_minutes_end.setProgress(max_minutes);
                    }

                    if(max_minutes > seekBar_minutes_end.getProgress()){ // 현재 설정된 분이 영상의 분(minute) 보다 작으면
                        seekBar_seconds_end.setMax(59); // 초의 max가 59
                    } else { // 현재 설정된 분이 영상의 분(minute)과 같으면
                        seekBar_seconds_end.setMax(max_seconds); // 초의 max는 영상의 초(second)

                        if(seekBar_seconds_end.getProgress() > max_seconds){ // 설정의 초 max가 바뀌었는데 second의 max가 안바뀌었으니 재설정
                            seekBar_seconds_end.setProgress(max_seconds);
                        }
                    }
                }
                break;
        }
    }
}
