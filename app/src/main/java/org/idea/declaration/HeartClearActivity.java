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

public class HeartClearActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_clear);

        ActionBar bar = getSupportActionBar();
        bar.setTitle(R.string.action_clear);
        bar.setDisplayHomeAsUpEnabled(true);

        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        float textSize = settings.getFloat("TextSize", Constants.DEFAULT_TEXT_SIZE);

        TextView tv = findViewById(R.id.message);
        tv.setTextSize(textSize);
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
