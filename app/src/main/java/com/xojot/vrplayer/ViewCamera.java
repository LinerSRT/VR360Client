package com.xojot.vrplayer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.WindowManager;

import ru.liner.vr360client.utils.Worker;

public class ViewCamera implements SensorEventListener {
    private static final long ANIMATION_THREAD_INTERVAL_MS = 18;
    private static final float EYE_Z_CHANGE_DECAY = 0.85f;
    private static final float EYE_Z_CHANGE_LIMIT = 0.04f;
    private static final float EYE_Z_DEFAULT = 0.0f;
    private static final float EYE_Z_DEFAULT_HMD = 0.7f;
    private static final float EYE_Z_DEFAULT_SPHERICAL = 1.0f;
    private static final float FOV_Y_DEFAULT = ((float) Math.toRadians(70.0d));
    private static final float FOV_Y_DEFAULT_HMD = ((float) Math.toRadians(60.0d));
    private static final float FOV_Y_DEFAULT_MAX = ((float) Math.toRadians(110.0d));
    private static final float FOV_Y_DEFAULT_MIN = ((float) Math.toRadians(20.0d));
    private static final float FOV_Y_HMD_MAX = ((float) Math.toRadians(120.0d));
    private static final float FOV_Y_MAX_DAMPING_DECAY = 0.98f;
    private static final float FOV_Y_MAX_DAMPING_LIMIT = (FOV_Y_DEFAULT_MAX + ((float) Math.toRadians(1.0d)));
    private static final float FOV_Y_MAX_OVER_DECAY = 0.065f;
    private static final float FOV_Y_MAX_OVER_LIMIT = ((float) Math.toRadians(160.0d));
    private static final float FOV_Y_MIN_DAMPING_DECAY = 0.96f;
    private static final float FOV_Y_MIN_DAMPING_LIMIT = (FOV_Y_DEFAULT_MIN - ((float) Math.toRadians(0.5d)));
    private static final float FOV_Y_MIN_OVER_LIMIT = ((float) Math.toRadians(10.0d));
    private static final float FOV_Y_RESET_DECAY = 0.95f;
    private static final float FOV_Y_RESET_LIMIT = ((float) Math.toRadians(3.0d));
    private static final float FOV_Y_SPHERICAL_EYE_MAX = ((float) Math.toRadians(140.0d));
    private static final float MOMENTUM_DECAY = 0.95f;
    private static final float MOMENTUM_DECAY_ADJUST = 0.025f;
    private static final float MOMENTUM_LIMIT = 1.0E-4f;
    private static final float PAN_AUTO_DEFAULT = ((float) Math.toRadians(0.14545454545454545d));
    private static final float PAN_RESET_DECAY = 0.92f;
    private static final float PAN_RESET_LIMIT = ((float) Math.toRadians(0.3d));
    private static final float PITCH_DAMPING_DECAY = 0.99f;
    private static final float PITCH_DAMPING_LIMIT = (((float) Math.toRadians(90.0d)) + ((float) Math.toRadians(0.5d)));
    private static final float PITCH_DECAY_ADJUST = 0.02f;
    private static final float PITCH_MAX = ((float) Math.toRadians(360.0d));
    private static final float PITCH_MIN = ((float) Math.toRadians(-360.0d));
    private static final float PITCH_OVER_DECAY = 0.3f;
    private static final float YAW_MAX = ((float) Math.toRadians(90.0d));
    private static final float YAW_MIN = ((float) Math.toRadians(-90.0d));
    private boolean autoPanning;
    private final Context context;
    private final WindowManager windowManager;
    private float defaultEyeZ;
    private float defaultFovY;
    private boolean doEyeZResetAnimation;
    private boolean doFovDamping;
    private boolean doFovYResetAnimation;
    private boolean doMomentum;
    private boolean doPanningResetAnimation;
    private boolean doPitchDamping;
    private float eyeZ;
    private boolean fovInitialized;
    private float fovY;
    private float height;
    private boolean hmd;
    private boolean isLandscape;
    private boolean isPointerPivot;
    private float maxFovY;
    private float maxFovYDampingLimit;
    private float maxFovYOverLimit;
    private float minFovY;
    private float minFovYDampingLimit;
    private float minFovYOverLimit;
    private final float[] orientation = new float[3];
    private float prevH;
    private final float[] prevOrientation = new float[3];
    private float prevR;
    private float prevX;
    private float prevY;
    private final float[] remapM = new float[16];
    private final float[] rotM = new float[16];
    private float rotX;
    private boolean rotXReset;
    private float rotY;
    private boolean rotYReset;
    private float rotZ;
    private boolean rotZReset;
    private SensorManager sensorManager;
    private boolean sensorMotion;
    private float[] sensorRaw = new float[3];
    private boolean sensorRegistered;
    private final float[] sensorXYZ = new float[3];
    private float sensorYaw;
    private boolean spherical;
    private float touchPitch;
    private float touchYaw;
    private boolean turnReverse;
    private float width;
    private float zoom;
    private Worker animationThread;


