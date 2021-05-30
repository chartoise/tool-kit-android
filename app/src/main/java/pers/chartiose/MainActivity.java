package pers.chartiose;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicBoolean;

import pers.chartiose.tool.kit.R;
import pers.chartiose.util.FileUtils;
import pers.chartiose.view.LogView;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private LogView mLogView = null;
    private ProgressBar mProgressBar = null;
    private TextView mTextViewProgress = null;
    private final AtomicBoolean mWorking = new AtomicBoolean(false);
    private static final Handler gMainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestPermissions(new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 0);
        }

        mLogView = findViewById(R.id.log_view);
        mProgressBar = findViewById(R.id.progress_bar);
        mTextViewProgress = findViewById(R.id.text_view_progress);
        findViewById(R.id.button_chose_directory).setOnClickListener(view -> {
            if (mWorking.get()) {
                Toast.makeText(this, "Please wait until the work is over", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 0);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLogView.d(TAG, "onActivityResult, requestCode:" + requestCode + ", resultCode:" + resultCode + ", data:" + data);
        if (data == null) {
            mLogView.e(TAG, "onActivityResult, data is null");
            return;
        }
        mLogView.d(TAG, "uri:" + data.getData());
        mWorking.set(true);
        new Thread(() -> {
            int deletedFilesCount = FileUtils.deleteDuplicatedFiles(this, data.getData(), this::updateProgress);
            gMainThreadHandler.post(() -> {
                Toast.makeText(this, "Finish deleting " + deletedFilesCount + " duplicated files", Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Finish deleting " + deletedFilesCount + " duplicated files")
                        .setPositiveButton("OK", null)
                        .create()
                        .show();
            });
            cancelProgress();
            mWorking.set(false);
        }).start();
    }

    @SuppressLint("SetTextI18n")
    private void updateProgress(int progress, int maxProgress) {
        gMainThreadHandler.post(() -> {
            if (mProgressBar.getMax() != maxProgress) {
                mProgressBar.setMax(maxProgress);
            }
            mProgressBar.setProgress(progress);
            if (!mProgressBar.isShown()) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            mTextViewProgress.setText(progress + "/" + maxProgress);
            if (!mTextViewProgress.isShown()) {
                mTextViewProgress.setVisibility(View.VISIBLE);
            }
        });
    }

    private void cancelProgress() {
        gMainThreadHandler.post(() -> {
            mProgressBar.setProgress(0);
            mProgressBar.setVisibility(View.GONE);
            mTextViewProgress.setVisibility(View.GONE);
        });
    }
}