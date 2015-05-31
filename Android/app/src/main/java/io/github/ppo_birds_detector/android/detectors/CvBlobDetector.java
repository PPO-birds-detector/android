package io.github.ppo_birds_detector.android.detectors;

import android.util.Log;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Rect;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.opencv.features2d.FeatureDetector;

import java.util.ArrayList;
import java.util.List;

import io.github.ppo_birds_detector.android.DetectedObject;
import io.github.ppo_birds_detector.android.DetectorView;
import io.github.ppo_birds_detector.android.Utils;

/**
 * Created by ike on 17/05/15.
 */
public class CvBlobDetector extends CvDetector {

    private DetectorView mDetectorView;

    private Mat mPrevMat;
    private Mat mDiff;
    private Mat mHsvBackgroundMat;
    private Mat mHsvCurrentMat;
    private boolean mDisplayRaw;
    private float mRatio;

    // contours
    private int mMinContourArea = 70;

    // Canny edge finding algorithm params
    private int mCannyThreshold1 = 50;
    private int mCannyThreshold2 = 90;

    // SimpleBlobDetector params
    private int mMinArea = 70;

    private String mParamsFilename = "blob_params.yml";
    private String mParamsFilePath = "";

    @Override
    public void onStart() {
        mDiff = new Mat();
        mHsvBackgroundMat = new Mat();
        mHsvCurrentMat = new Mat();

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

        Mat filtered = filterFrame(prev, current);
        findDetectedObjects(filtered, detectedObjects);
        return filtered;
    }

    private Mat filterFrame(Mat prev, Mat current){
        Mat filtered = new Mat();

        Core.absdiff(prev, current, filtered);
        Imgproc.Canny(filtered, filtered, mCannyThreshold1, mCannyThreshold2);

        return filtered;
    }

    private List<DetectedObject> findDetectedObjects(Mat frame, List<DetectedObject> detectedObjects) {
        List<MatOfPoint> contours;
        Mat hierarchy;
        contours = new ArrayList<MatOfPoint>();
        hierarchy = new Mat();

        Imgproc.findContours(frame, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CV_CONTOURS_MATCH_I1);

        for (MatOfPoint contour : contours){
            if (Imgproc.contourArea(contour) > mMinContourArea) {
                Rect rect = Imgproc.boundingRect(contour);
                float width = (float) Math.abs(rect.br().x - rect.tl().x);
                float height = (float) Math.abs(rect.br().y - rect.tl().y);
                width = width / frame.width();
                height = height / frame.height();

                float x = (float) rect.tl().x / frame.width();
                float y = (float) rect.tl().y / frame.height();

                DetectedObject detectedObject = new DetectedObject(x, y, width, height);
                detectedObjects.add(detectedObject);
            }
        }

        hierarchy.release();


        /* blob detection */
        /*
        Imgproc.blur(frame, frame, new Size(5, 5));
        Mat diff = new Mat();
        Core.absdiff(mPrevMat, frame, diff);
        //Mat foregroundMask = Mat.zeros(diff.rows(), diff.cols(), CvType.CV_8UC1);
        Imgproc.threshold(diff, diff, 20, 255, 0);

        FeatureDetector blobDetector = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);
        if (mParamsFilePath != "")
            blobDetector.read(mParamsFilePath);

        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        blobDetector.detect(diff, keypoints);

        Features2d.drawKeypoints(frame, keypoints, frame);

        int imgWidth = frame.width();
        int imgHeight = frame.height();

        for (KeyPoint kp : keypoints.toArray()) {
            float x = (float)(kp.pt.x / imgWidth);
            float y = (float)(kp.pt.y / imgHeight);
            float width = kp.size / imgWidth;
            float height = kp.size / imgHeight;
            x -= width;
            y -= height;
            DetectedObject detectedObject = new DetectedObject(x, y, width, height);
            detectedObjects.add(detectedObject);
            Log.d("detected objects", x + ":" + y + " " + width);
        }
        */

        return detectedObjects;
    }

    @Override
    public void setDetectorView(DetectorView view) {
        mDetectorView = view;
    }

    @Override
    public void toggleDisplay() {
        mDisplayRaw = !mDisplayRaw;
    }

    private String constructDetectorParams(){
        String paramsString = "%YAML:1.0";
        paramsString += "\nfilterByArea: 1";
        paramsString += "\nfilterByCircularity: 0";
        paramsString += "\nfilterByInertia: 0";
        paramsString += "\nfilterByConvexity: 0";
        paramsString += "\nfilterByColor: 0";
        paramsString += "\nminArea: " + mMinArea;
        paramsString += "\nmaxArea: 2000000";
        paramsString += "\nminThreshold: 20";
        paramsString += "\nmaxThreshold: 225";
        paramsString += "\nthresholdStep: 40";

        return paramsString;
    }

    public CvBlobDetector(){
        super();

        /*
        OpenCv's SimpleBlobDetector requires parameters to be passed via a .yml file,
        thus the code below
         */
        /*
        String paramsString = constructDetectorParams();
        try {
            mParamsFilePath = Utils.writeToFile(mParamsFilename, paramsString);
        }
        catch (Exception e){
            Log.e("Blob error", e.getMessage());
        }
        */
    }
}