    ViewCamera(Context context) {
        this.context = context;
        this.windowManager = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
    }

    void setView(float f, float f2) {
        this.width = f;
        this.height = f2;
        this.isLandscape = f > f2;
        if (this.fovInitialized && this.prevH != f2) {
            swapFovXY();
        } else if (!this.fovInitialized) {
            initFov();
        }
    }

    public void onResume() {
        Sensor defaultSensor;
        this.sensorManager = (SensorManager) this.context.getSystemService(Context.SENSOR_SERVICE);
        defaultSensor = this.sensorManager.getDefaultSensor(15);
        if (defaultSensor != null) {
            this.sensorManager.registerListener(this, defaultSensor, 1);
            this.sensorRegistered = true;
        } else {
            this.sensorRegistered = false;
        }
        if (animationThread == null) {
            animationThread = new Worker() {
                @Override
                public void execute() {
                    cameraAnimation();
                }

                @Override
                public long delay() {
                    return ViewCamera.ANIMATION_THREAD_INTERVAL_MS;
                }
            };
        }
        this.animationThread.start();
    }

    public void onPause() {
        if (this.sensorRegistered) {
            this.sensorManager.unregisterListener(this);
            this.sensorRegistered = false;
        }
        this.animationThread.stop();
    }

    private void initFov() {
        if (hmd) {
            defaultFovY = FOV_Y_DEFAULT_HMD;
            maxFovY = FOV_Y_HMD_MAX;
            minFovY = FOV_Y_DEFAULT_MIN;
            maxFovYDampingLimit = FOV_Y_MAX_DAMPING_LIMIT;
            minFovYDampingLimit = FOV_Y_MIN_DAMPING_LIMIT;
            maxFovYOverLimit = FOV_Y_MAX_OVER_LIMIT;
            minFovYOverLimit = FOV_Y_MIN_OVER_LIMIT;
        } else if (width < height) {
            defaultFovY = FOV_Y_DEFAULT;
            maxFovY = spherical ? FOV_Y_SPHERICAL_EYE_MAX : FOV_Y_DEFAULT_MAX;
            minFovY = FOV_Y_DEFAULT_MIN;
            maxFovYDampingLimit = FOV_Y_MAX_DAMPING_LIMIT;
            minFovYDampingLimit = FOV_Y_MIN_DAMPING_LIMIT;
            maxFovYOverLimit = FOV_Y_MAX_OVER_LIMIT;
            minFovYOverLimit = FOV_Y_MIN_OVER_LIMIT;
        } else {
            defaultFovY = getFovXtoY(FOV_Y_DEFAULT, width, height);
            maxFovY = getFovXtoY(spherical ? FOV_Y_SPHERICAL_EYE_MAX : FOV_Y_DEFAULT_MAX, width, height);
            minFovY = getFovXtoY(FOV_Y_DEFAULT_MIN, width, height);
            maxFovYDampingLimit = getFovXtoY(FOV_Y_MAX_DAMPING_LIMIT, width, height);
            minFovYDampingLimit = getFovXtoY(FOV_Y_MIN_DAMPING_LIMIT, width, height);
            maxFovYOverLimit = getFovXtoY(FOV_Y_MAX_OVER_LIMIT, width, height);
            minFovYOverLimit = getFovXtoY(FOV_Y_MIN_OVER_LIMIT, width, height);
        }
        prevH = height;
        doFovYResetAnimation = true;
        fovInitialized = true;
    }

