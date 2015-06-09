package io.github.ppo_birds_detector.android.detectors;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;
import io.github.ppo_birds_detector.android.DetectedObject;

/**
 * This is an extension of the BulkDetector
 * it links detected motion blocks into whole objects.
 * This should allow for future middle mass and speed
 * computations.
 */
public class CvBlockDetector extends CvBulkDetector {

    public static int BLOCKS_H = 16;
    public static int BLOCKS_V = 12;

    private List<DetectedObject> mPrevDetectedObjects = null;

    @Override
    protected List<DetectedObject> findDetectedObjects(Mat frame, List<DetectedObject> detectedObjects) {
        int width = frame.width();
        int height = frame.height();

        int blockSizeX = (int) (width / (float) BLOCKS_H);
        int blockSizeY = (int) (height / (float) BLOCKS_V);

        // each cell holds the id of the object detected in it
        int [][] idGrid = new int[BLOCKS_H][BLOCKS_H];

        // indicates if an object was detected in a cell
        boolean[][] detectedGrid = new boolean[BLOCKS_H][BLOCKS_V];

        int idCounter = 0;

        for (int y = 0; y <= height - blockSizeY; y += blockSizeY) {
            for (int x = 0; x <= width - blockSizeX; x += blockSizeX) {
                if (detectInBlock(frame, x, y, blockSizeX, blockSizeY)) {

                    int gridX = x / blockSizeX;
                    int gridY = y / blockSizeY;
                    detectedGrid[gridX][gridY] = true;

                    if (gridY > 0 && detectedGrid[gridX][gridY - 1]) {
                        int id = idGrid[gridX][gridY - 1];
                        DetectedObject detectedObject = detectedObjects.get(id - 1);
                        detectedObject.height = Math.max(y + blockSizeY, detectedObject.height);

                        if (gridX > 0 && detectedGrid[gridX - 1][gridY]){
                            int prevId = idGrid[gridX][gridY - 1];

                            if (prevId != id) {
                                // link the object from the previous cell to this object
                                detectedObject.x = Math.min(detectedObject.x, x);
                                detectedObjects.set(prevId - 1, detectedObject);
                            }
                        }
                    }
                    else if (gridX > 0 && detectedGrid[gridX - 1][gridY]){
                        int id = idGrid[gridX - 1][gridY];
                        DetectedObject detectedObject = detectedObjects.get(id - 1);
                        detectedObject.width = Math.max(x + blockSizeX, detectedObject.width);
                    }
                    else{
                        // new object
                        idCounter++;

                        /* TODO: the information about the current end position (endX and endY) is currently stored
                         in the width and height fields */
                        DetectedObject detectedObject = new DetectedObject(x, y, x + blockSizeX, y + blockSizeY, idCounter);
                        detectedObjects.add(detectedObject);
                    }
                    idGrid[gridX][gridY] = idCounter;
                }
            }
        }

        // remove duplicates
        boolean [] ids = new boolean [idCounter + 1];
        List<DetectedObject> nonDuplicateObjects = new ArrayList<DetectedObject>();
        for (DetectedObject detectedObject : detectedObjects){
            if (!ids[detectedObject.id]){
                ids[detectedObject.id] = true;
                nonDuplicateObjects.add(detectedObject);
            }
        }
        detectedObjects = nonDuplicateObjects;

        // compute the object's width and height
        for (DetectedObject detectedObject : detectedObjects){
            detectedObject.width = (detectedObject.width - detectedObject.x) / width;
            detectedObject.height = (detectedObject.height - detectedObject.y) / height;

            detectedObject.x = detectedObject.x / width;
            detectedObject.y = detectedObject.y / height;
        }

        calculateSpeed(detectedObjects);

        mPrevDetectedObjects = detectedObjects;

        return detectedObjects;
    }

    private static float POS_THRESHOLD = 0.2f;
    private static float SIZE_THRESHOLD = 0.15f;

    private void calculateSpeed(List<DetectedObject> detectedObjects){
        if (mPrevDetectedObjects == null){
            return;
        }

        for (DetectedObject curr : detectedObjects){
            DetectedObject prev = null;
            for (DetectedObject prevObj : mPrevDetectedObjects){
                float xDiff = curr.x - prevObj.x;
                float yDiff = curr.y - prevObj.y;
                float wDiff = curr.width - prevObj.width;
                float hDiff = curr.height - prevObj.height;

                if (Math.abs(xDiff) < POS_THRESHOLD && Math.abs(yDiff) < POS_THRESHOLD
                        && Math.abs(wDiff) < SIZE_THRESHOLD && Math.abs(hDiff) < SIZE_THRESHOLD){
                    // matched pair
                    prev = prevObj;
                    break;
                }
            }
            if (prev != null) {
                curr.hasSpeed = true;

                curr.speedStartX = Math.max(curr.x, prev.x);
                curr.speedEndX = Math.min(curr.x, prev.x);
                curr.speedStartY = Math.max(curr.y, prev.y);
                curr.speedEndY = Math.min(curr.y, prev.y);

                mPrevDetectedObjects.remove(prev);
            }
        }
    }
}
