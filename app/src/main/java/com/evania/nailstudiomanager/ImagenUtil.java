package com.evania.nailstudiomanager;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ImagenUtil {
    public static Uri[] preparar(Context ctx, Uri[] in) {
        if (in == null) return null;
        Uri[] out = new Uri[in.length];
        for (int i = 0; i < in.length; i++) out[i] = reducir(ctx, in[i]);
        return out;
    }

    private static Uri reducir(Context ctx, Uri u) {
        try {
            ContentResolver cr = ctx.getContentResolver();
            String mime = cr.getType(u);
            if (mime == null || !mime.startsWith("image/") || mime.contains("gif") || mime.contains("svg")) return u;

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            try (InputStream is = cr.openInputStream(u)) { BitmapFactory.decodeStream(is, null, o); }
            if (o.outWidth <= 0 || o.outHeight <= 0) return u;

            int lado = Math.max(o.outWidth, o.outHeight), s = 1;
            while (lado / (s * 2) >= 2048) s *= 2;
            o.inJustDecodeBounds = false;
            o.inSampleSize = s;
            Bitmap bm;
            try (InputStream is = cr.openInputStream(u)) { bm = BitmapFactory.decodeStream(is, null, o); }
            if (bm == null) return u;

            int rot = 0;
            try (InputStream is = cr.openInputStream(u)) {
                int ori = new ExifInterface(is).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                if (ori == 6) rot = 90; else if (ori == 3) rot = 180; else if (ori == 8) rot = 270;
            } catch (Exception ignored) {}
            if (rot != 0) {
                Matrix m = new Matrix();
                m.postRotate(rot);
                Bitmap r = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                bm.recycle();
                bm = r;
            }

            File f = File.createTempFile("foto_", ".jpg", ctx.getCacheDir());
            try (OutputStream os = new FileOutputStream(f)) { bm.compress(Bitmap.CompressFormat.JPEG, 90, os); }
            bm.recycle();
            return Uri.fromFile(f);
        } catch (Throwable t) {
            return u;
        }
    }
}
