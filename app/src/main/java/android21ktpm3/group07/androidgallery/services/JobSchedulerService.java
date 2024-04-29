package android21ktpm3.group07.androidgallery.services;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

/**
 * This class is used to schedule a job that listens for changes in the MediaStore.
 *
 * @see <a href="https://stackoverflow.com/a/46375945">Code reference</a>
 */
@SuppressLint("SpecifyJobSchedulerIdRange")
public class JobSchedulerService extends JobService {
    public static final int JOB_ID = 0;
    public static final String ACTION_MEDIA_STORE_CHANGED = "MediaStore-changed";

    // A pre-built JobInfo we use for scheduling our job.
    private static JobInfo JOB_INFO = null;

    private Context context;


    // Schedule this job, replace any existing one.
    public static void scheduleJob(Context context) {
        if (JOB_INFO != null) {
            ((JobScheduler) context.getSystemService(JobScheduler.class)).schedule(JOB_INFO);
        } else {
            JobScheduler js = context.getSystemService(JobScheduler.class);
            JobInfo.Builder builder = new JobInfo.Builder(
                    JOB_ID,
                    new ComponentName(context, JobSchedulerService.class)
            );

            builder.addTriggerContentUri(new JobInfo.TriggerContentUri(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS));
            builder.setTriggerContentMaxDelay(500);

            JOB_INFO = builder.build();
            js.schedule(JOB_INFO);
        }
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        context = this;
        // Did we trigger due to a content change?
        if (params.getTriggeredContentAuthorities() != null) {
            if (params.getTriggeredContentUris() != null) {
                // If we have details about which URIs changed, then iterate through them
                // and collect either the ids that were impacted or note that a generic
                // change has happened.
                ArrayList<String> ids = new ArrayList<>();
                for (Uri uri : params.getTriggeredContentUris()) {
                    Log.d("JobSchedulerService", "Uri: " + uri.toString());

                    if (uri != null) {
                        Intent intent = new Intent(ACTION_MEDIA_STORE_CHANGED);
                        intent.putExtra("uri", uri.toString());
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                }
                jobFinished(params, true); // see this, we are saying we just finished the job
                // We will emulate taking some time to do this work, so we can see batching happen.
                scheduleJob(this);
            }
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
