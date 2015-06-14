package io.github.ppo_birds_detector.android;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;

import io.github.ppo_birds_detector.android.detectors.CvBlockDetector;
import io.github.ppo_birds_detector.android.detectors.CvBulkDetector;
import io.github.ppo_birds_detector.android.detectors.CvDetector;
import io.github.ppo_birds_detector.android.detectors.CvEdgeDetector;

/**
 * Created by ike on 14/06/15.
 */
public class CvVideoActivity extends Activity {
    private static final String TAG = "CvVideoActivity";
    private static final int SELECT_VIDEO = 100;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    Intent videoPickerIntent = new Intent(Intent.ACTION_PICK);
                    videoPickerIntent.setType("video/*");
                    startActivityForResult(videoPickerIntent, SELECT_VIDEO);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    private TextView mPath;
    private Handler mHandler = new Handler();
    private ImageView mImageView;
    private ExtractMpegFrames mExtractor;
    private CvDetector mDetector;
    private DetectorView mDetectorView;
    private FrameLayout mLayout;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_video);

        mPath = (TextView) findViewById(R.id.url);
        mImageView = (ImageView) findViewById(R.id.imageView);

        mExtractor = new ExtractMpegFrames();

        mDetectorView = (DetectorView) findViewById(R.id.detector);

        mDetector = new CvBlockDetector();
        mDetector.setDetectorView(mDetectorView);

        mLayout = (FrameLayout) findViewById(R.id.layout);
        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDetector.toggleDisplay();
            }
        });

        Log.d("TAG", "-------------- AFTER CREATE");

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

            Log.d("TAG", "-------------- AFTER LOADED");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SELECT_VIDEO) {
            if (resultCode == RESULT_OK) {
                mPath.setText(intent.getDataString());
                try {
                    Log.d("TAG", "-------------- AFTER RESULT");
                    Cursor c = getContentResolver().query(intent.getData(), null, null, null, null);
                    c.moveToNext();
                    String path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
                    c.close();
                    Log.d("TAG", path);
                    final Bitmap bmp = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
                    mDetector.onStart();
                    mExtractor.setFrameListener(mHandler, new ExtractMpegFrames.OnFrameListener() {
                        @Override
                        public void onFrame(ByteBuffer bytes) {
//                            bmp.copyPixelsFromBuffer(bytes);
                            Log.d("TAG", "-------------- AFTER FRAME");
                            Mat rgba = new Mat(480, 640, CvType.CV_8UC4);
                            rgba.put(0, 0, bytes.array());
                            Mat gray = new Mat();
                            Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_BGRA2GRAY);
                            Mat frame = mDetector.onFrame(rgba, gray);
                            Log.d(TAG, "w " + rgba.width() + " h" + rgba.height());
                            org.opencv.android.Utils.matToBitmap(frame, bmp);
                            mImageView.setImageBitmap(bmp);
                            mImageView.invalidate();
                        }
                    });
                    mExtractor.testExtractMpegFrames(path);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mExtractor.onPause();
        mDetector.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detectors_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.select_bulk_detector:
                mDetector.onStop();
                mDetector = new CvBulkDetector();
                initDetector();
                return true;
            case R.id.select_block_detector:
                mDetector.onStop();
                mDetector = new CvBlockDetector();
                initDetector();
                return true;
            case R.id.select_edge_detector:
                mDetector.onStop();
                mDetector = new CvEdgeDetector();
                initDetector();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initDetector(){
        if (mDetector != null) {
            if (mDetectorView == null) {
                mDetectorView = (DetectorView) findViewById(R.id.detector);
            }
            mDetector.onStart();
            mDetector.setDetectorView(mDetectorView);
        }
    }
}
