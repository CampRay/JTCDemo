package cn.bmob.imdemo.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.imdemo.R;
import cn.bmob.imdemo.adapter.ChatAdapter;
import cn.bmob.imdemo.adapter.OnRecyclerViewListener;
import cn.bmob.imdemo.base.ParentWithNaviActivity;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMTextMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.listener.MessageListHandler;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.newim.listener.MessagesQueryListener;
import cn.bmob.newim.listener.ObseverListener;
import cn.bmob.newim.notification.BmobNotificationManager;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;

/**聊天界面
 * @author :smile
 * @project:ChatActivity
 * @date :2016-01-25-18:23
 */
public class QuestionChatActivity extends ParentWithNaviActivity implements ObseverListener,MessageListHandler{

    @Bind(R.id.ll_question_chat)
    LinearLayout ll_question_chat;
    @Bind(R.id.ll_pay)
    LinearLayout ll_pay;
    @Bind(R.id.ll_answer)
    LinearLayout ll_answer;

    @Bind(R.id.sw_refresh)
    SwipeRefreshLayout sw_refresh;

    @Bind(R.id.rc_view)
    RecyclerView rc_view;

//    @Bind(R.id.edit_msg)
//    EditText edit_msg;
    //支付按鈕
    @Bind(R.id.tv_pay)
    TextView tv_pay;
    //拒絕按鈕
    @Bind(R.id.tv_cancel)
    TextView tv_cancel;





    ChatAdapter adapter;
    protected LinearLayoutManager layoutManager;
    BmobIMConversation c;

