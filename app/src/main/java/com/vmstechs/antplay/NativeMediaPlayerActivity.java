/**
 *
 * Created by 公众号：字节流动 on 2021/3/16.
 * https://github.com/githubhaohao/LearnFFmpeg
 * 最新文章首发于公众号：字节流动，有疑问或者技术交流可以添加微信 Byte-Flow ,领取视频教程, 拉你进技术交流群
 *
 * */

package com.vmstechs.antplay;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.vmstechs.antplay.media.FFMediaPlayer;
import com.vmstechs.antplay.media.MySurfaceView;

import static com.vmstechs.antplay.media.FFMediaPlayer.MEDIA_PARAM_VIDEO_DURATION;
import static com.vmstechs.antplay.media.FFMediaPlayer.MEDIA_PARAM_VIDEO_HEIGHT;
import static com.vmstechs.antplay.media.FFMediaPlayer.MEDIA_PARAM_VIDEO_WIDTH;
import static com.vmstechs.antplay.media.FFMediaPlayer.MSG_DECODER_DONE;
import static com.vmstechs.antplay.media.FFMediaPlayer.MSG_DECODER_INIT_ERROR;
import static com.vmstechs.antplay.media.FFMediaPlayer.MSG_DECODER_READY;
import static com.vmstechs.antplay.media.FFMediaPlayer.MSG_DECODING_TIME;
import static com.vmstechs.antplay.media.FFMediaPlayer.MSG_REQUEST_RENDER;
import static com.vmstechs.antplay.media.FFMediaPlayer.VIDEO_RENDER_ANWINDOW;

public class NativeMediaPlayerActivity extends AppCompatActivity implements SurfaceHolder.Callback, FFMediaPlayer.EventCallback{
    private static final String TAG = "MediaPlayerActivity";
    private static final String[] REQUEST_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private static final int PERMISSION_REQUEST_CODE = 1;
    private MySurfaceView mSurfaceView = null;
    private FFMediaPlayer mMediaPlayer = null;
    private SeekBar mSeekBar = null;
    private boolean mIsTouch = false;
    private String mVideoPath = "tcp://103.182.65.183:9001";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(this);

        mSeekBar = findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch() called with: progress = [" + seekBar.getProgress() + "]");
                if(mMediaPlayer != null) {
                    mMediaPlayer.seekToPosition(mSeekBar.getProgress());
                    mIsTouch = false;
                }

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasPermissionsGranted(REQUEST_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, REQUEST_PERMISSIONS, PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!hasPermissionsGranted(REQUEST_PERMISSIONS)) {
                Toast.makeText(this, "We need the permission: WRITE_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceCreated() called with: surfaceHolder = [" + surfaceHolder + "]");
        mMediaPlayer = new FFMediaPlayer();
        mMediaPlayer.addEventCallback(this);
        mMediaPlayer.init(mVideoPath, VIDEO_RENDER_ANWINDOW, surfaceHolder.getSurface());
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
        Log.d(TAG, "surfaceChanged() called with: surfaceHolder = [" + surfaceHolder + "], format = [" + format + "], w = [" + w + "], h = [" + h + "]");
        mMediaPlayer.play();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceDestroyed() called with: surfaceHolder = [" + surfaceHolder + "]");
        mMediaPlayer.stop();
        mMediaPlayer.unInit();

    }

    @Override
    public void onPlayerEvent(final int msgType, final float msgValue) {
        Log.d(TAG, "onPlayerEvent() called with: msgType = [" + msgType + "], msgValue = [" + msgValue + "]");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (msgType) {
                    case MSG_DECODER_INIT_ERROR:
                        break;
                    case MSG_DECODER_READY:
                        onDecoderReady();
                        break;
                    case MSG_DECODER_DONE:
                        break;
                    case MSG_REQUEST_RENDER:
                        break;
                    case MSG_DECODING_TIME:
                        if(!mIsTouch)
                            mSeekBar.setProgress((int) msgValue);
                        break;
                    default:
                        break;
                }
            }
        });

    }

    private void onDecoderReady() {
        int videoWidth = (int) mMediaPlayer.getMediaParams(MEDIA_PARAM_VIDEO_WIDTH);
        int videoHeight = (int) mMediaPlayer.getMediaParams(MEDIA_PARAM_VIDEO_HEIGHT);
        if(videoHeight * videoWidth != 0)
            mSurfaceView.setAspectRatio(videoWidth, videoHeight);

        int duration = (int) mMediaPlayer.getMediaParams(MEDIA_PARAM_VIDEO_DURATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mSeekBar.setMin(0);
        }
        mSeekBar.setMax(duration);
    }

    protected boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
