package io.github.ppo_birds_detector.android.detectors;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

import io.github.ppo_birds_detector.android.DetectorView;

/**
 * Created by ike on 17/05/15.
 */
public abstract class CvDetector {
    public abstract void onStart();

    public abstract void onStop();

    public abstract Mat onFrame(Mat rgba, Mat gray);

    public abstract void setDetectorView(DetectorView view);

    public abstract void toggleDisplay();
}
