package io.github.ppo_birds_detector.android;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ike on 20/04/15.
 */
class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera mCamera;
    private List<Camera.Size> mSupportedPreviewSizes;
    private Camera.Size mPreviewSize;

    public CameraPreview(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Log.d("TAG", "Preview constructor");

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        getHolder().addCallback(this);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera) {
        if (mCamera == camera) { return; }
        Log.d("TAG", "setCamera");

        stopPreviewAndFreeCamera();

        mCamera = camera;

        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
//            for (Camera.Size size : mSupportedPreviewSizes)
//                Log.d("TAG", "Size: " + size.width + ", " + size.height);
            mPreviewSize = mSupportedPreviewSizes.get(0);
        }
    }

    /**
     * When this function returns, mCamera will be null.
     */
    private void stopPreviewAndFreeCamera() {

        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();

            mCamera.setPreviewCallback(null);

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mCamera.release();

            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Log.d("TAG", "surfChanged");
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        requestLayout();
        mCamera.setParameters(parameters);

        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mSurfaceChangedListener.onPreviewSurfaceChanged();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();
        }
    }

    private OnPreviewSurfaceChangedListener mSurfaceChangedListener;
    public void addOnPreviewSurfaceChangedListener(OnPreviewSurfaceChangedListener listener) {
        mSurfaceChangedListener = listener;
    }

    public interface OnPreviewSurfaceChangedListener {
        public void onPreviewSurfaceChanged();
    }
}