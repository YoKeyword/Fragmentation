package me.yokeyword.fragmentation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentationMagician;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation_swipeback.core.ISwipeBackActivity;

/**
 * Thx https://github.com/ikew0ng/SwipeBackLayout.
 * <p>
 * Created by YoKey on 16/4/19.
 */
public class SwipeBackLayout extends FrameLayout {
    /**
     * Edge flag indicating that the left edge should be affected.
     */
    public static final int EDGE_LEFT = ViewDragHelper.EDGE_LEFT;

    /**
     * Edge flag indicating that the right edge should be affected.
     */
    public static final int EDGE_RIGHT = ViewDragHelper.EDGE_RIGHT;

    public static final int EDGE_ALL = EDGE_LEFT | EDGE_RIGHT;


    /**
     * A view is not currently being dragged or animating as a result of a
     * fling/snap.
     */
    public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;

    /**
     * A view is currently being dragged. The position is currently changing as
     * a result of user input or simulated user input.
     */
    public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;

    /**
     * A view is currently settling into place as a result of a fling or
     * predefined non-interactive motion.
     */
    public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;

    /**
     * A view is currently drag finished.
     */
    public static final int STATE_FINISHED = 3;

    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;
    private static final float DEFAULT_PARALLAX = 0.33f;
    private static final int FULL_ALPHA = 255;
    private static final float DEFAULT_SCROLL_THRESHOLD = 0.4f;
    private static final int OVERSCROLL_DISTANCE = 10;

    private float mScrollFinishThreshold = DEFAULT_SCROLL_THRESHOLD;

    private ViewDragHelper mHelper;

    private float mScrollPercent;
    private float mScrimOpacity;

    private FragmentActivity mActivity;
    private View mContentView;
    private ISupportFragment mFragment;
    private Fragment mPreFragment;

    private Drawable mShadowLeft;
    private Drawable mShadowRight;
    private Rect mTmpRect = new Rect();

    private int mEdgeFlag;
    private boolean mEnable = true;
    private int mCurrentSwipeOrientation;
    private float mParallaxOffset = DEFAULT_PARALLAX;

    private boolean mCallOnDestroyView;

    private boolean mInLayout;

    private int mContentLeft;
    private int mContentTop;

    /**
     * The set of listeners to be sent events through.
     */
    private List<OnSwipeListener> mListeners;

    private Context mContext;

    public enum EdgeLevel {
        MAX, MIN, MED
    }

