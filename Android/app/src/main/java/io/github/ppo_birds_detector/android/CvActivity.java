package io.github.ppo_birds_detector.android;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import io.github.ppo_birds_detector.android.detectors.CvBulkDetector;
import io.github.ppo_birds_detector.android.detectors.CvEdgeDetector;
import io.github.ppo_birds_detector.android.detectors.CvDetector;
import io.github.ppo_birds_detector.android.detectors.CvBlockDetector;

/**
 * Created by ike on 17/05/15.
 */
public class CvActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String  TAG = "CvActivity";

    private WideCameraView mOpenCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    private CvDetector mDetector;
    private DetectorView mDetectorView;
    private FrameLayout mLayout;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.image_manipulation_surface_view);

        mOpenCvCameraView = (WideCameraView) findViewById(R.id.image_manipulations_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mDetectorView = (DetectorView) findViewById(R.id.detector);

        mDetector = new CvBlockDetector();
        mDetector.setDetectorView(mDetectorView);

        mLayout = (FrameLayout) findViewById(R.id.layout);
        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDetector.toggleDisplay();
            }
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detectors_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.select_bulk_detector:
                mDetector.onStop();
                mDetector = new CvBulkDetector();
                initDetector();
                return true;
            case R.id.select_edge_detector:
                mDetector.onStop();
                mDetector = new CvEdgeDetector();
                initDetector();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initDetector(){
        if (mDetector != null) {
            if (mDetectorView == null) {
                mDetectorView = (DetectorView) findViewById(R.id.detector);
            }
            mDetector.onStart();
            mDetector.setDetectorView(mDetectorView);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float aspectRatio = size.x / (float) size.y;
        mOpenCvCameraView.setWideResolution(aspectRatio);
        float cameraRatio = mOpenCvCameraView.getRatio();
        mDetectorView.setRatio(cameraRatio);

        mDetector.onStart();
    }

    public void onCameraViewStopped() {
        mDetector.onStop();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return mDetector.onFrame(inputFrame);
    }
}
