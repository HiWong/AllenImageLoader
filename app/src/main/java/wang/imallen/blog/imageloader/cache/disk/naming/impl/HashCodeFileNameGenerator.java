package wang.imallen.blog.imageloader.cache.disk.naming.impl;

import wang.imallen.blog.imageloader.cache.disk.naming.FileNameGenerator;

/**
 * Created by allen on 15-9-5.
 */
public class HashCodeFileNameGenerator implements FileNameGenerator {

    @Override
    public String generate(String imageUri) {
        return String.valueOf(imageUri.hashCode());
    }
}
