package android.support.v4.app;


import java.util.Collections;

/**
 * http://stackoverflow.com/questions/23504790/android-multiple-fragment-transaction-ordering
 */
public class FragmentTransactionBugFixHack {

    public static void reorderIndices(FragmentManager fragmentManager) {
        if (!(fragmentManager instanceof FragmentManagerImpl))
            return;
        try {
            FragmentManagerImpl fragmentManagerImpl = (FragmentManagerImpl) fragmentManager;
            if (fragmentManagerImpl.mAvailIndices != null && fragmentManagerImpl.mAvailIndices.size() > 1) {
//                System.out.println("排序前-->" + fragmentManagerImpl.mAvailIndices);
                Collections.sort(fragmentManagerImpl.mAvailIndices, Collections.reverseOrder());
//                System.out.println("排序后-->" + fragmentManagerImpl.mAvailIndices);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}