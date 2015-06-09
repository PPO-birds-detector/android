package io.github.ppo_birds_detector.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ike on 20/04/15.
 */
public class DetectorView extends View {
    private List<DetectedObject> mDetectedObjects;
    private Paint mPaint;
    private int [] mColors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA};
    private float mCameraRatio;

    public DetectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDetectedObjects = new ArrayList<>();
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4.0f);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cWidth = canvas.getWidth();
        float cHeight = canvas.getHeight();
        float top, left, bottom, right;
        float ratio = cWidth / cHeight;

        float width = cWidth * (mCameraRatio / ratio);
        int offX = (int)((cWidth - width) / 2.0f);

        for (DetectedObject obj : mDetectedObjects) {
            // get color
            int colorId = obj.id % mColors.length;
            mPaint.setColor(mColors[colorId]);

            // draw the bounding rectangle
            left = offX + obj.x * width;
            top = obj.y * cHeight;
            right = offX + (obj.x + obj.width) * width;
            bottom = (obj.y + obj.height) * cHeight;
            canvas.drawRect(left, top, right, bottom, mPaint);

            if (obj.hasSpeed) {
                // draw speed vector
                canvas.drawLine(obj.speedStartX, obj.speedStartY, obj.speedEndX, obj.speedEndY, mPaint);
            }
        }
    }

    public void setDetectedObjects(List<DetectedObject> list) {
        mDetectedObjects = list;
        postInvalidate();
    }

    public void setRatio(float cameraRatio) {
        mCameraRatio = cameraRatio;
    }
}
