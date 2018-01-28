package org.idea.declaration;

import android.content.Context;
import android.content.res.AssetManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by rickwang on 2018/1/28.
 */

public class Store {
    static final Logger LOG = LoggerFactory.getLogger(Store.class);

    static List<String> favorites = null;

    public static List<String> loadFavorites(Context context) {
        if (favorites == null) {
            favorites = new ArrayList<>();

            File f = new File(context.getCacheDir(), Constants.FAVORITE_FILE);
            if (f.canRead()) {
                try {
                    FileInputStream fis = new FileInputStream(f);
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
                        String ln = br.readLine();
                        while ((ln = br.readLine()) != null) {
                            favorites.add(ln);
                        }

                    } finally {
                        fis.close();
                    }

                } catch (Exception e) {
                    LOG.error("Failed to load favorite file", e);
                }
            }
        }

        return favorites;
    }

    public static void addFavorite(Context context, String line) {
        List<String> favorites = loadFavorites(context);
        if (!favorites.contains(line)) {
            favorites.add(line);

            saveFavorites(context);
        }
    }

    public static boolean removeFavorite(Context context, String line) {
        List<String> favorites = loadFavorites(context);
        if (favorites.remove(line)) {
            saveFavorites(context);

            return true;
        }

        return false;
    }

    public static void saveFavorites(Context context) {
        File f = new File(context.getCacheDir(), Constants.FAVORITE_FILE);
        try {
            FileOutputStream fos = new FileOutputStream(f);
            try {
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(fos, "UTF-8"));
                for (String ln : favorites) {
                    pw.println(ln);
                }

                pw.flush();

            } finally {
                fos.close();
            }

        } catch (Exception e) {
            LOG.error("Failed to save favorite file", e);
        }
    }

    public static List<List<String>> buildDeclarations(AssetManager assets) {
        List<List<String>> declarations = new ArrayList<>();

        try {
            InputStream is = assets.open(Constants.DECLARATION_FILE);
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                List<String> lines = null;

                String ln;
                while ((ln = br.readLine()) != null) {
                    if (ln.startsWith("*")) {
                        if (lines != null) {
                            declarations.add(lines);
                        }

                        lines = new ArrayList<>();

                    } else {
                        if (lines != null) {
                            lines.add(ln);
                        }
                    }
                }

                if (lines != null) {
                    declarations.add(lines);
                }

            } finally {
                is.close();
            }

        } catch (IOException e) {
            LOG.error("Failed to load the declaration.txt", e);
        }

        return declarations;
    }
}
