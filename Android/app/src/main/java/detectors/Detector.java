package detectors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;

import java.io.ByteArrayOutputStream;
import java.util.List;

import io.github.ppo_birds_detector.android.DetectedObject;
import io.github.ppo_birds_detector.android.DetectorView;
import io.github.ppo_birds_detector.android.MainActivity;

/**
 * Created by Alek on 2015-04-26.
 */
public abstract class Detector implements MainActivity.IDetector {
    protected DetectorView mDetectorView;

    private Camera.Parameters _parameters;
    private Camera.Size _cameraSize;

    public void setCameraParameters(Camera.Parameters parameters){
        _parameters = parameters;
        _cameraSize = _parameters.getPreviewSize();
    }
    protected Bitmap dataToBitmap(byte [] data){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuv = new YuvImage(data, _parameters.getPreviewFormat(), _cameraSize.width, _cameraSize.height, null);

        // bWidth and bHeight define the size of the bitmap you wish the fill with the preview image
        yuv.compressToJpeg(new Rect(0, 0, _cameraSize.width, _cameraSize.height), 50, out);
        byte[] bytes = out.toByteArray();
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
    }
    public void detect(byte[] frame) {
        List<DetectedObject> detectedObjects = processFrame(frame);
        mDetectorView.setDetectedObjects(detectedObjects);
    }

    protected abstract List<DetectedObject> processFrame(byte [] frame);

    @Override
    public void setDetectorView(DetectorView view) {
        mDetectorView = view;
    }

}