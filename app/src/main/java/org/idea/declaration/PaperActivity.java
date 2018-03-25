package org.idea.declaration;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PaperActivity extends AppCompatActivity {
    static final Logger LOG = LoggerFactory.getLogger(PaperActivity.class);

    static final String PREFIX = "Daniel/";

    ActionBar bar;
    LinearLayout contentView;

    File file;

    String[] subjects;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper);

        bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setTitle(R.string.paper);

        contentView = findViewById(R.id.content);

        file = new File(getFilesDir(), getString(R.string.daniel_file));

        executor.execute(new Runnable() {
            @Override
            public void run() {
                doLoad();
            }
        });
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();

        super.onDestroy();
    }

    ZipInputStream open() throws IOException {
        return new ZipInputStream(new FileInputStream(file));
    }

    void doLoad() {
        try {
            ZipInputStream zis = open();
            try {
                subjects = loadSubjects(zis);

                for (String subject : subjects) {
                    LOG.info("subject: {}", subject);
                }

            } finally {
                zis.close();
            }

        } catch (IOException e) {
            LOG.error("Failed to open Daniel.zip", e);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onRefresh(null);
            }
        });
    }

    String[] loadSubjects(ZipInputStream zis) throws IOException {
        Set<String> subjects = new LinkedHashSet<>();

        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            String name = ze.getName();
            if (name.startsWith(PREFIX)) {
                StringTokenizer st = new StringTokenizer(name, "/");
                st.nextToken();
                if (st.hasMoreTokens()) {
                    String subject = st.nextToken();
                    if (!subject.startsWith(".")) {
                        subjects.add(subject);
                    }
                }
            }
        }

        return subjects.toArray(new String[subjects.size()]);
    }

    ImageView newImageView(ZipInputStream zis) {
        ImageView iv = new ImageView(this);
        Bitmap bm = BitmapFactory.decodeStream(zis);
        iv.setImageBitmap(bm);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setAdjustViewBounds(true);

        return iv;
    }

    TextView newTextView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(Globals.textSize);

        return tv;
    }

    void addBottomPadding() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(0, (int) Globals.textSize * 2, 0, (int) Globals.textSize * 2);
        contentView.addView(new TextView(this), lp);
    }

    void openSubject(String subject) throws IOException {
        bar.setTitle(subject);

        contentView.removeAllViews();

        String screen = String.format("%s%s/Screen.png", PREFIX, subject);
        String text = String.format("%s%s/text.txt", PREFIX, subject);

        ZipInputStream zis = open();
        try {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                String name = ze.getName();
                if (name.equals(screen)) {
                    ImageView iv = newImageView(zis);

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    lp.setMargins(0, (int) Globals.textSize, 0, 0);

                    contentView.addView(iv, lp);

                } else if (name.equals(text)) {
                    InputStreamReader isr = new InputStreamReader(zis);
                    BufferedReader br = new BufferedReader(isr);
                    String ln;
                    while ((ln = br.readLine()) != null) {
                        TextView tv = newTextView(ln);

                        contentView.addView(tv);
                    }

                    addBottomPadding();

                    break;
                }
            }

        } finally {
            zis.close();
        }

        ScrollView sv = findViewById(R.id.scrollView);
        sv.fullScroll(ScrollView.FOCUS_UP);
    }

    // ======

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.paper, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_subject) {
            onSubjects();

            return true;
        } else if (id == android.R.id.home) {
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onSubjects() {
        new AlertDialog.Builder(this)
                .setItems(subjects, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            openSubject(subjects[i]);

                        } catch (Exception e) {
                            LOG.error("Failed to open subject", e);
                        }
                    }
                })
                .create()
                .show();
    }

    public void onRefresh(View view) {
        try {
            int i = (int) (Math.random() * subjects.length);
            openSubject(subjects[i]);

        } catch (Exception e) {
            LOG.error("Failed to open subject", e);
        }
    }
}
