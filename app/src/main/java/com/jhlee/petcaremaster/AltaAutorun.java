package com.jhlee.petcaremaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AltaAutorun extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent it = new Intent(context, MainActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(it);
    }
}
