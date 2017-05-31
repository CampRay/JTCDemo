package cn.bmob.imdemo.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.imdemo.R;
import cn.bmob.imdemo.base.BaseFragment;
import cn.bmob.imdemo.ui.CounselorActivity;
import cn.bmob.imdemo.ui.CounselorInfoActivity;


public class HomeFragment extends BaseFragment {
    protected View rootView = null;

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView= inflater.inflate(R.layout.fragment_home,container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @OnClick({R.id.btn_service_love,R.id.btn_service_stock,R.id.btn_service_invest,R.id.btn_service_beauty,R.id.btn_service_work,R.id.btn_service_health,R.id.btn_service_law,R.id.btn_service_more})
    public void onServiceClick(View v) {
        startActivity(CounselorActivity.class,null);
    }

    @OnClick(R.id.iv_online)
    public void onCounselorClick(View v) {
        startActivity(CounselorInfoActivity.class,null);
    }
}
