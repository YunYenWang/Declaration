package org.idea.declaration;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainBroadcastReceiver extends BroadcastReceiver {
    static final Logger LOG = LoggerFactory.getLogger(MainBroadcastReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("stop".equals(intent.getAction())) {
            context.stopService(new Intent(context, BackgroundMusicService.class));
        }
    }
}