    private void swapFovXY() {
        fovY = getFovXtoY(fovY, prevH, height);
        defaultFovY = getFovXtoY(defaultFovY, prevH, height);
        maxFovY = getFovXtoY(maxFovY, prevH, height);
        minFovY = getFovXtoY(minFovY, prevH, height);
        maxFovYDampingLimit = getFovXtoY(maxFovYDampingLimit, prevH, height);
        minFovYDampingLimit = getFovXtoY(minFovYDampingLimit, prevH, height);
        maxFovYOverLimit = getFovXtoY(maxFovYOverLimit, prevH, height);
        minFovYOverLimit = getFovXtoY(minFovYOverLimit, prevH, height);
        prevH = height;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0087  */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x007c  */
    /* JADX WARNING: Removed duplicated region for block: B:34:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00bd  */
    public void cameraPanning(MotionEvent motionEvent) {
        float sqrt = 0.0f;
        int action;
        float f = width * 0.5f;
        float f2 = height * 0.5f;
        if (hmd) {
            f = width * 0.5f;
        }
        float f3 = -f2;
        float tan = f2 / ((float) Math.tan(fovY * 0.5f));

        int pointerCount = motionEvent.getPointerCount();
        if (pointerCount == 1) {
            f = motionEvent.getX() - f;
            f3 = -(motionEvent.getY() - f2);
        } else if (pointerCount >= 2) {
            int findPointerIndex = motionEvent.findPointerIndex(motionEvent.getPointerId(0));
            int findPointerIndex2 = motionEvent.findPointerIndex(motionEvent.getPointerId(1));
            float x = motionEvent.getX(findPointerIndex) - f;
            float x2 = motionEvent.getX(findPointerIndex2) - f;
            f = -(motionEvent.getY(findPointerIndex) - f2);
            x2 -= x;
            f2 = (-(motionEvent.getY(findPointerIndex2) - f2)) - f;
            sqrt = (float) Math.sqrt((x2 * x2) + (f2 * f2));
            float f4 = (x2 * 0.5f) + x;
            f3 = f + (0.5f * f2);
            f = f4;
        }

        action = motionEvent.getAction() & 255;
        if (action == MotionEvent.ACTION_DOWN) {
            touchYaw = 0.0f;
            touchPitch = 0.0f;
            prevX = f;
            prevY = f3;
            doMomentum = false;
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_OUTSIDE) {
            doMomentum = true;
            doPitchDamping = true;
            doFovDamping = true;
        } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
            zoom = 0.0f;
            prevX = f;
            prevY = f3;
            prevR = sqrt;
            doMomentum = false;
            doPitchDamping = false;
            doFovDamping = false;
        } else if (action == MotionEvent.ACTION_POINTER_UP) {
            isPointerPivot = true;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            return;
        }
        if (isPointerPivot) {
            prevX = f;
            prevY = f3;
            isPointerPivot = false;
            return;
        }
        float f5 = f - prevX;
        f2 = f3 - prevY;
        float sqrt2 = (float) Math.sqrt(((f5 * f5) + (f2 * f2)) + (tan * tan));
        f2 /= sqrt2;
        tan /= sqrt2;
        touchYaw += (float) Math.atan2(f5 / sqrt2, tan / (eyeZ + EYE_Z_DEFAULT_SPHERICAL));
        touchPitch += (float) Math.atan2(f2, tan / (eyeZ + EYE_Z_DEFAULT_SPHERICAL));
        prevX = f;
        prevY = f3;
        if (pointerCount >= 2) {
            zoom = ((float) Math.atan2(((double) ((sqrt - prevR) / sqrt2)) * 0.5d, 1.0d)) * 2.0f;
            prevR = sqrt;
        }
    }

