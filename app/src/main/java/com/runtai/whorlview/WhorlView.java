package com.runtai.whorlview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * Thread-like loading view<br>
 * Customizable attributes: color, rotation speed (X radians/s）<br>
 */
public class WhorlView extends View {
    private static final String COLOR_SPLIT = "_";

    public static final int FAST = 1;
    public static final int MEDIUM = 0;
    public static final int SLOW = 2;

    private static final int PARALLAX_FAST = 60;
    private static final int PARALLAX_MEDIUM = 72;
    private static final int PARALLAX_SLOW = 90;

    private static final long REFRESH_DURATION = 16L;

    // Current animation time
    private long mCircleTime;
    // Color of each layer
    private int[] mLayerColors;
    // spinning speed
    private final int mCircleSpeed;
    // Parallax differential
    private int mParallaxSpeed;
    // Arc length
    private final float mSweepAngle;
    // Arc width
    private final float mStrokeWidth;

    public WhorlView(Context context) {
        this(context, null, 0);
    }

    public WhorlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WhorlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // The default outer layer is the slowest 270 degrees/s
        final int defaultCircleSpeed = 270;
        final float defaultSweepAngle = 90f;
        final float defaultStrokeWidth = 5f;
        final String defaultColors = "#F44336_#4CAF50_#5677fc";
        if (attrs != null) {
            @SuppressLint("CustomViewStyleable") final TypedArray typedArray = context.obtainStyledAttributes(
                    attrs, R.styleable.whorlview_style);
            String colors = typedArray.getString(R.styleable.whorlview_style_whorlview_circle_colors);
            if (TextUtils.isEmpty(colors)) {
                colors = defaultColors;
            }
            parseStringToLayerColors(colors);
            mCircleSpeed = typedArray.getInt(R.styleable.whorlview_style_whorlview_circle_speed, defaultCircleSpeed);
            int index = typedArray.getInt(R.styleable.whorlview_style_whorlview_parallax, 0);
            setParallax(index);
            mSweepAngle = typedArray.getFloat(R.styleable.whorlview_style_whorlview_sweepAngle, defaultSweepAngle);
            if (mSweepAngle <= 0 || mSweepAngle >= 360) {
                throw new IllegalArgumentException("sweep angle out of bound");
            }
            mStrokeWidth = typedArray.getFloat(R.styleable.whorlview_style_whorlview_strokeWidth, defaultStrokeWidth);
            typedArray.recycle();
        } else {
            parseStringToLayerColors(defaultColors);
            mCircleSpeed = defaultCircleSpeed;
            mParallaxSpeed = PARALLAX_MEDIUM;
            mSweepAngle = defaultSweepAngle;
            mStrokeWidth = defaultStrokeWidth;
        }
    }

    /**
     * string Type of color segmentation and convert to color value
     *
     * @param colors Colors
     */
    private void parseStringToLayerColors(@NonNull String colors) {
        String[] colorArray = colors.split(COLOR_SPLIT);
        mLayerColors = new int[colorArray.length];
        for (int i = 0; i < colorArray.length; i++) {
            try {
                mLayerColors[i] = Color.parseColor(colorArray[i]);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("whorlview_circle_colors can not be parsed | " + ex.getLocalizedMessage());
            }
        }
    }

    private void setParallax(int index) {
        switch (index) {
            case FAST:
                mParallaxSpeed = PARALLAX_FAST;
                break;
            case MEDIUM:
                mParallaxSpeed = PARALLAX_MEDIUM;
                break;
            case SLOW:
                mParallaxSpeed = PARALLAX_SLOW;
                break;
            default:
                throw new IllegalStateException("no such parallax type");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mLayerColors.length; i++) {
            float angle = (mCircleSpeed + mParallaxSpeed * i) * mCircleTime * 0.001f;
            drawArc(canvas, i, angle);
        }
    }

    private boolean mIsCircling = false;

    /**
     * start anim
     */
    public void start() {
        mIsCircling = true;
        new Thread(() -> {
            mCircleTime = 0L;
            while (mIsCircling) {
                invalidateWrap();
                mCircleTime = mCircleTime + REFRESH_DURATION;
                try {
                    Thread.sleep(REFRESH_DURATION);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stop() {
        mIsCircling = false;
        mCircleTime = 0L;
        invalidateWrap();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void invalidateWrap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            postInvalidateOnAnimation();
        } else {
            postInvalidate();
        }
    }

    /**
     * Draw an arc
     *
     * @param canvas        Same as name
     * @param index         From the inside out
     * @param startAngle    Same as name
     */
    private void drawArc(Canvas canvas, int index, float startAngle) {
        Paint paint = checkArcPaint(index);
        // The largest circle is the boundary of the view
        RectF oval = checkRectF(index);
        canvas.drawArc(oval, startAngle, mSweepAngle, false, paint);
    }

    private Paint mArcPaint;

    private Paint checkArcPaint(int index) {
        if (mArcPaint == null) {
            mArcPaint = new Paint();
        } else {
            mArcPaint.reset();
        }
        mArcPaint.setColor(mLayerColors[index]);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mStrokeWidth);
        mArcPaint.setAntiAlias(true);
        return mArcPaint;
    }

    private RectF mOval;

    private RectF checkRectF(int index) {
        if (mOval == null) {
            mOval = new RectF();
        }
        float start = index * (mStrokeWidth + mIntervalWidth) + mStrokeWidth / 2;
        float end = getMinLength() - start;
        mOval.set(start, start, end, end);
        return mOval;
    }

    private int getMinLength() {
        return Math.min(getWidth(), getHeight());
    }

    private float mIntervalWidth;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minSize = (int) (mStrokeWidth * 4 * mLayerColors.length + mStrokeWidth);
        int wantSize = (int) (mStrokeWidth * 8 * mLayerColors.length + mStrokeWidth);
        int size = measureSize(widthMeasureSpec, wantSize, minSize);
        calculateIntervalWidth(size);
        setMeasuredDimension(size, size);
    }

    /**
     * Calculation interval size
     *
     * @param size Same as name
     */
    private void calculateIntervalWidth(int size) {
        float wantIntervalWidth = (float) (size / (mLayerColors.length * 2)) - mStrokeWidth;
        // To prevent the interval from being too large, the maximum is 3 times the arc width
        float maxIntervalWidth = mStrokeWidth * 4;
        mIntervalWidth = Math.min(wantIntervalWidth, maxIntervalWidth);
    }

    /**
     * Measure the width and height of the view
     *
     * @param measureSpec   Same as name
     * @param wantSize      Same as name
     * @param minSize       Same as name
     * @return              Pending description
     */
    public static int measureSize(int measureSpec, int wantSize, int minSize) {
        int result; // Initial value is always 0
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // The size of the view that the parent layout wants
            result = specSize;
        } else {
            result = wantSize;
            if (specMode == MeasureSpec.AT_MOST) {
                // wrap_content
                result = Math.min(result, specSize);
            }
        }
        // The measured size and the minimum size, whichever is greater
        return Math.max(result, minSize);
    }
}
