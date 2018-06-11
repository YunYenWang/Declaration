package org.idea.declaration;

import android.media.MediaPlayer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LightLanguageActivity extends AppCompatActivity {

//    MediaPlayer player;

    ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_language);

        ActionBar bar = getSupportActionBar();
        bar.setTitle(R.string.action_light_language);
        bar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    protected void onDestroy() {
        executor.shutdown();

//        player.stop();

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
}
