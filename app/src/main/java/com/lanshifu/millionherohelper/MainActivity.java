package com.lanshifu.millionherohelper;

import android.annotation.TargetApi;
import android.content.DialogInterface;
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
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lanshifu.baselibrary.base.BaseActivity;
import com.lanshifu.baselibrary.log.LogHelper;
import com.lanshifu.baselibrary.utils.StorageUtil;
import com.lanshifu.baselibrary.utils.SystemUtil;
import com.lanshifu.baselibrary.utils.ToastUtil;
import com.lanshifu.millionherohelper.bean.ModeDB;
import com.lanshifu.millionherohelper.mvp.presenter.MainPresenter;
import com.lanshifu.millionherohelper.mvp.view.MainView;
import com.yhao.floatwindow.FloatWindow;
import com.yhao.floatwindow.MoveType;
import com.yhao.floatwindow.Screen;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class MainActivity extends BaseActivity<MainPresenter> implements MainView {


    private static final String TAG = "lxb";
    private static final int REQUEST_MEDIA_PROJECTION = 10;
    @Bind(R.id.iv_screen)
    ImageView mIvScreen;
    @Bind(R.id.btn_screen)
    Button mBtnScreen;
    @Bind(R.id.tv_select)
    TextView mTvSelect;
    @Bind(R.id.tv_mode)
    TextView mTvMode;

    private TextView mTv_result;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private ImageReader mImageReader;
    private int mScreenWidth;
    private int mScreenHeight;
    List<ModeDB> modeDBList;
    String[] items = {"百万英雄", "百万黄金屋"};
    private ModeDB mCurrentModeDB;

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

        mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
        mScreenHeight = getWindowManager().getDefaultDisplay().getHeight();
        LogHelper.d("width:" + mScreenWidth + ",heitht: " + mScreenHeight);

        openFlow();
        mPresenter.initBaiduOrc();

        mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, 0x1, 2);

        mPresenter.initDatabase();

        int id = SPUtil.getInstance().getInt(SPUtil.KEY_MODE);
        mCurrentModeDB = DataSupport.find(ModeDB.class, id);
        updateView();
    }

    private void updateView() {
        if(mCurrentModeDB != null){
            mTvMode.setText(mCurrentModeDB.getName());
            mPresenter.setCurrentModeDB(mCurrentModeDB);
        }else {
            mTvMode.setText("请选择模式");
        }
    }


    @Override
    protected int getTBMenusId() {
        return R.menu.menu_main;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.about) {
            showAboutMeDialog();
        }
        if (item.getItemId() == R.id.add) {
            showAddOrUpdateModeDialog(false,"添加项目");
        }
        if (item.getItemId() == R.id.help) {
            showInfoDialog("使用说明", "选择模式，然后把悬浮窗拉到答题区域外，点击搜索答案即可");
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAboutMeDialog() {
        showInfoDialog("关于作者", "蓝师傅 404985095\r");
    }

    private void showInfoDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .show();
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
    public void updateDBSuccess(List<ModeDB> modeDBList) {
        this.modeDBList = modeDBList;
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


    @OnClick({R.id.btn_screen, R.id.tv_select, R.id.tv_mode, R.id.btn_change_param})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_screen:
                showDialog();
                break;

            case R.id.tv_select:
            case R.id.tv_mode:
                showSelectModeDialog();
                break;

            case R.id.btn_change_param:
                showAddOrUpdateModeDialog(true,"更新参数");
                break;
        }
    }

    private void showSelectModeDialog() {
        items = new String[modeDBList.size()];
        for (int i = 0; i < modeDBList.size(); i++) {
            ModeDB modeDB = modeDBList.get(i);
            items[i] = modeDB.getName();
        }
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCurrentModeDB = modeDBList.get(which);
                        SPUtil.getInstance().putInt(SPUtil.KEY_MODE, mCurrentModeDB.getId());
                        updateView();
                        showShortToast("已切换模式为：" + items[which]);
                        mPresenter.setCurrentModeDB(mCurrentModeDB);
                        dialog.dismiss();
                    }
                }).show();
    }


    private void showAddOrUpdateModeDialog(final boolean update, String title) {
        View view = View.inflate(this,R.layout.layout_add_mode,null);
        final EditText et_name = view.findViewById(R.id.et_name);
        final EditText et_x = view.findViewById(R.id.et_x);
        final EditText et_y = view.findViewById(R.id.et_y);
        final EditText et_width = view.findViewById(R.id.et_width);
        final EditText et_heitht = view.findViewById(R.id.et_heitht);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(mCurrentModeDB != null){
            et_x.setText(mCurrentModeDB.getX() +"");
            et_y.setText(mCurrentModeDB.getY() +"");
            et_width.setText(mCurrentModeDB.getWidth() +"");
            et_heitht.setText(mCurrentModeDB.getHeight() +"");
        }
        if (update){
            et_name.setText(mCurrentModeDB.getName());
        }
        builder.setTitle(title);

        builder.setView(view);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (update){
                            mCurrentModeDB.setName(et_name.getText().toString());
                            mCurrentModeDB.setX(Integer.parseInt(et_x.getText().toString()));
                            mCurrentModeDB.setY(Integer.parseInt(et_y.getText().toString()));
                            mCurrentModeDB.setWidth(Integer.parseInt(et_width.getText().toString()));
                            mCurrentModeDB.setHeight(Integer.parseInt(et_heitht.getText().toString()));
                            boolean save = mCurrentModeDB.save();
                            updateView();
                            showShortToast("更新 "+ (save ? "成功": "失败"));
                        }else {
                            ModeDB modeDB = new ModeDB(et_name.getText().toString(),
                                    Integer.parseInt(et_x.getText().toString()),
                                    Integer.parseInt(et_y.getText().toString()),
                                    Integer.parseInt(et_width.getText().toString()),
                                    Integer.parseInt(et_heitht.getText().toString()));
                            boolean save = modeDB.save();
                            showShortToast("添加项目 "+ (save ? "成功": "失败"));
                        }
                        mPresenter.initDatabase();
                        dialog.dismiss();
                    }
                });
        builder.show();

    }

}

