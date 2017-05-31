package cn.bmob.imdemo.ui;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.imdemo.R;
import cn.bmob.imdemo.adapter.QuestionVoicePlayClickListener;
import cn.bmob.imdemo.base.ParentWithNaviActivity;
import cn.bmob.imdemo.bean.QuestionConversation;
import cn.bmob.imdemo.util.ViewUtil;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMTextMessage;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.newim.listener.ObseverListener;
import cn.bmob.newim.notification.BmobNotificationManager;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import static cn.bmob.newim.core.BmobIMClient.getContext;

public class QuestionReviewActivity extends ParentWithNaviActivity implements ObseverListener {

    @Bind(R.id.ll_attpic)
    LinearLayout ll_pic_layout;
    @Bind(R.id.ll_attvoice)
    LinearLayout ll_voice_layout;
    @Bind(R.id.ll_attvedio)
    LinearLayout ll_vedio_layout;

    @Bind(R.id.edit_msg)
    TextView edit_msg;
    @Bind(R.id.iv_pic)
    ImageView iv_pic;
    @Bind(R.id.iv_voice)
    ImageView iv_voice;
    @Bind(R.id.vv_vedio)
    VideoView vv_video;

    //報價按鈕
    @Bind(R.id.ll_quote)
    LinearLayout ll_quote;
    @Bind(R.id.tv_quote)
    TextView tv_quote;
    @Bind(R.id.tv_refuse)
    TextView tv_refuse;


    BmobIMConversation c;

    String pictureUrl;
    String vedioUrl;

    AlertDialog dialog;
    @Override
    protected String title() {
        return "查看問題";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_review);

        c= BmobIMConversation.obtain(BmobIMClient.getInstance(), (BmobIMConversation) getBundle().getSerializable("c"));
        c.setConversationType(QuestionConversation.ConversationType);
        BmobIMMessage msg=(BmobIMMessage) getBundle().getSerializable("m");
        int s=(int) getBundle().getSerializable("s");;
        if(s==1) {
            ll_quote.setVisibility(View.VISIBLE);
        }
        else{
            ll_quote.setVisibility(View.GONE);
        }
        initNaviView();
        initQuestionContentView(msg);
        hideSoftInputView();
    }

    //加載問題內容
    public void initQuestionContentView(BmobIMMessage msg){
        String desc ="";
        try {
            String extra = msg.getExtra();
            if(!TextUtils.isEmpty(extra)){
                JSONObject json =new JSONObject(extra);
                desc = json.getString("desc");
                pictureUrl = json.getString("picture");
                String voiceUrl = json.getString("voice");
                vedioUrl = json.getString("vedio");
                edit_msg.setText(desc.toCharArray(),0,desc.length());
                if(!pictureUrl.isEmpty()){
                    //這是獲取外部存儲上的應用程序目錄，會隨程序一起刪除
//                    File dir =getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
//                    if (!dir.exists()) {
//                        dir.mkdirs();
//                    }
//                    File saveFile = new File(dir, voice.getFilename());
//                    if(saveFile.exists()){
//                        saveFile.delete();
//                    }
//                    BmobFile picture=new BmobFile(System.currentTimeMillis()+".jpg","",pictureUrl);
//                    picture.download(saveFile, null);
                    ll_pic_layout.setVisibility(View.VISIBLE);

                    ViewUtil.setPicture(pictureUrl,R.mipmap.counselor,iv_pic,null);

                }
                if(!voiceUrl.isEmpty()){
                    BmobFile voice=new BmobFile(System.currentTimeMillis()+".amr","",voiceUrl);
                    //這是獲取外部存儲上的應用程序目錄，會隨程序一起刪除
                    File dir =getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File saveFile = new File(dir, voice.getFilename());
                    if(saveFile.exists()){
                        saveFile.delete();
                    }
                    voice.download(saveFile, null);
                    ll_voice_layout.setVisibility(View.VISIBLE);
                    iv_voice.setOnClickListener(new QuestionVoicePlayClickListener(getContext(),saveFile.getPath(),iv_voice));
                }
                if(!vedioUrl.isEmpty()){
                    Uri uri=Uri.parse(vedioUrl);
                    ll_vedio_layout.setVisibility(View.VISIBLE);
                    //设置视频控制器,VideoView通过与MediaController类结合使用，开发者可以不用自己控制播放与暂停
                    vv_video.setMediaController(new MediaController(this));
                    //设置视频路径
                    vv_video.setVideoURI(uri);
                    vv_video.pause();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //点击查看图片
    @OnClick(R.id.iv_pic)
    public void onPicClick(View view){
        Bundle bundle = new Bundle();
        bundle.putSerializable("url", pictureUrl);
        startActivity(FullScreenImageActivity.class, bundle, false);
        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
    }

    //報價按鈕
    @OnClick(R.id.tv_quote)
    public void onQuoteClick(View view){
        final EditText quoteText=new EditText(this);
        quoteText.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        dialog=builder.setTitle("請輸入報價").setIcon(android.R.drawable.ic_dialog_info).setView(quoteText).setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hideSoftInputView();
                String text=quoteText.getText().toString();
                BmobIMTextMessage msg =new BmobIMTextMessage();
                msg.setContent("OFFER");
                //可设置额外信息
                Map<String,Object> map =new HashMap<>();
                map.put("desc", text);
                msg.setExtraMap(map);
                c.sendMessage(msg, listener);
                dialog.dismiss();
            }
        }).setNegativeButton("取消",null).show();

    }
    //點拒絕按鈕
    @OnClick(R.id.tv_refuse)
    public void onRefuseClick(View view){
        BmobIMTextMessage msg =new BmobIMTextMessage();
        msg.setContent("REFUSE");
        c.sendMessage(msg, listener);
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
     * 发送報價消息：
     * type: 0，取消， 1， 報價
     */
    private void sendMessage(int type, String price){
        BmobIMTextMessage msg =new BmobIMTextMessage();
        if(type==1) {
            msg.setContent("OFFER");
            //可设置额外信息
            Map<String, Object> map = new HashMap<>();
            map.put("desc", price);
            msg.setExtraMap(map);
        }
        else{
            msg.setContent("REFUSE");
        }
        c.sendMessage(msg, listener);
    }


    /**
     * 消息发送监听器
     */
    public MessageSendListener listener =new MessageSendListener() {

        @Override
        public void onProgress(int value) {
            super.onProgress(value);
        }

        @Override
        public void onStart(BmobIMMessage msg) {
            super.onStart(msg);
        }

        @Override
        public void done(BmobIMMessage msg, BmobException e) {
            finish();
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
        hideSoftInputView();
        super.onDestroy();
    }
}
