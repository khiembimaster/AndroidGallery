package android21ktpm3.group07.androidgallery.helpers;

import androidx.concurrent.futures.CallbackToFutureAdapter;

import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;

public class ListenableFutureHelper {
    public static <T> ListenableFuture<T> toListenableFuture(Task<T> task) {
        return CallbackToFutureAdapter.getFuture(completer -> task
                .addOnCanceledListener(completer::setCancelled)
                .addOnFailureListener(completer::setException)
                .addOnSuccessListener(completer::set)
        );
    }
}
