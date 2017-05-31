package cn.bmob.imdemo.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.imdemo.Config;
import cn.bmob.imdemo.R;
import cn.bmob.imdemo.adapter.QuestionVoicePlayClickListener;
import cn.bmob.imdemo.base.ParentWithNaviActivity;
import cn.bmob.imdemo.bean.QuestionConversation;
import cn.bmob.imdemo.util.Util;
import cn.bmob.imdemo.util.ViewUtil;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMTextMessage;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.core.BmobRecordManager;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.newim.listener.ObseverListener;
import cn.bmob.newim.listener.OnRecordChangeListener;
import cn.bmob.newim.notification.BmobNotificationManager;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

import static cn.bmob.newim.core.BmobIMClient.getContext;

public class QuestionActivity extends ParentWithNaviActivity implements ObseverListener {

    @Bind(R.id.ll_question)
    LinearLayout ll_question;
    @Bind(R.id.ll_attpic)
    LinearLayout ll_pic_layout;
    @Bind(R.id.ll_attvoice)
    LinearLayout ll_voice_layout;
    @Bind(R.id.ll_attvedio)
    LinearLayout ll_vedio_layout;
    @Bind(R.id.edit_msg)
    EditText edit_msg;
    @Bind(R.id.iv_pic)
    ImageView iv_pic;
    @Bind(R.id.iv_voice)
    ImageView iv_voice;
    @Bind(R.id.vv_vedio)
    VideoView vv_video;

    //附件按鈕
    @Bind(R.id.tv_picture)
    TextView tv_picture;
    @Bind(R.id.tv_camera)
    TextView tv_camera;
    @Bind(R.id.tv_vedio)
    TextView tv_vedio;
    @Bind(R.id.tv_mic)
    TextView tv_mic;


    // 语音有关
    @Bind(R.id.layout_record)
    RelativeLayout layout_record;
    @Bind(R.id.tv_voice_tips)
    TextView tv_voice_tips;
    @Bind(R.id.iv_record)
    ImageView iv_record;
    private Drawable[] drawable_Anims;// 话筒动画
    BmobRecordManager recordManager;

    BmobIMConversation c;
    BmobFile picFile;
    BmobFile voiceFile;
    BmobFile vedioFile;
    int sType=0;
    @Override
    protected String title() {
        return "錄製問題";
    }

    @Override
    public Object right() {
        return "提交";
        //return R.drawable.base_action_bar_add_bg_selector;
    }

    @Override
    public ParentWithNaviActivity.ToolBarListener setToolBarListener() {
        return new ParentWithNaviActivity.ToolBarListener() {
            @Override
            public void clickLeft() {finish();}

            @Override
            public void clickRight() {
                //提交錄製問題
                sendMessage();
            }
        };
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        c= BmobIMConversation.obtain(BmobIMClient.getInstance(), (BmobIMConversation) getBundle().getSerializable("c"));
        c.setConversationType(QuestionConversation.ConversationType);
        sType=(int)getBundle().getSerializable("s");
        initNaviView();
        initVoiceView();
        hideSoftInputView();
    }

    /**
     * 初始化语音布局
     * @param
     * @return void
     */
    private void initVoiceView() {
        tv_mic.setOnTouchListener(new VoiceTouchListener());
        initVoiceAnimRes();
        initRecordManager();
    }

    /**
     * 初始化语音动画资源
     * @Title: initVoiceAnimRes
     * @param
     * @return void
     */
    private void initVoiceAnimRes() {
        drawable_Anims = new Drawable[] {
                getResources().getDrawable(R.mipmap.chat_icon_voice2),
                getResources().getDrawable(R.mipmap.chat_icon_voice3),
                getResources().getDrawable(R.mipmap.chat_icon_voice4),
                getResources().getDrawable(R.mipmap.chat_icon_voice5),
                getResources().getDrawable(R.mipmap.chat_icon_voice6) };
    }

