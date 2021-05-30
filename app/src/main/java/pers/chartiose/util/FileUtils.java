package pers.chartiose.util;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FileUtils {
    private static final String TAG = "FileUtils";

    public interface Callback {
        void onProgress(int Progress, int maxProgress);
    }

    public static void deleteDuplicatedFiles(Context context, Uri treeDocumentUri) {
        deleteDuplicatedFiles(context, treeDocumentUri);
    }

    public static int deleteDuplicatedFiles(Context context, Uri treeDocumentUri, Callback callback) {
        if (context == null || treeDocumentUri == null) {
            Log.e(TAG, "deleteDuplicatedFiles: illegal argument, context:" + context + ", treeDocumentUri:" + treeDocumentUri);
            throw new IllegalArgumentException();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !DocumentsContract.isTreeUri(treeDocumentUri)) {
            Log.e(TAG, "deleteDuplicatedFiles: treeDocumentUri is not a tree document uri");
            throw new IllegalArgumentException("is not a tree document uri");
        }

        DocumentFile directory = DocumentFile.fromTreeUri(context, treeDocumentUri);
        if (directory == null) {
            Log.e(TAG, "deleteDuplicatedFiles: fromTreeUri return null");
            return 0;
        }

        long startTime = SystemClock.elapsedRealtime();
        ExecutorService executorService = new ThreadPoolExecutor(0, 16,
                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        DocumentFile[] files = directory.listFiles();
        Set<String> md5Set = new ConcurrentSkipListSet<>();
        AtomicInteger progress = new AtomicInteger(0);
        AtomicInteger deletedFilesCount = new AtomicInteger(0);

        for (DocumentFile file : files) {
            if (!file.isFile()) {
                continue;
            }

            executorService.submit(() -> {
                InputStream inputStream = null;
                try {
                    inputStream = context.getContentResolver().openInputStream(file.getUri());
                    if (!md5Set.add(MD5Utils.md5(inputStream))) {
                        Log.d(TAG, "deleteDuplicatedFiles, delete " + file.getName());
                        file.delete();
                        deletedFilesCount.incrementAndGet();
                    }
                } catch (Exception exception) {
                    Log.e(TAG, "exception: " + exception);
                    exception.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException ioException) {
                            Log.e(TAG, "ioException: " + ioException);
                            ioException.printStackTrace();
                        }
                    }
                }
                if (callback != null) {
                    callback.onProgress(progress.incrementAndGet(), files.length);
                }
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "deleteDuplicatedFiles, cost:" + (SystemClock.elapsedRealtime() - startTime) + "ms");
        return deletedFilesCount.get();
    }

}
