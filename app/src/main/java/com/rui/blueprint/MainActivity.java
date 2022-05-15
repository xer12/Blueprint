package com.rui.blueprint;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @Description: 主类，用来画图的
 * @Author: 陈锐
 * @Date: 2022/5/15
 **/
public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener{
    private static final String TAG = "MainActivity";

    // 用来监控的ImageView
    private ImageView mIvDrawPlace;
    private Button btn_red, btn_green, btn_blue, btn_clear, btn_save;
    // 画图参数
    private Canvas mCanvas;
    private Paint mPaint;
    private Bitmap mDrawBitmap;
    private Bitmap mBgBitmap;
    private float mStartX;
    private float mStartY;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate");

        mIvDrawPlace = findViewById(R.id.iv_drawplace);
        mIvDrawPlace.setOnTouchListener(this);
        btn_red = findViewById(R.id.btn_red);
        btn_green = findViewById(R.id.btn_green);
        btn_blue = findViewById(R.id.btn_blue);
        btn_clear = findViewById(R.id.btn_clear);
        btn_save = findViewById(R.id.btn_save);
        btn_red.setOnClickListener(this);
        btn_green.setOnClickListener(this);
        btn_blue.setOnClickListener(this);
        btn_clear.setOnClickListener(this);
        btn_save.setOnClickListener(this);

        btn_red.setBackgroundColor(Color.RED);
        btn_green.setBackgroundColor(Color.GREEN);
        btn_blue.setBackgroundColor(Color.BLUE);

        init();
        index = 1;
    }

    private void init() {
        // 背景
        mBgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_white);
        // 画布, 用来画的bitmap, 显示宽和高和背景一致
        mDrawBitmap = Bitmap.createBitmap(
                mBgBitmap.getWidth(), mBgBitmap.getHeight(), mBgBitmap.getConfig());
        // 创建画笔
        mPaint = new Paint();
        mCanvas = new Canvas(mDrawBitmap);
        // 先画背景， new Matrix()矩阵，用来做数学运算的，当前没有用到，从mBgBitmap中读取数据，保存在mDrawBitmap中
        mCanvas.drawBitmap(mBgBitmap, new Matrix(), mPaint);
        // 将绘制的图，放到ImageView中
        mIvDrawPlace.setImageBitmap(mDrawBitmap);
        // 设置颜色
        mPaint.setColor(Color.rgb(243, 28, 28));
        mPaint.setStrokeWidth(30);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        float coordinateX = motionEvent.getX();
        float coordinateY = motionEvent.getY();
        String coordinate = String.format("(%f, %f)", coordinateX, coordinateY);
        switch (view.getId()) {
            //监听ImageView的位置变化
            case R.id.iv_drawplace: {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mStartX = coordinateX;
                        mStartY = coordinateY;
                        mCanvas.drawLine(mStartX, mStartY, coordinateX, coordinateY, mPaint);
                        mIvDrawPlace.setImageBitmap(mDrawBitmap);
                        Log.d(TAG, "Down: " + coordinate);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d(TAG, "Move: " + coordinate);
                        mCanvas.drawLine(mStartX, mStartY, coordinateX, coordinateY, mPaint);
                        mIvDrawPlace.setImageBitmap(mDrawBitmap);
                        mStartX = coordinateX;
                        mStartY = coordinateY;
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "Up: " + coordinate);
                        break;
                    default:
                        break;
                }
            }
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_red:
                Log.i(TAG, "set red");
                mPaint.setColor(Color.RED);
                break;
            case R.id.btn_green:
                Log.i(TAG, "set green");
                mPaint.setColor(Color.GREEN);
                break;
            case R.id.btn_blue:
                Log.i(TAG, "set blue");
                mPaint.setColor(Color.BLUE);
                break;
            case R.id.btn_clear:
                Log.i(TAG, "set clear");
                init();
                break;
            case R.id.btn_save:
                Log.i(TAG, "set save to " + index + ".png");
                save();
                break;
            default:
                //TBD
        }
    }

    // 保存并展示在系统的界面上
    public void save(){
        File catchDir = this.getDir("Blueprint", Context.MODE_PRIVATE);
        String pictureName  = index + ".png";
        File filepath = catchDir.toPath().resolve(pictureName).toFile();
        try {
            FileOutputStream fos = new FileOutputStream(filepath);
            mDrawBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), mDrawBitmap, pictureName, pictureName);
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(path)));
        Log.i(TAG, "已经成功保存到" + pictureName + filepath.getPath());

        /*// 发送sd卡就绪广播，会重新扫描SD卡，然后就会更新到数据库中，然后就可以在系统的相册展示
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MEDIA_MOUNTED);
        intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
        sendBroadcast(intent);*/

        index++;
    }
}