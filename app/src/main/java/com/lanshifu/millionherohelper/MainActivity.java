package com.lanshifu.millionherohelper;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lanshifu.baselibrary.base.BaseActivity;
import com.lanshifu.baselibrary.utils.StorageUtil;
import com.lanshifu.baselibrary.utils.SystemUtil;
import com.lanshifu.baselibrary.utils.ToastUtil;
import com.lanshifu.millionherohelper.mvp.presenter.MainPresenter;
import com.lanshifu.millionherohelper.mvp.view.MainView;
import com.yhao.floatwindow.FloatWindow;
import com.yhao.floatwindow.MoveType;
import com.yhao.floatwindow.Screen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity<MainPresenter> implements MainView {


    private static final String TAG = "lxb";
    private static final int REQUEST_MEDIA_PROJECTION = 10;
    @Bind(R.id.iv_screen)
    ImageView mIvScreen;
    @Bind(R.id.editText)
    EditText mEditText;
    @Bind(R.id.btn_screen)
    Button mBtnScreen;
    @Bind(R.id.btn_save)
    Button mBtnSave;

    private TextView mTv_result;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private ImageReader mImageReader;

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
        setTitleText("答题辅助");
        hideBackIcon();

        openFlow();
        mPresenter.initBaiduOrc();

        mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mMediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
        mImageReader = ImageReader.newInstance(SystemUtil.getScreenWidth(this), 2000, 0x1, 2);
    }

    private void openFlow() {
        View view = View.inflate(this, R.layout.layout_flowview, null);
        mTv_result = view.findViewById(R.id.tv_result);
        view.findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTv_result.setText("正在查询...");
                startCapture();
//                mPresenter.getScreenAndParseText();
            }
        });

        FloatWindow
                .with(MainApplication.getContext())
                .setView(view)
                .setX(Screen.width, 0.6f)                       //100px
                .setY(Screen.height, 0.6f)        //屏幕高度的 60%
                .setDesktopShow(true)
                .setMoveType(MoveType.slide)
                .build();
    }


    @Override
    public void hasRootPermission(boolean root) {
        if (!root) {
//            FloatWindow.destroy();
        }

    }

    @Override
    public void heroResult(String result, long time) {
        mTv_result.setText(result);
        mTv_result.append("\n耗时：" + time);
    }

    @Override
    public void heroError(String text) {
        mTv_result.setText("出错了：" + text);
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


    private void showDialog() {
        String oldImagePath = StorageUtil.getAppRootDir() + "save.png";
        Bitmap bitmap = BitmapFactory.decodeFile(oldImagePath);
        if (bitmap == null) {
            ToastUtil.showLongToast("请先点击搜索答案生成截图");
            return;
        }
        ImageView imageVeiw = new ImageView(this);
        imageVeiw.setImageBitmap(bitmap);

        new AlertDialog.Builder(this)
                .setView(imageVeiw)
                .show();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startCapture() {
        mPresenter.setStartTime(System.currentTimeMillis());
        String mImagePath = StorageUtil.getAppRootDir();
        String mImageName = "screen.png";
        Log.e(TAG, "image name is : " + mImageName);
        Image image = mImageReader.acquireLatestImage();
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();
        if (bitmap != null) {
            Log.e(TAG, "bitmap  create success ");
            try {
                File fileFolder = new File(mImagePath);
                if (!fileFolder.exists())
                    fileFolder.mkdirs();
                File file = new File(mImagePath, mImageName);
                if (!file.exists()) {
                    Log.e(TAG, "file create success ");
                    file.createNewFile();
                }
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
                Log.e(TAG, "file save success ");
//                Toast.makeText(this.getApplicationContext(), "截图成功", Toast.LENGTH_SHORT).show();
                mPresenter.compress(mImagePath + mImageName, mImagePath + "save.png");
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setUpMediaProjection(int resultCode, Intent resultData) {
        mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, resultData);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setUpVirtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture",
                SystemUtil.getScreenWidth(this), 2000, 2,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION && resultCode == RESULT_OK) {
            setUpMediaProjection(resultCode, data);
            setUpVirtualDisplay();
        }
    }



    @OnClick({R.id.btn_save, R.id.btn_screen})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                String height = mEditText.getText().toString();
                ToastUtil.showShortToast("功能没实现");

                break;
            case R.id.btn_screen:
                showDialog();
                break;
        }
    }
}

