package com.lanshifu.millionherohelper.mvp.view;

import com.lanshifu.baselibrary.basemvp.BaseView;
import com.lanshifu.millionherohelper.bean.ModeDB;

import java.util.List;

/**
 * Created by lanshifu on 2018/1/14.
 */

public interface MainView extends BaseView {

    void hasRootPermission(boolean root);

    void heroResult(String result, long time);

    void heroError(String text);

    void updateDBSuccess(List<ModeDB> modeDBList);
}
