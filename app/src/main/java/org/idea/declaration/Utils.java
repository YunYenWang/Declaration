package org.idea.declaration;

import android.content.Context;
import android.content.Intent;

/**
 * Created by rickwang on 2018/1/28.
 */

public class Utils {

    public static void shareTo(Context context, String message) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, String.format("%s : %s", context.getString(R.string.app_name), message));
        context.startActivity(intent);
    }
}
