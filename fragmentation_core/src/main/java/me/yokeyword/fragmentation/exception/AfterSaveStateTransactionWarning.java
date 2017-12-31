package me.yokeyword.fragmentation.exception;

import android.util.Log;

/**
 * Perform the transaction action after onSaveInstanceState.
 * <p>
 * This is dangerous because the action can
 * be lost if the activity needs to later be restored from its state.
 * <p>
 * <p>
 * If you don't want to lost the action:
 * <p>
 * //    // ReceiverActivity or Fragment:
 * //    void start() {
 * //        startActivityForResult(new Intent(this, SenderActivity.class), 100);
 * //    }
 * //
 * //    @Override
 * //    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 * //        super.onActivityResult(requestCode, resultCode, data);
 * //        if (requestCode == 100 && resultCode == 100) {
 * //            // begin transaction
 * //        }
 * //    }
 * //
 * //    // SenderActivity or Fragment:
 * //    void do(){ // Let ReceiverActivity（or Fragment）begin transaction
 * //        setResult(100);
 * //        finish();
 * //    }
 * <p>
 * Created by YoKey on 17/12/26.
 */
public class AfterSaveStateTransactionWarning extends RuntimeException {

    public AfterSaveStateTransactionWarning(String action) {
        super("Warning: Perform this " + action + " action after onSaveInstanceState!");
        Log.w("Fragmentation", getMessage());
    }

}
