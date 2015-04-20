package io.github.ppo_birds_detector.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ike on 20/04/15.
 */
public class DetectorView extends View {
    private List<DetectedObject> mDetectedObjects;
    private Paint mPaint;

    public DetectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDetectedObjects = new ArrayList<>();
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4.0f);
    }

    public void setDetectedObjects(List<DetectedObject> detectedObjects) {
//        Log.d("TAG", "setDetectedObj");
        mDetectedObjects = detectedObjects;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Log.d("TAG", "draw");
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        for (DetectedObject obj : mDetectedObjects) {
            canvas.drawRect(obj.x * width, obj.y * height, (obj.x + obj.width) * width, (obj.y + obj.height) * height, mPaint);
        }
    }
}
