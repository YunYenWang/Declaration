package org.idea.declaration;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
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

    File newFile(String name) {
        return new File(getFilesDir(), name);
    }

    File newFile(@StringRes int resId) {
        return newFile(getString(resId));
    }

    URL newURL(@StringRes int resId) throws IOException {
        return new URL(getString(resId));
    }

    String readFileToString(@StringRes int resId) throws IOException {
        File f = newFile(resId);
        String s = f.canRead()? FileUtils.readFileToString(f, Constants.UTF8) : "";

        return s;
    }

    void writeToFile(@StringRes int resId, String s) throws IOException {
        File f = newFile(resId);
        FileUtils.write(f, s, Constants.UTF8);
    }

    void setStatus(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText(message);
            }
        });
    }

    void nextActivity() {
        startActivity(new Intent(this, NavigationActivity.class));
        finish();
    }

    void showErrorDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(R.string.download_error)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                nextActivity();
                            }
                        })
                        .show();
            }
        });
    }

    void init() {
        if (downloadDaniel() && downloadDeclaration()) {
            nextActivity();

        } else {
            showErrorDialog();
        }
    }

    boolean downloadDaniel() {
        try {
            String version = IOUtils.toString(newURL(R.string.daniel_file_version_url), Constants.UTF8).trim();
            String current = readFileToString (R.string.daniel_file_version).trim();

            LOG.info("Daniel Current: {}, New: {}", current, version);

            File file = newFile(R.string.daniel_file);

            // version is different and checksum is wrong
            if (!current.equals(version) || !Utils.checksum(file, newURL(R.string.daniel_file_md5_url))) {
                if (download(R.string.daniel_file_url, file)) {
                    writeToFile(R.string.daniel_file_version, version); // save the last version file

                } else {
                    return false;
                }
            }

        } catch (Exception e) {
            LOG.error("Network is not available", e);
        }

        return true;
    }

    boolean downloadDeclaration() {
        try {
            String version = IOUtils.toString(newURL(R.string.declaration_file_version_url), Constants.UTF8).trim();
            String current = readFileToString (R.string.declaration_file_version).trim();

            LOG.info("Declaration Current: {}, New: {}", current, version);

            File file = newFile(R.string.declaration_file);

            // version is different and checksum is wrong
            if (!current.equals(version) || !Utils.checksum(file, newURL(R.string.declaration_file_md5_url))) {
                if (download(R.string.declaration_file_url, file)) {
                    writeToFile(R.string.declaration_file_version, version); // save the last version file

                } else {
                    return false;
                }
            }

        } catch (Exception e) {
            LOG.error("Network is not available", e);
        }

        return true;
    }

    boolean download(@StringRes int resId, File file) throws IOException {
        try {
            URL url = newURL(resId);
            InputStream is = url.openStream();
            FileOutputStream fos = new FileOutputStream(file);
            try {
                long total = 0;

                byte[] buf = new byte[Constants.BUFFER_SIZE];
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

            return true;

        } catch (Exception e) {
            LOG.error("Failed to download file", e);

            setStatus(getString(R.string.download_error) + " - " + e.getMessage());
        }

        return false;
    }
}
