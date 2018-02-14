package org.idea.declaration;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.github.chrisbanes.photoview.PhotoView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmotionTableActivity extends AppCompatActivity {
    static final Logger LOG = LoggerFactory.getLogger(EmotionTableActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotion_table);

        ActionBar bar = getSupportActionBar();
        bar.setTitle(R.string.action_emotion);
        bar.setDisplayHomeAsUpEnabled(true);

        try {
            Bitmap bmp = BitmapFactory.decodeStream(getAssets().open("emotion.png"));
            PhotoView photo = findViewById(R.id.photo);
            photo.setImageBitmap(bmp);

        } catch (Exception e) {
            LOG.error("Failed to open emotion.png");
        }
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
}
