package wang.imallen.blog.imageloader.constant;

import java.util.Locale;

/**
 * Created by allen on 15-9-14.
 */
public enum Schema {

    HTTP("http"),HTTPS("https"),FILE("file"),CONTENT("content"),
    ASSETS("assets"),DRAWABLE("drawable"),UNKNOWN("");

    private String schema;
    private String uriPrefix;

    private Schema(String schema)
    {
        this.schema=schema;
        uriPrefix=schema+"://";
    }

    public static Schema ofUri(String uri)
    {
        if(uri!=null)
        {
            for(Schema s:values())
            {
                if(s.belongsTo(uri))
                {
                    return s;
                }
            }
        }
        return UNKNOWN;
    }

    private boolean belongsTo(String uri)
    {
        return uri.toLowerCase(Locale.US).startsWith(uriPrefix);
    }

    /**
     * Appends schema to incoming path
     * @param path
     * @return
     */
    public String wrap(String path)
    {
        return uriPrefix+path;
    }

    /**
     * removed schema part("schema://") from incoming uri
     * @param uri
     * @return
     */
    public String crop(String uri)
    {
        if(!belongsTo(uri))
        {
            throw new IllegalArgumentException(String.
                    format("URI [%1$s] doesn't have expected scheme [%2$s]", uri, schema));
        }
        return uri.substring(uriPrefix.length());
    }


}
