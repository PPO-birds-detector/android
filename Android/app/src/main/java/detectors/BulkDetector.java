package detectors;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

import Catalano.Imaging.Concurrent.Filters.Grayscale;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Difference;
import Catalano.Imaging.Filters.Erosion;
import Catalano.Imaging.Filters.Threshold;
import Catalano.Imaging.IBaseInPlace;
import io.github.ppo_birds_detector.android.DetectedObject;

/**
 * Created by Alek on 2015-04-27.
 */
public class BulkDetector extends Detector {
    private FastBitmap prevFrame;

    @Override
    protected List<DetectedObject> processFrame(byte [] frame){
        List<DetectedObject> detectedObjects;
        FastBitmap nextFrame = new FastBitmap(dataToBitmap(frame));

        if(prevFrame == null){
            prevFrame = nextFrame;
            detectedObjects = new ArrayList<>();
        }else{
            detectedObjects = BulkMotionDetect(prevFrame, nextFrame);
            prevFrame = nextFrame;
        }
        return detectedObjects;
    }
    private List<DetectedObject> BulkMotionDetect(FastBitmap previousFrame, FastBitmap currentFrame){
        List<DetectedObject> detectedObjects = new ArrayList<>();;

        // create filters
        Difference differenceFilter = new Difference( );
        Threshold thresholdFilter = new Threshold( 15 );
        Grayscale grayscale = new Grayscale();
        Erosion erosion = new Erosion();

        grayscale.applyInPlace(previousFrame);
        grayscale.applyInPlace(currentFrame);

        // set backgroud frame as an overlay for difference filter
        differenceFilter.setOverlayImage(previousFrame);

        differenceFilter.applyInPlace( currentFrame );
        thresholdFilter.applyInPlace( currentFrame );
        erosion.applyInPlace(currentFrame);

        return detectedObjects;
    }
}
