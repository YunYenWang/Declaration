package org.idea.declaration;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MeditationActivity extends AppCompatActivity {
    static final Logger LOG = LoggerFactory.getLogger(MeditationActivity.class);

    TextView status;
    ToggleButton playButton;

    MediaPlayer player;
    File file;

    ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meditation);

        ActionBar bar = getSupportActionBar();
        bar.setTitle(R.string.action_meditation);
        bar.setDisplayHomeAsUpEnabled(true);

        status = findViewById(R.id.status);

        TextView text = findViewById(R.id.text);
        try {
            text.setText(IOUtils.toString(getAssets().open("meditation.txt"), "UTF-8"));
            text.setTextSize(Globals.textSize);

        } catch (Exception e) {
            LOG.error("Failed to open meditation.txt", e);
        }

        playButton = findViewById(R.id.play);
        playButton.setVisibility(View.INVISIBLE);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playButton.isChecked()) {
                    player.start();

                } else {
                    player.pause();
                }
            }
        });

        player = new MediaPlayer();

        file = new File(getFilesDir(), "mediation.mp3");

        try {
            DownloadHelper downloader = new DownloadHelper(this, executor,
                    new URL(getString(R.string.meditation_audio_url)),
                    new URL(getString(R.string.meditation_audio_md5_url)),
                    file);

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
                    prepare();
                }
            });

            downloader.run();

        } catch (Exception e) {
            LOG.error("Failed to check and download the file", e);
        }

//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    if (file.canRead() && Utils.checksum(file, new URL(getString(R.string.meditation_audio_md5_url)))) {
//                        prepare();
//
//                    } else {
//                        setStatus(R.string.downloading);
//
//                        executor.execute(new Runnable() {
//                            @Override
//                            public void run() {
//                                doDownload();
//                            }
//                        });
//                    }
//
//                } catch (Exception e) {
//                    LOG.error("Failed to download the mediation file", e);
//
//                    prepare();
//                }
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();

        player.stop();

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

//    void setStatus(final String text) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                status.setText(text);
//            }
//        });
//    }
//
//    void setStatus(@StringRes int resId) {
//        setStatus(getString(resId));
//    }
//
//    void doDownload() {
//        try {
//            URL url = new URL(getString(R.string.meditation_audio_url));
//            InputStream is = url.openStream();
//            FileOutputStream fos = new FileOutputStream(file);
//            try {
//                long total = 0;
//
//                byte[] buf = new byte[Constants.BUFFER_SIZE];
//                int s;
//                while ((s = is.read(buf)) > 0) {
//                    fos.write(buf, 0, s);
//
//                    setStatus(String.format("%,d bytes", total += s));
//                }
//
//                fos.flush();
//
//            } finally {
//                fos.close();
//            }
//
//            setStatus(getString(R.string.downloaded));
//
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    prepare();
//                }
//            });
//
//        } catch (Exception e) {
//            LOG.error("Failed to download file", e);
//
//            setStatus(getString(R.string.download_error));
//        }
//    }

    void prepare() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    player.setDataSource(file.getAbsolutePath());
                    player.prepare();

                    playButton.setVisibility(View.VISIBLE);

                } catch (Exception e) {
                    LOG.error("Failed to open audio file", e);
                }
            }
        });
    }
}
