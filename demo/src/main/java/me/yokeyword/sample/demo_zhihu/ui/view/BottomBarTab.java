package me.yokeyword.sample.demo_zhihu.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import me.yokeyword.sample.R;

/**
 * Created by YoKeyword on 16/6/3.
 */
public class BottomBarTab extends FrameLayout {
    private ImageView mIcon;
    private Context mContext;
    private int mTabPosition = -1;

    public BottomBarTab(Context context, @DrawableRes int icon) {
        this(context, null, icon);
    }

    public BottomBarTab(Context context, AttributeSet attrs, int icon) {
        this(context, attrs, 0, icon);
    }

    public BottomBarTab(Context context, AttributeSet attrs, int defStyleAttr, int icon) {
        super(context, attrs, defStyleAttr);
        init(context, icon);
    }

    private void init(Context context, int icon) {
        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{R.attr.selectableItemBackgroundBorderless});
        Drawable drawable = typedArray.getDrawable(0);
        setBackgroundDrawable(drawable);
        typedArray.recycle();

        mIcon = new ImageView(context);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 27, getResources().getDisplayMetrics());
        LayoutParams params = new LayoutParams(size, size);
        params.gravity = Gravity.CENTER;
        mIcon.setImageResource(icon);
        mIcon.setLayoutParams(params);
        mIcon.setColorFilter(ContextCompat.getColor(context, R.color.tab_unselect));
        addView(mIcon);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            mIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else {
            mIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.tab_unselect));
        }
    }

    public void setTabPosition(int position) {
        mTabPosition = position;
        if (position == 0) {
            setSelected(true);
        }
    }

    public int getTabPosition() {
        return mTabPosition;
    }
}
