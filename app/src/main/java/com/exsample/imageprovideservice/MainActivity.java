package com.exsample.imageprovideservice;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.ImageReader;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.media.projection.MediaProjectionManager;
import android.media.projection.MediaProjection;
import android.hardware.display.VirtualDisplay;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    public final static int port = 10001;
    public static ImageReader mImageReader = null;
    public static int mDisplayWidth = 0;
    public static int mDisplayHeight = 0;

    private final int REQUEST_MEDIA_PROJECTION = 1;

    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView text = (TextView)findViewById(R.id.text_ip);
        text.setText(getWifiIPAddress(this));

        mMediaProjectionManager = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent permissionIntent = mMediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(permissionIntent, REQUEST_MEDIA_PROJECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_MEDIA_PROJECTION == requestCode) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
            }
            else {
                // MediaProjectionの取得
                mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);

                DisplayMetrics metrics = getResources().getDisplayMetrics();
                mDisplayWidth = metrics.widthPixels;
                mDisplayHeight = metrics.heightPixels;
                int density = metrics.densityDpi;

                Log.d(TAG, "setup VirtualDisplay");
                mImageReader = ImageReader.newInstance(mDisplayWidth, mDisplayHeight, PixelFormat.RGBA_8888, 2);
                mVirtualDisplay = mMediaProjection.createVirtualDisplay("Capturing Display",
                        mDisplayWidth, mDisplayHeight, density,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        mImageReader.getSurface(), null, null);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(getBaseContext(), ImageProvideService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(getBaseContext(), ImageProvideService.class));
        mMediaProjection.stop();
        mVirtualDisplay.release();
    }

    /**
     * wifiのIPアドレスとポート番号を取得します。
     * @param context Context
     * @return IPアドレスとポート番号
     */
    private String getWifiIPAddress(Context context) {
        WifiManager manager = (WifiManager)context.getSystemService(WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        int ipAddr = info.getIpAddress();
        String ipString = String.format("%02d.%02d.%02d.%02d:%d",
                (ipAddr>>0)&0xff, (ipAddr>>8)&0xff, (ipAddr>>16)&0xff, (ipAddr>>24)&0xff, port);
        return ipString;
    }
}