    private void eyeZResetAnimation() {
        if (eyeZ < defaultEyeZ + EYE_Z_CHANGE_LIMIT && eyeZ > defaultEyeZ - EYE_Z_CHANGE_LIMIT) {
            eyeZ = defaultEyeZ;
            doEyeZResetAnimation = false;
        } else if (eyeZ < defaultEyeZ) {
            if (eyeZ == 0.0f) {
                eyeZ = PITCH_DECAY_ADJUST;
            }
            eyeZ /= EYE_Z_CHANGE_DECAY;
        } else {
            if (eyeZ == 0.0f) {
                eyeZ = PITCH_DECAY_ADJUST;
            }
            eyeZ *= EYE_Z_CHANGE_DECAY;
        }
    }

    private void fovYResetAnimation() {
        if (fovY < defaultFovY + FOV_Y_RESET_LIMIT && fovY > defaultFovY - FOV_Y_RESET_LIMIT) {
            fovY = defaultFovY;
            doFovYResetAnimation = false;
        } else if (fovY < defaultFovY) {
            fovY /= FOV_Y_RESET_DECAY;
        } else {
            fovY *= FOV_Y_RESET_DECAY;
        }
    }

    private void panningResetAnimation() {
        touchYaw = 0.0f;
        touchPitch = 0.0f;
        if (rotY >= PAN_RESET_LIMIT || rotY <= (-PAN_RESET_LIMIT)) {
            rotY *= PAN_RESET_DECAY;
        } else {
            rotY = 0.0f;
            rotYReset = true;
        }
        if (sensorMotion) {
            rotXReset = true;
            rotZReset = true;
        } else {
            if (rotX >= PAN_RESET_LIMIT || rotX <= (-PAN_RESET_LIMIT)) {
                rotX *= PAN_RESET_DECAY;
            } else {
                rotX = 0.0f;
                rotXReset = true;
            }
            if (rotZ >= PAN_RESET_LIMIT || rotZ <= (-PAN_RESET_LIMIT)) {
                rotZ *= PAN_RESET_DECAY;
            } else {
                rotZ = 0.0f;
                rotZReset = true;
            }
        }
        if (rotXReset && rotYReset && rotZReset) {
            rotXReset = false;
            rotYReset = false;
            rotZReset = false;
            doPanningResetAnimation = false;
        }
    }

    private void initEyeZ() {
        if (hmd) {
            defaultEyeZ = EYE_Z_DEFAULT_HMD;
        } else if (spherical) {
            defaultEyeZ = EYE_Z_DEFAULT_SPHERICAL;
        } else {
            defaultEyeZ = 0.0f;
        }
        doEyeZResetAnimation = true;
    }

