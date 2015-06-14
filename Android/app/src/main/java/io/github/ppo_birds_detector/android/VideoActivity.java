package io.github.ppo_birds_detector.android;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.nio.ByteBuffer;

/**
 * Created by ike on 14/06/15.
 */
public class VideoActivity extends Activity {
    private static final int SELECT_VIDEO = 100;

    private TextView mPath;
    private Handler mHandler = new Handler();
    private ImageView mImageView;
    private ExtractMpegFrames mExtractor;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_video);

        mPath = (TextView) findViewById(R.id.url);
        mImageView = (ImageView) findViewById(R.id.imageView);

        mExtractor = new ExtractMpegFrames();

        Intent videoPickerIntent = new Intent(Intent.ACTION_PICK);
        videoPickerIntent.setType("video/*");
        startActivityForResult(videoPickerIntent, SELECT_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SELECT_VIDEO) {
            if (resultCode == RESULT_OK) {
                mPath.setText(intent.getDataString());
                try {
                    Cursor c = getContentResolver().query(intent.getData(), null, null, null, null);
                    c.moveToNext();
                    String path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
                    c.close();
                    Log.d("TAG", path);
                    final Bitmap bmp = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
                    mExtractor.setFrameListener(mHandler, new ExtractMpegFrames.OnFrameListener() {
                        @Override
                        public void onFrame(ByteBuffer bytes) {
                            bmp.copyPixelsFromBuffer(bytes);
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
    protected void onPause() {
        super.onPause();
        mExtractor.onPause();
    }
}
