package wang.imallen.blog.imageloader.utils;

import java.util.Comparator;

import wang.imallen.blog.imageloader.assist.ImageSize;

/**
 * Created by allen on 15-9-13.
 */
public class MemoryCacheUtils {

    private static final String URI_AND_SIZE_SEPARATOR="_";
    private static final String WIDTH_AND_HEIGHT_SEPARATOR="x";

    private MemoryCacheUtils()
    {

    }


    public static String generateKey(String imageUri,ImageSize targetSize)
    {
        return new StringBuilder(imageUri).append(URI_AND_SIZE_SEPARATOR)
                .append(targetSize.getWidth()).append(WIDTH_AND_HEIGHT_SEPARATOR)
                .append(targetSize.getHeight()).toString();
    }

    public static Comparator<String>createFuzzyKeyComparator()
    {
        return new Comparator<String>()
        {
            @Override
            public int compare(String key1,String key2) {
                String imageUri1=key1.substring(0,key1.lastIndexOf(URI_AND_SIZE_SEPARATOR));
                String imageUri2=key2.substring(0,key2.lastIndexOf(URI_AND_SIZE_SEPARATOR));
                return imageUri1.compareTo(imageUri2);
            }
        };
    }

    public static String getImageUri(String key)
    {
        return key.substring(0,key.lastIndexOf(URI_AND_SIZE_SEPARATOR));
    }


}
