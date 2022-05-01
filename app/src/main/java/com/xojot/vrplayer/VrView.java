package com.xojot.vrplayer;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import ru.liner.vr360client.R;
import ru.liner.vr360client.utils.Worker;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 01.05.2022, воскресенье
 **/
public class VrView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private Context context;
    private SurfaceTexture surfaceTexture;
    private ViewCamera camera;
    private Media media;
    private MediaPlayer mediaPlayer;
    private RenderThread renderThread;
    private boolean contentLoaded;
    private boolean frameAvailable;
    private boolean isViewChanged;
    private boolean doFramePause;
    private boolean doFrameStart;
    private int maxGLTextureSize;
    private long prevNanoTime;
    private float fps;
    private int fpsCount;

    public VrView(Context context) {
        this(context, null);
    }

    public VrView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        if (!isInEditMode()) {
            System.loadLibrary("native-lib");
            setEGLContextClientVersion(2);
            setEGLContextFactory(new EGLContextFactory() {
                @Override
                public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
                    return egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, new int[]{12440, 2, 12344});
                }

                @Override
                public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
                    egl.eglDestroyContext(display, context);
                }
            });
            setEGLConfigChooser(8, 8, 8, 8, 1, 1);
            setRenderer(this);
            setRenderMode(0);
            camera = new ViewCamera(context);
            media = new Media(context);
            media.setMediaCallback(new MediaCallback() {
                @Override
                public void onMediaLoaded(MediaPlayer mp) {
                    mediaPlayer = mp;
                    contentLoaded = false;
                }
            });
        }
    }


    public native void jniInit(AssetManager assetManager);

    public native void jniRender(float rotationX, float rotationY, float rotationZ, float fovY, float eyeZ, boolean hmd);

    public native void jniSetProjectionType(int projectionType, float verticalFov, float aspectRation);

    public native void jniSetScreenSize(int width, int height);

    public native void jniSetStereoType(int stereoType);

    public native void jniSetupModel(Bitmap bitmap);

    public native int jniSetupModelExt();


    public void onResume() {
        super.onResume();
        this.camera.onResume();
        this.contentLoaded = false;
        this.isViewChanged = true;
        if (renderThread == null)
            renderThread = new RenderThread();
        renderThread.start();
    }

    public void onPause() {
        super.onPause();
        camera.onPause();
        if (renderThread != null)
            renderThread.stop();
    }


    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        frameAvailable = true;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        jniInit(context.getAssets());
        int[] iArr = new int[1];
        GLES20.glGetIntegerv(3379, iArr, 0);
        this.maxGLTextureSize = iArr[0];
        this.contentLoaded = false;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        jniSetScreenSize(width, height);
        camera.setView((float) width, (float) height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (this.contentLoaded) {
            synchronized (this) {
                if ( media.isPrepared() && frameAvailable) {
                    surfaceTexture.updateTexImage();
                    frameAvailable = false;
                    if (doFramePause) {
                        media.pause();
                        doFramePause = false;
                    }
                    if (doFrameStart) {
                        media.start();
                        doFrameStart = false;
                    }
                }
            }
            if (isViewChanged) {
                jniSetProjectionType(media.getProjectionType().ordinal(), media.getVerticalFov(), media.getAspectRatio());
                jniSetStereoType(media.getStereoType().ordinal());
                isViewChanged = false;
            }
            jniRender(camera.getRotX(), camera.getRotY(), camera.getRotZ(), camera.getFovY(), camera.getEyeZ(), camera.isHmd());
        } else {
            loadContent();
        }
        measureFps();
    }



    private void loadContent() {
        if (mediaPlayer != null) {
            synchronized (this) {
                frameAvailable = false;
            }
            surfaceTexture = new SurfaceTexture(jniSetupModelExt());
            surfaceTexture.setOnFrameAvailableListener(this);
            Surface surface = new Surface(surfaceTexture);
            mediaPlayer.setSurface(surface);
            surface.release();
            isViewChanged = true;
            contentLoaded = true;
        }
    }


    private void measureFps() {
        long nanoTime = System.nanoTime();
        if (1000000000 < nanoTime - this.prevNanoTime) {
            this.fps = (((float) this.fpsCount) * 1.0E9f) / ((float) (nanoTime - this.prevNanoTime));
            this.fpsCount = 0;
            this.prevNanoTime = nanoTime;
            return;
        }
        this.fpsCount++;
    }



    private class RenderThread extends Worker {

        @Override
        public void execute() {
            VrView.this.requestRender();
        }

        @Override
        public long delay() {
            return 16;
        }
    }


    public float getFps() {
        return this.fps;
    }

    public boolean isContentLoaded() {
        return this.contentLoaded;
    }

    public void setViewChanged(boolean viewChanged) {
        isViewChanged = viewChanged;
    }

    public void setDataSource(Uri uri) {
        this.media.setContentUri(uri);
    }

    public void seekTo(int i) {
        this.media.seekTo(i);
    }

    public boolean isPlaying() {
        return this.media.isPlaying();
    }

    public void pause() {
        this.media.pause();
    }

    public void setDoFramePause() {
        this.doFramePause = true;
    }

    public void start() {
        this.media.start();
    }

    public void setDoFrameStart() {
        this.doFrameStart = true;
    }


    public int getDuration() {
        return this.media.getDuration();
    }

    public int getCurrentPosition() {
        return this.media.getCurrentPosition();
    }

    public boolean isPrepared() {
        return this.media.isPrepared();
    }

    public boolean isLooping() {
        return this.media.isLooping();
    }

    public int getBufferPosition() {
        return (int) Math.floor(((double) this.media.getDuration()) * (((double) this.media.getBufferPercentage()) / 100.0d));
    }

    public void setStereoType(Media.StereoType stereoType) {
        this.media.setStereoType(stereoType);
    }

    public Media.StereoType getStereoType() {
        return this.media.getStereoType();
    }

    public void setProjectionType(Media.ProjectionType projectionType) {
        this.media.setProjectionType(projectionType);
    }

    public Media.ProjectionType getProjectionType() {
        return this.media.getProjectionType();
    }


    public float getMaxGLTextureSize() {
        return (float) this.maxGLTextureSize;
    }

    public float getMediaWidth() {
        return (float) this.media.getWidth();
    }

    public float getMediaAspect() {
        return this.media.getAspectRatio();
    }

    public float getMediaVFov() {
        return this.media.getVerticalFov();
    }

    public float getMediaHFov() {
        return this.media.getHorizontalFov();
    }

    public float getMediaHeight() {
        return (float) this.media.getHeight();
    }

    public float getMediaDFov() {
        return this.media.getDistanceFov();
    }

    public void cameraPanning(MotionEvent motionEvent) {
        this.camera.cameraPanning(motionEvent);
    }

    public float getEyeZ() {
        return this.camera.getEyeZ();
    }

    public float getMaxFovY() {
        return this.camera.getMaxFovY();
    }

    public float getMinFovY() {
        return this.camera.getMinFovY();
    }

    public float getFovY() {
        return this.camera.getFovY();
    }

    public float getRotX() {
        return this.camera.getRotX();
    }

    public float getRotY() {
        return this.camera.getRotY();
    }

    public float getRotZ() {
        return this.camera.getRotZ();
    }

    public float[] getSensorRaw() {
        return this.camera.getSensorRaw();
    }

    public boolean isAutoPanning() {
        return this.camera.isAutoPanning();
    }

    public void setAutoPanning(boolean z) {
        this.camera.setAutoPanning(z);
    }

    public void setCameraReset(boolean z) {
        this.camera.setCameraReset(z);
    }

    public ViewCamera getCamera() {
        return camera;
    }

    public Media getMedia() {
        return media;
    }

    public boolean isCameraReset() {
        return this.camera.isCameraReset();
    }

    public void setSensorMotion(boolean z) {
        this.camera.setSensorMotion(z);
    }

    public boolean isSensorMotion() {
        return this.camera.isSensorMotion();
    }

    public void setSpherical(boolean z) {
        this.camera.setSpherical(z);
    }

    public boolean isSpherical() {
        return this.camera.isSpherical();
    }

    public void setHmd(boolean z) {
        this.camera.setHmd(z);
    }

    public boolean isHmd() {
        return this.camera.isHmd();
    }

    public boolean isSensorAvailable() {
        return this.camera.isSensorRegistered();
    }
}
