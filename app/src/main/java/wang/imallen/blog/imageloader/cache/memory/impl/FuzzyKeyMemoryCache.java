package wang.imallen.blog.imageloader.cache.memory.impl;

import android.graphics.Bitmap;

import java.util.Collection;
import java.util.Comparator;

import wang.imallen.blog.imageloader.cache.memory.MemoryCache;
import wang.imallen.blog.imageloader.log.Logger;
import wang.imallen.blog.imageloader.utils.MemoryCacheUtils;


public class FuzzyKeyMemoryCache implements MemoryCache{

    private static final String TAG=FuzzyKeyMemoryCache.class.getSimpleName();

    private final MemoryCache cache;

    public FuzzyKeyMemoryCache(MemoryCache memoryCache)
    {
        this.cache=memoryCache;
    }

    /**
     * maybe we should use imageUri as key
     * @param key
     * @param value
     * @return
     */
    @Override
    public boolean put(String key,Bitmap value)
    {
        //we need to remove first if key exists, then put new value
        //No, actually not,because LruCache will handle this.
        return cache.put(MemoryCacheUtils.getImageUri(key),value);
    }

    @Override
    public Bitmap get(String key)
    {
        return cache.get(MemoryCacheUtils.getImageUri(key));
    }

    @Override
    public Bitmap remove(String key)
    {
        return cache.remove(MemoryCacheUtils.getImageUri(key));
    }

    @Override
    public void clear()
    {
        cache.clear();
    }

    @Override
    public Collection<String>keys()
    {
        return cache.keys();
    }

}


/**
 * Created by allen on 15-9-13.
 */
/*
public class FuzzyKeyMemoryCache implements MemoryCache{

    private static final String TAG=FuzzyKeyMemoryCache.class.getSimpleName();

    private final MemoryCache cache;
    private final Comparator<String>keyComparator;


    public FuzzyKeyMemoryCache(MemoryCache cache)
    {
        this.cache=cache;
        this.keyComparator= MemoryCacheUtils.createFuzzyKeyComparator();
    }


    @Override
    public boolean put(String key,Bitmap value)
    {
        synchronized (cache)
        {
            String keyToRemove=null;
            for(String cacheKey:cache.keys())
            {
                if(keyComparator.compare(key,cacheKey)==0)
                {
                    Logger.d(TAG, "keyComparator.compare(key,cacheKey)==0");
                    keyToRemove=cacheKey;
                    break;
                }

             }

             if(keyToRemove!=null)
             {
                cache.remove(keyToRemove);
             }
        }
        return cache.put(key,value);
    }


    @Override
    public Bitmap get(String key)
    {
        return cache.get(key);
        //return cache.get(MemoryCacheUtils.getImageUri(key));
    }

    @Override
    public Bitmap remove(String key)
    {
        return cache.remove(key);
        //return cache.remove(MemoryCacheUtils.getImageUri(key));
    }

    @Override
    public void clear()
    {
        cache.clear();
    }

    @Override
    public Collection<String>keys()
    {
        return cache.keys();
    }

}
*/

