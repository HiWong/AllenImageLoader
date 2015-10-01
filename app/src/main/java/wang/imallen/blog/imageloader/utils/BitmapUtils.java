package wang.imallen.blog.imageloader.utils;

import android.graphics.Bitmap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by allen on 15-9-14.
 */
public class BitmapUtils {

    public static InputStream getCompressInputStream(Bitmap bitmap)
    {
        if (bitmap != null)
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            //////////////////do we need to recycle bitmap?
            bitmap.recycle();
            ////////////////////
            return new ByteArrayInputStream(bos.toByteArray());
        }
        return null;
    }

}
