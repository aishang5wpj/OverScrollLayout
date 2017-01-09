package com.xiaohongshu.dampingscrolling;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.Toast;

/**
 * Created by wupengjian on 17/1/7.
 */
public class DampingScrollViewGroup extends FrameLayout {

    private static final int HOVER_TAP_SLOP = 20;
    private static final int DIRECTION_LEFT = 0x0001;
    private static final int DIRECTION_TOP = 0x0010;
    private static final int DIRECTION_RIGHT = 0x0100;
    private static final int DIRECTION_BOTTOM = 0x1000;
    private static final int DIRECTION_DEFAULT = DIRECTION_TOP;
    private static final float DEFAULT_FATOR = 1;

    /**
     * 允许产生阻尼效果的方向
     */
    private int mTargetDirection;
    /**
     * 阻尼因子
     */
    private float mFator = DEFAULT_FATOR;
    private Scroller mScroller;
    /**
     * 记录上一次触摸事件
     */
    private MotionEvent mLastMotionEvent, mLastInterceptMotionEvent;
    /**
     * 本次事件流产生的阻尼方向
     */
    private int mLastDirection;
    private OnDampingCallback mOnDampingCallback;

    public DampingScrollViewGroup(Context context) {
        this(context, null);
    }

    public DampingScrollViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DampingScrollViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.DampingScrollViewGroup, defStyleAttr, 0);
        int count = a.getIndexCount();
        for (int i = 0; i < count; i++) {
            int index = a.getIndex(i);
            switch (index) {
                case R.styleable.DampingScrollViewGroup_dampingDirection:
                    mTargetDirection |= a.getInt(index, DIRECTION_DEFAULT);
                    break;
                case R.styleable.DampingScrollViewGroup_dampingFactor:
                    mFator = a.getFloat(index, DEFAULT_FATOR);
                    break;
            }
        }
        a.recycle();

        mScroller = new Scroller(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        boolean intercept;
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            intercept = false;
        } else {
            if (null == mOnDampingCallback) {

                mOnDampingCallback = try2GetOnDampingCallback();
            }
            intercept = isMoving(ev, mLastInterceptMotionEvent) && mOnDampingCallback.needDamping(ev, mLastInterceptMotionEvent);
        }
        mLastInterceptMotionEvent = MotionEvent.obtain(ev);
        return intercept;
    }

    /**
     * 确保当前是滚动事件,而不是点击
     *
     * @param newMotionEvent
     * @param oldMotionEvent
     * @return
     */
    private boolean isMoving(MotionEvent newMotionEvent, MotionEvent oldMotionEvent) {

        int dx = (int) (newMotionEvent.getRawX() - oldMotionEvent.getRawX());
        int dy = (int) (newMotionEvent.getRawY() - oldMotionEvent.getRawY());
        return Math.abs(dx) > HOVER_TAP_SLOP || Math.abs(dy) > HOVER_TAP_SLOP;
    }

    private OnDampingCallback try2GetOnDampingCallback() {
        View child = getChildAt(0);
        if (child instanceof ViewPager) {
            return new ViewPagerDampingCallback((ViewPager) child);
        } else if (child instanceof ScrollView) {
            return new ScrollViewDampingCallback((ScrollView) child);
        } else if (child instanceof HorizontalScrollView) {
            return new HorizontalScrollViewDampingCallback((HorizontalScrollView) child);
        } else if (child instanceof RecyclerView) {
            return new RecyclerViewDampingCallback((RecyclerView) child);
        }
        return new SimpleDampingCallback();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        int currentDirection = -1;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionEvent = MotionEvent.obtain(event);
                break;
            case MotionEvent.ACTION_MOVE:

                if (null == mLastMotionEvent) {
                    //MotionEvent被子View消费后,是走不到Action_down的
                    mLastMotionEvent = mLastInterceptMotionEvent;
                }

                int dx = (int) (event.getRawX() - mLastMotionEvent.getRawX());
                int dy = (int) (event.getRawY() - mLastMotionEvent.getRawY());
                //如果水平方向的位移大于竖直方向,则只处理水平方向上的阻尼效果
                if (Math.abs(dx) > Math.abs(dy)) {
                    //处理左边的阻尼效果
                    if (dx > 0 && (mTargetDirection & DIRECTION_LEFT) == DIRECTION_LEFT) {

                        v("左边");
                        currentDirection = DIRECTION_LEFT;
                    }
                    //处理右边的阻尼效果
                    else if (dx < 0 && (mTargetDirection & DIRECTION_RIGHT) == DIRECTION_RIGHT) {

                        v("右边");
                        currentDirection = DIRECTION_RIGHT;
                    }
                    //避免水平方向的阻尼效果产生后又产生一个树枝方向的阻尼效果
                    boolean isHorizontal = mLastDirection == DIRECTION_LEFT || mLastDirection == DIRECTION_RIGHT;
                    boolean isDirectionEnable = currentDirection != -1 && (mTargetDirection & currentDirection) == currentDirection;
                    if (mLastDirection == 0 || isHorizontal && isDirectionEnable) {

                        mLastDirection = currentDirection;
                        smoothScrollBy(-(int) (dx * mFator), 0);
                    }
                } else {
                    //否则处理竖直方向上的效果
                    //处理顶部阻尼
                    if (dy > 0 && (mTargetDirection & DIRECTION_TOP) == DIRECTION_TOP) {

                        v("顶部");
                        currentDirection = DIRECTION_TOP;
                    }
                    //处理底部阻尼
                    else if (dy < 0 && (mTargetDirection & DIRECTION_BOTTOM) == DIRECTION_BOTTOM) {

                        v("底部");
                        currentDirection = DIRECTION_BOTTOM;
                    }
                    //避免阻尼效果产生后又产生一个不同方向的阻尼效果
                    boolean isVertical = mLastDirection == DIRECTION_TOP || mLastDirection == DIRECTION_BOTTOM;
                    boolean isDirectionEnable = currentDirection != -1 && (mTargetDirection & currentDirection) == currentDirection;
                    if (mLastDirection == 0 || isVertical && isDirectionEnable) {

                        mLastDirection = currentDirection;
                        smoothScrollBy(0, -(int) (dy * mFator));
                    }
                }
                mLastMotionEvent = MotionEvent.obtain(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                mLastDirection = 0;
                mLastMotionEvent = null;
                smoothScrollTo(0, 0);
                break;
        }
        return true;
    }

    private void smoothScrollTo(int fx, int fy) {

        int dx = fx - mScroller.getFinalX();
        int dy = fx - mScroller.getFinalY();
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

    protected void v(String msg) {
        final String text = msg;
        if (!TextUtils.isEmpty(text)) {
            Log.v("damping_log", text);
        }
    }

    public static boolean isMoving2Left(MotionEvent newMotionEvent, MotionEvent oldMotionEvent) {

        int dx = (int) (newMotionEvent.getRawX() - oldMotionEvent.getRawX());
        int dy = (int) (newMotionEvent.getRawY() - oldMotionEvent.getRawY());
        return dx > 0 && Math.abs(dx) > Math.abs(dy);
    }

    public static boolean isMoving2Top(MotionEvent newMotionEvent, MotionEvent oldMotionEvent) {

        int dx = (int) (newMotionEvent.getRawX() - oldMotionEvent.getRawX());
        int dy = (int) (newMotionEvent.getRawY() - oldMotionEvent.getRawY());
        return dy > 0 && Math.abs(dx) < Math.abs(dy);
    }

    public static boolean isMoving2Right(MotionEvent newMotionEvent, MotionEvent oldMotionEvent) {

        int dx = (int) (newMotionEvent.getRawX() - oldMotionEvent.getRawX());
        int dy = (int) (newMotionEvent.getRawY() - oldMotionEvent.getRawY());
        return dx < 0 && Math.abs(dx) > Math.abs(dy);
    }

    public static boolean isMoving2Bottom(MotionEvent newMotionEvent, MotionEvent oldMotionEvent) {

        int dx = (int) (newMotionEvent.getRawX() - oldMotionEvent.getRawX());
        int dy = (int) (newMotionEvent.getRawY() - oldMotionEvent.getRawY());
        return dy < 0 && Math.abs(dx) < Math.abs(dy);
    }

    public void setOnDampingCallback(OnDampingCallback callback) {
        mOnDampingCallback = callback;
    }

    public interface OnDampingCallback {

        boolean needDamping(MotionEvent newMotionEvent, MotionEvent oldMotionEvent);
    }

    /**
     * Created by wupengjian on 17/1/9.
     */
    public static class SimpleDampingCallback implements OnDampingCallback {

        @Override
        public boolean needDamping(MotionEvent newMotionEvent, MotionEvent oldMotionEvent) {
            return true;
        }
    }

    /**
     * Created by wupengjian on 17/1/9.
     */
    public static class ViewPagerDampingCallback implements OnDampingCallback {

        private ViewPager mViewPager;

        public ViewPagerDampingCallback(ViewPager viewPager) {

            mViewPager = viewPager;
        }

        @Override
        public boolean needDamping(MotionEvent newMotionEvent, MotionEvent oldMotionEvent) {

            if (null == newMotionEvent || null == oldMotionEvent) {
                return false;
            }
            //左边是否需要处理阻尼效果
            boolean isLeftDamping = isMoving2Left(newMotionEvent, oldMotionEvent) && mViewPager.getCurrentItem() == 0;
            //右边是否需要处理阻尼效果
            boolean isRightDamping = isMoving2Right(newMotionEvent, oldMotionEvent) && mViewPager.getCurrentItem() == mViewPager.getAdapter().getCount() - 1;
            //左边或右边需要处理阻尼效果
            return isLeftDamping || isRightDamping;
        }
    }

    /**
     * Created by wupengjian on 17/1/9.
     */
    public static class ScrollViewDampingCallback implements OnDampingCallback {

        private ScrollView mScrollView;

        public ScrollViewDampingCallback(ScrollView scrollView) {

            mScrollView = scrollView;
        }

        @Override
        public boolean needDamping(MotionEvent newMotionEvent, MotionEvent oldMotionEvent) {

            if (null == newMotionEvent || null == oldMotionEvent) {
                return false;
            }
            boolean isTopDamping = isMoving2Top(newMotionEvent, oldMotionEvent) && mScrollView.getScrollY() == 0;
            View childView = mScrollView.getChildAt(0);
            boolean isBottomDamping = isMoving2Bottom(newMotionEvent, oldMotionEvent)
                    && childView != null && childView.getMeasuredHeight() <= mScrollView.getScrollY() + mScrollView.getHeight();
            return isTopDamping || isBottomDamping;
        }
    }

    /**
     * Created by wupengjian on 17/1/9.
     */
    public static class HorizontalScrollViewDampingCallback implements OnDampingCallback {

        private HorizontalScrollView mScrollView;

        public HorizontalScrollViewDampingCallback(HorizontalScrollView scrollView) {

            mScrollView = scrollView;
        }

        @Override
        public boolean needDamping(MotionEvent newMotionEvent, MotionEvent oldMotionEvent) {

            if (null == newMotionEvent || null == oldMotionEvent) {
                return false;
            }
            boolean isLeftDamping = isMoving2Left(newMotionEvent, oldMotionEvent) && mScrollView.getScrollX() == 0;
            View childView = mScrollView.getChildAt(0);
            boolean isRightDamping = isMoving2Right(newMotionEvent, oldMotionEvent)
                    && childView != null && childView.getMeasuredWidth() <= mScrollView.getScrollX() + mScrollView.getWidth();
            return isLeftDamping || isRightDamping;
        }
    }


    /**
     * Created by wupengjian on 17/1/9.
     */
    public static class RecyclerViewDampingCallback implements OnDampingCallback {

        private RecyclerView mRecyclerView;

        public RecyclerViewDampingCallback(RecyclerView recyclerView) {

            mRecyclerView = recyclerView;
        }

        @Override
        public boolean needDamping(MotionEvent newMotionEvent, MotionEvent oldMotionEvent) {

            if (null == newMotionEvent || null == oldMotionEvent) {
                return false;
            }
            boolean isTopDamping = isMoving2Top(newMotionEvent, oldMotionEvent) && !(ViewCompat.canScrollVertically(mRecyclerView, -1));
            boolean isBottomDamping = isMoving2Bottom(newMotionEvent, oldMotionEvent) && !ViewCompat.canScrollVertically(mRecyclerView, 1);
            return isTopDamping || isBottomDamping;
        }
    }
}
