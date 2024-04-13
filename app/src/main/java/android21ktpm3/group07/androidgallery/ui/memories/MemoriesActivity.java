package android21ktpm3.group07.androidgallery.ui.memories;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;




import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.FileDataSource;
import androidx.media3.exoplayer.ExoPlaybackException;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.ui.PlayerView;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.models.Photo;

public class MemoriesActivity extends AppCompatActivity implements Player.Listener {


    private List<Photo> memoriesPhoto;
    private List<Photo> alo;

    private Uri videoLocation;
    private VideoView videoView;

    private boolean isVideoPlaying = false;

    private PlayerView playerView;
    private ExoPlayer player;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memories_view);

        Button playButton = findViewById(R.id.bt_play);

        memoriesPhoto = getIntent().getParcelableArrayListExtra("memoriesPhotos");
        playerView = findViewById(R.id.playerView);
//videoView = findViewById(R.id.videoView);


        Bitmap[] frames;
        player = new ExoPlayer.Builder(this).build();
        //player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
        createVideoFromImages();
        playerView.setPlayer(player);
        playButton = findViewById(R.id.bt_play);
        prepareVideo();

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Xử lý sự kiện nhấn nút play ở đây
                // Ví dụ: play hoặc pause video
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.play();
                }
            }
        });
    }

    //   playerView.setPlayer(player);

//


    private void createVideoFromImages() {

        String strCommand = "ffmpeg -loop 1 -t 3 -i " + getDrawableImagePath(R.drawable.avatar01) +
                " -loop 1 -t 3 -i " + getDrawableImagePath(R.drawable.avatar01) +
                " -loop 1 -t 3 -i " + getDrawableImagePath(R.drawable.avatar01) +
                " -loop 1 -t 3 -i " + getDrawableImagePath(R.drawable.avatar01) +

                " -filter_complex [0:v]trim=duration=3,fade=t=out:st=2.5:d=0.5[v0];" +
                "[1:v]trim=duration=3,fade=t=in:st=0:d=0.5,fade=t=out:st=2.5:d=0.5[v1];" +
                "[2:v]trim=duration=3,fade=t=in:st=0:d=0.5,fade=t=out:st=2.5:d=0.5[v2];" +
                "[3:v]trim=duration=3,fade=t=in:st=0:d=0.5,fade=t=out:st=2.5:d=0.5[v3];" +
                "[v0][v1][v2][v3]concat=n=4:v=1:a=0,format=yuv420p[v] -map [v] -preset ultrafast " +
                getOutputVideoPath();
        String ffmpegCommand = "ffmpeg -framerate 1/3 -i " + getDrawableImagePath(R.drawable.avatar01) + " -i " + getDrawableImagePath(R.drawable.avatar01) + " -i " + getDrawableImagePath(R.drawable.avatar01) + " -i " + getDrawableImagePath(R.drawable.avatar01) + " -filter_complex " +
                "\"[0:v][1:v][2:v][3:v]concat=n=4:v=1[outv]\" -map \"[outv]\" -c:v libx264 -r 30 -pix_fmt yuv420p output_video.mp4";


        executeFFmpegCommand(ffmpegCommand);
        System.out.println("alo");

        // displayVideo(getOutputVideoPath());
        // executeFFmpegCommand(strCommand);
    }

    private void displayVideo(String videoPath) {
        // Set the video source
        videoView.setVideoURI(Uri.parse("file://" + videoPath));

        // Start playing the video
        videoView.start();
    }

    @SuppressLint("RestrictedApi")
    @OptIn(markerClass = UnstableApi.class)
    private void prepareVideo() {
        playerView.setPlayer(player);
        DataSource.Factory dataSourceFactory = new FileDataSource.Factory();

        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.fromFile(new File(getOutputVideoPath()))));

        player.addMediaSource(videoSource);
        player.prepare();
        player.setPlayWhenReady(true);
        // player.play();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onPlayerError(PlaybackException error) {
        Player.Listener.super.onPlayerError(error);
        Log.e(TAG, "TYPE_SOURCE: " + error.getMessage());
        //Restart the playback
        player.stop(); // Stop the current playback
        player.release(); // Reset the player to its initial state
        player.prepare(); // Prepare the player for playback
        player.play(); // Start the playback again


        // You may also choose to retry playback or display an error message to the user
    }


    @Override
    protected void onStart() {
        super.onStart();
//        playerView.setPlayer(player);
        prepareVideo(); // Call prepareVideo method here
    }


    private void executeFFmpegCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec("chmod +x " + command);

            process.waitFor();
            System.out.println(process.getOutputStream());
            // prepareVideo();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


    }

    private String getDrawableImagePath(int drawableId) {
        return "android.resource://" + getPackageName() + "/" + drawableId;
    }

    private String convertDrawableToImage(int drawableId) {

        BitmapDrawable drawable = (BitmapDrawable) ContextCompat.getDrawable(this, drawableId);
        Bitmap bitmap = drawable.getBitmap();

        // Save the bitmap to a temporary file
        File tempDir = getApplicationContext().getCacheDir();
        File tempFile = new File(tempDir, "temp_image.png");
        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tempFile.getAbsolutePath();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.pause();
    }


    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    private void releasePlayer() {
        if (player != null) {
            player.stop();
            player.release();
        }
    }

    private void prepareAndPlay() {
        if (player != null && !player.isPlaying()) {
            player.prepare();
            player.play();
        }
    }


    private Bitmap[] generateFrames(List<Photo> photos) {
        Bitmap[] frames = new Bitmap[photos.size()];
        for (int i = 0; i < photos.size(); i++) {
            String imagePath = photos.get(i).getPath();
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                frames[i] = bitmap;
            } else {
                // Handle case when bitmap cannot be loaded
            }
        }
        return frames;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
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

