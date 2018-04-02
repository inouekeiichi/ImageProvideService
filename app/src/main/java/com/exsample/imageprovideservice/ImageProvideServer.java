package com.exsample.imageprovideservice;

/**
 * Created by kei on 2018/03/21.
 */
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.lang.Runnable;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ImageProvideServer implements Runnable {
    final static String TAG = "ImageProvideServer";

    public ImageProvideServer()
    {

    }

    private Bitmap getScreenshot() {
        Bitmap bitmap = null;
        int pixelstride = 0;
        int rowstride = 0;
        int rowpadding = 0;

        try {

            // ImageReaderから画面を取り出す
            Image image = MainActivity.mImageReader.acquireLatestImage();
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();

            pixelstride = planes[0].getPixelStride();
            rowstride = planes[0].getRowStride();
            rowpadding = rowstride - pixelstride * MainActivity.mDisplayWidth;

            // データからBitmapを生成
            bitmap = Bitmap.createBitmap(
                    MainActivity.mDisplayWidth + rowpadding / pixelstride, MainActivity.mDisplayHeight,
                    Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            image.close();

        } catch (Exception e)
        {
            Log.d("", e.toString());
        }
        return bitmap;
    }
    @Override
    public void run() {
        int count = 0;
        ServerSocket serversocket = null;
        Socket socket;

        try {
            while (true) {

                serversocket = new ServerSocket(MainActivity.port);

                while (true) {
                    socket = serversocket.accept();

                    // 画面取得
                    Bitmap cap = getScreenshot();

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    // ByteArrayOutputStreamに画面出力
                    cap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    BufferedOutputStream buf = new BufferedOutputStream(socket.getOutputStream());

                    // ネットワークストリームに画面データ出力
                    buf.write(bos.toByteArray());

                    Log.d(TAG, "ImageProvideServer " + count + " " + bos.size());

                    socket.close();
                    count++;

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }
}
