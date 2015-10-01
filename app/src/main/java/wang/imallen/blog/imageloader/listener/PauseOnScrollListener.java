package wang.imallen.blog.imageloader.listener;

import android.widget.AbsListView;

import wang.imallen.blog.imageloader.core.AllenImageLoader;

/**
 * * Listener-helper for {@linkplain android.widget.AbsListView list views} ({@link android.widget.ListView}, {@link android.widget.GridView}) which can
 * {@linkplain wang.imallen.blog.imageloader.core.AllenImageLoader#pause() pause ImageLoader's tasks} while list view is scrolling (touch scrolling and/or
 * fling). It prevents redundant loadings.<br />
 * Set it to your list view's {@link android.widget.AbsListView#setOnScrollListener(android.widget.AbsListView.OnScrollListener) setOnScrollListener(...)}.<br />
 * This listener can wrap your custom {@linkplain android.widget.AbsListView.OnScrollListener listener}.
 *
 * Created by allen on 15-9-12.
 */
public class PauseOnScrollListener implements AbsListView.OnScrollListener{

    private AllenImageLoader allenImageLoader;

    private final boolean pauseOnScroll;
    private final boolean pauseOnFling;
    private final AbsListView.OnScrollListener onScrollListener;

    public PauseOnScrollListener(AllenImageLoader allenImageLoader,boolean pauseOnScroll,boolean pauseOnFling)
    {
        this(allenImageLoader,pauseOnScroll,pauseOnFling,null);
    }

    public PauseOnScrollListener(AllenImageLoader allenImageLoader,boolean pauseOnScroll,boolean pauseOnFling,
                                 AbsListView.OnScrollListener onScrollListener)
    {
        this.allenImageLoader=allenImageLoader;
        this.pauseOnScroll=pauseOnScroll;
        this.pauseOnFling=pauseOnFling;
        this.onScrollListener=onScrollListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState)
        {
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                allenImageLoader.resume();
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                if(pauseOnScroll)
                {
                    allenImageLoader.pause();
                }
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                if(pauseOnFling)
                {
                    allenImageLoader.pause();
                }
                break;
        }
        if(null!=onScrollListener)
        {
            onScrollListener.onScrollStateChanged(view,scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(null!=onScrollListener)
        {
            onScrollListener.onScroll(view,firstVisibleItem,visibleItemCount,totalItemCount);
        }
    }
}
