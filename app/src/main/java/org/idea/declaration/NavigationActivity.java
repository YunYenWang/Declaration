package org.idea.declaration;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;

import javax.microedition.khronos.opengles.GL;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static final Logger LOG = LoggerFactory.getLogger(NavigationActivity.class);

    List<List<String>> declarations;
    LinearLayout favorites;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadPreferences();

        toolbar.setBackgroundColor(getResources().getColor(R.color.toolbarColor));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        favorites = findViewById(R.id.favorites);
        registerTextSizeChanged();
        reloadFavorites();

        IntentFilter filter = new IntentFilter("ReloadFavorites");
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                reloadFavorites();
            }
        }, filter);

        declarations = Store.buildDeclarations(this);

        viewPager = findViewById(R.id.container);
        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {

            @Override
            public int getCount() {
                return declarations.size();
            }

            @Override
            public Fragment getItem(int position) {
                return ContentFragment.newInstance(declarations.get(position));
            }
        });

        today();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    void loadPreferences() {
        try {
            SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
            Globals.textSize = settings.getFloat("TextSize", Constants.DEFAULT_TEXT_SIZE);

        } catch (Exception e) {
            LOG.error("Failed to load preferences", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_favor) {
            if (favorites.getVisibility() == View.VISIBLE) {
                favorites.setVisibility(View.GONE);
                item.setIcon(R.mipmap.ic_favorite_border_white_24dp);

            } else {
                favorites.setVisibility(View.VISIBLE);
                item.setIcon(R.mipmap.ic_favorite_white_24dp);
            }

            return true;

        } else if (id == R.id.action_paper) {
            Intent intent = new Intent(this, PaperActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_clear) {
            Intent intent = new Intent(this, HeartClearActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_paper) {
            Intent intent = new Intent(this, PaperActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {
            Utils.shareTo(this, "https://play.google.com/store/apps/details?id=org.idea.declaration");

        } else if (id == R.id.nav_meditation) {
            Intent intent = new Intent(this, MeditationActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_bgm) {
            Intent intent = new Intent(this, BackgroundMusicActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_emotion) {
            Intent intent = new Intent(this, EmotionTableActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_exit) {
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // ======

    void reloadFavorites() {
        favorites.removeAllViews();

        int color = getResources().getColor(R.color.favoriteColor);

        for (final String line : Store.loadFavorites(this)) {
            final View v = getLayoutInflater().inflate(R.layout.favor_item, null);

            TextView tv = v.findViewById(R.id.text);
            tv.setTextSize(Globals.textSize);
            tv.setTextColor(color);
            tv.setText(line);
            tv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new AlertDialog.Builder(NavigationActivity.this)
                            .setItems(
                                    new String[]{getString(R.string.remove), getString(R.string.share)},
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (i == 0) {
                                                favorites.removeView(v);
                                                Store.removeFavorite(NavigationActivity.this, line);

                                            } else if (i == 1) {
                                                Utils.shareTo(NavigationActivity.this, line);
                                            }
                                        }
                                    }).show();

                    return true;
                }
            });

            favorites.addView(v);
        }
    }

    void registerTextSizeChanged() {
        IntentFilter filter = new IntentFilter("TextSize");
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                float size = intent.getFloatExtra("TextSize", Constants.DEFAULT_TEXT_SIZE);
                int s = favorites.getChildCount();
                for (int i = 0;i < s;i++) {
                    TextView tv = favorites.getChildAt(i).findViewById(R.id.text);
                    tv.setTextSize(size);
                }
            }
        }, filter);
    }

    // ======

    void today() {
        int index = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1;
        if (index >= declarations.size()) {
            index = (int) (Math.random() * declarations.size());
        }

        viewPager.setCurrentItem(index);
    }

    public void onRefreshClicked(View view) {
        viewPager.setCurrentItem((int) (Math.random() * declarations.size()));
    }

    void saveAdjustedTextSize() {
        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("TextSize", Globals.textSize);
        editor.commit();
    }

    public void onLargeTextSizeClicked(View view) {
        Globals.textSize += 2;

        saveAdjustedTextSize();

        Intent intent = new Intent("TextSize");
        intent.putExtra("TextSize", Globals.textSize);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void onSmallTextSizeClicked(View view) {
        if (Globals.textSize > Constants.DEFAULT_TEXT_SIZE) {
            Globals.textSize -= 2;

            saveAdjustedTextSize();
        }

        Intent intent = new Intent("TextSize");
        intent.putExtra("TextSize", Globals.textSize);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
