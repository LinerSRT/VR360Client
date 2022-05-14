package ru.liner.vr360client.utils;
import static android.content.Context.WINDOW_SERVICE;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.WindowManager;


/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 01.05.2022, воскресенье
 **/
public class VrModeChanger extends Worker{
    @Override
    public void execute() {

    }

    @Override
    public long delay() {
        return 0;
    }
//    private WindowManager windowManager;
//    private Activity activity;
//    private VrView vrView;
//    private Callback callback;
//
//    public VrModeChanger(Activity activity, VrView vrView) {
//        this.activity = activity;
//        this.vrView = vrView;
//        this. windowManager = (WindowManager) activity.getSystemService(WINDOW_SERVICE);
//    }
//
//    @Override
//    public void execute() {
//        if (vrView.isPrepared()) {
//            activity.runOnUiThread(() -> {
//                int rotation = windowManager.getDefaultDisplay().getRotation();
//                activity.setRequestedOrientation((rotation == 0 || rotation == 1) ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
//                vrView.setSensorMotion(true);
//                vrView.setHmd(true);
//                vrView.setProjectionType(Media.ProjectionType.EQUIRECTANGULAR);
//                vrView.setStereoType(Media.StereoType.MONO);
//                vrView.setCameraReset(true);
//                vrView.setViewChanged(true);
//                if (vrView.isCameraReset()) {
//                    vrView.seekTo(0);
//                    if(callback != null)
//                        callback.onVRPrepared(vrView);
//                }
//            });
//        }
//    }
//
//    @Override
//    public long delay() {
//        return 16;
//    }
//
//    public void setCallback(Callback callback) {
//        this.callback = callback;
//    }
//
//    public interface Callback{
//        void onVRPrepared(VrView vrView);
//    }

}