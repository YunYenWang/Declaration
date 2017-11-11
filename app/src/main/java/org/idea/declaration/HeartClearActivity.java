package org.idea.declaration;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class HeartClearActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_clear);
    }

    public void onOpenLink(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://goo.gl/nuldeD"));
        startActivity(intent);
    }
}
