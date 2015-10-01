package wang.imallen.blog.imageloader.cache.disk;

import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import wang.imallen.blog.imageloader.info.ImageLoadingInfo;
import wang.imallen.blog.imageloader.utils.IOUtils;

/**
 * Created by allen on 15-9-13.
 */

public interface DiskCache
{
    File get(String imageUri);

    boolean save(String imageUri,InputStream imageStream,
                 IOUtils.CopyListener listener) throws IOException;

    boolean save(String imageUri,Bitmap bitmap) throws IOException;

    boolean remove(String imageUri);

    void close();

    void clear();

}



/*
public interface DiskCache
{
    File get(String imageUrl);

    boolean save(String imageUri,InputStream imageStream,
                 IOUtils.CopyListener listener) throws IOException;

    boolean save(String imageUri,Bitmap bitmap) throw IOException;

    boolean remove(String imageUri);

    void close();

    void clear();

}
*/
