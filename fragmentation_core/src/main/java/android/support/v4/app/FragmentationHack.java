package android.support.v4.app;


import android.util.SparseArray;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * http://stackoverflow.com/questions/23504790/android-multiple-fragment-transaction-ordering
 * <p>
 * Created by YoKey on 16/1/22.
 */
public class FragmentationHack {
    private static boolean sSupportLessThan25dot4 = false;

    static {
        Field[] fields = FragmentManagerImpl.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("mAvailIndices")) {
                sSupportLessThan25dot4 = true;
                break;
            }
        }
    }

    public static boolean isExecutingActions(FragmentManager fragmentManager) {
        if (!(fragmentManager instanceof FragmentManagerImpl))
            return false;
        try {
            FragmentManagerImpl fragmentManagerImpl = (FragmentManagerImpl) fragmentManager;
            return fragmentManagerImpl.mExecutingActions;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * To fix the FragmentManagerImpl.mAvailIndices incorrect ordering when pop() multiple Fragments
     * on pre-support-v4-25.4.0
     */
    @SuppressWarnings("unchecked")
    public static void reorderIndices(FragmentManager fragmentManager) {
        if (!sSupportLessThan25dot4) return;
        if (!(fragmentManager instanceof FragmentManagerImpl))
            return;
        try {
            FragmentManagerImpl fragmentManagerImpl = (FragmentManagerImpl) fragmentManager;
            Object object = getValue(fragmentManagerImpl, "mAvailIndices");
            if (object == null) return;

            ArrayList<Integer> arrayList = (ArrayList<Integer>) object;
            if (arrayList.size() > 1) {
                Collections.sort(arrayList, Collections.reverseOrder());
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
            return fragmentManagerImpl.mStateSaved;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * On 25.4.0+，fragmentManager.getFragments () returns mAdd, instead of the mActive on 25.4.0-
     */
    @SuppressWarnings("unchecked")
    public static List<Fragment> getActiveFragments(FragmentManager fragmentManager) {
        if (!(fragmentManager instanceof FragmentManagerImpl))
            return Collections.EMPTY_LIST;
        // For pre-25.4.0
        if (sSupportLessThan25dot4) return fragmentManager.getFragments();

        // For compat 25.4.0+
        try {
            FragmentManagerImpl fragmentManagerImpl = (FragmentManagerImpl) fragmentManager;
            // Since v4-25.4.0，mActive: ArrayList -> SparseArray
            return getActiveList(fragmentManagerImpl.mActive);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fragmentManager.getFragments();
    }

    @SuppressWarnings("unchecked")
    private static List<Fragment> getActiveList(SparseArray<Fragment> active) {
        if (active == null) {
            return Collections.EMPTY_LIST;
        }
        final int count = active.size();
        ArrayList<Fragment> fragments = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            fragments.add(active.valueAt(i));
        }
        return fragments;
    }

    private static Object getValue(Object object, String fieldName) throws Exception {
        Field field;
        Class<?> clazz = object.getClass();
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) {
        }
        return null;
    }
}