    private void cameraAnimation() {
        if (doEyeZResetAnimation) {
            eyeZResetAnimation();
        }
        if (doFovYResetAnimation) {
            fovYResetAnimation();
        }
        if (doPanningResetAnimation) {
            panningResetAnimation();
            return;
        }
        float adjustEyeZFovY;
        rotY += touchYaw - sensorYaw;
        if (!sensorMotion) {
            if (rotX > YAW_MAX || rotX < YAW_MIN) {
                touchPitch *= PITCH_OVER_DECAY - adjustEyeZFovY(0.05f);
            }
            rotX -= touchPitch;
        }
        sensorYaw = 0.0f;
        if (autoPanning) {
            if (turnReverse) {
                rotY -= PAN_AUTO_DEFAULT * (eyeZ + EYE_Z_DEFAULT_SPHERICAL);
            } else {
                rotY += PAN_AUTO_DEFAULT * (eyeZ + EYE_Z_DEFAULT_SPHERICAL);
            }
        }
        if (doMomentum) {
            turnReverse = touchYaw < 0.0f;
            adjustEyeZFovY = MOMENTUM_DECAY - adjustEyeZFovY(MOMENTUM_DECAY_ADJUST);
            touchYaw *= adjustEyeZFovY;
            touchPitch *= adjustEyeZFovY;
            if (touchYaw < MOMENTUM_LIMIT && touchYaw > -1.0E-4f && touchPitch < MOMENTUM_LIMIT && touchPitch > -1.0E-4f) {
                touchYaw = 0.0f;
                touchPitch = 0.0f;
                doMomentum = false;
            }
        } else {
            touchYaw = 0.0f;
            touchPitch = 0.0f;
        }
        if (rotY > PITCH_MAX) {
            rotY -= PITCH_MAX;
        } else if (rotY < PITCH_MIN) {
            rotY -= PITCH_MIN;
        }
        if (doPitchDamping) {
            if (rotX > YAW_MAX) {
                if (rotX < PITCH_DAMPING_LIMIT + adjustEyeZFovY((float) Math.toRadians(4.0d))) {
                    rotX = YAW_MAX;
                    doPitchDamping = false;
                } else {
                    rotX *= PITCH_DAMPING_DECAY - adjustEyeZFovY(PITCH_DECAY_ADJUST);
                }
            } else if (rotX < YAW_MIN) {
                if (rotX > (-PITCH_DAMPING_LIMIT) - adjustEyeZFovY((float) Math.toRadians(4.0d))) {
                    rotX = YAW_MIN;
                    doPitchDamping = false;
                } else {
                    rotX *= PITCH_DAMPING_DECAY - adjustEyeZFovY(PITCH_DECAY_ADJUST);
                }
            }
        }
        adjustEyeZFovY = FOV_Y_MAX_OVER_DECAY;
        if (isLandscape) {
            adjustEyeZFovY = 0.105f;
        }
        if (fovY > maxFovY) {
            zoom *= adjustEyeZFovY;
            fovY -= zoom;
        } else if (fovY < minFovY) {
            zoom *= PITCH_OVER_DECAY;
            fovY -= zoom;
        } else {
            fovY -= zoom;
            zoom = 0.0f;
        }
        if (doFovDamping) {
            if (fovY > maxFovY) {
                if (fovY < maxFovYDampingLimit) {
                    fovY = maxFovY;
                    doFovDamping = false;
                } else {
                    fovY *= FOV_Y_MAX_DAMPING_DECAY;
                }
            } else if (fovY < minFovY) {
                if (fovY > minFovYDampingLimit) {
                    fovY = minFovY;
                    doFovDamping = false;
                } else {
                    fovY /= FOV_Y_MIN_DAMPING_DECAY;
                }
            }
        }
        if (fovY > maxFovYOverLimit) {
            fovY = maxFovYOverLimit;
        } else if (fovY < minFovYOverLimit) {
            fovY = minFovYOverLimit;
        }
    }

    private float adjustEyeZFovY(float f) {
        return ((fovY / defaultFovY) * (eyeZ + EYE_Z_DEFAULT_SPHERICAL)) * f;
    }

    float getFovY() {
        return fovY;
    }

    float getRotX() {
        return rotX;
    }

    float getRotY() {
        return rotY;
    }

    float getRotZ() {
        return rotZ;
    }

    float getEyeZ() {
        return eyeZ;
    }

    float getMaxFovY() {
        return maxFovY;
    }

    float getMinFovY() {
        return minFovY;
    }

    private float getFovXtoY(float f, float f2, float f3) {
        return ((float) Math.atan2(f3 * 0.5f, (f2 * 0.5f) / ((float) Math.tan(f * 0.5f)))) * 2.0f;
    }

    float[] getSensorRaw() {
        return sensorRaw;
    }

