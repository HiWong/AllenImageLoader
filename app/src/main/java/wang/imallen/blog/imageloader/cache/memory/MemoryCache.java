package wang.imallen.blog.imageloader.cache.memory;

import android.graphics.Bitmap;

import java.util.Collection;

/**
 * Created by allen on 15-9-12.
 */
public interface MemoryCache {

    boolean put(String key,Bitmap value);

    Bitmap get(String key);

    Bitmap remove(String key);

    void clear();

    Collection<String>keys();

}
