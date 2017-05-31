package cn.bmob.imdemo.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;

import butterknife.OnClick;
import cn.bmob.imdemo.R;
import cn.bmob.imdemo.base.ParentWithNaviActivity;
import cn.bmob.imdemo.bean.QuestionConversation;
import cn.bmob.imdemo.bean.User;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMUserInfo;

public class CounselorInfoActivity  extends ParentWithNaviActivity {

    User user;
    BmobIMUserInfo info;
    @Override
    protected String title() {
        return "吳敏君";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counselor_info);
        initNaviView();
        //因為是demo，使用一個預告創建的固定的專家用戶
        //user=(User)getBundle().getSerializable("u");
        user=new User();
        user.setObjectId("f798d4c742");
        user.setUsername("demo");
//        user.setObjectId("5e06cc4d63");
//        user.setUsername("lifei");
        if(user.getObjectId().equals(getCurrentUid())){
//            btn_add_friend.setVisibility(View.GONE);
//            btn_chat.setVisibility(View.GONE);
        }else{
//            btn_add_friend.setVisibility(View.VISIBLE);
//            btn_chat.setVisibility(View.VISIBLE);
        }
        //构造聊天方的用户信息:传入用户id、用户名和用户头像三个参数
        info = new BmobIMUserInfo(user.getObjectId(),user.getUsername(),user.getAvatar());

    }

    @Nullable
    @OnClick(R.id.btn_add_question)
    public void onClickBtnQuestion(View v){
        //启动一个常態会话，设置isTransient设置为false,则会在本地数据库的会话列表中先创建（如果没有）与该用户的会话信息，且将用户信息存储到本地的用户表中
        //暂态会话一般用于自定义消息的发送，比如，添加好友的请求，在对方还没有同意的情况下，你并不希望在自己的会话列表中显示该会话
        BmobIMConversation c = BmobIM.getInstance().startPrivateConversation(info,false,null);
        c.setConversationType(QuestionConversation.ConversationType);
        //c.setConversationTitle("錄製問題");
        Bundle bundle = new Bundle();
        bundle.putSerializable("c", c);
        bundle.putSerializable("s", 0);
        startActivity(QuestionActivity.class, bundle, false);
    }

    @OnClick(R.id.btn_add_cat)
    public void onClickBtnPrePay(View v){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("确定要支付吗？").setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BmobIMConversation c = BmobIM.getInstance().startPrivateConversation(info,false,null);
                c.setConversationType(QuestionConversation.ConversationType);
                Bundle bundle = new Bundle();
                bundle.putSerializable("c", c);
                startActivity(ChatActivity.class, bundle, false);

            }
        }).setNegativeButton("取消",null).show();

    }
}
