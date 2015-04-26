package detectors;

import java.util.ArrayList;
import java.util.List;

import detectors.Detector;
import io.github.ppo_birds_detector.android.DetectedObject;

/**
 * Created by Alek on 2015-04-26.
 */
public class SimpleDetector extends Detector {
    @Override
    protected List<DetectedObject> processFrame(byte [] frame){
        List<DetectedObject> detectedObjects = new ArrayList<>();
        DetectedObject detectedObject = new DetectedObject(0.5f, 0.5f, 0.2f,0.2f);
        detectedObjects.add(detectedObject);
        return detectedObjects;
    }
}
