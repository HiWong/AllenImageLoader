package wang.imallen.blog.imageloader.deque;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by allen on 15-9-11.
 */
public class LIFOLinkedBlockingDeque<T> extends LinkedBlockingDeque<T> {

    private static final long serialVersionUID = -4114786347960826192L;

    @Override
    public boolean offer(T e)
    {
        return super.offerFirst(e);
    }

    @Override
    public T remove()
    {
        return super.removeFirst();
    }
}
