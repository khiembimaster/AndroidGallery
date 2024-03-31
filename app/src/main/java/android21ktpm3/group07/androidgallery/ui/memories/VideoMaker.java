package android21ktpm3.group07.androidgallery.ui.memories;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class VideoMaker {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public VideoMaker(Context context) {
        VideoMaker.context = context;
    }

    public void createVideoFromBitmaps(Bitmap[] bitmaps, String outputVideoPath) {
        try {
            // Prepare the output directory
            File outputDir = new File(Environment.getExternalStorageDirectory(), "output_videos");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // Ensure ffmpeg has executable permissions
            String ffmpegPath = getFFmpegPath();
            Process process = Runtime.getRuntime().exec("chmod 777 " + ffmpegPath);
            process.waitFor();

            // Prepare the command to execute ffmpeg
            String[] command = new String[]{ffmpegPath, "-y", "-f", "image2pipe", "-vcodec", "mjpeg", "-r", "30", "-i", "-", "-c:v", "libx264", "-pix_fmt", "yuv420p", outputVideoPath};

            // Start ffmpeg process
// Ensure ffmpeg has executable permissions
            Process ffmpegProcess = Runtime.getRuntime().exec("chmod -R 777 " + Arrays.toString(command));
            ffmpegProcess.waitFor();



// Start ffmpeg process
         //   Process ffmpegProcess = Runtime.getRuntime().exec(command);

            // Write bitmap data to stdin of ffmpeg
            try (BufferedOutputStream ffmpegInput = new BufferedOutputStream(ffmpegProcess.getOutputStream())) {
                for (Bitmap bitmap : bitmaps) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ffmpegInput);
                    ffmpegInput.flush();
                }
            }

            // Wait for the ffmpeg process to finish
            ffmpegProcess.waitFor();

            // Get the exit code of the process
            int exitCode = ffmpegProcess.exitValue();

            // Check if ffmpeg command was successful
            if (exitCode != 0) {
                // Handle error
                System.err.println("ffmpeg command failed with exit code: " + exitCode);
            } else {
                System.out.println("Video creation successful");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String getFFmpegPath() {
        // Assuming ffmpeg is located in the app's assets folder
        try {
            InputStream inputStream;
            // Open the ffmpeg file as an input stream
            try {
                inputStream = context.getAssets().open("ffmpeg");
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }


            // Determine the path where the ffmpeg executable will be extracted
            File cacheDir = context.getCacheDir();
            File ffmpegFile = new File(cacheDir, "ffmpeg");

            // Copy the ffmpeg file from assets to cache directory
            FileOutputStream outputStream = new FileOutputStream(ffmpegFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            // Close streams
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            // Set executable permissions for the ffmpeg file
            ffmpegFile.setExecutable(true);

            // Return the absolute path of the ffmpeg file
            return ffmpegFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
