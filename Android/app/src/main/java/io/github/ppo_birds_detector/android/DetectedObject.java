package io.github.ppo_birds_detector.android;

/**
 * Created by ike on 20/04/15.
 */
public class DetectedObject {
    // Center of the detected object with respect to the width of the original image 0.0 .. 1.0
    public float x;
    public float y;
    public float width;
    public float height;

    public boolean hasSpeed = false;
    public float speedStartX;
    public float speedEndX;
    public float speedStartY;
    public float speedEndY;

    public int id;

    public DetectedObject(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public DetectedObject(float x, float y, float width, float height, int id) {
        this(x, y, width, height);

        this.id = id;
    }
}
