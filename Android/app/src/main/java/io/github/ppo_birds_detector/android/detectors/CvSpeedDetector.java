package io.github.ppo_birds_detector.android.detectors;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;

import io.github.ppo_birds_detector.android.DetectedObject;
import io.github.ppo_birds_detector.android.DetectorView;

public class CvSpeedDetector extends CvBulkDetector {

    public static int BLOCKS_H = 16;
    public static int BLOCKS_V = 12;

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

                    if (gridX > 0 && detectedGrid[gridX - 1][gridY]){
                        int id = idGrid[gridX - 1][gridY];
                        DetectedObject detectedObject = detectedObjects.get(id - 1);
                        detectedObject.width = Math.max(x, detectedObject.width);
                    }else if (gridY > 0 && detectedGrid[gridX][gridY - 1]){
                        int id = idGrid[gridX][gridY - 1];
                        DetectedObject detectedObject = detectedObjects.get(id - 1);
                        detectedObject.height = Math.max(y, detectedObject.height);
                    }
                    else{
                        // new object
                        idCounter++;

                        DetectedObject detectedObject = new DetectedObject(x, y, x + blockSizeX, y + blockSizeY, idCounter);
                        detectedObjects.add(detectedObject);
                    }
                    idGrid[gridX][gridY] = idCounter;
                }
            }
        }

        for (DetectedObject detectedObject : detectedObjects){
            detectedObject.width = (detectedObject.width - detectedObject.x) / width;
            detectedObject.height = (detectedObject.height - detectedObject.y) / height;

            detectedObject.x = detectedObject.x / width;
            detectedObject.y = detectedObject.y / height;
        }

        return detectedObjects;
    }
}
