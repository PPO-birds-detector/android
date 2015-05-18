package io.github.ppo_birds_detector.android;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;
import org.opencv.core.Size;

import java.util.List;

/**
 * Created by ike on 18/05/15.
 */
public class WideCameraView extends JavaCameraView {

    public WideCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public List<Camera.Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(Camera.Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public Camera.Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }

    public float getRatio() {
        Camera.Size resolution = getResolution();
        int width = resolution.width;
        int height = resolution.height;
        return width / (float) height;
    }

    public void setWideResolution(float ratio) {
        List<Camera.Size> sizes = getResolutionList();
        Camera.Size resolution = getResolution();
        int width = resolution.width;
        int height = resolution.height;
        float nearestRatioDiff = Math.abs(ratio - (width / (float) height));
        Camera.Size newSize = resolution;

        if (sizes != null) {
            for (Camera.Size size : sizes) {
                float sizeRatio = size.width / (float) size.height;
                float diff = Math.abs(ratio - sizeRatio);
                if (diff >= 0 && diff < nearestRatioDiff && size.width == width) {
                    newSize = size;
                    nearestRatioDiff = diff;
                }
            }
        }

        setResolution(newSize);
    }
}
