package android.support.v4.app;


import java.lang.reflect.Field;
import java.util.Collections;

/**
 * http://stackoverflow.com/questions/23504790/android-multiple-fragment-transaction-ordering
 */
public class FragmentTransactionBugFixHack {
    private static boolean mNeedHack;

    static {
        Field[] fields = FragmentManagerImpl.class.getDeclaredFields();
        boolean supportLessThan25dot4 = false;
        for (Field field : fields) {
            if (field.getName().equals("mAvailIndices")) {
                supportLessThan25dot4 = true;
                break;
            }
        }
        mNeedHack = supportLessThan25dot4;
    }

    public static void reorderIndices(FragmentManager fragmentManager) {
        if (!mNeedHack) return;

        if (!(fragmentManager instanceof FragmentManagerImpl))
            return;
        try {
            FragmentManagerImpl fragmentManagerImpl = (FragmentManagerImpl) fragmentManager;
            if (fragmentManagerImpl.mAvailIndices != null && fragmentManagerImpl.mAvailIndices.size() > 1) {
                Collections.sort(fragmentManagerImpl.mAvailIndices, Collections.reverseOrder());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isStateSaved(FragmentManager fragmentManager) {
        if (!(fragmentManager instanceof FragmentManagerImpl))
            return false;
        try {
            FragmentManagerImpl fragmentManagerImpl = (FragmentManagerImpl) fragmentManager;
            // 从5年前一直到当前的Support-25.0.1,该字段没有变化过
            return fragmentManagerImpl.mStateSaved;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}