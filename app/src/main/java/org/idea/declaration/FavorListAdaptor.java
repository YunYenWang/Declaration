package org.idea.declaration;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by rickwang on 2017/11/4.
 */

public class FavorListAdaptor extends BaseAdapter {
    final Context context;

    final List<Integer> ids = new ArrayList<>();

    public FavorListAdaptor(Context context) {
        this.context = context;

        for (String fn : context.getCacheDir().list()) {
            if (fn.startsWith("favor.")) {
                int position = Integer.parseInt(fn.substring(6));
                ids.add(position);
            }
        }

        Collections.sort(ids);
    }

    @Override
    public int getCount() {
        return ids.size();
    }

    @Override
    public Integer getItem(int i) {
        return ids.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        int position = getItem(i);

        FavorItem fi = (FavorItem) view;
        if (fi == null) {
            fi = new FavorItem(context);
        }

        return fi.update(position);
    }

    static class FavorItem extends LinearLayout {
        public FavorItem(Context context) {
            super(context);

            inflate(context, R.layout.favor_item, this);
        }

        public View update(int position) {
            TextView tv = (TextView) findViewById(R.id.text);
            tv.setText(String.format("%02d", position + 1));

            return this;
        }

    }
}
