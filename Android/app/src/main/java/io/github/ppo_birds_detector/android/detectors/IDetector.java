package io.github.ppo_birds_detector.android.detectors;

import io.github.ppo_birds_detector.android.DetectorView;

/**
 * Created by Alek on 2015-05-18.
 */
public interface IDetector {
    public void detect(byte[] frame);
    public void setDetectorView(DetectorView view);
}