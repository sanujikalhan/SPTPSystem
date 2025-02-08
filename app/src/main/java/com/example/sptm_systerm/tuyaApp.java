package com.example.sptm_systerm;
import com.thingclips.smart.android.user.api.ILoginCallback;
import com.thingclips.smart.android.user.bean.User;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.sdk.api.INeedLoginListener;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class tuyaApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThingHomeSdk.setDebugMode(true);
        ThingHomeSdk.init(this, "hfeqthchgngnwp8stx8y", "4wh8cn8pu45c5cx9rad8qt8mswj9dmwn");;

        ThingHomeSdk.setOnNeedLoginListener(new INeedLoginListener() {
            @Override
            public void onNeedLogin(Context context) {
                startActivity(new Intent(tuyaApp.this,LoginActivity.class));
            }
        });

    }


    }





