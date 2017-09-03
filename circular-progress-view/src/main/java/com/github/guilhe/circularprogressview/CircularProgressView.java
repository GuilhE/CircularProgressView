package com.github.guilhe.circularprogressview;

import android.animation.Animator;
import android.animation.FloatEvaluator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by gdelgado on 30/08/2017.
 */
@SuppressWarnings("unused")
public class CircularProgressView extends View {

    private static final String TAG = CircularProgressView.class.getSimpleName();

    private static final float DEFAULT_VIEW_PADDING_DP = 10;
    private static final float DEFAULT_SHADOW_PADDING_DP = 5;
    private static final float DEFAULT_STROKE_THICKNESS_DP = 10;
    private static final int DEFAULT_MAX_WIDTH_DP = 100;
    private static final int DEFAULT_MAX = 100;
    private static final int DEFAULT_STARTING_ANGLE = 275;
    private static final int DEFAULT_ANIMATION_MILLIS = 1000;
    private static final int DEFAULT_PROGRESS_COLOR = Color.BLACK;
    private static final float DEFAULT_BACKGROUND_ALPHA = 0.3f;
    private static final TimeInterpolator DEFAULT_INTERPOLATOR = new DecelerateInterpolator();

    private int mMax;
    private boolean mShadowEnabled;
    private int mStartingAngle;
    private float mProgress;
    private float mProgressStrokeThickness;
    private int mProgressColor;
    private int mBackgroundColor;

    private RectF mProgressRectF;
    private RectF mShadowRectF;
    private Outline mOutline;
    private Paint mBackgroundPaint;
    private Paint mProgressPaint;
    private Paint mShadowPaint;
    private int mLastValidRawMeasuredDim;
    private float mLastValidStrokeThickness;

    private TimeInterpolator mInterpolator;
    private Animator mProgressAnimator;

    public CircularProgressView(Context context) {
        super(context);
        init(context, null);
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircularProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CircularProgressView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mLastValidStrokeThickness = DEFAULT_STROKE_THICKNESS_DP;
        mInterpolator = DEFAULT_INTERPOLATOR;
        mProgressRectF = new RectF();
        mShadowRectF = new RectF();

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setStyle(Paint.Style.STROKE);

        if (attrs != null) {
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircularProgressView, 0, 0);
            try {
                mMax = typedArray.getInt(R.styleable.CircularProgressView_max, DEFAULT_MAX);
                mShadowEnabled = typedArray.getBoolean(R.styleable.CircularProgressView_shadow, true);
                mStartingAngle = typedArray.getInteger(R.styleable.CircularProgressView_startingAngle, DEFAULT_STARTING_ANGLE);
                mProgress = typedArray.getFloat(R.styleable.CircularProgressView_progress, 0);
                mProgressStrokeThickness = typedArray.getDimension(R.styleable.CircularProgressView_progressBarThickness, convertDpToPx(context, DEFAULT_STROKE_THICKNESS_DP));
                mProgressColor = typedArray.getInt(R.styleable.CircularProgressView_progressBarColor, DEFAULT_PROGRESS_COLOR);
                mBackgroundColor = typedArray.getInt(R.styleable.CircularProgressView_backgroundColor, mProgressColor);
            } finally {
                typedArray.recycle();
            }
        } else {
            mProgressStrokeThickness = convertDpToPx(context, DEFAULT_STROKE_THICKNESS_DP);
            mShadowEnabled = true;
            mMax = DEFAULT_MAX;
            mStartingAngle = DEFAULT_STARTING_ANGLE;
            mProgressColor = DEFAULT_PROGRESS_COLOR;
            mBackgroundColor = mProgressColor;
        }

