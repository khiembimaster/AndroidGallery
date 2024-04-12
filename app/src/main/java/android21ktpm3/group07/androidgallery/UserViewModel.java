package android21ktpm3.group07.androidgallery;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;


public class UserViewModel extends ViewModel {
    private SavedStateHandle savedStateHandle;
    private static final String CAN_UPLOAD_KEY = "Can Upload";
    private static final String CAN_SIGN_IN_KEY = "Can Sign In";
    private static final String CAN_LOG_OUT_KEY = "Can Log Out";
    private static final String IS_SIGN_IN_PROCESSING_KEY = "Is Sign In Processing";
    private static final String IS_BACKUP_PROCESSING_KEY = "Is Backup Processing";
    private static final String FIREBASE_USER_KEY = "Firebase User";
    private static final String TOTAL_IMAGES_LEFT_KEY = "Total Images Left";
    private final MutableLiveData<Boolean> canUpload;
    private final MutableLiveData<FirebaseUser> firebaseUser;
    private final MutableLiveData<Boolean> isSignInProcessing;
    private final MutableLiveData<Boolean> isBackupProcessing;
    private final MutableLiveData<Boolean> canSignIn;
    private final MutableLiveData<Boolean> canLogOut;
    private final MutableLiveData<Long> totalImagesLeft;

    public UserViewModel(SavedStateHandle savedStateHandle){
        this.savedStateHandle = savedStateHandle;
        canUpload = savedStateHandle.getLiveData(CAN_UPLOAD_KEY);
        isSignInProcessing = savedStateHandle.getLiveData(IS_SIGN_IN_PROCESSING_KEY);
        firebaseUser = savedStateHandle.getLiveData(FIREBASE_USER_KEY);
        canSignIn = savedStateHandle.getLiveData(CAN_SIGN_IN_KEY);
        canLogOut = savedStateHandle.getLiveData(CAN_LOG_OUT_KEY);
        totalImagesLeft = savedStateHandle.getLiveData(TOTAL_IMAGES_LEFT_KEY);
        isBackupProcessing = savedStateHandle.getLiveData(IS_BACKUP_PROCESSING_KEY, false);
    }

    public MutableLiveData<Boolean> getCanUpload() {
        return canUpload;
    }

    public MutableLiveData<FirebaseUser> getFirebaseUser() {
        return firebaseUser;
    }

    public MutableLiveData<Boolean> getIsSignInProcessing() {
        return isSignInProcessing;
    }

    public MutableLiveData<Boolean> getCanSignIn() {
        return canSignIn;
    }

    public MutableLiveData<Boolean> getCanLogOut() {
        return canLogOut;
    }
    public MutableLiveData<Long> getTotalImagesLeft() {
        return totalImagesLeft;
    }

    public void setCanUpload(Boolean canUpload){
        this.canUpload.setValue(canUpload);
        savedStateHandle.set(CAN_UPLOAD_KEY, canUpload);
    }
    public void setFirebaseUser(FirebaseUser user){
        this.firebaseUser.setValue(user);
        savedStateHandle.set(FIREBASE_USER_KEY, user);
    }
    public void setIsSignInProcessing(Boolean isSignInProcessing){
        this.isSignInProcessing.setValue(isSignInProcessing);
        savedStateHandle.set(IS_SIGN_IN_PROCESSING_KEY, isSignInProcessing);
    }
    public void setCanSignIn(Boolean canSignIn){
        this.canSignIn.setValue(canSignIn);
        savedStateHandle.set(CAN_SIGN_IN_KEY, canSignIn);
    }
    public void setCanLogOut(Boolean canLogOut){
        this.canLogOut.setValue(canLogOut);
        savedStateHandle.set(CAN_LOG_OUT_KEY, canLogOut);
    }
    public void setTotalImagesLeft(Long totalImagesLeft){
        this.totalImagesLeft.setValue(totalImagesLeft);
        savedStateHandle.set(TOTAL_IMAGES_LEFT_KEY, totalImagesLeft);
    }
    public MutableLiveData<Boolean> getIsBackupProcessing() {
        return isBackupProcessing;
    }
    public void setIsBackupProcessing(Boolean isBackupProcessing){
        this.isBackupProcessing.setValue(isBackupProcessing);
        savedStateHandle.set(IS_BACKUP_PROCESSING_KEY, isBackupProcessing);
    }
}
