package cn.bmob.imdemo.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.imdemo.R;
import cn.bmob.imdemo.base.BaseActivity;
import cn.bmob.imdemo.util.ViewUtil;

public class FullScreenImageActivity extends BaseActivity {
    @Bind(R.id.iv_fullimage)
    ImageView iv_fullimage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);	//无title
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);	//全屏

        setContentView(R.layout.activity_full_screen_image);
        try {
            String imagePath = (String) getBundle().getSerializable("img");
            String imageUrl = (String) getBundle().getSerializable("url");
            //设置图片
            if (!TextUtils.isEmpty(imageUrl)) {//显示本地图片
                //不知道为什么图片的文件路径不能用在这个方法中，不知道需要的url路径是什么
                ViewUtil.setPicture(imageUrl, R.mipmap.ic_launcher, iv_fullimage, null);
            }
        }
        catch (Exception e){finish();}
    }

    @OnClick(R.id.iv_fullimage)
    public void onPicClick(View v){
        finish();
    }
}
