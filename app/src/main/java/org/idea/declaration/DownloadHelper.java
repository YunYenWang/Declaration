package org.idea.declaration;

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Executor;

public class DownloadHelper {
    static final Logger LOG = LoggerFactory.getLogger(DownloadHelper.class);

    final Context context;
    final Executor executor;

    final URL url;
    final URL md5;
    final File file;

    Listener listener = new Listener() {
        @Override
        public void onStatus(String status) {
        }

        @Override
        public void onReady() {
        }
    };

    public DownloadHelper(Context context, Executor executor, URL url, URL md5, File file) {
        this.context = context;
        this.executor = executor;

        this.url = url;
        this.md5 = md5;
        this.file = file;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void run() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    listener.onStatus(context.getString(R.string.pulling));

                    if (file.canRead() && Utils.checksum(file, md5)) { // check from network
                        listener.onReady();

                        listener.onStatus(context.getString(R.string.downloaded));

                    } else {
                        listener.onStatus(context.getString(R.string.downloading)); // ready to download

                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                doDownload();
                            }
                        });
                    }

                } catch (Exception e) {
                    LOG.error("Failed to download the mediation file", e);

                    listener.onReady();
                }
            }
        });
    }

    void doDownload() {
        try {
            InputStream is = url.openStream();
            FileOutputStream fos = new FileOutputStream(file);
            try {
                long total = 0;

                byte[] buf = new byte[Constants.BUFFER_SIZE];
                int s;
                while ((s = is.read(buf)) > 0) {
                    fos.write(buf, 0, s);

                    listener.onStatus(String.format("%,d bytes", total += s));
                }

                fos.flush();

            } finally {
                fos.close();
            }

            listener.onStatus(context.getString(R.string.downloaded));

            listener.onReady();

        } catch (Exception e) {
            LOG.error("Failed to download file", e);

            listener.onStatus(context.getString(R.string.download_error));
        }
    }

    // ======

    public interface Listener {
        void onStatus(String status);

        void onReady();
    }
}
