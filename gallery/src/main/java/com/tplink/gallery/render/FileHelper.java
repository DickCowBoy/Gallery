package com.tplink.gallery.render;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.FileNotFoundException;

public class FileHelper {

    private FileHelper() {

    }

    public static ParcelFileDescriptor getFileDescriptor(Context context, Uri uri) {
        try {
            return context.getContentResolver()
                    .openFileDescriptor(uri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
