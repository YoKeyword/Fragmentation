package me.yokeyword.fragmentation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.List;

import me.yokeyword.fragmentation_swipeback.*;
import me.yokeyword.fragmentation_swipeback.R;

/**
 * SwipeBackLayout
 * Created by YoKeyword on 16/4/19.
 */
public class SwipeBackLayout extends FrameLayout {
    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;
    private static final int FULL_ALPHA = 255;
    private static final float DEFAULT_SCROLL_THRESHOLD = 0.3f;
    private static final int OVERSCROLL_DISTANCE = 10;

    private ViewDragHelper mHelper;

    private float mScrollPercent;
    private float mScrimOpacity;

    private FragmentActivity mActivity;
    private View mContentView;
    private SupportFragment mFragment;
    private SupportFragment mPreFragment;

    private Drawable mShadowLeft;
    private Rect mTmpRect = new Rect();

    private boolean mEnable = true;

    public SwipeBackLayout(Context context) {
        this(context, null);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mHelper = ViewDragHelper.create(this, new ViewDragCallback());
        setShadow(me.yokeyword.fragmentation_swipeback.R.drawable.shadow_left);
    }

    /**
     * Set a drawable used for edge shadow.
     */
    public void setShadow(Drawable shadow) {
        mShadowLeft = shadow;
        invalidate();
    }

    /**
     * Set a drawable used for edge shadow.
     */
    public void setShadow(int resId) {
        setShadow(getResources().getDrawable(resId));
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

        mShadowLeft.setBounds(childRect.left - mShadowLeft.getIntrinsicWidth(), childRect.top, childRect.left, childRect.bottom);
        mShadowLeft.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
        mShadowLeft.draw(canvas);
    }

    private void drawScrim(Canvas canvas, View child) {
        final int baseAlpha = (DEFAULT_SCRIM_COLOR & 0xff000000) >>> 24;
        final int alpha = (int) (baseAlpha * mScrimOpacity);
        final int color = alpha << 24;

        canvas.clipRect(0, 0, child.getLeft(), getHeight());
        canvas.drawColor(color);
    }

    @Override
    public void computeScroll() {
        mScrimOpacity = 1 - mScrollPercent;
        if (mScrimOpacity >= 0) {
            if (mHelper.continueSettling(true)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
    }

    public void setFragment(SupportFragment fragment, View view) {
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

    private void setContentView(View view) {
        mContentView = view;
    }

    public void setEnableGesture(boolean enable) {
        mEnable = enable;
    }

    class ViewDragCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            boolean dragEnable = mHelper.isEdgeTouched(ViewDragHelper.EDGE_LEFT);
            if (mPreFragment == null) {
                if (dragEnable && mFragment != null) {
                    List<Fragment> fragmentList = mFragment.getFragmentManager().getFragments();
                    if (fragmentList != null && fragmentList.size() > 1) {
                        int index = fragmentList.indexOf(mFragment);
                        for (int i = index - 1; i >= 0; i--) {
                            Fragment fragment = fragmentList.get(i);
                            if (fragment != null && fragment.getView() != null) {
                                fragment.getView().setVisibility(VISIBLE);
                                mPreFragment = (SupportFragment) fragment;
                                break;
                            }
                        }
                    }
                }
            } else {
                if (mPreFragment.getView() != null) {
                    mPreFragment.getView().setVisibility(VISIBLE);
                }
            }
            return dragEnable;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return Math.min(child.getWidth(), Math.max(left, 0));
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            mScrollPercent = Math.abs((float) left / (getWidth() + mShadowLeft.getIntrinsicWidth()));
            invalidate();

            if (mScrollPercent > 1) {
                if (mFragment != null) {
                    if (mPreFragment != null) {
                        mPreFragment.mLocking = true;
                    }
                    if (!mFragment.isDetached()) {
                        mFragment.popForSwipeBack();
                    }
                    if (mPreFragment != null) {
                        mPreFragment.mLocking = false;
                    }
                } else {
                    if (!mActivity.isFinishing()) {
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
            } else {
                if (mActivity != null && mActivity.getSupportFragmentManager().getBackStackEntryCount() == 1) {
                    return 1;
                }
            }
            return 0;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            final int childWidth = releasedChild.getWidth();

            int left, top = 0;
            left = xvel > 0 || xvel == 0 && mScrollPercent > DEFAULT_SCROLL_THRESHOLD ? childWidth + mShadowLeft.getIntrinsicWidth() + OVERSCROLL_DISTANCE : 0;

            mHelper.settleCapturedViewAt(left, top);
            invalidate();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mEnable) return super.onInterceptTouchEvent(ev);
        return mHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mEnable) return super.onTouchEvent(event);
        mHelper.processTouchEvent(event);
        return true;
    }
}
