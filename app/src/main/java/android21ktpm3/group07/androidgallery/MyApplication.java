package android21ktpm3.group07.androidgallery;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;

import javax.inject.Inject;

import android21ktpm3.group07.androidgallery.services.JobSchedulerService;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class MyApplication extends Application implements Configuration.Provider {
    @Inject
    HiltWorkerFactory workerFactory;

    @Override
    public void onCreate() {
        super.onCreate();

        JobSchedulerService.scheduleJob(getApplicationContext());
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {

        return new Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .setJobSchedulerJobIdRange(1, Integer.MAX_VALUE)
                .build();
    }
}
