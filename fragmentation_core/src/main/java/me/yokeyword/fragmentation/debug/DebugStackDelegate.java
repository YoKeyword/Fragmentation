package me.yokeyword.fragmentation.debug;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentationHack;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation.Fragmentation;
import me.yokeyword.fragmentation.R;

/**
 * Created by YoKey on 17/6/13.
 */

public class DebugStackDelegate implements SensorEventListener {
    private FragmentActivity mActivity;
    private SensorManager mSensorManager;
    private AlertDialog mStackDialog;

    public DebugStackDelegate(FragmentActivity activity) {
        this.mActivity = activity;
    }

    public void onCreate(int mode) {
        if (mode != Fragmentation.SHAKE) return;
        mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onPostCreate(int mode) {
        if (mode != Fragmentation.BUBBLE) return;
        View root = mActivity.findViewById(android.R.id.content);
        if (root instanceof FrameLayout) {
            FrameLayout content = (FrameLayout) root;
            final ImageView stackView = new ImageView(mActivity);
            stackView.setImageResource(R.drawable.fragmentation_ic_stack);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.END;
            final int dp18 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, mActivity.getResources().getDisplayMetrics());
            params.topMargin = dp18 * 7;
            params.rightMargin = dp18;
            stackView.setLayoutParams(params);
            content.addView(stackView);
            stackView.setOnTouchListener(new StackViewTouchListener(stackView, dp18 / 4));
            stackView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFragmentStackHierarchyView();
                }
            });
        }
    }

    public void onDestroy() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        float[] values = event.values;
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            int value = 12;
            if ((Math.abs(values[0]) >= value || Math.abs(values[1]) >= value || Math.abs(values[2]) >= value)) {
                showFragmentStackHierarchyView();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * 调试相关:以dialog形式 显示 栈视图
     */
    public void showFragmentStackHierarchyView() {
        if (mStackDialog != null && mStackDialog.isShowing()) return;
        DebugHierarchyViewContainer container = new DebugHierarchyViewContainer(mActivity);
        container.bindFragmentRecords(getFragmentRecords());
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mStackDialog = new AlertDialog.Builder(mActivity)
                .setView(container)
                .setPositiveButton(android.R.string.cancel, null)
                .setCancelable(true)
                .create();
        mStackDialog.show();
    }

    /**
     * 调试相关:以log形式 打印 栈视图
     */
    public void logFragmentRecords(String tag) {
        List<DebugFragmentRecord> fragmentRecordList = getFragmentRecords();
        if (fragmentRecordList == null) return;

        StringBuilder sb = new StringBuilder();

        for (int i = fragmentRecordList.size() - 1; i >= 0; i--) {
            DebugFragmentRecord fragmentRecord = fragmentRecordList.get(i);

            if (i == fragmentRecordList.size() - 1) {
                sb.append("═══════════════════════════════════════════════════════════════════════════════════\n");
                if (i == 0) {
                    sb.append("\t栈顶\t\t\t").append(fragmentRecord.fragmentName).append("\n");
                    sb.append("═══════════════════════════════════════════════════════════════════════════════════");
                } else {
                    sb.append("\t栈顶\t\t\t").append(fragmentRecord.fragmentName).append("\n\n");
                }
            } else if (i == 0) {
                sb.append("\t栈底\t\t\t").append(fragmentRecord.fragmentName).append("\n\n");
                processChildLog(fragmentRecord.childFragmentRecord, sb, 1);
                sb.append("═══════════════════════════════════════════════════════════════════════════════════");
                Log.i(tag, sb.toString());
                return;
            } else {
                sb.append("\t↓\t\t\t").append(fragmentRecord.fragmentName).append("\n\n");
            }

            processChildLog(fragmentRecord.childFragmentRecord, sb, 1);
        }
    }

    private List<DebugFragmentRecord> getFragmentRecords() {
        List<DebugFragmentRecord> fragmentRecordList = new ArrayList<>();

        List<Fragment> fragmentList = FragmentationHack.getActiveFragments(mActivity.getSupportFragmentManager());

        if (fragmentList == null || fragmentList.size() < 1) return null;

        for (Fragment fragment : fragmentList) {
            addDebugFragmentRecord(fragmentRecordList, fragment);
        }
        return fragmentRecordList;
    }

    private void processChildLog(List<DebugFragmentRecord> fragmentRecordList, StringBuilder sb, int childHierarchy) {
        if (fragmentRecordList == null || fragmentRecordList.size() == 0) return;

        for (int j = 0; j < fragmentRecordList.size(); j++) {
            DebugFragmentRecord childFragmentRecord = fragmentRecordList.get(j);
            for (int k = 0; k < childHierarchy; k++) {
                sb.append("\t\t\t");
            }
            if (j == 0) {
                sb.append("\t子栈顶\t\t").append(childFragmentRecord.fragmentName).append("\n\n");
            } else if (j == fragmentRecordList.size() - 1) {
                sb.append("\t子栈底\t\t").append(childFragmentRecord.fragmentName).append("\n\n");
                processChildLog(childFragmentRecord.childFragmentRecord, sb, ++childHierarchy);
                return;
            } else {
                sb.append("\t↓\t\t\t").append(childFragmentRecord.fragmentName).append("\n\n");
            }

            processChildLog(childFragmentRecord.childFragmentRecord, sb, childHierarchy);
        }
    }

    private List<DebugFragmentRecord> getChildFragmentRecords(Fragment parentFragment) {
        List<DebugFragmentRecord> fragmentRecords = new ArrayList<>();

        List<Fragment> fragmentList = FragmentationHack.getActiveFragments(parentFragment.getChildFragmentManager());
        if (fragmentList == null || fragmentList.size() < 1) return null;

        for (int i = fragmentList.size() - 1; i >= 0; i--) {
            Fragment fragment = fragmentList.get(i);
            addDebugFragmentRecord(fragmentRecords, fragment);
        }
        return fragmentRecords;
    }

    private void addDebugFragmentRecord(List<DebugFragmentRecord> fragmentRecords, Fragment fragment) {
        if (fragment != null) {
            int backStackCount = fragment.getFragmentManager().getBackStackEntryCount();
            CharSequence name = fragment.getClass().getSimpleName();
            if (backStackCount == 0) {
                name = span(name);
            } else {
                for (int j = 0; j < backStackCount; j++) {
                    FragmentManager.BackStackEntry entry = fragment.getFragmentManager().getBackStackEntryAt(j);
                    if (entry.getName().equals(fragment.getClass().getName())) {
                        break;
                    }
                    if (j == backStackCount - 1) {
                        name = span(name);
                    }
                }
            }
            fragmentRecords.add(new DebugFragmentRecord(name, getChildFragmentRecords(fragment)));
        }
    }

    @NonNull
    private CharSequence span(CharSequence name) {
        name = name + " *";
        return name;
    }

    private class StackViewTouchListener implements View.OnTouchListener {
        private View stackView;
        private float dX, dY = 0f;
        private float downX, downY = 0f;
        private boolean isClickState;
        private int clickLimitValue;

        StackViewTouchListener(View stackView, int clickLimitValue) {
            this.stackView = stackView;
            this.clickLimitValue = clickLimitValue;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float X = event.getRawX();
            float Y = event.getRawY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isClickState = true;
                    downX = X;
                    downY = Y;
                    dX = stackView.getX() - event.getRawX();
                    dY = stackView.getY() - event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(X - downX) < clickLimitValue && Math.abs(Y - downY) < clickLimitValue && isClickState) {
                        isClickState = true;
                    } else {
                        isClickState = false;
                        stackView.setX(event.getRawX() + dX);
                        stackView.setY(event.getRawY() + dY);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (X - downX < clickLimitValue && isClickState) {
                        stackView.performClick();
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }
}
