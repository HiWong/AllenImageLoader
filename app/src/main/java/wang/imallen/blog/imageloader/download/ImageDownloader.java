package wang.imallen.blog.imageloader.download;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by allen on 15-9-14.
 */
public interface ImageDownloader
{
    InputStream getStream(String imageUri) throws IOException;

}
