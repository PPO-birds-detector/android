package io.github.ppo_birds_detector.android;

import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by Alek on 2015-05-30.
 */
public class Utils {
    public static String writeToFile(String filename, String data) throws IOException{
        File root = new File(Environment.getExternalStorageDirectory(), "");
        if (!root.exists()) {
            root.mkdirs();
        }
        File gpxfile = new File(root, filename);
        FileWriter writer = new FileWriter(gpxfile);
        writer.write("");
        writer.write(data);
        writer.flush();
        writer.close();

        return gpxfile.getAbsolutePath();
    }
}
