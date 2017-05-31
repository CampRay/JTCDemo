package cn.bmob.imdemo.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.imdemo.R;
import cn.bmob.imdemo.base.ParentWithNaviActivity;

public class CounselorActivity extends ParentWithNaviActivity {
    @Bind(R.id.layout_counselor)
    LinearLayout ll_counselor1;
    @Bind(R.id.layout_counselor2)
    LinearLayout ll_counselor2;

    @Override
    protected String title() {
        return "諮詢專家列表";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counselor);
        initNaviView();
    }

    @OnClick({R.id.layout_counselor,R.id.layout_counselor2})
    public void onCounselorClick(View view) {
        startActivity(CounselorInfoActivity.class,null,false);
    }

    @Override
    public ParentWithNaviActivity.ToolBarListener setToolBarListener() {
        return new ParentWithNaviActivity.ToolBarListener() {
            @Override
            public void clickLeft() {
                finish();
            }

            @Override
            public void clickRight() {

            }
        };
    }


}
