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
    private static final String IS_PROCESSING_KEY = "Is Processing";
    private static final String FIREBASE_USER_KEY = "Firebase User";
    private final MutableLiveData<Boolean> canUpload;
    private final MutableLiveData<FirebaseUser> firebaseUser;
    private final MutableLiveData<Boolean> isProcessing;
    private final MutableLiveData<Boolean> canSignIn;
    private final MutableLiveData<Boolean> canLogOut;


    public UserViewModel(SavedStateHandle savedStateHandle){
        this.savedStateHandle = savedStateHandle;
        canUpload = savedStateHandle.getLiveData(CAN_UPLOAD_KEY);
        isProcessing = savedStateHandle.getLiveData(IS_PROCESSING_KEY);
        firebaseUser = savedStateHandle.getLiveData(FIREBASE_USER_KEY);
        canSignIn = savedStateHandle.getLiveData(CAN_SIGN_IN_KEY);
        canLogOut = savedStateHandle.getLiveData(CAN_LOG_OUT_KEY);
    }

    public MutableLiveData<Boolean> getCanUpload() {
        return canUpload;
    }

    public MutableLiveData<FirebaseUser> getFirebaseUser() {
        return firebaseUser;
    }

    public MutableLiveData<Boolean> getIsProcessing() {
        return isProcessing;
    }

    public MutableLiveData<Boolean> getCanSignIn() {
        return canSignIn;
    }

    public MutableLiveData<Boolean> getCanLogOut() {
        return canLogOut;
    }

    public void setCanUpload(Boolean canUpload){
        this.canUpload.setValue(canUpload);
        savedStateHandle.set(CAN_UPLOAD_KEY, canUpload);
    }
    public void setFirebaseUser(FirebaseUser user){
        this.firebaseUser.setValue(user);
        savedStateHandle.set(FIREBASE_USER_KEY, user);
    }
    public void setIsProcessing(Boolean isProcessing){
        this.isProcessing.setValue(isProcessing);
        savedStateHandle.set(IS_PROCESSING_KEY, isProcessing);
    }
    public void setCanSignIn(Boolean canSignIn){
        this.canSignIn.setValue(canSignIn);
        savedStateHandle.set(CAN_SIGN_IN_KEY, canSignIn);
    }
    public void setCanLogOut(Boolean canLogOut){
        this.canLogOut.setValue(canLogOut);
        savedStateHandle.set(CAN_LOG_OUT_KEY, canLogOut);
    }



}
