package com.lanshifu.millionherohelper.mvp.presenter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.jaredrummler.android.shell.Shell;
import com.lanshifu.baselibrary.basemvp.BasePresenter;
import com.lanshifu.baselibrary.log.LogHelper;
import com.lanshifu.baselibrary.network.RxScheduler;
import com.lanshifu.baselibrary.utils.FileUtil;
import com.lanshifu.baselibrary.utils.ToastUtil;
import com.lanshifu.millionherohelper.MainApplication;
import com.lanshifu.millionherohelper.SPUtil;
import com.lanshifu.millionherohelper.bean.ModeDB;
import com.lanshifu.millionherohelper.mvp.view.MainView;
import com.lanshifu.millionherohelper.network.MyObserver;
import com.lanshifu.millionherohelper.network.RetrofitHelper;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import okhttp3.ResponseBody;

/**
 * Created by lanshifu on 2018/1/14.
 */

public class MainPresenter extends BasePresenter<MainView> {

    private static final String PLUGIN_NAME = "imageplugin.apk";
    private String mTempPicPath;
    private String mPicturePath;
    private HashMap<String, Integer> mAnswers;
    private long starTime = 0;

    private int mCounter = 0;
    private final static int MODE_MILLION = 0;
    private final static int MODE_HUANGJINWU = 1;
    private int mCurrentMode = 0;


    public void checkRootPermission() {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> e) throws Exception {
                e.onNext(Shell.SU.available());
            }
        }).compose(RxScheduler.<Boolean>io_main())
                .subscribe(new MyObserver<Boolean>() {
                    @Override
                    public void _onNext(Boolean root) {
                        mView.hasRootPermission(root);
                        initBaiduOrc();
                        if (root) {
                        } else {
                            ToastUtil.showShortToast("手机没有root权限，无法使用部分功能");
                        }
                    }

                    @Override
                    public void _onError(String e) {
                        ToastUtil.showShortToast(e);
                    }
                });
    }

    public void initBaiduOrc() {
        OCR.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                // 调用成功，返回AccessToken对象
                String token = result.getAccessToken();
                LogHelper.d("lxb ->" + result.getAccessToken());
            }

            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError子类SDKError对象
                LogHelper.e("lxb ->" + error.getMessage());
            }
        }, MainApplication.getContext(), "DZYaZQ9esTBGtOnXR4mCccU4", "9G1qzWVLLuG5xxSPZgP3sPx4KchK2g1i");
    }


    public void cropBitmap(Bitmap bitmap, String savePath) {
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        mCurrentMode = SPUtil.getInstance().getInt(SPUtil.KEY_MODE);
        int x, y, width, height;
        if (mCurrentMode == MODE_MILLION) {
            x = 70;
            y = 285;
            width = w - 120;
            height = 1000;
        } else if (mCurrentMode == MODE_HUANGJINWU) {
            x = 70;
            y = 1050;
            width = w - 100;
            height = 600;
        } else {
            x = 70;
            y = h / 2;
            width = w - 70;
            height = 1000;
        }


        Bitmap bm = Bitmap.createBitmap(bitmap, x, y, width, height, null, false);
        FileUtil.saveBitmap(bm, savePath);
    }


    public void compress(final String oldImagePath, final String savaPath) {
        LogHelper.d("截图时间: " + (System.currentTimeMillis() - starTime));
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                Bitmap bitmap = BitmapFactory.decodeFile(oldImagePath);
                cropBitmap(bitmap, savaPath);
                e.onNext(savaPath);
            }
        }).compose(RxScheduler.<String>io_main())
                .subscribe(new MyObserver<String>() {
                    @Override
                    public void _onNext(String s) {
                        mPicturePath = s;
                        LogHelper.d(s);
                        long screenTime = System.currentTimeMillis() - starTime;
                        LogHelper.d("截图加裁剪时间: " + screenTime);
                        picToText();
                    }

                    @Override
                    public void _onError(String e) {
                        LogHelper.e(e);
                    }
                });
    }


    private void picToText() {
        // 通用文字识别参数设置
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(false);
        param.setDetectLanguage(true);
        param.setImageFile(new File(mPicturePath));
        param.setLanguageType("CHN_ENG");

        // 调用通用文字识别服务
        OCR.getInstance().recognizeGeneralBasic(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult result) {
                // 调用成功，返回GeneralResult对象
                long picToTextTime = System.currentTimeMillis();
                LogHelper.d("总时间: " + (picToTextTime - starTime));
                String[] words = new String[result.getWordList().size()];
                for (int i = 0; i < result.getWordList().size(); i++) {
                    WordSimple word = result.getWordList().get(i);
                    words[i] = word.getWords();
                }

                //  9演员章子怡没有参演过以下哪部电影?《建国大业》《非诚勿扰1》《非常完美》
                // 9演员章子怡没有参演过以下哪部电影?
                String question = "";
                int start = 0;
                for (int i = 0; i < words.length; i++) {
                    question = question + words[i];
                    if (words[i].contains("?")) {
                        start = i + 1;
                        break;
                    }
                }
                int index = question.indexOf(".");
                if (index != -1) {
                    question = question.substring(index);
                }
                LogHelper.d("问题 " + question);
                mAnswers = new HashMap<String, Integer>();
                for (int i = start; i < words.length; i++) {
                    String item = words[i];
                    if (mCurrentMode == MODE_HUANGJINWU &&
                            (item.startsWith("A.") || item.startsWith("B.") || item.startsWith("C."))) {
                        item = item.substring(2);
                    }
                    LogHelper.d("选项" + i + 1 + ":" + item);
                    mAnswers.put(item, 0);
                }
                baidu(question);
            }

            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError对象
                LogHelper.e("识别失败 " + error);
                mView.heroError(error.getMessage());
            }
        });
    }


    private void baidu(final String text) {
        RetrofitHelper.getInstance().getDefaultApi().get(text)
                .compose(RxScheduler.<ResponseBody>io_main())
                .subscribe(new MyObserver<ResponseBody>() {
                    @Override
                    public void _onNext(ResponseBody responseBody) {

                        StringBuilder result = new StringBuilder();
                        LogHelper.d("得出结果总时间 " + (System.currentTimeMillis() - starTime));
                        try {
                            String string = responseBody.string();
                            for (Map.Entry<String, Integer> stringIntegerEntry : mAnswers.entrySet()) {
                                String key = stringIntegerEntry.getKey().replace("《", "").replace("》", "");
                                mCounter = 0;
                                int count = countStr(string, key);
                                result.append(key + " :" + count);
                                result.append("\n");
                                LogHelper.d(key + " :" + count);
                            }
                            mView.heroResult(result.toString(), System.currentTimeMillis() - starTime);

                        } catch (IOException e) {
                            mView.heroError(e.getMessage());
                        }

                    }

                    @Override
                    public void _onError(String e) {
                        ToastUtil.showShortToast(e);
                        mView.heroError(e);
                    }
                });
    }


    /**
     * 判断str1中包含str2的个数
     *
     * @param str1
     * @param str2
     * @return mCounter
     */
    public int countStr(String str1, String str2) {
        if (!str1.contains(str2)) {
            return 0;
        } else {
            mCounter++;
            countStr(str1.substring(str1.indexOf(str2) +
                    str2.length()), str2);
            return mCounter;
        }
    }

    public void setStartTime(long starTime) {
        this.starTime = starTime;
    }




    public void initDatabase(){
        List<ModeDB> all = DataSupport.findAll(ModeDB.class);
        if (all.size() == 0){

        }
    }


}
