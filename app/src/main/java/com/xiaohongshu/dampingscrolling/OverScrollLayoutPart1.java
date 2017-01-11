package com.xiaohongshu.dampingscrolling;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * Created by wupengjian on 17/1/7.
 */
public class OverScrollLayoutPart1 extends FrameLayout {

    private static final float DEFAULT_FATOR = 1;
    /**
     * 阻尼因子
     */
    private float mFator = DEFAULT_FATOR;
    private Scroller mScroller;
    /**
     * 记录上一次触摸事件
     */
    private MotionEvent mLastMotionEvent;

    public OverScrollLayoutPart1(Context context) {
        this(context, null);
    }

    public OverScrollLayoutPart1(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverScrollLayoutPart1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.OverScrollLayout, defStyleAttr, 0);
        int count = a.getIndexCount();
        for (int i = 0; i < count; i++) {
            int index = a.getIndex(i);
            switch (index) {
                case R.styleable.OverScrollLayout_dampingFactor:
                    mFator = a.getFloat(index, DEFAULT_FATOR);
                    break;
            }
        }
        a.recycle();

        mScroller = new Scroller(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionEvent = MotionEvent.obtain(event);
                break;
            case MotionEvent.ACTION_MOVE:

                int dx = (int) (event.getRawX() - mLastMotionEvent.getRawX());
                int dy = (int) (event.getRawY() - mLastMotionEvent.getRawY());
                if (Math.abs(dx) < Math.abs(dy) && dy > 0) {

                    smoothScrollBy(0, -(int) (dy * mFator));
                }
                mLastMotionEvent = MotionEvent.obtain(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                mLastMotionEvent = null;
                smoothScrollTo(0, 0);
                break;
        }
        return true;
    }

    private void smoothScrollTo(int fx, int fy) {

        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        smoothScrollBy(dx, dy);
    }

    private void smoothScrollBy(int dx, int dy) {

        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
        super.computeScroll();
    }
}
