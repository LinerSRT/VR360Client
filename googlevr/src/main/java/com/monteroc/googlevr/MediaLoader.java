/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.monteroc.googlevr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.Surface;
import com.monteroc.googlevr.Rendering.*;

import java.net.URI;


public class MediaLoader {
    private static final String TAG = "MediaLoader";

    public static final String MEDIA_FORMAT_KEY = "stereoFormat";
    private static final int DEFAULT_SURFACE_HEIGHT_PX = 2048;

    /**
     * A spherical mesh for video should be large enough that there are no stereo artifacts.
     */
    private static final int SPHERE_RADIUS_METERS = 50;

    /**
     * These should be configured based on the video type. But this sample assumes 360 video.
     */
    private static final int DEFAULT_SPHERE_VERTICAL_DEGREES = 180;
    private static final int DEFAULT_SPHERE_HORIZONTAL_DEGREES = 360;

    /**
     * The 360 x 180 sphere has 15 degree quads. Increase these if lines in your video look wavy.
     */
    private static final int DEFAULT_SPHERE_ROWS = 12;
    private static final int DEFAULT_SPHERE_COLUMNS = 24;

    private final Context context;
    // This can be replaced by any media player that renders to a Surface. In a real app, this
    // media player would be separated from the rendering code. It is left in this class for
    // simplicity.
    // This should be set or cleared in a synchronized manner.
    MediaPlayer mediaPlayer;
    // This sample also supports loading images.
    Bitmap mediaImage;
    // If the video or image fails to load, a placeholder panorama is rendered with error text.
    String errorText;

    // Due to the slow loading media times, it's possible to tear down the app before mediaPlayer is
    // ready. In that case, abandon all the pending work.
    // This should be set or cleared in a synchronized manner.
    private boolean isDestroyed = false;

    // The type of mesh created depends on the type of media.
    Mesh mesh;
    // The sceneRenderer is set after GL initialization is complete.
    private SceneRenderer sceneRenderer;
    // The displaySurface is configured after both GL initialization and media loading.
    private Surface displaySurface;

    // The actual work of loading media happens on a background thread.
    private MediaLoaderTask mediaLoaderTask;

    public MediaLoader(Context context) {
        this.context = context;
    }

    /**
     * Loads custom videos based on the Intent or load the default video. See the Javadoc for this
     * class for information on generating a custom intent via adb.
     */
    public void loadMedia(Uri mediaUri, PlayerController playerController) {
        // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
        // take 100s of milliseconds.
        // Note that this sample doesn't cancel any pending mediaLoaderTasks since it assumes only one
        // Intent will ever be fired for a single Activity lifecycle.
        mediaLoaderTask = new MediaLoaderTask(mediaUri, playerController);
        mediaLoaderTask.execute();
    }

    /**
     * Notifies MediaLoader that GL components have initialized.
     */
    public void onGlSceneReady(SceneRenderer sceneRenderer) {
        this.sceneRenderer = sceneRenderer;
        displayWhenReady();
    }

    /**
     * Helper class to media loading. This accesses the disk and decodes images so it needs to run in
     * the background.
     */
    private class MediaLoaderTask extends AsyncTask<Intent, Void, Void> {
        private final PlayerController playerController;
        private Uri mediaUri;

        public MediaLoaderTask(Uri mediaUri, PlayerController playerController) {
            this.playerController = playerController;
            this.mediaUri = mediaUri;
        }

        @Override
        protected Void doInBackground(Intent... intent) {

            int stereoFormat = Mesh.MEDIA_MONOSCOPIC;
            mesh = Mesh.createUvSphere(
                    SPHERE_RADIUS_METERS, DEFAULT_SPHERE_ROWS, DEFAULT_SPHERE_COLUMNS,
                    DEFAULT_SPHERE_VERTICAL_DEGREES, DEFAULT_SPHERE_HORIZONTAL_DEGREES,
                    stereoFormat);

            // Based on the Intent's data, load the appropriate media from disk.

            MediaPlayer mp = MediaPlayer.create(context, mediaUri);
            synchronized (MediaLoader.this) {
                // This needs to be synchronized with the methods that could clear mediaPlayer.
                mediaPlayer = mp;
            }

            displayWhenReady();
            return null;
        }

        @Override
        public void onPostExecute(Void unused) {
            // Set or clear the UI's mediaPlayer on the UI thread.
            if (playerController != null) {
                playerController.setMediaPlayer(mediaPlayer);
            }
        }
    }


