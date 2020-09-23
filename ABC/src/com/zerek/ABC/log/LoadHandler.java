package com.zerek.ABC.log;

import android.os.Handler;

// For simplification
public class LoadHandler extends Handler {
    public static final int whatProgress = 1;
    public static final int whatComplete = 2;

    private long size;
    private long total;
    private int rate;

    public LoadHandler() {
        super();
    }

    private boolean isComplete() {
        if (total == 0)
            return false;
        return total >= size;
    }

    public void checkProgress(long iSize, FileDesc fileDesc) {
        size += iSize;

        if (fileDesc != null)
            total += fileDesc.size;

        int newRate = size == 0 ? 0 : (int) (total * 100 / size);
        boolean bComplete  = isComplete();
        if (newRate - rate > 3 || bComplete) {
            rate = newRate;
            sendMessage(obtainMessage(bComplete ? whatComplete : whatProgress, rate, 0, fileDesc));
        }
    }
}
