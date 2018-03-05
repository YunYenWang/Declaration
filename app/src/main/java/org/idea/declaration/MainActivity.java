package org.idea.declaration;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    static final Logger LOG = LoggerFactory.getLogger(MainActivity.class);

    TextView status;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = findViewById(R.id.message);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();

        super.onDestroy();
    }

    // ======

    void setStatus(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText(message);
            }
        });
    }

    void init() {
        LOG.info("init");

        try {
            String version = IOUtils.toString(new URL(getString(R.string.daniel_file_version_url)), "UTF-8");

            File dfv = new File(getCacheDir(), getString(R.string.daniel_file_version));
            String curr = (dfv.canRead())? FileUtils.readFileToString(dfv, "UTF-8") : "";

            LOG.info("Current: {}, New: {}", curr, version);

            if (!curr.equals(version)) {
                doDownload();
                FileUtils.write(dfv, version, "UTF-8");
            }

        } catch (Exception e) {
            LOG.error("Something wrong", e);
        }

        startActivity(new Intent(this, NavigationActivity.class));
        finish();
    }

    void doDownload() throws IOException {
        try {
            File file = new File(getCacheDir(), getString(R.string.daniel_file));

            URL url = new URL(getString(R.string.daniel_file_url));
            InputStream is = url.openStream();
            FileOutputStream fos = new FileOutputStream(file);
            try {
                long total = 0;

                byte[] buf = new byte[4096];
                int s;
                while ((s = is.read(buf)) > 0) {
                    fos.write(buf, 0, s);

                    setStatus(String.format("%,d bytes", total += s));
                }

                fos.flush();

            } finally {
                fos.close();
            }

            setStatus(getString(R.string.downloaded));

        } catch (Exception e) {
            LOG.error("Failed to download file", e);

            setStatus(getString(R.string.download_error));
        }
    }
}
