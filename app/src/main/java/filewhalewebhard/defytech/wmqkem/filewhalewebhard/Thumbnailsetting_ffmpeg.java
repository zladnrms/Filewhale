package filewhalewebhard.defytech.wmqkem.filewhalewebhard;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.victor.loading.rotate.RotateLoading;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.File;

/**
 * Created by kim on 2016-10-07.
 */

public class Thumbnailsetting_ffmpeg extends Dialog {

    DiscreteSeekBar seekBar_hours, seekBar_minutes, seekBar_seconds;
    String filePath = "";
    ImageView iv_thumb_thumbnail;

    int max_hours;
    int max_minutes;
    int max_seconds;
    int[] timestorage = new int[3];

    FFmpeg ffmpeg;
    String TAG = "FFMPEG";
    private RotateLoading rotateLoading;

    public Thumbnailsetting_ffmpeg(Context context) {
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
        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);

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
                setThumbnailAtTime();
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
                setThumbnailAtTime();
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
                setThumbnailAtTime();
            }
        });


        btn_dissmiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        ffmpeg = FFmpeg.getInstance(getContext());

        loadFFMpegBinary();
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

    private void setThumbnailAtTime() {

        String hours, minutes, seconds, times;
        int[] nowTime = getTime();

        if(nowTime[0] < 10){
            hours = "0"+nowTime[0];
        } else {
            hours = String.valueOf(nowTime[0]);
        }
        if(nowTime[1] < 10){
            minutes = "0"+nowTime[1];
        } else {
            minutes = String.valueOf(nowTime[1]);
        }
        if(nowTime[2] < 10){
            seconds = "0"+nowTime[2];
        } else {
            seconds = String.valueOf(nowTime[2]);
        }

        times = hours + ":" + minutes + ":" + seconds;

        String cmd = "-y -i " + filePath + " -ss " + times + " -t 00:00:01 -vcodec png -vframes 1 /storage/emulated/0/DCIM/Camera/ffmpeg_encode.png";

        String[] command = cmd.split(" ");
        if (command.length != 0) {
            execFFmpegBinary(command);
        } else {
            Toast.makeText(getContext(), "ㅇㅇ", Toast.LENGTH_LONG).show();
        }
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

    private void loadFFMpegBinary() {
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    showUnsupportedExceptionDialog();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        }
    }

    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d(TAG, "Failure command : ffmpeg "+s);
                }

                @Override
                public void onSuccess(String s) {
                    Log.d(TAG, "Success command : ffmpeg "+s);
                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Progress : ffmpeg "+s);
                }

                @Override
                public void onStart() {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    rotateLoading.start();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg "+command);
                    rotateLoading.stop();

                    // 이미지 크기 클 때 줄이기 위한 수단
                    int n = 1; // 2048 * n
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    n = 2;

                    Bitmap bitmap = BitmapFactory.decodeFile("/storage/emulated/0/DCIM/Camera/ffmpeg_encode.png", options);

                    //  이미지 크기 클 때 줄이기 위한 수단 //
                    iv_thumb_thumbnail.setImageBitmap(bitmap);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(getContext())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("타이틀2")
                .setMessage("메세지")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();

    }
}
