package wang.imallen.blog.imageloader.cache.memory.impl;

import android.graphics.Bitmap;
import android.util.LruCache;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import wang.imallen.blog.imageloader.cache.memory.MemoryCache;
import wang.imallen.blog.imageloader.log.Logger;

public class LruMemoryCache implements MemoryCache{

    private final LinkedHashMap<String,Bitmap> map;

    private final int maxSize;
    private int size;

    public LruMemoryCache(int maxSize)
    {
        if(maxSize<=0)
        {
            throw new IllegalArgumentException("maxSize<=0");
        }
        this.maxSize=maxSize;
        this.map=new LinkedHashMap<>(0,0.75f,true);
    }

    @Override
    public final Bitmap get(String key)
    {
        if(null==key)
        {
            throw new NullPointerException("key==null");
        }

        synchronized (this)
        {
            return map.get(key);
        }

    }

    @Override
    public final boolean put(String key,Bitmap value)
    {
        if(null==key||null==value)
        {
            throw new NullPointerException("key==null||value==null");
        }

        synchronized (this)
        {
            size+=sizeOf(key,value);
            Bitmap previous=map.put(key,value);
            if(previous!=null)
            {
                size-=sizeOf(key,previous);
            }
        }

        trimToSize(maxSize);
        return true;
    }

    private void trimToSize(int maxSize)
    {
        while(true)
        {
            String key;
            Bitmap value;
            synchronized (this)
            {
                if(size<0||(map.isEmpty()&&size!=0))
                {
                    throw new IllegalStateException(getClass().getName() + ".sizeOf() is reporting inconsistent results!");
                }

                if(size<=maxSize||map.isEmpty())
                {
                    break;
                }

                Map.Entry<String,Bitmap>toEvict=map.entrySet().iterator().next();
                if(null==toEvict)
                {
                    break;
                }
                key=toEvict.getKey();
                value=toEvict.getValue();
                map.remove(key);
                size-=sizeOf(key,value);
            }
        }
    }

    @Override
    public final Bitmap remove(String key)
    {
        if(null==key)
        {
            throw new NullPointerException("key==null");
        }

        synchronized (this)
        {
            Bitmap previous=map.remove(key);
            if(previous!=null)
            {
                size-=sizeOf(key,previous);
            }
            return previous;
        }

    }

    @Override
    public Collection<String> keys()
    {
        synchronized (this)
        {
            return new HashSet<String>(map.keySet());
        }
    }

    @Override
    public void clear()
    {
        trimToSize(-1);
    }

    /**
     * this method is vital
     * @param key
     * @param value
     * @return
     */
    private int sizeOf(String key,Bitmap value)
    {
        return value.getRowBytes()*value.getHeight();
    }

    @Override
    public synchronized final String toString()
    {
        return String.format("LruCache[maxSize=%d]", maxSize);
    }


}




/**
 * The reason that we cannot use LruCache<String,Bitmap> is that
 * LruCache#sizeOf(K key,V value) always return 1 instead of bitmap.getRowBytes*bitmap.getHeight().
 * Created by allen on 15-9-11.
 */
/*
public class LruMemoryCache implements MemoryCache {

    private static final String TAG=LruMemoryCache.class.getSimpleName();

    private LruCache<String,Bitmap>memoryCache;

    public LruMemoryCache(int maxSize)
    {
        final int maxMemory=(int)Runtime.getRuntime().maxMemory();
        int cacheSize=maxMemory/4;
        if(maxSize>0)
        {
            cacheSize=maxSize;
        }
        Logger.d(TAG, "cacheSize=" + cacheSize);
        memoryCache=new LruCache<String,Bitmap>(cacheSize)
        {
            @Override
            protected int sizeOf(String key,Bitmap bitmap)
            {
                return bitmap.getRowBytes()*bitmap.getHeight();
            }
        };

    }

    @Override
    public Bitmap get(String key) {
        return memoryCache.get(key);
    }

    @Override
    public boolean put(String key,Bitmap value) {
        if(null==value||null==key)
        {
            return false;
        }
        memoryCache.put(key,value);
        return true;
    }

    @Override
    public Bitmap remove(String key) {
        return memoryCache.remove(key);
    }

    @Override
    public void clear() {
        memoryCache.evictAll();
    }
}
*/
