package pers.chartiose.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class LogView extends ScrollView {
    private TextView mLogView;
    private static final Handler gMainThreadHandler = new Handler(Looper.getMainLooper());
    @SuppressLint("ConstantLocale")
    private static final SimpleDateFormat gSimpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS ", Locale.getDefault());

    public LogView(Context context) {
        super(context);
        init();
    }

    public LogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mLogView = new TextView(getContext());
        addView(mLogView, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    }

    public void d(String tag, String message) {
        print(Log.DEBUG, tag, message);
    }

    public void e(String tag, String message) {
        print(Log.ERROR, tag, message);
    }

    public void i(String tag, String message) {
        print(Log.INFO, tag, message);
    }

    public void w(String tag, String message) {
        print(Log.WARN, tag, message);
    }

    public void v(String tag, String message) {
        print(Log.VERBOSE, tag, message);
    }

    private void print(int priority, String tag, String text) {
        Log.println(priority, tag, text);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(gSimpleDateFormat.format(new Date()))
                .append(Long.toString(Thread.currentThread().getId()))
                .append(" ")
                .append(tag)
                .append(": ")
                .append(text)
                .append("\n");

        switch (priority) {
            case Log.WARN:
            case Log.ERROR:
                spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.RED), 0, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                break;
            default:
                spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                break;
        }

        gMainThreadHandler.post(() -> print(spannableStringBuilder));
    }

    public void clear() {
        gMainThreadHandler.post(() -> mLogView.setText(null));
    }

    private void print(SpannableStringBuilder text) {
        mLogView.append(text);
    }
}
