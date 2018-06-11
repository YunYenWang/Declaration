package org.idea.declaration;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LightLanguageActivity extends AppCompatActivity {
    static final Logger LOG = LoggerFactory.getLogger(LightLanguageActivity.class);

    MediaPlayer player;

    Map<ToggleButton, File> files = new HashMap<>();
    ToggleButton playing;

    ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_language);

        ActionBar bar = getSupportActionBar();
        bar.setTitle(R.string.action_light_language);
        bar.setDisplayHomeAsUpEnabled(true);

        build(R.id.entityButton, R.id.entityStatus, R.string.ll_entity_file, R.string.ll_entity_file_url, R.string.ll_entity_file_md5_url);
        build(R.id.cancerButton, R.id.cancerStatus, R.string.ll_cancer_file, R.string.ll_cancer_file_url, R.string.ll_cancer_file_md5_url);
        build(R.id.depressionButton, R.id.depressionStatus, R.string.ll_depression_file, R.string.ll_depression_file_url, R.string.ll_depression_file_md5_url);
        build(R.id.sleepButton, R.id.sleepStatus, R.string.ll_sleep_file, R.string.ll_sleep_file_url, R.string.ll_sleep_file_md5_url);
    }

    void preparePlayer() {
        try {
            stopPlayer();

            player = new MediaPlayer();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (playing == null) {
                        return;
                    }

                    if (playing.isChecked()) {
                        LOG.info("Completed");

                        preparePlayer();

                        player.start();
                    }
                }
            });

            File file = files.get(playing);
            player.setDataSource(file.getAbsolutePath());
            player.prepare();

        } catch (Exception e) {
            LOG.error("Failed to replay this music", e);
        }
    }

    void stopPlayer() {
        if (player != null) {
            player.stop();
            player.release();
        }
    }

    void build(@IdRes int buttonId, @IdRes int statusId, @StringRes int fileId, @StringRes int urlId, @StringRes int md5Id) {
        final ToggleButton b = findViewById(buttonId);
        b.setVisibility(View.INVISIBLE);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playing != null) {
                    player.pause();

                    if (playing != b) {
                        playing.setChecked(false);
                    }
                }

                if (b.isChecked()) {
                    if (playing != b) {
                        playing = b;

                        preparePlayer();
                    }

                    player.start();
                }
            }
        });

        final File file = new File(getFilesDir(), getString(fileId));

        try {
            DownloadHelper downloader = new DownloadHelper(this, executor,
                    new URL(getString(urlId)),
                    new URL(getString(md5Id)),
                    file);

            final TextView status = findViewById(statusId);

            downloader.setListener(new DownloadHelper.Listener() {
                @Override
                public void onStatus(final String text) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            status.setText(text);
                        }
                    });
                }

                @Override
                public void onReady() {
                    files.put(b, file);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            b.setVisibility(View.VISIBLE);
                        }
                    });
                }
            });

            downloader.run();

        } catch (Exception e) {
            LOG.error("Failed to check and download the file", e);
        }
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();

        stopPlayer();

        super.onDestroy();
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

    public void onOpenLink(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://ecbc.com.tw/2018/05/10/extra_resources1/"));
        startActivity(intent);
    }
}
