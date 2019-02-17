package com.jby.stocktake.shareObject;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class CustomToast {
    public CustomToast(final Context context, final String message){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
