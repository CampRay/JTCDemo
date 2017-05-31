package cn.bmob.imdemo.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;

import cn.bmob.imdemo.R;

/**
 * Created by Phills on 5/24/2017.
 */

public class QuestionVoicePlayClickListener implements View.OnClickListener {

    ImageView iv_voice;
    private AnimationDrawable anim = null;
    Context mContext;
    MediaPlayer mediaPlayer = null;
    String localPath;

    public static boolean isPlaying = false;
    public static QuestionVoicePlayClickListener currentPlayListener = null;

    public QuestionVoicePlayClickListener(Context context, String localPath,ImageView voice) {
        this.iv_voice = voice;
        this.mContext = context.getApplicationContext();
        this.localPath=localPath;
        currentPlayListener = this;
    }

    @SuppressWarnings("resource")
    public void startPlayRecord(String filePath, boolean isUseSpeaker) {
        if (!(new File(filePath).exists())) {
            return;
        }
        AudioManager audioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = new MediaPlayer();
        if (isUseSpeaker) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        } else {
            audioManager.setSpeakerphoneOn(false);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        }

        try {
            mediaPlayer.reset();
            // 单独使用此方法会报错播放错误:setDataSourceFD failed.: status=0x80000000
            // mediaPlayer.setDataSource(filePath);
            // 因此采用此方式会避免这种错误
            FileInputStream fis = new FileInputStream(new File(filePath));
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer arg0) {
                    isPlaying = true;
                    arg0.start();
                    startRecordAnimation();
                }
            });
            mediaPlayer
                    .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            stopPlayRecord();
                        }

                    });
            currentPlayListener = this;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopPlayRecord() {
        stopRecordAnimation();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        isPlaying = false;
    }

    private void startRecordAnimation() {
        iv_voice.setImageResource(R.drawable.anim_chat_voice_right);
        anim = (AnimationDrawable) iv_voice.getDrawable();
        anim.start();
    }

    private void stopRecordAnimation() {
        iv_voice.setImageResource(R.mipmap.voice_left3);
        if (anim != null) {
            anim.stop();
        }
    }

    @Override
    public void onClick(View arg0) {
        if (isPlaying) {
            currentPlayListener.stopPlayRecord();
        }

        // 如果是收到的消息，则需要先下载后播放
        //String localPath = BmobDownloadManager.getDownLoadFilePath(message);
        startPlayRecord(localPath, true);
    }

}