    @Override
    protected String title() {
        return c.getConversationTitle();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_chat);
        c= BmobIMConversation.obtain(BmobIMClient.getInstance(), (BmobIMConversation) getBundle().getSerializable("c"));
        initNaviView();
        initSwipeLayout();
    }

    private void initSwipeLayout(){
        sw_refresh.setEnabled(true);
        layoutManager = new LinearLayoutManager(this);
        rc_view.setLayoutManager(layoutManager);
        adapter = new ChatAdapter(this,c);
        rc_view.setAdapter(adapter);
        ll_question_chat.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ll_question_chat.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                sw_refresh.setRefreshing(true);
                //自动刷新
                queryMessages(null);
            }
        });
        //RecyclerView下拉加载
        sw_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                BmobIMMessage msg = adapter.getFirstMessage();
                queryMessages(msg);
            }
        });
        //设置RecyclerView的点击事件
        adapter.setOnRecyclerViewListener(new OnRecyclerViewListener() {
            //點擊某個消息，通過判斷消息類型來決定如何處理
            @Override
            public void onItemClick(int position) {
                BmobIMMessage msg=adapter.getItem(position);
                String desc ="";
                BmobFile picture =null;
                BmobFile voice =null;
                BmobFile vedio =null;
                try {
                    String extra = msg.getExtra();
                    if(!TextUtils.isEmpty(extra)){
                        JSONObject json =new JSONObject(extra);
                        desc = json.getString("desc");
                        picture = (BmobFile)json.get("picture");
                        voice = (BmobFile)json.get("voice");
                        vedio = (BmobFile)json.get("vedio");

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final BmobIMUserInfo info = msg.getBmobIMUserInfo();
                String content =  msg.getContent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("c", c);
                bundle.putSerializable("m", msg);

                //是否是提問人
                boolean isUser=(msg.getFromId().equals(getCurrentUid()));
                if(content.equals("QUESTION")){
                    //如果是專家，並且沒有報過價
                    if(!isUser) {
                        //s:0 不能報價；1 可以報價
                        bundle.putSerializable("s", 1);
                    }
                    else{
                        bundle.putSerializable("s", 0);
                    }
                    startActivity(QuestionReviewActivity.class, bundle);
                }else if(content.equals("ANSWER")){
                    bundle.putSerializable("s", 0);
                    startActivity(QuestionReviewActivity.class, bundle);
                }

            }

            @Override
            public boolean onItemLongClick(int position) {
                //这里省了个懒，直接长按就删除了该消息
                c.deleteMessage(adapter.getItem(position));
                adapter.remove(position);
                return true;
            }
        });
    }

    Toast toast;

    @OnClick(R.id.tv_pay)
    public void onPayClick(View view){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("确定要支付吗？").setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BmobIMTextMessage msg =new BmobIMTextMessage();
                msg.setContent("PAY");
                c.sendMessage(msg, listener);

            }
        }).setNegativeButton("取消",null).show();

    }

    @OnClick(R.id.tv_cancel)
    public void onCancelClick(View view){
        BmobIMTextMessage msg =new BmobIMTextMessage();
        msg.setContent("CANCEL");
        c.sendMessage(msg, listener);
    }

    //回答问题按钮事件
    @OnClick(R.id.ll_answer)
    public void onAnswerClick(View view){
        Bundle bundle = new Bundle();
        bundle.putSerializable("c", c);
        //傳入是否是回答問題的參數
        bundle.putSerializable("s", 1);
        startActivity(QuestionActivity.class, bundle);
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
            adapter.addMessage(msg);
            scrollToBottom();
        }

        @Override
        public void done(BmobIMMessage msg, BmobException e) {
            adapter.notifyDataSetChanged();
            ll_pay.setVisibility(View.INVISIBLE);
            scrollToBottom();
            if (e != null) {
                toast(e.getMessage());
            }
        }
    };

    /**首次加载，可设置msg为null，下拉刷新的时候，默认取消息表的第一个msg作为刷新的起始时间点，默认按照消息时间的降序排列
     * @param msg
     */
    public void queryMessages(BmobIMMessage msg){
        c.queryMessages(msg, 10, new MessagesQueryListener() {
            @Override
            public void done(List<BmobIMMessage> list, BmobException e) {
                sw_refresh.setRefreshing(false);
                if (e == null) {
                    if (null != list && list.size() > 0) {
                        adapter.addMessages(list);
                        layoutManager.scrollToPositionWithOffset(list.size() - 1, 0);
                        //獲取最後的消息
                        BmobIMMessage msg=adapter.getItem(list.size()-1);
                        String content =  msg.getContent();
                        //是否是提問人
                        boolean isUser=msg.getFromId().equals(getCurrentUid());
                        if(content.equals("OFFER")) {
                            if (!isUser) {
                                //要顯示支付按鈕
                                ll_pay.setVisibility(View.VISIBLE);
                            } else {
                                //不顯示支付按鈕
                                ll_pay.setVisibility(View.GONE);
                            }
                        }
                        else if(content.equals("PAY")) {
                            if (!isUser) {
                                //要顯示回答按鈕
                                ll_answer.setVisibility(View.VISIBLE);
                            } else {
                                //不顯示回答按鈕
                                ll_answer.setVisibility(View.GONE);
                            }
                        }
                        else{
                            ll_pay.setVisibility(View.GONE);
                            ll_answer.setVisibility(View.GONE);
                        }
                    }
                } else {
                    toast(e.getMessage() + "(" + e.getErrorCode() + ")");
                }
            }
        });
    }

    private void scrollToBottom() {
        layoutManager.scrollToPositionWithOffset(adapter.getItemCount() - 1, 0);
    }

    @Override
    public void onMessageReceive(List<MessageEvent> list) {
        Logger.i("聊天页面接收到消息：" + list.size());
        //当注册页面消息监听时候，有消息（包含离线消息）到来时会回调该方法
        for (int i=0;i<list.size();i++){
            addMessage2Chat(list.get(i));
        }
    }


    /**添加消息到聊天界面中
     * @param event
     */
    private void addMessage2Chat(MessageEvent event){
        BmobIMMessage msg =event.getMessage();
        if(c!=null && event!=null && c.getConversationId().equals(event.getConversation().getConversationId()) //如果是当前会话的消息
                && !msg.isTransient()){//并且不为暂态消息
            if(adapter.findPosition(msg)<0){//如果未添加到界面中
                adapter.addMessage(msg);
                //更新该会话下面的已读状态
                c.updateReceiveStatus(msg);
            }
            scrollToBottom();
        }else{
            Logger.i("不是与当前聊天对象的消息");
        }
    }


    @Override
    protected void onResume() {
        //锁屏期间的收到的未读消息需要添加到聊天界面中
        addUnReadMessage();
        //添加页面消息监听器
        BmobIM.getInstance().addMessageListHandler(this);
        // 有可能锁屏期间，在聊天界面出现通知栏，这时候需要清除通知
        BmobNotificationManager.getInstance(this).cancelNotification();
        super.onResume();
    }

    /**
     * 添加未读的通知栏消息到聊天界面
     */
    private void addUnReadMessage(){
        List<MessageEvent> cache = BmobNotificationManager.getInstance(this).getNotificationCacheList();
        if(cache.size()>0){
            int size =cache.size();
            for(int i=0;i<size;i++){
                MessageEvent event = cache.get(i);
                addMessage2Chat(event);
            }
        }
        scrollToBottom();
    }

    @Override
    protected void onPause() {
        //移除页面消息监听器
        BmobIM.getInstance().removeMessageListHandler(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //更新此会话的所有消息为已读状态
        if(c!=null){
            c.updateLocalCache();
        }
        hideSoftInputView();
        super.onDestroy();
    }

}
