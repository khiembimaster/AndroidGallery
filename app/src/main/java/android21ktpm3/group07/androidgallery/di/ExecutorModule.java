package android21ktpm3.group07.androidgallery.di;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@InstallIn(SingletonComponent.class)
@Module
public class ExecutorModule {
    @Singleton
    @Provides
    public static ExecutorService provideExecutorService() {
        Log.d("ExecutorModule", "Executor service injected");
        // TODO: check if this is the best executor service for the app
        return Executors.newCachedThreadPool();
    }
}
