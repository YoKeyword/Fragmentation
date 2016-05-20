package me.yokeyword.fragmentation.debug;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import java.util.List;

import me.yokeyword.fragmentation.R;

/**
 * Created by YoKeyword on 16/2/21.
 */
public class DebugHierarchyViewContainer extends ScrollView {
    private Context mContext;

    private LinearLayout mLinearLayout;

    private int mItemHeight;
    private int mPadding;

    public DebugHierarchyViewContainer(Context context) {
        super(context);
        initView(context);
    }

    public DebugHierarchyViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public DebugHierarchyViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        mLinearLayout = new LinearLayout(context);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        addView(mLinearLayout);

        mItemHeight = dip2px(50);
        mPadding = dip2px(16);
    }

    private int dip2px(float dp) {
        float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void bindFragmentRecords(List<DebugFragmentRecord> fragmentRecords) {
        mLinearLayout.removeAllViews();

        if (fragmentRecords == null) return;

        DebugHierarchyViewContainer.this.setView(fragmentRecords, 0, null);
    }

    private void setView(final List<DebugFragmentRecord> fragmentRecordList, final int hierarchy, final TextView tvItem) {
        for (int i = fragmentRecordList.size() - 1; i >= 0; i--) {
            DebugFragmentRecord child = fragmentRecordList.get(i);
            int tempHierarchy = hierarchy;

            final TextView childTvItem;
            childTvItem = getTextView(child, tempHierarchy);
            childTvItem.setTag(R.id.hierarchy, tempHierarchy);

            final List<DebugFragmentRecord> childFragmentRecord = child.childFragmentRecord;
            if (childFragmentRecord != null && childFragmentRecord.size() > 0) {
                tempHierarchy++;
                childTvItem.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_right, 0, 0, 0);
                final int finalChilHierarchy = tempHierarchy;
                childTvItem.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v.getTag(R.id.isexpand) != null) {
                            boolean isExpand = (boolean) v.getTag(R.id.isexpand);
                            if (isExpand) {
                                childTvItem.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_right, 0, 0, 0);
                                DebugHierarchyViewContainer.this.removeView(finalChilHierarchy);
                            } else {
                                handleExpandView(childFragmentRecord, finalChilHierarchy, childTvItem);

                            }
                            v.setTag(R.id.isexpand, !isExpand);
                        } else {
                            childTvItem.setTag(R.id.isexpand, true);
                            handleExpandView(childFragmentRecord, finalChilHierarchy, childTvItem);
                        }
                    }
                });
            } else {
                childTvItem.setPadding(childTvItem.getPaddingLeft() + mPadding, 0, mPadding, 0);
            }

            if (tvItem == null) {
                mLinearLayout.addView(childTvItem);
            } else {
                mLinearLayout.addView(childTvItem, mLinearLayout.indexOfChild(tvItem) + 1);
            }
        }
    }

    private void handleExpandView(List<DebugFragmentRecord> childFragmentRecord, int finalChilHierarchy, TextView childTvItem) {
        DebugHierarchyViewContainer.this.setView(childFragmentRecord, finalChilHierarchy, childTvItem);
        childTvItem.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_expandable, 0, 0, 0);
    }

    private void removeView(int hierarchy) {
        int size = mLinearLayout.getChildCount();
        for (int i = size - 1; i >= 0; i--) {
            View view = mLinearLayout.getChildAt(i);
            if ((int) view.getTag(R.id.hierarchy) >= hierarchy) {
                mLinearLayout.removeView(view);
            }
        }
    }

    private TextView getTextView(DebugFragmentRecord fragmentRecord, int hierarchy) {
        TextView tvItem = new TextView(mContext);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mItemHeight);
        tvItem.setLayoutParams(params);
        if (hierarchy == 0) {
            tvItem.setTextColor(Color.BLACK);
            tvItem.setTextSize(16);
        }
        tvItem.setGravity(Gravity.CENTER_VERTICAL);
        tvItem.setPadding((int) (mPadding + hierarchy * mPadding * 1.5), 0, mPadding, 0);
        tvItem.setCompoundDrawablePadding(mPadding / 2);

        TypedArray a = mContext.obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground});
        tvItem.setBackgroundDrawable(a.getDrawable(0));
        a.recycle();

        tvItem.setText(fragmentRecord.fragmentName);

        return tvItem;
    }
}
