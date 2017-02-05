package me.yokeyword.fragmentation.debug;

import android.view.MotionEvent;
import android.view.View;

/**
 * @Hide
 * Created by YoKey on 17/2/5.
 */
public class StackViewTouchListener implements View.OnTouchListener {
    private View stackView;
    private float dX, dY = 0f;
    private float downX, downY = 0f;
    private boolean isClickState;
    private int clickLimitValue;

    public StackViewTouchListener(View stackView, int clickLimitValue) {
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
