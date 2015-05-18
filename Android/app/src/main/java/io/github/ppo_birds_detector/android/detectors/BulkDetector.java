package io.github.ppo_birds_detector.android.detectors;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

import Catalano.Imaging.Concurrent.Filters.Grayscale;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Difference;
import Catalano.Imaging.Filters.Erosion;
import Catalano.Imaging.Filters.Threshold;
import io.github.ppo_birds_detector.android.DetectedObject;

/**
 * Created by Alek on 2015-04-27.
 */
public class BulkDetector extends Detector {
    private Bitmap prevFrame;

    // percentage of image
    public static float BLOCK_RATIO = 0.1f;
    public static int THRESHOLD = 15;

    @Override
    protected List<DetectedObject> processFrame(byte[] frame) {
        List<DetectedObject> detectedObjects;
        Bitmap nextFrame = dataToBitmap(frame);
        nextFrame = Bitmap.createScaledBitmap(nextFrame, 256, 256, false);
//        FastBitmap nextFrame = new FastBitmap();

        if (prevFrame == null) {
            prevFrame = nextFrame;
            detectedObjects = new ArrayList<>();
        } else {
            detectedObjects = BulkMotionDetect(prevFrame, nextFrame);
            prevFrame = nextFrame;
        }
        return detectedObjects;
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    private List<DetectedObject> BulkMotionDetect(Bitmap previousFrame, Bitmap currentFrame) {
        List<DetectedObject> detectedObjects;

        Bitmap bmp = toGrayscale(previousFrame);
        Bitmap bmp2 = toGrayscale(currentFrame);

        FastBitmap prev, next, filtered;
        prev = new FastBitmap(bmp);
        next = new FastBitmap(bmp2);
//        prev = new FastBitmap(previousFrame);
//        next = new FastBitmap(currentFrame);
        filtered = getFilteredFrame(prev, next);

        if (mFilteredView != null)
            mFilteredView.setImageBitmap(filtered.toBitmap());

        detectedObjects = findDetectedObjects(filtered);

        return detectedObjects;
    }

    private FastBitmap getFilteredFrame(FastBitmap previousFrame, FastBitmap currentFrame) {
        // create filters
        Difference differenceFilter = new Difference();
        Threshold thresholdFilter = new Threshold(THRESHOLD);
        Grayscale grayscale = new Grayscale(Grayscale.Algorithm.Lightness);
        Erosion erosion = new Erosion();

        // apply filters
        previousFrame.toGrayscale();
        currentFrame.toGrayscale();
//        grayscale.applyInPlace(previousFrame);
//        grayscale.applyInPlace(currentFrame);

        // set background frame as an overlay for difference filter
        differenceFilter.setOverlayImage(previousFrame);

        differenceFilter.applyInPlace(currentFrame);
        thresholdFilter.applyInPlace(currentFrame);
        erosion.applyInPlace(currentFrame);

        return currentFrame;
    }

    private List<DetectedObject> findDetectedObjects(FastBitmap frame) {
        List<DetectedObject> detectedObjects = new ArrayList<>();
        int width = frame.getWidth();
        int heigth = frame.getHeight();

        int blockSize = (int)(BLOCK_RATIO * width);

        for (int x = 0; x < width - blockSize; x += blockSize) {
            for (int y = 0; y < heigth - blockSize; y += blockSize) {
                if (detectInBlock(frame, x, y, blockSize)) {
                    //detected movement in block
                    float X = (float)x / frame.getWidth();
                    float Y = (float)y / frame.getHeight();
                    float objWidth = (float)blockSize / frame.getWidth();
                    DetectedObject detectedObject = new DetectedObject(X, Y, objWidth, objWidth);
                    detectedObjects.add(detectedObject);
                }
            }
        }

        return detectedObjects;
    }

    private boolean detectInBlock(FastBitmap bmp, int startX, int startY, int blockSize) {
        float val = 0f;

        for (int x = startX; x < startX + blockSize; ++x) {
            for (int y = startY; y < startY + blockSize; ++y) {
                int v = bmp.getGray(y, x);
                val += v;
            }
        }

        // average the value
        val = val / blockSize / blockSize;

        return val > 0.1f;
    }
}
