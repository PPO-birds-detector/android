package io.github.ppo_birds_detector.android;

import android.hardware.Camera;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import detectors.BulkDetector;
import detectors.Detector;
import detectors.MockDetector;

public class MainActivity extends ActionBarActivity {

    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private Detector mDetector;
    private DetectorView mDetectorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TAG", "onCreate");
        setContentView(R.layout.activity_main);
        mCameraPreview = (CameraPreview) findViewById(R.id.preview);
        mCameraPreview.addOnPreviewSurfaceChangedListener(new CameraPreview.OnPreviewSurfaceChangedListener() {
            @Override
            public void onPreviewSurfaceChanged() {
//              mCamera.setDisplayOrientation(90);
                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
//                        Log.d("TAG", "previewFrame");
                        mDetector.detect(data);
                    }
                });

                // Important: Call startPreview() to start updating the preview surface.
                // Preview must be started before you can take a picture.
                mCamera.startPreview();
                mDetector.setCameraParameters(mCamera.getParameters());
            }
        });
        mDetectorView = (DetectorView) findViewById(R.id.detector);
        mDetector = new BulkDetector();
        mDetector.setDetectorView(mDetectorView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TAG", "onResume");
        safeCameraOpen(0);
        mCameraPreview.setCamera(mCamera);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCameraAndPreview();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;

        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);
            qOpened = (mCamera != null);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }

    private void releaseCameraAndPreview() {
        mCameraPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public interface IDetector {
        public void detect(byte[] frame);
        public void setDetectorView(DetectorView view);
    }
}
