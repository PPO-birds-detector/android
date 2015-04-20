package io.github.ppo_birds_detector.android;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ike on 20/04/15.
 */
public class SimpleDetector implements MainActivity.Detector {
    private DetectorView mDetectorView;

    public SimpleDetector() {
    }

    @Override
    public void setDetectorView(DetectorView view) {
        mDetectorView = view;
    }

    public void detect(byte[] frame) {
        List<DetectedObject> detectedObjects = new ArrayList<>();
        detectedObjects.add(new DetectedObject(0.5f, 0.5f, 0.1f, 0.1f));
        detectedObjects.add(new DetectedObject(0.8f, 0.7f, 0.1f, 0.1f));
        mDetectorView.setDetectedObjects(detectedObjects);
    }
}