    void setHmd(boolean hmd) {
        this.hmd = hmd;
        if (width < height) {
            fovInitialized = false;
        } else {
            initFov();
        }
        initEyeZ();
        doFovDamping = true;
    }

    boolean isHmd() {
        return hmd;
    }

    void setSpherical(boolean spherical) {
        this.spherical = spherical;
        if (spherical) {
            defaultEyeZ = EYE_Z_DEFAULT_SPHERICAL;
            if (width < height) {
                maxFovY = FOV_Y_SPHERICAL_EYE_MAX;
            } else {
                maxFovY = getFovXtoY(FOV_Y_SPHERICAL_EYE_MAX, width, height);
            }
        } else {
            defaultEyeZ = EYE_Z_DEFAULT;
            if (width < height) {
                maxFovY = FOV_Y_DEFAULT_MAX;
            } else {
                maxFovY = getFovXtoY(FOV_Y_DEFAULT_MAX, width, height);
            }
        }
        doEyeZResetAnimation = true;
        doFovDamping = true;
    }

    boolean isSpherical() {
        return spherical;
    }

    void setSensorMotion(boolean sensorMotion) {
        this.sensorMotion = sensorMotion;
        float f = PITCH_MAX * 0.5f;
        if (rotY > f) {
            rotY -= PITCH_MAX;
        } else if (rotY < (-f)) {
            rotY += PITCH_MAX;
        }
        doPanningResetAnimation = true;
    }

    boolean isSensorMotion() {
        return sensorMotion;
    }

    boolean isSensorRegistered() {
        return sensorRegistered;
    }

    void setAutoPanning(boolean autoPanning) {
        this.autoPanning = autoPanning;
    }

    boolean isAutoPanning() {
        return autoPanning;
    }

    void setCameraReset(boolean cameraReset) {
        if (cameraReset) {
            if (rotY > PITCH_MAX * 0.5f) {
                rotY -= PITCH_MAX;
            } else if (rotY < -(PITCH_MAX * 0.5f)) {
                rotY += PITCH_MAX;
            }
            doPanningResetAnimation = true;
            doEyeZResetAnimation = true;
            doFovYResetAnimation = true;
            return;
        }
        rotX = 0.0f;
        rotY = 0.0f;
        rotZ = 0.0f;
        if (spherical) {
            eyeZ = EYE_Z_DEFAULT_SPHERICAL;
        } else if (hmd) {
            eyeZ = EYE_Z_DEFAULT_HMD;
        } else {
            eyeZ = EYE_Z_DEFAULT;
        }
        fovY = defaultFovY;
    }

    boolean isCameraReset() {
        return fovY == defaultFovY && rotY == 0.0f && (sensorMotion || rotX == 0.0f && rotZ == 0.0f);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int type = sensorEvent.sensor.getType();
        if ((type == 15 || type == 11) && sensorMotion) {
            sensorRaw = sensorEvent.values;
            sensorXYZ[0] = sensorEvent.values[0];
            sensorXYZ[1] = sensorEvent.values[1];
            sensorXYZ[2] = sensorEvent.values[2];
            SensorManager.getRotationMatrixFromVector(rotM, sensorXYZ);
            switch (windowManager.getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_0:
                    SensorManager.remapCoordinateSystem(rotM, 1, 3, remapM);
                    break;
                case Surface.ROTATION_90:
                    SensorManager.remapCoordinateSystem(rotM, 3, 129, remapM);
                    break;
                case Surface.ROTATION_180:
                    SensorManager.remapCoordinateSystem(rotM, 129, 131, remapM);
                    break;
                case Surface.ROTATION_270:
                    SensorManager.remapCoordinateSystem(rotM, 131, 1, remapM);
                    break;
            }
            SensorManager.getOrientation(remapM, orientation);
            sensorYaw = orientation[0] - prevOrientation[0];
            rotX = -orientation[1];
            rotZ = -orientation[2];
            prevOrientation[0] = orientation[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