    /**
     * 初始化语音管理器,设置相关事件处理
     */
    private void initRecordManager(){
        // 语音相关管理器
        recordManager = BmobRecordManager.getInstance(this);
        // 设置音量大小监听--在这里开发者可以自己实现：当剩余10秒情况下的给用户的提示，类似微信的语音那样
        recordManager.setOnRecordChangeListener(new OnRecordChangeListener() {

            @Override
            public void onVolumnChanged(int value) {
                iv_record.setImageDrawable(drawable_Anims[value]);
            }

            @Override
            public void onTimeChanged(int recordTime, String localPath) {
                Logger.i("voice", "已录音长度:" + recordTime);
                if (recordTime >= BmobRecordManager.MAX_RECORD_TIME) {// 1分钟结束，发送消息
                    // 需要重置按钮
                    tv_mic.setPressed(false);
                    tv_mic.setClickable(false);
                    // 取消录音框
                    layout_record.setVisibility(View.INVISIBLE);
                    // 发送语音消息
                    uploadFile(localPath, 1);
                    //是为了防止过了录音时间后，会多发一条语音出去的情况。
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            tv_mic.setClickable(true);
                        }
                    }, 1000);
                }
            }
        });
    }

    /**
     * 长按说话
     * @author smile
     * @date 2014-7-1 下午6:10:16
     */
    class VoiceTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!Util.checkSdCard()) {
                        toast("發送語音需要sdcard支持！");
                        return false;
                    }
                    try {
                        v.setPressed(true);
                        layout_record.setVisibility(View.VISIBLE);
                        tv_voice_tips.setText(getString(R.string.voice_cancel_tips));
                        // 开始录音
                        recordManager.startRecording(c.getConversationId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                case MotionEvent.ACTION_MOVE: {
                    if (event.getY() < 0) {
                        tv_voice_tips.setText(getString(R.string.voice_cancel_tips));
                        tv_voice_tips.setTextColor(Color.RED);
                    } else {
                        tv_voice_tips.setText(getString(R.string.voice_up_tips));
                        tv_voice_tips.setTextColor(Color.WHITE);
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                    v.setPressed(false);
                    layout_record.setVisibility(View.INVISIBLE);
                    try {
                        if (event.getY() < 0) {// 放弃录音
                            recordManager.cancelRecording();
                            Logger.i("voice", "放棄錄製語音");
                        } else {
                            int recordTime = recordManager.stopRecording();
                            if (recordTime > 1) {
                                // 上傳语音文件
                                uploadFile(recordManager.getRecordFilePath(c.getConversationId()),1);
                            } else {// 录音时间过短，则提示录音过短的提示
                                layout_record.setVisibility(View.GONE);
                                showShortToast().show();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                default:
                    return false;
            }
        }
    }



    Toast toast;

    /**
     * 显示录音时间过短的Toast
     * @Title: showShortToast
     * @return void
     */
    private Toast showShortToast() {
        if (toast == null) {
            toast = new Toast(this);
        }
        View view = LayoutInflater.from(this).inflate(
                R.layout.include_chat_voice_short, null);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        return toast;
    }

    private String pictureFilePath;
    //点击附加的图片对象事件处理
    @OnClick(R.id.iv_pic)
    public void onAttrPicClick(View view){
        Bundle bundle = new Bundle();
        bundle.putSerializable("url", pictureFilePath);
        startActivity(FullScreenImageActivity.class, bundle, false);
        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
    }

    //點圖片按鈕
    @OnClick(R.id.tv_picture)
    public void onPictureClick(View view){
        selectImageFromLocal();
    }
    //點照像按鈕
    @OnClick(R.id.tv_camera)
    public void onCameraClick(View view){
        selectImageFromCamera();
    }

    //點攝像按鈕
    @OnClick(R.id.tv_vedio)
    public void onVedioClick(View view){
        selectVedioFromCamera();
    }

    /**
     * 选择图片
     * @Title: selectImage
     * @Description: TODO
     * @param
     * @return void
     * @throws
     */
    public void selectImageFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, Config.REQUESTCODE_TAKE_LOCAL);
    }

    private String localCameraPath = "";// 拍照后得到的图片地址
    /**
     * 启动相机拍照 startCamera
     *
     * @Title: startCamera
     * @throws
     */
    public void selectImageFromCamera() {
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //使用外部存儲，調用相機必須用外部存儲
        String state = Environment.getExternalStorageState(); //拿到sdcard是否可用的状态码
        if (state.equals(Environment.MEDIA_MOUNTED)){   //如果可用
            File dir = new File(Config.JTC_PICTURE_PATH);
            //這是獲取外部存儲上的應用程序目錄，會隨程序一起刪除
            //File dir =getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, String.valueOf(System.currentTimeMillis()) + ".jpg");
//        //使用項目內容存儲
//        File file = new File(getContext().getFilesDir(),String.valueOf(System.currentTimeMillis()) + ".jpg");

            localCameraPath = file.getPath();
            //toast(localCameraPath);
            Uri imageUri = Uri.fromFile(file);
            openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(openCameraIntent,Config.REQUESTCODE_TAKE_CAMERA);
        }else {
            toast("沒有sdcard");
        }

    }

    private String localVedioPath = "";// 錄像后得到的图片地址
    public void selectVedioFromCamera() {
        Intent openVedioIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        //使用外部存儲，調用相機必須用外部存儲
        String state = Environment.getExternalStorageState(); //拿到sdcard是否可用的状态码
        if (state.equals(Environment.MEDIA_MOUNTED)){   //如果可用
            File dir = new File(Config.JTC_PICTURE_PATH);
            //這是獲取外部存儲上的應用程序目錄，會隨程序一起刪除
            //File dir =getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, String.valueOf(System.currentTimeMillis()));
//        //使用項目內容存儲
//        File file = new File(getContext().getFilesDir(),String.valueOf(System.currentTimeMillis()) + ".jpg");

            localVedioPath = file.getPath();
            //toast(localCameraPath);
            Uri vedioUri = Uri.fromFile(file);
            //openVedioIntent.putExtra(MediaStore.EXTRA_OUTPUT, vedioUri);
            //MediaStore.EXTRA_VIDEO_QUALITY此值在最低质量最小文件尺寸时是0，在最高质量最大文件尺寸时是１.
            openVedioIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY ,0);
            openVedioIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
            startActivityForResult(openVedioIntent,Config.REQUESTCODE_TAKE_VEDIO);
        }else {
            toast("沒有sdcard");
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Config.REQUESTCODE_TAKE_CAMERA:// 当取到值的时候才上传path路径下的图片到服务器
                    uploadFile(localCameraPath,0);
                    break;
                case Config.REQUESTCODE_TAKE_LOCAL:
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(
                                    selectedImage, null, null, null, null);
                            cursor.moveToFirst();
                            int columnIndex = cursor.getColumnIndex("_data");
                            String localSelectPath = cursor.getString(columnIndex);
                            cursor.close();
                            if (localSelectPath == null
                                    || localSelectPath.equals("null")) {
                                toast("找不到你想要的圖片");
                                return;
                            }
                            //toast(localSelectPath);
                            uploadFile(localSelectPath,0);
                        }
                    }
                    break;
                case Config.REQUESTCODE_TAKE_VEDIO:// 当取到值的时候才上传path路径下的图片到服务器
                    if (data != null) {
                        Uri  uri = data.getData();//可以通过这个播放
                        Uri uriVideo = data.getData();
                       Cursor cursor=this.getContentResolver().query(uriVideo, null, null, null, null);
                        if (cursor.moveToNext()) {
                            /* _data：文件的绝对路径 ，_display_name：文件名 */
                            String strVideoPath = cursor.getString(cursor.getColumnIndex("_data"));
                            cursor.close();
                            if (strVideoPath == null|| strVideoPath.equals("null")) {
                                toast("找不到視頻文件");
                                return;
                            }
                            //toast(strVideoPath);
                            uploadFile(strVideoPath,2);
                        }


                    }
                    break;
            }
        }
    }

    public void uploadFile(final String filePath,final int type){
        final BmobFile bmobFile = new BmobFile(new File(filePath));
        final Context thisActivity=this;
        bmobFile.uploadblock(new UploadFileListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void done(BmobException e) {
                if(e==null){
                    switch(type){
                        case 0:
                            picFile=bmobFile;
                            pictureFilePath=bmobFile.getUrl();
//                            Bitmap bm = BitmapFactory.decodeFile(filePath);
//                            iv_pic.setImageBitmap(bm);
                            ViewUtil.setPicture(pictureFilePath, R.mipmap.ic_launcher, iv_pic, null);
                            ll_pic_layout.setVisibility(View.VISIBLE);

                            break;
                        case 1:
                            voiceFile=bmobFile;
                            ll_voice_layout.setVisibility(View.VISIBLE);
                            iv_voice.setOnClickListener(new QuestionVoicePlayClickListener(getContext(),filePath,iv_voice));
                            break;
                        case 2:
                            vedioFile=bmobFile;
                            ll_vedio_layout.setVisibility(View.VISIBLE);
                            //设置视频控制器,VideoView通过与MediaController类结合使用，开发者可以不用自己控制播放与暂停
                            vv_video.setMediaController(new MediaController(thisActivity));
                            //设置视频路径
                            //Uri uri = Uri.parse( filePath );
                            //vv_video.setVideoURI(uri);
                            vv_video.setVideoPath(filePath);
                            vv_video.pause();

//                            //播放完成回调
//                            vv_video.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
//                                //播放完成了
//                                @Override
//                                public void onCompletion(MediaPlayer mp) {}
//                            });
//                            vv_video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                                @Override
//                                public void onPrepared(MediaPlayer mp) {
//                                    mp.start();// 播放
//                                }
//                            });
                            break;
                        default:
                            break;
                    }

                    //bmobFile.getFileUrl()--返回的上传文件的服務器完整地址
                    //toast("上傳文件成功:" + bmobFile.getFileUrl());
                }else{
                    toast("上傳文件失敗：" + e.getMessage());
                }
            }
            @Override
            public void onProgress(Integer value) {
                // 返回的上传进度（百分比）
            }
        });
    }

    /**
     * 下載上傳的文件
     * @param file
     */
    private void downloadFile(BmobFile file){
        //允许设置下载文件的存储路径，默认下载文件的目录为：context.getApplicationContext().getCacheDir()+"/bmob/"
        File saveFile = new File(Environment.getExternalStorageDirectory(), file.getFilename());
        file.download(saveFile, new DownloadFileListener() {
            @Override
            public void onStart() {
                toast("開始下載...");
            }
            @Override
            public void done(String savePath,BmobException e) {
                if(e==null){
                    toast("下載成功，保存路徑:"+savePath);
                }else{
                    toast("下載失败："+e.getErrorCode()+","+e.getMessage());
                }
            }
            @Override
            public void onProgress(Integer value, long newworkSpeed) {
                Log.i("bmob","下載进度："+value+","+newworkSpeed);
            }
        });
    }

    /**
     * 刪除上傳的文件
     * @param file
     */
    private void deleteFile(BmobFile file){
        file.delete(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e==null){
                    toast("文件删除成功");
                }else{
                    toast("文件删除失败："+e.getErrorCode()+","+e.getMessage());
                }
            }
        });
    }

    /**
     * 发送文本消息
     */
    private void sendMessage(){
        String text=edit_msg.getText().toString();
//        if(TextUtils.isEmpty(text.trim())){
//            toast("請輸入消息內容");
//            return;
//        }
        BmobIMTextMessage msg =new BmobIMTextMessage();
        //msg.setMsgType("QUESTION");
        if(sType==1) {
            msg.setContent("ANSWER");
        }
        else{
            msg.setContent("QUESTION");
        }
        //可设置额外信息
        Map<String,Object> map =new HashMap<>();
        map.put("desc", text);
        map.put("picture", picFile==null?"":picFile.getUrl());
        map.put("voice", voiceFile==null?"":voiceFile.getUrl());
        map.put("vedio", vedioFile==null?"":vedioFile.getUrl());
        msg.setExtraMap(map);
        c.sendMessage(msg, listener);
    }


    /**
     * 消息发送监听器
     */
    public MessageSendListener listener =new MessageSendListener() {

        @Override
        public void onProgress(int value) {
            super.onProgress(value);
            //文件类型的消息才有进度值
            Logger.i("onProgress："+value);
        }

        @Override
        public void onStart(BmobIMMessage msg) {
            super.onStart(msg);
        }

        @Override
        public void done(BmobIMMessage msg, BmobException e) {
            //toast("問題提交成功");
            startActivity(MainActivity.class,null,true);
            //finish();
            if (e != null) {
                toast(e.getMessage());
            }
        }
    };





    @Override
    protected void onResume() {
        BmobNotificationManager.getInstance(this).cancelNotification();
        super.onResume();
    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //清理资源
        if(recordManager!=null){
            recordManager.clear();
        }
        hideSoftInputView();
        super.onDestroy();
    }
}
