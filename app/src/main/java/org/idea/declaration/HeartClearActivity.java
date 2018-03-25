package org.idea.declaration;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartClearActivity extends AppCompatActivity {
    static final Logger LOG = LoggerFactory.getLogger(HeartClearActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_clear);

        ActionBar bar = getSupportActionBar();
        bar.setTitle(R.string.action_clear);
        bar.setDisplayHomeAsUpEnabled(true);

        TextView tv = findViewById(R.id.message);
        tv.setTextSize(Globals.textSize);
    }

    public void onOpenLink(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://goo.gl/nuldeD"));
        startActivity(intent);
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
