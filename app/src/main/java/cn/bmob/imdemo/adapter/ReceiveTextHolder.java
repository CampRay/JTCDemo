package cn.bmob.imdemo.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.text.SimpleDateFormat;

import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.imdemo.R;
import cn.bmob.imdemo.adapter.base.BaseViewHolder;
import cn.bmob.imdemo.base.ImageLoaderFactory;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.v3.datatype.BmobFile;

/**
 * 接收到的文本类型
 */
public class ReceiveTextHolder extends BaseViewHolder {

  @Bind(R.id.iv_avatar)
  protected ImageView iv_avatar;

  @Bind(R.id.tv_time)
  protected TextView tv_time;

  @Bind(R.id.tv_message)
  protected TextView tv_message;

  public ReceiveTextHolder(Context context, ViewGroup root,OnRecyclerViewListener onRecyclerViewListener) {
    super(context, root, R.layout.item_chat_received_message,onRecyclerViewListener);
  }

  @OnClick({R.id.iv_avatar})
  public void onAvatarClick(View view) {

  }

  @Override
  public void bindData(Object o) {
    final BmobIMMessage message = (BmobIMMessage)o;
    String desc ="";
    BmobFile picture =null;
    BmobFile voice =null;
    BmobFile vedio =null;
    try {
      String extra = message.getExtra();
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
    final BmobIMUserInfo info = message.getBmobIMUserInfo();
    String content =  message.getContent();

    if(content.equals("QUESTION")){
      content="提出問題 （點擊查看）";
      ImageLoaderFactory.getLoader().loadAvator(iv_avatar,info != null ? info.getAvatar() : null, R.mipmap.head);
    }else if(content.equals("OFFER")){
      content="提出報價 - HK$"+desc;
      ImageLoaderFactory.getLoader().loadAvator(iv_avatar,info != null ? info.getAvatar() : null, R.mipmap.head2);
    }else if(content.equals("REFUSE")){
      content="拒絕處理";
      ImageLoaderFactory.getLoader().loadAvator(iv_avatar,info != null ? info.getAvatar() : null, R.mipmap.head2);
    }else if(content.equals("PAY")){
      content="支付成功";
      ImageLoaderFactory.getLoader().loadAvator(iv_avatar,info != null ? info.getAvatar() : null, R.mipmap.head);
    }else if(content.equals("CANCEL")){
      content="取消問題";
      ImageLoaderFactory.getLoader().loadAvator(iv_avatar,info != null ? info.getAvatar() : null, R.mipmap.head);
    }else if(content.equals("ANSWER")){
      content="回答問題 （點擊查看）";
      ImageLoaderFactory.getLoader().loadAvator(iv_avatar,info != null ? info.getAvatar() : null, R.mipmap.head2);
    }
    else{
      ImageLoaderFactory.getLoader().loadAvator(iv_avatar,info != null ? info.getAvatar() : null, R.mipmap.head);
    }
    tv_message.setText(content);

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
    String time = dateFormat.format(message.getCreateTime());
    tv_time.setText(time);
//
//    iv_avatar.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        //toast("点击" + info.getName() + "的头像");
//      }
//    });
    tv_message.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          //toast("点击"+message.getContent());
          if(onRecyclerViewListener!=null){
            //触发Activity页面层中的RecyclerView数据列表视图的对象点击事件
            onRecyclerViewListener.onItemClick(getAdapterPosition());
          }
        }
    });

    tv_message.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
          if (onRecyclerViewListener != null) {
            onRecyclerViewListener.onItemLongClick(getAdapterPosition());
          }
          return true;
        }
    });
  }

  public void showTime(boolean isShow) {
    tv_time.setVisibility(isShow ? View.VISIBLE : View.GONE);
  }
}