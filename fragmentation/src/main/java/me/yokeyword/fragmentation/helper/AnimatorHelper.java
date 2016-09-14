package me.yokeyword.fragmentation.helper;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import me.yokeyword.fragmentation.R;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

/**
 * Created by YoKeyword on 16/7/26.
 */
public final class AnimatorHelper {
    private Animation noAnim, fixNoAnim;
    public Animation enterAnim, exitAnim, popEnterAnim, popExitAnim;

    private Context context;
    private FragmentAnimator fragmentAnimator;

    public AnimatorHelper(Context context, FragmentAnimator fragmentAnimator) {
        this.context = context;
        this.fragmentAnimator = fragmentAnimator;
        initEnterAnim();
        initExitAnim();
        initPopEnterAnim();
        initPopExitAnim();
    }

    public Animation getNoAnim() {
        if (noAnim == null) {
            noAnim = AnimationUtils.loadAnimation(context, R.anim.no_anim);
        }
        return noAnim;
    }

    public Animation getFixNoAnim() {
        if (fixNoAnim == null) {
            fixNoAnim = new Animation() {
            };
        }
        return fixNoAnim;
    }

    private Animation initEnterAnim() {
        if (fragmentAnimator.getEnter() == 0) {
            enterAnim = getNoAnim();
        } else {
            enterAnim = AnimationUtils.loadAnimation(context, fragmentAnimator.getEnter());
        }
        return enterAnim;
    }

    private Animation initExitAnim() {
        if (fragmentAnimator.getExit() == 0) {
            exitAnim = getNoAnim();
        } else {
            exitAnim = AnimationUtils.loadAnimation(context, fragmentAnimator.getExit());
        }
        return exitAnim;
    }

    private Animation initPopEnterAnim() {
        if (fragmentAnimator.getPopEnter() == 0) {
            popEnterAnim = getNoAnim();
        } else {
            popEnterAnim = AnimationUtils.loadAnimation(context, fragmentAnimator.getPopEnter());
        }
        return popEnterAnim;
    }

    private Animation initPopExitAnim() {
        if (fragmentAnimator.getPopExit() == 0) {
            // 用于解决 start新Fragment时,转场动画过程中上一个Fragment页面空白问题
            popExitAnim = AnimationUtils.loadAnimation(context, R.anim.pop_exit_no_anim);
        } else {
            popExitAnim = AnimationUtils.loadAnimation(context, fragmentAnimator.getPopExit());
        }
        return popExitAnim;
    }
}