    private synchronized void displayWhenReady() {
        if (isDestroyed) {
            // This only happens when the Activity is destroyed immediately after creation.
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            return;
        }

        if (displaySurface != null) {
            // Avoid double initialization caused by sceneRenderer & mediaPlayer being initialized before
            // displayWhenReady is executed.
            return;
        }

        if ((errorText == null && mediaImage == null && mediaPlayer == null) || sceneRenderer == null) {
            // Wait for everything to be initialized.
            return;
        }

        // The important methods here are the setSurface & lockCanvas calls. These will have to happen
        // after the GLView is created.
        if (mediaPlayer != null) {
            // For videos, attach the displaySurface and mediaPlayer.
            displaySurface = sceneRenderer.createDisplay(
                    mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight(), mesh);
            mediaPlayer.setSurface(displaySurface);
            // Start playback.
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } else if (mediaImage != null) {
            // For images, acquire the displaySurface and draw the bitmap to it. Since our Mesh class uses
            // an GL_TEXTURE_EXTERNAL_OES texture, it's possible to perform this decoding and rendering of
            // a bitmap in the background without stalling the GL thread. If the Mesh used a standard
            // GL_TEXTURE_2D, then it's possible to stall the GL thread for 100+ ms during the
            // glTexImage2D call when loading 4k x 4k panoramas and copying the bitmap's data.
            displaySurface = sceneRenderer.createDisplay(
                    mediaImage.getWidth(), mediaImage.getHeight(), mesh);
            Canvas c = displaySurface.lockCanvas(null);
            c.drawBitmap(mediaImage, 0, 0, null);
            displaySurface.unlockCanvasAndPost(c);
        } else {
            // Handle the error case by creating a placeholder panorama.
            mesh = Mesh.createUvSphere(
                    SPHERE_RADIUS_METERS, DEFAULT_SPHERE_ROWS, DEFAULT_SPHERE_COLUMNS,
                    DEFAULT_SPHERE_VERTICAL_DEGREES, DEFAULT_SPHERE_HORIZONTAL_DEGREES,
                    Mesh.MEDIA_MONOSCOPIC);

            // 4k x 2k is a good default resolution for monoscopic panoramas.
            displaySurface = sceneRenderer.createDisplay(
                    2 * DEFAULT_SURFACE_HEIGHT_PX, DEFAULT_SURFACE_HEIGHT_PX, mesh);
            // Render placeholder grid and error text.
            Canvas c = displaySurface.lockCanvas(null);
            renderEquirectangularGrid(c, errorText);
            displaySurface.unlockCanvasAndPost(c);
        }
    }

    /**
     * Renders a placeholder grid with optional error text.
     */
    private static void renderEquirectangularGrid(Canvas canvas, String message) {
        // Configure the grid. Each square will be 15 x 15 degrees.
        final int width = canvas.getWidth();
        final int height = canvas.getHeight();
        // This assumes a 4k resolution.
        final int majorWidth = width / 256;
        final int minorWidth = width / 1024;
        final Paint paint = new Paint();

        // Draw a black ground & gray sky background
        paint.setColor(Color.BLACK);
        canvas.drawRect(0, height / 2, width, height, paint);
        paint.setColor(Color.GRAY);
        canvas.drawRect(0, 0, width, height / 2, paint);

        // Render the grid lines.
        paint.setColor(Color.WHITE);

        for (int i = 0; i < DEFAULT_SPHERE_COLUMNS; ++i) {
            int x = width * i / DEFAULT_SPHERE_COLUMNS;
            paint.setStrokeWidth((i % 3 == 0) ? majorWidth : minorWidth);
            canvas.drawLine(x, 0, x, height, paint);
        }

        for (int i = 0; i < DEFAULT_SPHERE_ROWS; ++i) {
            int y = height * i / DEFAULT_SPHERE_ROWS;
            paint.setStrokeWidth((i % 3 == 0) ? majorWidth : minorWidth);
            canvas.drawLine(0, y, width, y, paint);
        }

        // Render optional text.
        if (message != null) {
            paint.setTextSize(height / 64);
            paint.setColor(Color.RED);
            float textWidth = paint.measureText(message);

            canvas.drawText(
                    message,
                    width / 2 - textWidth / 2, // Horizontally center the text.
                    9 * height / 16, // Place it slightly below the horizon for better contrast.
                    paint);
        }
    }

    public synchronized void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public synchronized void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    public synchronized void destroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isDestroyed = true;
    }
}
