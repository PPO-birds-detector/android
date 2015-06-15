package io.github.ppo_birds_detector.android.detectors;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.github.ppo_birds_detector.android.Bezier;
import io.github.ppo_birds_detector.android.DetectedObject;
import io.github.ppo_birds_detector.android.DetectorView;

/**
 * Created by ike on 17/05/15.
 */
public class CvBulkDetector extends CvDetector {
    private static final double THRESHOLD_DETECT = 0.5;
    private static final String TAG = "CvBulkDetector";
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
    private Mat mBezierLUT;
    private Mat mDilationElement;
    private int count;

    @Override
    public void onStart() {
        mDiff = new Mat();
        mHsvBackgroundMat = new Mat();
        mHsvCurrentMat = new Mat();
        mEroded = new Mat();

        int erosion_size = 1;
        mErosionElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
                new Size(2 * erosion_size + 1, 2 * erosion_size + 1),
                new Point(erosion_size, erosion_size));

        int dilation_size = 2;
        mDilationElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
                new Size(2 * dilation_size + 1, 2 * dilation_size + 1),
                new Point(dilation_size, dilation_size));

        mDisplayRaw = false;

        mRatio = 4.0f / 3.0f;
        if (mDetectorView != null)
            mRatio = mDetectorView.getWidth() / mDetectorView.getHeight();

        mBezierLUT = new Mat(1, 256, CvType.CV_8U);
        Bezier bezier = new Bezier(0.5f, 0.0f, 0.5f, 1.0f);
//        Bezier bezier = new Bezier(0.73f, 0.04f, 0.38f, 0.83f);
        for (int i = 0; i < 256; ++i) {
            float val = 255 * bezier.getValue(i / 255.0f, 255);
            mBezierLUT.row(0).col(i).setTo(new Scalar(val));
        }


        count = 0;
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
    public Mat onFrame(Mat rgba, Mat gray) {
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


        Size size = rgba.size();
        int rows = (int) size.height;
        int cols = (int) size.width;
        int top = 0;
//        int height = (int) (size.width / mRatio);
//        Mat wide = rgba.submat(top, top + height, 0, cols);



        List<DetectedObject> detectedObjects = new ArrayList<>();
        Mat detection = detectMotion(mPrevMat, gray, detectedObjects);
        mDetectorView.setDetectedObjects(detectedObjects);

        mPrevMat = gray;

        return mDisplayRaw ? rgba : detection;
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

        Mat frame = new Mat();
//        Mat dilated = new Mat();
//        int dilation_size = 10;
//        Mat dilationElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
//                new Size(2 * dilation_size + 1, 2 * dilation_size + 1),
//                new Point(dilation_size, dilation_size));

//        float THRESHOLD = 50.0f;
        Mat bg2 = new Mat();
        Mat cur2 = new Mat();
//        Imgproc.medianBlur(background, bg2, 5);
//        Imgproc.medianBlur(current, cur2, 5);
//        Core.absdiff(bg2, cur2, frame);
//        Core.absdiff(background, current, frame);
//        Imgproc.blur(background, bg2, new Size(3, 3));
//        Imgproc.blur(current, cur2, new Size(3, 3));
        Core.subtract(background, current, frame);
//        Mat foregroundMask = Mat.zeros(frame.rows(), frame.cols(), CvType.CV_8UC1);

        Mat afterLut = new Mat();
        Mat eroded = new Mat();

        Core.LUT(frame, mBezierLUT, afterLut);
        Imgproc.threshold(afterLut, frame, 15.0, 255, 0);
        Imgproc.erode(frame, eroded, mErosionElement);
        Imgproc.dilate(eroded, frame, mDilationElement);

        Core.addWeighted(current, 0.4, frame, 0.6, 0.0, cur2);
//        // Difference
//
//        Core.absdiff(background, current, mDiff);
//
//        // Threshold
//
//        Mat foregroundMask = Mat.zeros(mDiff.rows(), mDiff.cols(), CvType.CV_8UC1);
//        Imgproc.threshold(mDiff, foregroundMask, THRESHOLD, 255, 0);
//
//        // Erosion
//
//        Imgproc.erode(foregroundMask, mEroded, mErosionElement);

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyAppDir");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(TAG, "failed to create directory");
                return null;
            }
        }

        final Bitmap bmp = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(cur2, bmp);
        String path = mediaStorageDir.getPath() + File.separator + "testimage_" + count + ".png";
        Log.d(TAG, "Path: " + path);
        File mediaFile = new File(path);
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(mediaFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bmp.compress(Bitmap.CompressFormat.PNG, 80, stream);
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ++count;

        return cur2;
    }

    protected List<DetectedObject> findDetectedObjects(Mat frame, List<DetectedObject> detectedObjects) {
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

    protected boolean detectInBlock(Mat bmp, int startX, int startY, int blockSizeX, int blockSizeY) {
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
