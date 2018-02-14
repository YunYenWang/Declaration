package org.idea.declaration;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackgroundMusicService extends Service {
    static final Logger LOG = LoggerFactory.getLogger(BackgroundMusicService.class);

    String[] music = new String[] { "01.mp3", "02.mp3", "03.mp3", "04.mp3"};
    int index = -1;

    MyBinder binder = new MyBinder();

    MediaPlayer player;
    boolean playing = false;

    public BackgroundMusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        player = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public boolean isPlaying() {
        return playing;
    }

    public void play() {
        if (playing) {
            return;
        }

        doPlay();
    }

    void doPlay() {
        try {
            index = (index + 1) % music.length;

            AssetFileDescriptor afd = getAssets().openFd(music[index]);

            player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    LOG.info("Completed");

                    if (playing) {
                        doPlay();
                    }
                }
            });
            player.start();

            playing = true;

        } catch (Exception e) {
            LOG.error("Failed to play music", e);
        }
    }

    public void pause() {
        if (playing) {
            player.stop();

            playing = false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        pause();

        super.onDestroy();
    }

    public class MyBinder extends Binder {

        public BackgroundMusicService getService() {
            return BackgroundMusicService.this;
        }
    }
}
