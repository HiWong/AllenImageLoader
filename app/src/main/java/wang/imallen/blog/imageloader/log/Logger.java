package wang.imallen.blog.imageloader.log;

import android.util.Log;
import android.widget.Toast;

/**
 * Created by allen on 15-9-11.
 */
public class Logger {
    public static boolean DEBUG = true;

    public static void d(String msg) {
        d(Logger.class.getSimpleName(),msg);
    }

    public static void d(String tag,String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag,String msg)
    {
        Log.i(tag,msg);
    }

    public static void e(String tag,String msg)
    {
        if(DEBUG)
        {
            Log.e(tag,msg);
        }
    }
}
