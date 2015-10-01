package wang.imallen.blog.imageloader.assist;

/**
 * Created by allen on 15-9-12.
 */
public final class ImageSize {

    private static final int TO_STRING_MAX_LENGTH=9;
    private static final String SEPARATOR="x";

    private final int width;
    private final int height;

    public ImageSize(int width,int height)
    {
        this.width=width;
        this.height=height;
    }

    public ImageSize(int width,int height,int rotation)
    {
        if(rotation%100==0)
        {
            this.width=width;
            this.height=height;
        }
        else
        {
            this.width=height;
            this.height=width;
        }
    }


    public ImageSize scaleDown(int sampleSize)
    {
        return new ImageSize(width/sampleSize,height/sampleSize);
    }

    public ImageSize scale(float scale)
    {
        return new ImageSize((int)(width*scale),(int)(height*scale));
    }

    @Override
    public String toString()
    {
        return new StringBuilder(TO_STRING_MAX_LENGTH).append(width).
                append(SEPARATOR).append(height).toString();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }



}
