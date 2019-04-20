package com.example.satoshi.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    private static final int RESULT_PICK_IMAGEFILE = 1000;
//    private ImageView imageView;
    private MediaProjectionManager mMediaProjectionManager;
    private static final int REQUEST_CODE_SCREEN_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        imageView = (ImageView)findViewById(R.id.image_view);

        mMediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        Intent permissionIntent = mMediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(permissionIntent, REQUEST_CODE_SCREEN_CAPTURE);

        Button b = (Button) findViewById(R.id.button2);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"getScreenshot");
                Bitmap screenshot = getScreenshot();
                ImageView iv = (ImageView) findViewById(R.id.imageView);
                iv.setImageBitmap(screenshot);
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, RESULT_PICK_IMAGEFILE);
            }
        });
    }

    private static final String TAG = "ScreenCapture";
    private MediaProjection mMediaProjection;
    private int mWidth;
    private int mHeight;
    private ImageReader mImageReader; // スクリーンショット用
    private VirtualDisplay mVirtualDisplay;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (REQUEST_CODE_SCREEN_CAPTURE == requestCode) {
            if (resultCode != RESULT_OK) {
                //パーミッションなし
                Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                return;
            }
            // MediaProjectionの取得
            mMediaProjection =
                    mMediaProjectionManager.getMediaProjection(resultCode, intent);

            DisplayMetrics metrics = getResources().getDisplayMetrics();
            mWidth = metrics.widthPixels;
            mHeight = metrics.heightPixels;
            int density = metrics.densityDpi;

            Log.d(TAG,"setup VirtualDisplay");
            mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("Capturing Display",
                    mWidth, mHeight, density,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(), null, null);
        }
    }

    private Bitmap getScreenshot() {
        // ImageReaderから画面を取り出す
        Image image = mImageReader.acquireLatestImage();
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();

        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * mWidth;

        // バッファからBitmapを生成
        Bitmap bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        image.close();

        return bitmap;
    }
}
