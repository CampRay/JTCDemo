package cn.bmob.imdemo.bean;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.List;

import cn.bmob.imdemo.R;
import cn.bmob.imdemo.ui.QuestionChatActivity;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.v3.BmobUser;

/**
 * Created by Phills on 5/23/2017.
 */

public class QuestionConversation extends Conversation {
    public static final int ConversationType=5;
    private BmobIMConversation conversation;
    private BmobIMMessage lastMsg;

    public QuestionConversation(BmobIMConversation conversation){
        this.cName="問答會話";
        this.conversation = conversation;
        cId = conversation.getConversationId();
        List<BmobIMMessage> msgs =conversation.getMessages();
        if(msgs!=null && msgs.size()>0){
            lastMsg =msgs.get(0);
        }
    }

    public String getCurrentUid(){
        return BmobUser.getCurrentUser(User.class).getObjectId();
    }
    //会话对象的图标
    @Override
    public Object getAvatar() {
        return R.mipmap.head0;
    }
    //会话对象的最后消息时间
    @Override
    public long getLastMessageTime() {
        if(lastMsg!=null) {
            return lastMsg.getCreateTime();
        }else{
            return 0;
        }
    }
    //会话对象的最后消息内容
    @Override
    public String getLastMessageContent() {
        if(lastMsg!=null){
            String content =lastMsg.getContent();
            String name =conversation.getConversationTitle() ;
            if(lastMsg.getFromId().equals(getCurrentUid())){
                name="你";
            }
            if(content.equals("QUESTION")){
                return name+"提出了問題";
            }else if(content.equals("OFFER")){
                return name+"提出了報價";
            }else if(content.equals("REFUSE")){
                return name+"拒絕解決問題";
            }else if(content.equals("PAY")){
                return name+"支付了報價";
            }else if(content.equals("CANCEL")){
                return name+"取消了問題";
            }else if(content.equals("ANSWER")){
                return name+"回答了問題";
            }else{//开发者自定义的消息类型，需要自行处理
                return "[未知消息]";
            }
//            Integer status =lastMsg.getStatus();
//            String name = lastMsg.getName();
//            String toName = lastMsg.getToName();
//
//            if(TextUtils.isEmpty(name)){
//                name = lastQuestion.getUid();
//            }
//            switch (status) {
//                case Config.STATUS_QUESTION_NONE:
//                    return name+"請求您幫忙解決此問題";
//                case Config.STATUS_QUESTION_READED:
//                    return toName+"已查看了此問題";
//                case Config.STATUS_QUESTION_OFFER:
//                    return toName+"已經接受此問題並提出了報價";
//                case Config.STATUS_QUESTION_REFUSE:
//                    return toName+"拒絕解決此問題";
//                case Config.STATUS_QUESTION_OFFER_ACCEPT:
//                    return name+"已經支付了報價";
//                case Config.STATUS_QUESTION_OFFER_REFUSE:
//                    return name+"已經取消了此問題";
//                case Config.STATUS_QUESTION_ANSWER:
//                    return toName+"已經回答了此問題";
//                case Config.STATUS_QUESTION_ANSWER_READED:
//                    return name+"已經查看了問題回複";
//                default:
//                    return "";
//            }

        }else{
            return "";
        }
    }

    @Override
    public int getUnReadCount() {
        return (int) BmobIM.getInstance().getUnReadCount(cId);
    }

    @Override
    public void readAllMessages() {
        //批量更新未读未认证的消息为已读状态

    }

    /**
     * 会话点击事件
     * @param context
     */
    @Override
    public void onClick(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, QuestionChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("c", conversation);
        if (bundle != null) {
            intent.putExtra(context.getPackageName(), bundle);
        }
        context.startActivity(intent);
    }

    @Override
    public void onLongClick(Context context) {
        //以下两种方式均可以删除会话
//        BmobIM.getInstance().deleteConversation(conversation.getConversationId());
        BmobIM.getInstance().deleteConversation(conversation);

        //删除本地数据库

    }
}
