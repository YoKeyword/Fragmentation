package me.yokeyword.fragmentation;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import me.yokeyword.fragmentation.helper.ExceptionHandler;

/**
 * Created by YoKey on 17/2/5.
 */
public class Fragmentation {
    /**
     * Dont display stack view.
     */
    public static final int NONE = 0;
    /**
     * Shake it to display stack view.
     */
    public static final int SHAKE = 1;
    /**
     * As a bubble display stack view.
     */
    public static final int BUBBLE = 2;

    static volatile Fragmentation INSTANCE;

    private boolean debug;
    private int mode = BUBBLE;
    private ExceptionHandler handler;

    @IntDef({NONE, SHAKE, BUBBLE})
    @Retention(RetentionPolicy.SOURCE)
    @interface StackViewMode {
    }

    public static Fragmentation getDefault() {
        if (INSTANCE == null) {
            synchronized (Fragmentation.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Fragmentation(new FragmentationBuilder());
                }
            }
        }
        return INSTANCE;
    }

    Fragmentation(FragmentationBuilder builder) {
        debug = builder.debug;
        if (debug) {
            mode = builder.mode;
        } else {
            mode = NONE;
        }
        handler = builder.handler;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public ExceptionHandler getHandler() {
        return handler;
    }

    public void setHandler(ExceptionHandler handler) {
        this.handler = handler;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(@StackViewMode int mode) {
        this.mode = mode;
    }

    public static FragmentationBuilder builder() {
        return new FragmentationBuilder();
    }

    public static class FragmentationBuilder {
        private boolean debug;
        private int mode;
        private ExceptionHandler handler;

        /**
         * @param debug Suppressed Exception("Can not perform this action after onSaveInstanceState!") when debug=false
         */
        public FragmentationBuilder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        /**
         * Sets the mode to display the stack view
         * <p>
         * None if debug(false).
         * <p>
         * Default:NONE
         */
        public FragmentationBuilder stackViewMode(@StackViewMode int mode) {
            this.mode = mode;
            return this;
        }

        /**
         * @param handler Handled Exception("Can not perform this action after onSaveInstanceState!") when debug=false.
         */
        public FragmentationBuilder handleException(ExceptionHandler handler) {
            this.handler = handler;
            return this;
        }

        public Fragmentation install() {
            Fragmentation.INSTANCE = new Fragmentation(this);
            return Fragmentation.INSTANCE;
        }
    }
}
