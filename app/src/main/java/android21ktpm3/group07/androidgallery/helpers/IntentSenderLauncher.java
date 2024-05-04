package android21ktpm3.group07.androidgallery.helpers;

import android.app.Activity;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;

public class IntentSenderLauncher {
    private final ActivityResultLauncher<IntentSenderRequest> launcher;
    private IntentSenderResultCallback callback;

    public IntentSenderLauncher(ActivityResultCaller caller) {
        launcher = caller.registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        callback.onOK();
                    } else {
                        callback.onCanceled();
                    }
                }
        );
    }

    public void launch(IntentSenderRequest request, IntentSenderResultCallback callback) {
        this.callback = callback;
        launcher.launch(request);
    }

    public interface IntentSenderResultCallback {
        void onOK();

        void onCanceled();
    }
}
