package com.example.sptm_systerm;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.sdk.api.IResultCallback;

public class deleteAccount {
    private Context context;

    public deleteAccount(Context context) {
        this.context = context;
    }
    public void cancel(){
        String email =ThingHomeSdk.getUserInstance().getUser().getEmail();
        ThingHomeSdk.getUserInstance().cancelAccount(new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                Log.e("AccountDeletion", "Error deleting account: " + error);
            }

            @Override
            public void onSuccess() {
                Log.d("AccountDeletion", "Account deleted successfully for user ID: " );
                Toast.makeText(context, "Account deleted successfully"  + email,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
