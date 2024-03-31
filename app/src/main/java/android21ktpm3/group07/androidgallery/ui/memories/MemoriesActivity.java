package android21ktpm3.group07.androidgallery.ui.memories;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android21ktpm3.group07.androidgallery.MainActivity;
import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.models.Photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

public class MemoriesActivity extends AppCompatActivity implements BitmapToVideoEncoder.IBitmapToVideoEncoderCallback {

    private Context context;

    private List<Photo> memoriesPhoto;

    public MemoriesActivity() {

    }

    public MemoriesActivity(Context context, List<Photo> memoriesPhoto) {
        this.context = context;
        this.memoriesPhoto = memoriesPhoto;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.memories_view);
        context = this;


        memoriesPhoto = getIntent().getParcelableArrayListExtra("memoriesPhotos");

        LinearLayout containerLayout = findViewById(R.id.container_layout);


        // Generate frames and create a video in memory
        Bitmap[] frames = generateFrames(memoriesPhoto);

//        // Create VideoMaker object and generate video
//   //  VideoMaker videoMaker = new VideoMaker(context);
  String outputVideoPath = getOutputVideoPath();
//      //  videoMaker.createVideoFromBitmaps(frames, outputVideoPath);
//
//        // Play the video using VideoView
//        VideoView videoView = new VideoView(this);
//        containerLayout.addView(videoView);
//
//        // Set the video path to the output video file
//        String videoPath = "file://" + outputVideoPath;
//        videoView.setVideoURI(Uri.parse(videoPath));
//        videoView.start();

        BitmapToVideoEncoder encoder = new BitmapToVideoEncoder(this);
        encoder.startEncoding(frames[0].getWidth(), frames[0].getHeight(), new File(outputVideoPath));

        // Queue frames for encoding
        for (Bitmap frame : frames) {
            encoder.queueFrame(frame);
        }

        // Stop encoding
        encoder.stopEncoding();

        // Play the video using VideoView
        VideoView videoView = new VideoView(this);
        containerLayout.addView(videoView);

        // Set the video path to the output video file
        String videoPath = "file://" + outputVideoPath;
        videoView.setVideoPath(videoPath);
        videoView.start();

    }

    @Override
    public void onEncodingComplete(File outputFile) {
        Toast.makeText(this, "Encoding complete!", Toast.LENGTH_LONG).show();
    }


    private Bitmap[] generateFrames(List<Photo> photos) {
        Bitmap[] frames = new Bitmap[photos.size()];
        for (int i = 0; i < photos.size(); i++) {
            String imagePath = photos.get(i).getPath();
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            frames[i] = bitmap;
        }

        return frames;
    }
    private String getOutputVideoPath() {

        String destination = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        File directory = new File(destination);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return destination + File.separator + "output_video.mp4";
    }
}