        mBackgroundPaint.setColor(adjustAlpha(mBackgroundColor, DEFAULT_BACKGROUND_ALPHA));
        mProgressPaint.setColor(mProgressColor);
        mShadowPaint.setColor(adjustAlpha(Color.BLACK, 0.2f));
        setThickness(mProgressStrokeThickness, false);
    }

    /**
     * Either width or height, this view will use Math.min(width, height) value.
     * If an invalid size is set it won't take effect and a last valid size will be used.
     * Check {@link #onMeasure(int, int)}
     *
     * @param size in pixels
     */
    public void setSize(int size) {
        getLayoutParams().height = size;
        requestLayout();
    }

    /**
     * This method changes the progress bar starting angle.
     * The default value is 275 and it's equivalent to 12 o'clock.
     *
     * @param angle where the progress bar starts.
     */
    public void setStartingAngle(int angle) {
        mStartingAngle = angle;
        invalidate();
    }

    public int getStartingAngle() {
        return mStartingAngle;
    }

    /**
     * Sets progress bar max value (100%)
     *
     * @param max value
     */
    public void setMax(int max) {
        mMax = max;
        invalidate();
    }

    public int getMax() {
        return mMax;
    }

    public void setColor(int color) {
        setProgressColor(color);
        setBackgroundColor(color);
    }

    public void setColorResource(@ColorRes int resId) {
        setColor(ContextCompat.getColor(getContext(), resId));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setColor(Color color) {
        setColor(color.toArgb());
    }

    public void setProgressColor(int color) {
        mProgressColor = color;
        if (mBackgroundColor == -1) {
            setBackgroundColor(color);
        }
        mProgressPaint.setColor(color);
        invalidate();
    }

    public void setProgressColorResource(@ColorRes int resId) {
        setProgressColor(ContextCompat.getColor(getContext(), resId));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setProgressColor(Color color) {
        setProgressColor(color.toArgb());
    }

    public int getProgressColor() {
        return mProgressColor;
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        mBackgroundPaint.setColor(adjustAlpha(color, DEFAULT_BACKGROUND_ALPHA));
        invalidate();
    }

    public void setShadowColorResource(@ColorRes int resId) {
        setBackgroundColor(ContextCompat.getColor(getContext(), resId));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setBackgroundColor(Color color) {
        setBackgroundColor(color.toArgb());
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setShadowEnabled(boolean enable) {
        mShadowEnabled = enable;
        invalidate();
    }

    public boolean isShadowEnabled() {
        return mShadowEnabled;
    }

    /**
     * Changes progressBar, background and shadow line width.
     *
     * @param thickness in pixels
     */
    public void setProgressStrokeThickness(float thickness) {
        setThickness(thickness, true);
    }

    private void setThickness(float thickness, boolean requestLayout) {
        mProgressStrokeThickness = thickness;
        mBackgroundPaint.setStrokeWidth(mProgressStrokeThickness);
        mProgressPaint.setStrokeWidth(mProgressStrokeThickness);
        mShadowPaint.setStrokeWidth(mProgressStrokeThickness);
        if (requestLayout) {
            requestLayout();
        }
    }

    public float getProgressStrokeThickness() {
        return mProgressStrokeThickness;
    }

    public void setProgress(float progress) {
        setProgress(progress, false);
    }

    public void setProgress(float progress, boolean animate) {
        setProgress(progress, animate, DEFAULT_ANIMATION_MILLIS);
    }

    public void setProgress(float progress, boolean animate, long duration) {
        setProgress(progress, animate, duration, true);
    }

    public float getProgress() {
        return mProgress;
    }

    public void resetProgress() {
        setProgress(0);
    }

    public void resetProgress(boolean animate) {
        resetProgress(animate, DEFAULT_ANIMATION_MILLIS);
    }

    public void resetProgress(boolean animate, long duration) {
        setProgress(0, animate, duration, false);
    }

    public void setAnimationInterpolator(TimeInterpolator interpolator) {
        mInterpolator = interpolator == null ? DEFAULT_INTERPOLATOR : interpolator;
    }

    private void setProgress(float progress, boolean animate, long duration, boolean clockwise) {
        if (animate) {
            if (mProgressAnimator != null) {
                mProgressAnimator.cancel();
            }
            mProgressAnimator = getAnimator(getProgress(), clockwise ? progress : 0, duration, new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    setProgressValue(((Float) valueAnimator.getAnimatedValue()));
                }
            });
            mProgressAnimator.start();
        } else {
            setProgressValue(progress);
        }
    }

    private void setProgressValue(float value) {
        mProgress = value;
        invalidate();
    }

    private ValueAnimator getAnimator(double current, double next, long duration, ValueAnimator.AnimatorUpdateListener updateListener) {
        ValueAnimator animator = new ValueAnimator();
        animator.setInterpolator(mInterpolator);
        animator.setDuration(duration);
        animator.setObjectValues(current, next);
        animator.setEvaluator(new FloatEvaluator() {
            public Integer evaluate(float fraction, float startValue, float endValue) {
                return Math.round(startValue + (endValue - startValue) * fraction);
            }
        });
        animator.addUpdateListener(updateListener);
        return animator;
    }

    /**
     * Changes color's alpha by the factor
     *
     * @param color  The color to change alpha
     * @param factor 1.0f (solid) to 0.0f (transparent)
     * @return int - A color with modified alpha
     */
    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED ?
                MeasureSpec.getSize(heightMeasureSpec) : convertDpToPx(getContext(), DEFAULT_MAX_WIDTH_DP);

        int rawMeasuredDim = Math.max(Math.min(width, height), 0);
        float arcDim = mProgressStrokeThickness + convertDpToPx(getContext(), DEFAULT_VIEW_PADDING_DP);
        mProgressRectF.set(arcDim, arcDim, rawMeasuredDim - arcDim, rawMeasuredDim - arcDim);

        //To avoid creating a messy composition
        if (mProgressRectF.width() <= mProgressStrokeThickness) {
            rawMeasuredDim = mLastValidRawMeasuredDim;
            mProgressRectF.set(arcDim, arcDim, rawMeasuredDim - arcDim, rawMeasuredDim - arcDim);
            setThickness(mLastValidStrokeThickness, false);
        }
        mLastValidRawMeasuredDim = rawMeasuredDim;
        mLastValidStrokeThickness = mProgressStrokeThickness;

        int shadowPadding = convertDpToPx(getContext(), DEFAULT_SHADOW_PADDING_DP);
        mShadowRectF.set(mProgressRectF.left, shadowPadding + mProgressRectF.top, mProgressRectF.right, shadowPadding + mProgressRectF.bottom);
        setMeasuredDimension(rawMeasuredDim, rawMeasuredDim);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float angle = 360 * mProgress / mMax;
        if (mShadowEnabled) {
            canvas.drawArc(mShadowRectF, mStartingAngle, angle, false, mShadowPaint);
        }
        canvas.drawOval(mProgressRectF, mBackgroundPaint);
        canvas.drawArc(mProgressRectF, mStartingAngle, angle, false, mProgressPaint);
    }

    private static int convertDpToPx(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}