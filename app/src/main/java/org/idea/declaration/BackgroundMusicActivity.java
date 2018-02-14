package org.idea.declaration;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackgroundMusicActivity extends AppCompatActivity {
    static final Logger LOG = LoggerFactory.getLogger(BackgroundMusicActivity.class);

    LocalServiceConnection connection = new LocalServiceConnection();
    BackgroundMusicService service;

    ToggleButton playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_music);

        ActionBar bar = getSupportActionBar();
        bar.setTitle(R.string.action_bgm);
        bar.setDisplayHomeAsUpEnabled(true);

        TextView text = findViewById(R.id.text);
        text.setText(R.string.bgm_text);
        text.setTextSize(Globals.textSize);

        playButton = findViewById(R.id.play);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playButton.isChecked()) {
                    service.play();

                } else {
                    service.pause();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, BackgroundMusicService.class);

        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        unbindService(connection);

        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void onConnected(BackgroundMusicService service) {
        this.service = service;

        playButton.setChecked(service.isPlaying());
    }

    class LocalServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            onConnected(((BackgroundMusicService.MyBinder) iBinder).getService());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LOG.info("Un-bind service");
        }
    }
}
