package wang.imallen.blog.imageloader.cache.disk.impl;

import java.io.File;

import wang.imallen.blog.imageloader.cache.disk.naming.FileNameGenerator;

/**
 * Created by allen on 15-9-13.
 */
public class UnlimitedDiskCache extends BaseDiskCache{

    private static final String TAG=UnlimitedDiskCache.class.getSimpleName();

    public UnlimitedDiskCache(File cacheDir)
    {
        super(cacheDir);
    }

    public UnlimitedDiskCache(File cacheDir,File reserveCacheDir)
    {
        super(cacheDir,reserveCacheDir);
    }

    public UnlimitedDiskCache(File cacheDir,File reserveCacheDir,FileNameGenerator fileNameGenerator)
    {
        super(cacheDir,reserveCacheDir,fileNameGenerator);
    }



}
