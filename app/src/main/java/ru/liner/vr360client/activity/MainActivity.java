package ru.liner.vr360client.activity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.xojot.vrplayer.Media;
import com.xojot.vrplayer.VrView;

import java.io.File;

import ru.liner.vr360client.R;
import ru.liner.vr360client.tcp.TCPClient;
import ru.liner.vr360client.tcp.UDPMulticast;
import ru.liner.vr360client.utils.Worker;


@SuppressWarnings("unused")
public class MainActivity extends AppCompatActivity implements View.OnSystemUiVisibilityChangeListener {
    private VrView vrView;
    private Worker vrModeChanger;
    private WindowManager windowManager;
    private boolean isInVRMode;
    private int pausePosition;


    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        this.vrView = findViewById(R.id.vrView);
        vrModeChanger = new Worker() {
            @Override
            public void execute() {
                if (vrView.isPrepared()) {
                    runOnUiThread(() -> {
                        int rotation = windowManager.getDefaultDisplay().getRotation();
                        setRequestedOrientation((rotation == 0 || rotation == 1) ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                        vrView.setSensorMotion(true);
                        vrView.setHmd(true);
                        vrView.setProjectionType(Media.ProjectionType.EQUIRECTANGULAR);
                        vrView.setStereoType(Media.StereoType.MONO);
                        vrView.setCameraReset(true);
                        vrView.setViewChanged(true);
                        if (vrView.isCameraReset()) {
                            vrModeChanger.stop();
                            vrView.seekTo(0);

                        } else {
                            execute();
                        }
                    });
                }
            }

            @Override
            public long delay() {
                return 16;
            }
        };
        vrModeChanger.start();

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "sample360.mp4");
        if(file.exists()) {
            setContent(Uri.fromFile(file));
        } else {
            //setContent(Uri.parse("http://192.168.1.143:8888/"));
        }
    }

    private void setContent(Uri uri) {
        vrView.setDataSource(uri);
        vrView.setAutoPanning(false);
        vrView.setCameraReset(false);
        //vrView.setDoFrameStart();
        vrModeChanger.start();
    }


    @Override
    public void onResume() {
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this);
        setFullScreen();
        super.onResume();
        vrView.onResume();
        vrView.seekTo(pausePosition);
    }


    private void setFullScreen() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(4870);
    }

    @Override
    public void onPause() {
        super.onPause();
        vrView.pause();
        pausePosition = vrView.getCurrentPosition();
        vrView.onPause();
    }

    @Override
    public void onSystemUiVisibilityChange(int i) {
        if ((i & 4) == 0) {
            setFullScreen();
        }
    }
}
