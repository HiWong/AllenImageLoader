package wang.imallen.blog.imageloader.cache.disk.disklrucache;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * Created by allen on 15-9-6.
 */
final class Util {

    static final Charset US_ASCII=Charset.forName("US-ASCII");
    static final Charset UTF_8=Charset.forName("UTF-8");

    private Util()
    {

    }

    static String readFully(Reader reader) throws IOException
    {
        try
        {
            StringWriter writer=new StringWriter();
            char[]buffer=new char[1024];
            int count;
            while((count=reader.read(buffer))!=-1)
            {
                writer.write(buffer,0,count);
            }
            return writer.toString();
        }
        finally
        {
            reader.close();
        }
    }

    static void deleteContents(File dir) throws IOException
    {
        File[]files=dir.listFiles();
        if(null==files)
        {
            throw new IOException("not a readable directory:"+dir);
        }
        for(File file:files)
        {
            if(file.isDirectory())
            {
                deleteContents(file);
            }
            if(!file.delete())
            {
                throw new IOException("failed to delete file:"+file);
            }
        }
    }

    static void closeQuietly(Closeable closeable)
    {
        if(null!=closeable)
        {
            try
            {
                closeable.close();
            }
            catch(RuntimeException ex)
            {
                throw ex;
            }
            catch(Exception ignored)
            {
                ignored.printStackTrace();
            }
        }
    }


}
