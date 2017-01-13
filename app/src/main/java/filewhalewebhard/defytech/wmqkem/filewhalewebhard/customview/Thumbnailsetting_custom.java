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

public class Thumbnailsetting_custom extends Dialog {

    DiscreteSeekBar seekBar_hours, seekBar_minutes, seekBar_seconds;
    String filePath = "";
    ImageView iv_thumb_thumbnail;

    int max_hours;
    int max_minutes;
    int max_seconds;
    int[] timestorage = new int[3];

    public Thumbnailsetting_custom(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.thumbnailsetting_custom);

        seekBar_hours = (DiscreteSeekBar) findViewById(R.id.seekbar_hours);
        seekBar_minutes = (DiscreteSeekBar) findViewById(R.id.seekbar_minutes);
        seekBar_seconds = (DiscreteSeekBar) findViewById(R.id.seekbar_seconds);
        iv_thumb_thumbnail = (ImageView) findViewById(R.id.iv_thumb_thumbnail);
        Button btn_dissmiss = (Button) findViewById(R.id.btn_dismiss);

        seekBar_hours.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                setVideoTimeAdj();
                iv_thumb_thumbnail.setImageBitmap(setThumbnailAtTime());
            }
        });

        seekBar_minutes.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                setVideoTimeAdj();
                iv_thumb_thumbnail.setImageBitmap(setThumbnailAtTime());
            }
        });

        seekBar_seconds.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                setVideoTimeAdj();
                iv_thumb_thumbnail.setImageBitmap(setThumbnailAtTime());
            }
        });

        btn_dissmiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void getFileLength(long _hours, long _minutes, long _seconds) {
        max_hours = (int) _hours;
        max_minutes = (int) _minutes;
        max_seconds = (int) _seconds;

        Log.d("넘어온 시간 값", max_hours + "시간, " + max_minutes + "분, " + max_seconds + "초");

        setSetting();
    }

    public void setSetting() {
        if (max_hours == 0) {
            seekBar_hours.setVisibility(View.GONE);
        }
        if (max_minutes == 0) {
            seekBar_minutes.setVisibility(View.GONE);
        }
        if (max_seconds == 0) {
            seekBar_seconds.setVisibility(View.GONE);
        }
        seekBar_hours.setMax(max_hours);
        seekBar_minutes.setMax(max_minutes);
        seekBar_seconds.setMax(max_seconds);
    }

    public void setPath(String path) {
        filePath = path;
    }

    public int[] getTime() {
        timestorage[0] = seekBar_hours.getProgress();
        timestorage[1] = seekBar_minutes.getProgress();
        timestorage[2] = seekBar_seconds.getProgress();
        return timestorage;
    }

    private Bitmap setThumbnailAtTime() {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(filePath);

        long hours = 0, minutes = 0, seconds = 0;
        int[] nowTime = getTime();
        hours = nowTime[0] * 1000000 * 60 * 60;
        minutes = nowTime[1] * 1000000 * 60;
        seconds = nowTime[2] * 1000000;

        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(hours + minutes + seconds);
        return bitmap;
    }

    private void setVideoTimeAdj() {
        if (max_hours > seekBar_hours.getProgress()) { //  현재 설정된 시가 영상의 시(hour)보다 작으면
            seekBar_minutes.setMax(59); // 분은 max가 59
            seekBar_seconds.setMax(59); // 초는 max가 59
        } else { // 현재 설정된 시가 영상의 시(hour)와 같으면
            seekBar_minutes.setMax(max_minutes); // 분은 max가 영상의 분(minute)

            if (seekBar_minutes.getProgress() > max_minutes) { // 설정의 분 max가 바뀌었는데 minute의 max가 안바뀌었으니 재설정
                seekBar_minutes.setProgress(max_minutes);
            }
            if (max_minutes > seekBar_minutes.getProgress()) { // 현재 설정된 분이 영상의 분(minute) 보다 작으면
                seekBar_seconds.setMax(59); // 초의 max가 59
            } else { // 현재 설정된 분이 영상의 분(minute)과 같으면
                seekBar_seconds.setMax(max_seconds); // 초의 max는 영상의 초(second)

                if (seekBar_seconds.getProgress() > max_seconds) { // 설정의 초 max가 바뀌었는데 second의 max가 안바뀌었으니 재설정
                    seekBar_seconds.setProgress(max_seconds);
                }
            }
        }
    }
}
