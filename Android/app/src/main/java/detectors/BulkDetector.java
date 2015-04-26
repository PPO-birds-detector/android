package detectors;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

import io.github.ppo_birds_detector.android.DetectedObject;
import utils.ImageUtils;

/**
 * Created by Alek on 2015-04-26.
 */
public class BulkDetector extends Detector {

    @Override
    protected List<DetectedObject> processFrame(byte [] frame){
        List<DetectedObject> detectedObjects;
        Bitmap nextFrame = ImageUtils.toGrayscale(dataToBitmap(frame));
        if(_prevFrame == null){
            _prevFrame = nextFrame;
            detectedObjects = new ArrayList<>();
        }else{
            detectedObjects = BulkMotionDetect(_prevFrame, nextFrame);
            _prevFrame = nextFrame;
        }
        return detectedObjects;
    }

    //algorithm variables
    private Bitmap _prevFrame;
    private static int DIFF_THRESHOLD = 300;
    public static void setDIFF_THRESHOLD(int val){
        DIFF_THRESHOLD = val;
    }
    private static int BLOCK_SIZE = 50;
    public static void setBLOCK_SIZE(int val){
        BLOCK_SIZE = val;
    }
    private static int NOISE_THRESHOLD = 25;
    public static void setNOISE_THRESHOLD(int val){
        NOISE_THRESHOLD = val;
    }

    private List<DetectedObject> BulkMotionDetect(Bitmap frame1, Bitmap frame2){
        List<DetectedObject> detectedObjects = new ArrayList<>();
        int width = frame1.getWidth();
        int height = frame1.getHeight();
        float objWidth = (float)BLOCK_SIZE / width;
        float objHeigth = (float)BLOCK_SIZE / height;


        int [] block1 = new int[width * height];
        int [] block2 = new int[width * height];

        for (int x = 0; x < width - BLOCK_SIZE; x += BLOCK_SIZE){
            for (int y = 0; y < height - BLOCK_SIZE; y += BLOCK_SIZE){
                frame1.getPixels(block1, 0, width, x, y, BLOCK_SIZE, BLOCK_SIZE);
                frame2.getPixels(block2, 0, width, x, y, BLOCK_SIZE, BLOCK_SIZE);
                int diff = getBlockDiff(block1, block2);

                if (diff > DIFF_THRESHOLD){
                    DetectedObject detectedObject = new DetectedObject((float)x / width, (float)y / height, objWidth, objHeigth );
                    detectedObjects.add(detectedObject);
                }
            }
        }
        return detectedObjects;
    }
    private int getBlockDiff(int [] block1, int [] block2){
        int totalDiff = 0, diff;
        for (int i = 0; i < BLOCK_SIZE; ++i){
            diff = Math.max(block2[i] - block1[i], 0);
            //cancel noise
            if (diff > NOISE_THRESHOLD)
                totalDiff += diff;
        }
        return totalDiff;
    }
}
