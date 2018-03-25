package org.idea.declaration;

import android.content.Context;
import android.content.Intent;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by rickwang on 2018/1/28.
 */

public class Utils {
    static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    public static void shareTo(Context context, String message) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, String.format("%s : %s", context.getString(R.string.app_name), message));
        context.startActivity(intent);
    }

    public static boolean checksum(File file, URL url) throws IOException {
        String expected = IOUtils.toString(url, "UTF-8").split(" ")[0];

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(file);
            try {
                byte[] bytes = new byte[Constants.BUFFER_SIZE];
                int s;
                while ((s = fis.read(bytes)) > 0) {
                    md.update(bytes, 0, s);
                }

                String md5 = toString(md.digest());

                LOG.info("{} is {}. Expected MD5 is {}", file.getAbsolutePath(), md5, expected);

                return expected.equals(md5);

            } finally {
                fis.close();
            }

        } catch (Exception e) {
            LOG.error("Failed to calculate the MD5 checksum", e);
        }

        return false;
    }

    public static String toString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    public static void extract(File path, ZipInputStream zis) throws IOException {
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            String zna = ze.getName();
            String name = URLEncoder.encode(zna, "UTF-8");
            File f = new File(path, name);

            LOG.info("Save {} ({})", f.getAbsolutePath(), zna);

            File fd = f.getParentFile();
            if (!fd.isDirectory()) {
                if (fd.mkdirs() == false) {
                    throw new IOException("Failed to build the folder - " + fd.getAbsolutePath());
                }
            }

            FileOutputStream fos = new FileOutputStream(f);
            try {
                IOUtils.copy(zis, fos);
                fos.flush();

            } finally {
                fos.close();
            }
        }
    }
}
