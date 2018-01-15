package com.lanshifu.millionherohelper;

import android.view.View;
import android.widget.TextView;

import com.lanshifu.baselibrary.base.BaseActivity;
import com.lanshifu.millionherohelper.mvp.presenter.MainPresenter;
import com.lanshifu.millionherohelper.mvp.view.MainView;
import com.yhao.floatwindow.FloatWindow;
import com.yhao.floatwindow.Screen;

import org.w3c.dom.Text;

public class MainActivity extends BaseActivity<MainPresenter> implements MainView {


    private TextView mTv_result;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initPresenter() {
        super.initPresenter();
        mPresenter.setView(this);
    }

    @Override
    protected void initView() {
        setTitleText("百万英雄辅助");
        hideBackIcon();

        openFlow();
        mPresenter.checkRootPermission();
    }

    private void openFlow() {
        View view = View.inflate(this, R.layout.layout_flowview, null);
        mTv_result = view.findViewById(R.id.tv_result);
        view.findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTv_result.setText("正在查询...");
                mPresenter.getScreenAndParseText();
            }
        });

        FloatWindow
                .with(MainApplication.getContext())
                .setView(view)
                .setX(100)                       //100px
                .setY(Screen.height, 0.3f)        //屏幕高度的 30%
                .setDesktopShow(true)
                .build();
    }


    @Override
    public void hasRootPermission(boolean root) {
        if (!root){
            FloatWindow.destroy();
        }

    }

    @Override
    public void heroResult(String result, long time) {
        mTv_result.setText(result);
        mTv_result.append("\n耗时："+time);
    }

    @Override
    public void heroError(String text) {
        mTv_result.setText("出错了："+text);
    }

    @Override
    public void showProgressDialog(String s) {

    }

    @Override
    public void hideProgressDialog() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FloatWindow.destroy();
    }
}
