package com.lanshifu.millionherohelper.network;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * rx观察者封装，只需重写成功或者失败两个方法
 * Created by lanxiaobin on 2017/9/6.
 */

public abstract class MyObserver<T> implements Observer<T> {


    @Override
    public void onError(Throwable e) {
        String msg = e.getMessage();
        _onError(msg);
    }

    @Override
    public void onNext(T t) {
        _onNext(t);
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    public abstract void _onNext(T t);

    public abstract void _onError(String e);

}
