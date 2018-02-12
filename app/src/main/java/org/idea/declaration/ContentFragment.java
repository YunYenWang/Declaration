package org.idea.declaration;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rickwang on 2018/1/28.
 */

public class ContentFragment extends Fragment {
    static final Logger LOG = LoggerFactory.getLogger(ContentFragment.class);

    static final Gson GSON = new Gson();

    static final String ARG_LINES = "lines";

    public ContentFragment() {
    }

    public static ContentFragment newInstance(List<String> lines) {
        ContentFragment fragment = new ContentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LINES, GSON.toJson(lines));
        fragment.setArguments(args);
        return fragment;
    }

    // ======

    TextView newTextView(final LinearLayout content, final String line) {
        final TextView tv = new TextView(content.getContext());
        tv.setTextSize(Globals.textSize);
        tv.setText(line);
        tv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(content.getContext())
                        .setItems(
                                new String[] { getString(R.string.add), getString(R.string.share) },
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (i == 0) {
                                            Store.addFavorite(getContext(), line);

                                            Intent intent = new Intent("ReloadFavorites");
                                            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

                                        } else if (i == 1) {
                                            Utils.shareTo(getContext(), line);
                                        }
                                    }
                                }).show();

                return true;
            }
        });

        return tv;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final List<String> lines = GSON.fromJson(getArguments().getString(ARG_LINES), ArrayList.class);

        final View root = inflater.inflate(R.layout.fragment_declaration, container, false);

        final LinearLayout content = root.findViewById(R.id.canvas);
        content.removeAllViews();

        for (String line : lines) {
            TextView tv = newTextView(content, line);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 0, 0, (int) Globals.textSize);

            content.addView(tv, lp);
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(0, (int) Globals.textSize * 2, 0, (int) Globals.textSize * 2);
        content.addView(new TextView(content.getContext()), lp);


        // receive the broadcast message to change the text size
        IntentFilter filter = new IntentFilter("TextSize");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                float size = intent.getFloatExtra("TextSize", Constants.DEFAULT_TEXT_SIZE);
                int s = content.getChildCount();
                for (int i = 0;i < s;i++) {
                    TextView tv = (TextView) content.getChildAt(i);
                    tv.setTextSize(size);
                }
            }
        }, filter);

        return root;
    }
}
