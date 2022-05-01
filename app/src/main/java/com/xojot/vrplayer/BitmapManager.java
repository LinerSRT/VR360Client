package com.xojot.vrplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 01.05.2022, воскресенье
 **/
public class BitmapManager {
    private Callback callback;
    private Bitmap bitmap;

    public void setUri(Context context, Uri uri) {
        if(uri.getScheme().equals("http")){
            try {
                new HttpGetBitmap(this).execute(new URL(uri.toString()));
                return;
            } catch (MalformedURLException e ) {
                e.printStackTrace();
                return;
            }
        }
        try {
            getBitmapFromInputStream(context.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onPrepared(Bitmap bitmap);
    }

    private static class HttpGetBitmap extends AsyncTask<URL, Void, Void> {
        private WeakReference<BitmapManager> weakReference;

        public HttpGetBitmap(BitmapManager bitmapManager) {
            weakReference = new WeakReference<>(bitmapManager);
        }

        @Override
        protected Void doInBackground(URL... urls) {
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) urls[0].openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();
                BitmapManager manager = weakReference.get();
                if (httpURLConnection.getResponseCode() == 200 && manager != null) {
                    manager.getBitmapFromInputStream(httpURLConnection.getInputStream());
                }
                httpURLConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void getBitmapFromInputStream(InputStream inputStream) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        this.bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (callback != null)
            callback.onPrepared(bitmap);
    }

    public void release(){
        bitmap.recycle();
    }
}
