package wang.imallen.blog.imageloader.assist;

/**
 * Created by allen on 15-9-12.
 */
public class FailReason {

    private final FailType type;
    private final Throwable cause;

    public FailReason(FailType type,Throwable cause)
    {
        this.type=type;
        this.cause=cause;
    }

    public FailType getType() {
        return type;
    }

    public Throwable getCause() {
        return cause;
    }

    @Override
    public String toString()
    {
        StringBuilder sb=new StringBuilder();
        sb.append("type:");
        sb.append(type);
        sb.append("cause:");
        sb.append(cause.getMessage());
        return sb.toString();
    }

    public static enum FailType
    {
        IO_ERROR,
        DECODING_ERROR,
        NETWORK_DENIED,
        OUT_OF_MEMORY,
        UNKNOWN
    }

}
