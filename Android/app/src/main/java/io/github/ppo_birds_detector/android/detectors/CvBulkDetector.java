package io.github.ppo_birds_detector.android.detectors;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import io.github.ppo_birds_detector.android.DetectedObject;
import io.github.ppo_birds_detector.android.DetectorView;

/**
 * Created by ike on 17/05/15.
 */
public class CvBulkDetector extends CvDetector {
    private static final double THRESHOLD_DETECT = 10.5;
    public static int BLOCKS_H = 20;
    public static int BLOCKS_V = 15;
    public static float THRESHOLD = 30.0f;

    private DetectorView mDetectorView;

    private Mat mPrevMat;
    private Mat mDiff;
    private Mat mHsvBackgroundMat;
    private Mat mHsvCurrentMat;
    private Mat mEroded;
    private Mat mErosionElement;
    private boolean mDisplayRaw;
    private float mRatio;

    @Override
    public void onStart() {
        mDiff = new Mat();
        mHsvBackgroundMat = new Mat();
        mHsvCurrentMat = new Mat();
        mEroded = new Mat();

        int erosion_size = 1;
        mErosionElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                new Size(2 * erosion_size + 1, 2 * erosion_size + 1),
                new Point(erosion_size, erosion_size));

        mDisplayRaw = false;

        mRatio = 4.0f / 3.0f;
        if (mDetectorView != null)
            mRatio = mDetectorView.getWidth() / mDetectorView.getHeight();
    }

    @Override
    public void onStop() {
        // Explicitly deallocate Mats
        if (mHsvBackgroundMat != null)
            mHsvBackgroundMat.release();
        mHsvBackgroundMat = null;

        if (mHsvCurrentMat != null)
            mHsvCurrentMat.release();
        mHsvCurrentMat = null;

        if (mDiff != null)
            mDiff.release();
        mDiff = null;
    }

    @Override
    public Mat onFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//        Mat rgba = inputFrame.rgba();
//        Size sizeRgba = rgba.size();
//
//        int rows = (int) sizeRgba.height;
//        int cols = (int) sizeRgba.width;
//
//        int left = cols / 8;
//        int top = rows / 8;
//
//        int width = cols * 3 / 4;
//        int height = rows * 3 / 4;


        Mat rgba = inputFrame.rgba();
        Size size = rgba.size();
        int rows = (int) size.height;
        int cols = (int) size.width;
        int top = 0;
//        int height = (int) (size.width / mRatio);
//        Mat wide = rgba.submat(top, top + height, 0, cols);


        Mat gray = inputFrame.gray();

        List<DetectedObject> detectedObjects = new ArrayList<>();
        Mat detection = detectMotion(mPrevMat, gray, detectedObjects);
        mDetectorView.setDetectedObjects(detectedObjects);

        mPrevMat = gray;

        return mDisplayRaw ? inputFrame.rgba() : detection;
    }

    private Mat detectMotion(Mat prev, Mat current, List<DetectedObject> detectedObjects) {
        if (prev == null) {
            return current;
        }


//        Mat grayInnerWindow = gray.submat(top, top + height, left, left + width);
//        Mat rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
//        rgbaInnerWindow.setTo(grayInnerWindow);
//        Imgproc.Sobel(grayInnerWindow, mIntermediateMat, CvType.CV_8U, 1, 1);
//        Core.convertScaleAbs(mIntermediateMat, mIntermediateMat, 10, 0);
//        Imgproc.cvtColor(grayInnerWindow, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
//        grayInnerWindow.release();
//        rgbaInnerWindow.release();

        Mat filtered = getFilteredFrame(prev, current);
        findDetectedObjects(filtered, detectedObjects);
        return filtered;
    }

    private Mat getFilteredFrame(Mat background, Mat current) {
        // Convert color space

//        Imgproc.cvtColor(background, mHsvBackgroundMat, Imgproc.COLOR_BGR2HSV);
//        Imgproc.cvtColor(current, mHsvCurrentMat, Imgproc.COLOR_BGR2HSV);

        // Difference

        Core.absdiff(background, current, mDiff);

        // Threshold

        Mat foregroundMask = Mat.zeros(mDiff.rows(), mDiff.cols(), CvType.CV_8UC1);
        Imgproc.threshold(mDiff, foregroundMask, THRESHOLD, 255, 0);

        // Erosion

        Imgproc.erode(foregroundMask, mEroded, mErosionElement);

        return mEroded;
    }

    private List<DetectedObject> findDetectedObjects(Mat frame, List<DetectedObject> detectedObjects) {
        int width = frame.width();
        int height = frame.height();

        int blockSizeX = (int)(width / (float) BLOCKS_H);
        int blockSizeY = (int)(height / (float) BLOCKS_V);

        for (int y = 0; y <= height - blockSizeY; y += blockSizeY) {
            for (int x = 0; x <= width - blockSizeX; x += blockSizeX) {
                if (detectInBlock(frame, x, y, blockSizeX, blockSizeY)) {
                    //detected movement in block
                    float X = (float) x / width;
                    float Y = (float) y / height;
                    float objWidth = (float) blockSizeX / width;
                    float objHeight = (float) blockSizeY / height;
                    DetectedObject detectedObject = new DetectedObject(X, Y, objWidth, objHeight);
                    detectedObjects.add(detectedObject);
                }
            }
        }

        return detectedObjects;
    }

    private boolean detectInBlock(Mat bmp, int startX, int startY, int blockSizeX, int blockSizeY) {
        Mat subMat = bmp.submat(startY, startY + blockSizeY, startX, startX + blockSizeX);
        double val = Core.sumElems(subMat).val[0] / (blockSizeX * blockSizeY);
        return val > THRESHOLD_DETECT;
    }

    @Override
    public void setDetectorView(DetectorView view) {
        mDetectorView = view;
    }

    @Override
    public void toggleDisplay() {
        mDisplayRaw = !mDisplayRaw;
    }
}