    public SwipeBackLayout(Context context) {
        this(context, null);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    private void init() {
        mHelper = ViewDragHelper.create(this, new ViewDragCallback());
        setShadow(me.yokeyword.fragmentation_swipeback.R.drawable.shadow_left, EDGE_LEFT);
        setEdgeOrientation(EDGE_LEFT);
    }

    /**
     * Get ViewDragHelper
     */
    public ViewDragHelper getViewDragHelper() {
        return mHelper;
    }

    /**
     * Set scroll threshold, we will close the activity, when scrollPercent over
     * this value
     *
     * @param threshold
     */
    public void setScrollThresHold(float threshold) {
        if (threshold >= 1.0f || threshold <= 0) {
            throw new IllegalArgumentException("Threshold value should be between 0 and 1.0");
        }
        mScrollFinishThreshold = threshold;
    }

    public void setParallaxOffset(float offset) {
        this.mParallaxOffset = offset;
    }

    /**
     * Enable edge tracking for the selected edges of the parent view.
     * The callback's {@link ViewDragHelper.Callback#onEdgeTouched(int, int)} and
     * {@link ViewDragHelper.Callback#onEdgeDragStarted(int, int)} methods will only be invoked
     * for edges for which edge tracking has been enabled.
     *
     * @param orientation Combination of edge flags describing the edges to watch
     * @see #EDGE_LEFT
     * @see #EDGE_RIGHT
     */
    public void setEdgeOrientation(@EdgeOrientation int orientation) {
        mEdgeFlag = orientation;
        mHelper.setEdgeTrackingEnabled(orientation);

        if (orientation == EDGE_RIGHT || orientation == EDGE_ALL) {
            setShadow(me.yokeyword.fragmentation_swipeback.R.drawable.shadow_right, EDGE_RIGHT);
        }
    }

    @IntDef({EDGE_LEFT, EDGE_RIGHT, EDGE_ALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EdgeOrientation {
    }

    /**
     * Set a drawable used for edge shadow.
     */
    public void setShadow(Drawable shadow, int edgeFlag) {
        if ((edgeFlag & EDGE_LEFT) != 0) {
            mShadowLeft = shadow;
        } else if ((edgeFlag & EDGE_RIGHT) != 0) {
            mShadowRight = shadow;
        }
        invalidate();
    }

    /**
     * Set a drawable used for edge shadow.
     */
    public void setShadow(int resId, int edgeFlag) {
        setShadow(getResources().getDrawable(resId), edgeFlag);
    }

    /**
     * Add a callback to be invoked when a swipe event is sent to this view.
     *
     * @param listener the swipe listener to attach to this view
     */
    public void addSwipeListener(OnSwipeListener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        mListeners.add(listener);
    }

    /**
     * Removes a listener from the set of listeners
     *
     * @param listener
     */
    public void removeSwipeListener(OnSwipeListener listener) {
        if (mListeners == null) {
            return;
        }
        mListeners.remove(listener);
    }

    public interface OnSwipeListener {
        /**
         * Invoke when state change
         *
         * @param state flag to describe scroll state
         * @see #STATE_IDLE
         * @see #STATE_DRAGGING
         * @see #STATE_SETTLING
         * @see #STATE_FINISHED
         */
        void onDragStateChange(int state);

        /**
         * Invoke when edge touched
         *
         * @param oritentationEdgeFlag edge flag describing the edge being touched
         * @see #EDGE_LEFT
         * @see #EDGE_RIGHT
         */
        void onEdgeTouch(int oritentationEdgeFlag);

        /**
         * Invoke when scroll percent over the threshold for the first time
         *
         * @param scrollPercent scroll percent of this view
         */
        void onDragScrolled(float scrollPercent);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean isDrawView = child == mContentView;
        boolean drawChild = super.drawChild(canvas, child, drawingTime);
        if (isDrawView && mScrimOpacity > 0 && mHelper.getViewDragState() != ViewDragHelper.STATE_IDLE) {
            drawShadow(canvas, child);
            drawScrim(canvas, child);
        }
        return drawChild;
    }

    private void drawShadow(Canvas canvas, View child) {
        final Rect childRect = mTmpRect;
        child.getHitRect(childRect);

        if ((mCurrentSwipeOrientation & EDGE_LEFT) != 0) {
            mShadowLeft.setBounds(childRect.left - mShadowLeft.getIntrinsicWidth(), childRect.top, childRect.left, childRect.bottom);
            mShadowLeft.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadowLeft.draw(canvas);
        } else if ((mCurrentSwipeOrientation & EDGE_RIGHT) != 0) {
            mShadowRight.setBounds(childRect.right, childRect.top, childRect.right + mShadowRight.getIntrinsicWidth(), childRect.bottom);
            mShadowRight.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadowRight.draw(canvas);
        }
    }

    private void drawScrim(Canvas canvas, View child) {
        final int baseAlpha = (DEFAULT_SCRIM_COLOR & 0xff000000) >>> 24;
        final int alpha = (int) (baseAlpha * mScrimOpacity);
        final int color = alpha << 24;

        if ((mCurrentSwipeOrientation & EDGE_LEFT) != 0) {
            canvas.clipRect(0, 0, child.getLeft(), getHeight());
        } else if ((mCurrentSwipeOrientation & EDGE_RIGHT) != 0) {
            canvas.clipRect(child.getRight(), 0, getRight(), getHeight());
        }
        canvas.drawColor(color);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mInLayout = true;
        if (mContentView != null) {
            mContentView.layout(mContentLeft, mContentTop,
                    mContentLeft + mContentView.getMeasuredWidth(),
                    mContentTop + mContentView.getMeasuredHeight());
        }
        mInLayout = false;
    }

    @Override
    public void requestLayout() {
        if (!mInLayout) {
            super.requestLayout();
        }
    }

    @Override
    public void computeScroll() {
        mScrimOpacity = 1 - mScrollPercent;
        if (mScrimOpacity >= 0) {
            if (mHelper.continueSettling(true)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }

            if (mPreFragment != null && mPreFragment.getView() != null) {
                if (mCallOnDestroyView) {
                    mPreFragment.getView().setX(0);
                    return;
                }

                if (mHelper.getCapturedView() != null) {
                    int leftOffset = (int) ((mHelper.getCapturedView().getLeft() - getWidth()) * mParallaxOffset * mScrimOpacity);
                    mPreFragment.getView().setX(leftOffset > 0 ? 0 : leftOffset);
                }
            }
        }
    }

    /**
     * hide
     */
    public void internalCallOnDestroyView() {
        mCallOnDestroyView = true;
    }

    public void setFragment(final ISupportFragment fragment, View view) {
        this.mFragment = fragment;
        mContentView = view;
    }

    public void hiddenFragment() {
        if (mPreFragment != null && mPreFragment.getView() != null) {
            mPreFragment.getView().setVisibility(GONE);
        }
    }

    public void attachToActivity(FragmentActivity activity) {
        mActivity = activity;
        TypedArray a = activity.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.windowBackground
        });
        int background = a.getResourceId(0, 0);
        a.recycle();

        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
        decorChild.setBackgroundResource(background);
        decor.removeView(decorChild);
        addView(decorChild);
        setContentView(decorChild);
        decor.addView(this);
    }

    public void attachToFragment(ISupportFragment fragment, View view) {
        addView(view);
        setFragment(fragment, view);
    }

    private void setContentView(View view) {
        mContentView = view;
    }

    public void setEnableGesture(boolean enable) {
        mEnable = enable;
    }

    public void setEdgeLevel(EdgeLevel edgeLevel) {
        validateEdgeLevel(-1, edgeLevel);
    }

    public void setEdgeLevel(int widthPixel) {
        validateEdgeLevel(widthPixel, null);
    }

    private void validateEdgeLevel(int widthPixel, EdgeLevel edgeLevel) {
        try {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(metrics);
            Field mEdgeSize = mHelper.getClass().getDeclaredField("mEdgeSize");
            mEdgeSize.setAccessible(true);
            if (widthPixel >= 0) {
                mEdgeSize.setInt(mHelper, widthPixel);
            } else {
                if (edgeLevel == EdgeLevel.MAX) {
                    mEdgeSize.setInt(mHelper, metrics.widthPixels);
                } else if (edgeLevel == EdgeLevel.MED) {
                    mEdgeSize.setInt(mHelper, metrics.widthPixels / 2);
                } else {
                    mEdgeSize.setInt(mHelper, ((int) (20 * metrics.density + 0.5f)));
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            boolean dragEnable = mHelper.isEdgeTouched(mEdgeFlag, pointerId);
            if (dragEnable) {
                if (mHelper.isEdgeTouched(EDGE_LEFT, pointerId)) {
                    mCurrentSwipeOrientation = EDGE_LEFT;
                } else if (mHelper.isEdgeTouched(EDGE_RIGHT, pointerId)) {
                    mCurrentSwipeOrientation = EDGE_RIGHT;
                }

                if (mListeners != null) {
                    for (OnSwipeListener listener : mListeners) {
                        listener.onEdgeTouch(mCurrentSwipeOrientation);
                    }
                }

                if (mPreFragment == null) {
                    if (mFragment != null) {
                        List<Fragment> fragmentList = FragmentationMagician.getActiveFragments(((Fragment) mFragment).getFragmentManager());
                        if (fragmentList != null && fragmentList.size() > 1) {
                            int index = fragmentList.indexOf(mFragment);
                            for (int i = index - 1; i >= 0; i--) {
                                Fragment fragment = fragmentList.get(i);
                                if (fragment != null && fragment.getView() != null) {
                                    fragment.getView().setVisibility(VISIBLE);
                                    mPreFragment = fragment;
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    View preView = mPreFragment.getView();
                    if (preView != null && preView.getVisibility() != VISIBLE) {
                        preView.setVisibility(VISIBLE);
                    }
                }
            }
            return dragEnable;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int ret = 0;
            if ((mCurrentSwipeOrientation & EDGE_LEFT) != 0) {
                ret = Math.min(child.getWidth(), Math.max(left, 0));
            } else if ((mCurrentSwipeOrientation & EDGE_RIGHT) != 0) {
                ret = Math.min(0, Math.max(left, -child.getWidth()));
            }
            return ret;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);

            if ((mCurrentSwipeOrientation & EDGE_LEFT) != 0) {
                mScrollPercent = Math.abs((float) left / (mContentView.getWidth() + mShadowLeft.getIntrinsicWidth()));
            } else if ((mCurrentSwipeOrientation & EDGE_RIGHT) != 0) {
                mScrollPercent = Math.abs((float) left / (mContentView.getWidth() + mShadowRight.getIntrinsicWidth()));
            }
            mContentLeft = left;
            mContentTop = top;
            invalidate();

            if (mListeners != null && mHelper.getViewDragState() == STATE_DRAGGING && mScrollPercent <= 1 && mScrollPercent > 0) {
                for (OnSwipeListener listener : mListeners) {
                    listener.onDragScrolled(mScrollPercent);
                }
            }

            if (mScrollPercent > 1) {
                if (mFragment != null) {
                    if (mCallOnDestroyView) return;

                    if (!((Fragment) mFragment).isDetached()) {
                        onDragFinished();
                        mFragment.getSupportDelegate().popQuiet();
                    }
                } else {
                    if (!mActivity.isFinishing()) {
                        onDragFinished();
                        mActivity.finish();
                        mActivity.overridePendingTransition(0, 0);
                    }
                }
            }
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            if (mFragment != null) {
                return 1;
            }
            if (mActivity instanceof ISwipeBackActivity && ((ISwipeBackActivity) mActivity).swipeBackPriority()) {
                return 1;
            }
            return 0;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            final int childWidth = releasedChild.getWidth();

            int left = 0, top = 0;
            if ((mCurrentSwipeOrientation & EDGE_LEFT) != 0) {
                left = xvel > 0 || xvel == 0 && mScrollPercent > mScrollFinishThreshold ? (childWidth
                        + mShadowLeft.getIntrinsicWidth() + OVERSCROLL_DISTANCE) : 0;
            } else if ((mCurrentSwipeOrientation & EDGE_RIGHT) != 0) {
                left = xvel < 0 || xvel == 0 && mScrollPercent > mScrollFinishThreshold ? -(childWidth
                        + mShadowRight.getIntrinsicWidth() + OVERSCROLL_DISTANCE) : 0;
            }

            mHelper.settleCapturedViewAt(left, top);
            invalidate();
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            if (mListeners != null) {
                for (OnSwipeListener listener : mListeners) {
                    listener.onDragStateChange(state);
                }
            }
        }

        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
            super.onEdgeTouched(edgeFlags, pointerId);
            if ((mEdgeFlag & edgeFlags) != 0) {
                mCurrentSwipeOrientation = edgeFlags;
            }
        }
    }

    private void onDragFinished() {
        if (mListeners != null) {
            for (OnSwipeListener listener : mListeners) {
                listener.onDragStateChange(STATE_FINISHED);
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mEnable) return super.onInterceptTouchEvent(ev);
        try {
            return mHelper.shouldInterceptTouchEvent(ev);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mEnable) return super.onTouchEvent(event);
        try {
            mHelper.processTouchEvent(event);
            return true;
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return false;
    }
}
