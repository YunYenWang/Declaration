package org.idea.declaration;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final Logger LOG = LoggerFactory.getLogger(MainActivity.class);

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    FloatingActionButton favor;

    List<String> declarations = new ArrayList<>();
    int textSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        textSize = settings.getInt("TextSize", Constants.DEFAULT_TEXT_SIZE);

        buildDeclarations();

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                File f = newPositionFavoredFile(position); // adjust the icon of the favored button when the page is changed
                setFavorite(f.canRead());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // change the page randomly
        FloatingActionButton fab = findViewById(R.id.home);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem((int) (Math.random() * declarations.size()));
            }
        });

        // keep or remove the favored page
        favor = findViewById(R.id.favor);
        favor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = mViewPager.getCurrentItem();
                File f = newPositionFavoredFile(position);
                if (f.canRead()) {
                    f.delete();
                    setFavorite(false);

                    Snackbar.make(view, getString(R.string.remove_favor), Snackbar.LENGTH_SHORT).setAction("Action", null).show();

                } else {
                    touch(f);
                    setFavorite(true);

                    Snackbar.make(view, getString(R.string.add), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }
            }
        });

        // change the current page by date
        int index = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1;
        if (index >= declarations.size()) {
            index = (int) (Math.random() * declarations.size());
        }
        mViewPager.setCurrentItem(index);
    }

    // ======

    File newPositionFavoredFile(int position) {
        return new File(getCacheDir(), String.format(Constants.FAVOR_FILE_NAME_PREFIX_FORMAT, position));
    }

    // adjust icon of the favored page
    void setFavorite(boolean favorite) {
        favor.setImageResource(favorite? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
    }

    // just build an empty favor 'file'
    void touch(File f) {
        try {
            FileOutputStream fos = new FileOutputStream(f);
            try {
                fos.flush();

            } finally {
                fos.close();
            }

            setFavorite(true);

        } catch (Exception e) {
            LOG.error("Failed to touch the file - {}", f.getAbsolutePath(), e);
        }
    }

    void buildDeclarations() {
        try {
            InputStream is = getAssets().open(Constants.DECLARATION_FILE);
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                StringWriter sw = null;
                PrintWriter pw = null;

                String ln;
                while ((ln = br.readLine()) != null) {
                    if (ln.startsWith("*")) {
                        if (sw != null) {
                            pw.flush();
                            declarations.add(sw.toString());
                        }

                        sw = new StringWriter();
                        pw = new PrintWriter(sw);

                    } else {
                        if (pw != null) {
                            pw.println(ln);
                            pw.println();
                        }
                    }
                }

                if (sw != null) {
                    pw.flush();
                    declarations.add(sw.toString());
                }

            } finally {
                is.close();
            }

        } catch (IOException e) {
            LOG.error("Failed to load the declaration.txt", e);
        }
    }

    // ======

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_favor) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final FavorListAdaptor adaptor = new FavorListAdaptor(this);
            builder.setAdapter(adaptor, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int position = adaptor.getItem(i);
                    mViewPager.setCurrentItem(position);
                }
            });

            builder.show();

            return true;

        } else if (id == R.id.action_clear) {
            Intent intent = new Intent(this, HeartClearActivity.class);
            startActivity(intent);

            return true;

        } else if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ======

    public void onTextSizeIncreased(View view) {
        textSize += 2;

        saveAdjustedTextSize();

        Intent intent = new Intent("TextSize");
        intent.putExtra("TextSize", textSize);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void onTextSizeDecreased(View view) {
        if (textSize > Constants.DEFAULT_TEXT_SIZE) {
            textSize -= 2;

            saveAdjustedTextSize();
        }

        Intent intent = new Intent("TextSize");
        intent.putExtra("TextSize", textSize);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    void saveAdjustedTextSize() {
        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("TextSize", textSize);
        editor.commit();
    }

    public static class PlaceholderFragment extends Fragment {
        static final String ARG_DECLARATION = "declaration";
        static final String ARG_SIZE = "size";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(String declaration, int size) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putString(ARG_DECLARATION, declaration);
            args.putInt(ARG_SIZE, size);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            String declaration = getArguments().getString(ARG_DECLARATION);
            int size = getArguments().getInt(ARG_SIZE);

            final TextView textView = rootView.findViewById(R.id.message);
            textView.setTextSize(size);
            textView.setText(declaration);

            // receive the broadcast message to change the text size
            IntentFilter filter = new IntentFilter("TextSize");
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int size = intent.getIntExtra("TextSize", Constants.DEFAULT_TEXT_SIZE);
                    textView.setTextSize(size);
                }
            }, filter);

            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page. Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(declarations.get(position), textSize);
        }

        @Override
        public int getCount() {
            return declarations.size();
        }
    }
}
