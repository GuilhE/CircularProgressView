package com.github.guilhe.views;

import android.animation.Animator;
import android.animation.FloatEvaluator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.*;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gdelgado on 30/08/2017.
 */
@SuppressWarnings("unused")
public class CircularProgressView extends View {

    private static final String TAG = CircularProgressView.class.getSimpleName();

    private static final float ANGLE_OFFSET_FOR_MULTIPLE_ARC_PROGRESS = 6;
    private static final float DEFAULT_VIEW_PADDING_DP = 10;
    private static final float DEFAULT_SHADOW_PADDING_DP = 5;
    private static final float DEFAULT_STROKE_THICKNESS_DP = 10;
    private static final int DEFAULT_MAX_WIDTH_DP = 100;
    private static final int DEFAULT_MAX = 100;
    private static final int DEFAULT_STARTING_ANGLE = 270;
    private static final int DEFAULT_ANIMATION_MILLIS = 1000;
    private static final int DEFAULT_PROGRESS_COLOR = Color.BLACK;
    private static final float DEFAULT_BACKGROUND_ALPHA = 0.3f;
    private static final TimeInterpolator DEFAULT_INTERPOLATOR = new DecelerateInterpolator();

    private final float mDefaultViewPadding = dpToPx(DEFAULT_VIEW_PADDING_DP);
    private final float mDefaultShadowPadding = dpToPx(DEFAULT_SHADOW_PADDING_DP);
    private final float mDefaultStrokeThickness = dpToPx(DEFAULT_STROKE_THICKNESS_DP);
    private final int mDefaultMaxWidth = dpToPx(DEFAULT_MAX_WIDTH_DP);

    private int mMax;
    private boolean mShadowEnabled;
    private boolean mProgressThumbEnabled;
    private int mStartingAngle;
    private boolean mMultipleArcsEnabled;
    private float mProgressListTotal;
    private ArrayList<Float> mProgressList = new ArrayList<>();
    private ArrayList<Paint> mProgressPaintList = new ArrayList<>();
    private float mProgress;
    private float mProgressStrokeThickness;
    private float mProgressIconThickness;
    private int mProgressColor;
    private int mBackgroundColor;
    private boolean mBackgroundAlphaEnabled;
    private boolean mReverseEnabled;
    private boolean mProgressRounded;
    private List<Float> mValuesToDrawList = new ArrayList<>();

    private RectF mProgressRectF;
    private RectF mShadowRectF;
    private Paint mBackgroundPaint;
    private Paint mProgressPaint;
    private Paint mShadowPaint;
    private int mLastValidRawMeasuredDim;
    private float mLastValidStrokeThickness;

    private TimeInterpolator mInterpolator;
    private Animator mProgressAnimator;
    private OnProgressChangeAnimationCallback mCallback;

    public interface OnProgressChangeAnimationCallback {
        void onProgressChanged(float progress);

        void onAnimationFinished(float progress);
    }

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
        mLastValidStrokeThickness = mDefaultStrokeThickness;
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
                mProgressThumbEnabled = typedArray.getBoolean(R.styleable.CircularProgressView_progressThumb, false);
                mStartingAngle = typedArray.getInteger(R.styleable.CircularProgressView_startingAngle, DEFAULT_STARTING_ANGLE);
                mProgress = typedArray.getFloat(R.styleable.CircularProgressView_progress, 0);
                mProgressStrokeThickness = typedArray.getDimension(R.styleable.CircularProgressView_progressBarThickness, mDefaultStrokeThickness);
                mProgressColor = typedArray.getInt(R.styleable.CircularProgressView_progressBarColor, DEFAULT_PROGRESS_COLOR);
                mProgressRounded = typedArray.getBoolean(R.styleable.CircularProgressView_progressBarRounded, false);
                mBackgroundColor = typedArray.getInt(R.styleable.CircularProgressView_backgroundColor, mProgressColor);
                mBackgroundAlphaEnabled = typedArray.getBoolean(R.styleable.CircularProgressView_backgroundAlphaEnabled, true);
                mReverseEnabled = typedArray.getBoolean(R.styleable.CircularProgressView_reverse, false);
            } finally {
                typedArray.recycle();
            }
        } else {
            mProgressStrokeThickness = mDefaultStrokeThickness;
            mShadowEnabled = true;
            mMax = DEFAULT_MAX;
            mStartingAngle = DEFAULT_STARTING_ANGLE;
            mProgressColor = DEFAULT_PROGRESS_COLOR;
            mBackgroundColor = mProgressColor;
            mBackgroundAlphaEnabled = true;
            mReverseEnabled = false;
            mProgressRounded = false;
        }

        resetBackgroundPaint();
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setStrokeCap(mProgressRounded ? Paint.Cap.ROUND : Paint.Cap.SQUARE);
        mShadowPaint.setColor(adjustAlpha(Color.BLACK, 0.2f));
        mShadowPaint.setStrokeCap(mProgressPaint.getStrokeCap());
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
     * The default value is 270 and it's equivalent to 12 o'clock.
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

    /**
     * Changes progress and background color
     *
     * @param color - Color
     */
    public void setColor(int color) {
        setProgressColor(color);
        setBackgroundColor(color);
    }

    /**
     * You can simulate the use of this method with by calling {@link #setColor(int)} with ContextCompat:
     * setBackgroundColor(ContextCompat.getColor(resId));
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setColorResource(@ColorRes int resId) {
        setColor(getContext().getColor(resId));
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

    /**
     * You can simulate the use of this method with by calling {@link #setProgressColor(int)} with ContextCompat:
     * setProgressColor(ContextCompat.getColor(resId));
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setProgressColorResource(@ColorRes int resId) {
        setProgressColor(getContext().getColor(resId));
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
        resetBackgroundPaint();
        invalidate();
    }

    public void setBackgroundAlphaEnabled(boolean enabled) {
        mBackgroundAlphaEnabled = enabled;
        resetBackgroundPaint();
        invalidate();
    }

    public boolean isBackgroundAlphaEnabled() {
        return mBackgroundAlphaEnabled;
    }

    public void setReverseEnabled(boolean enabled) {
        mReverseEnabled = enabled;
        invalidate();
    }

    public boolean isReverseEnabled() {
        return mReverseEnabled;
    }

    public boolean isProgressRounded() {
        return mProgressRounded;
    }

    public void setProgressRounded(boolean enabled) {
        mProgressRounded = enabled;
        mProgressPaint.setStrokeCap(mProgressRounded ? Paint.Cap.ROUND : Paint.Cap.SQUARE);
        mShadowPaint.setStrokeCap(mProgressPaint.getStrokeCap());
        invalidate();
    }

    /**
     * You can simulate the use of this method with by calling {@link #setBackgroundColor(int)} with ContextCompat:
     * setBackgroundColor(ContextCompat.getColor(resId));
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setShadowColorResource(@ColorRes int resId) {
        setBackgroundColor(getContext().getColor(resId));
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

    public void setProgressThumbEnabled(boolean enable) {
        mProgressThumbEnabled = enable;
        invalidate();
    }

    public boolean isProgressThumbEnabled() {
        return mProgressThumbEnabled;
    }

    /**
     * Changes progressBar & progressIcon, background and shadow line width.
     *
     * @param thickness in pixels
     */
    public void setProgressStrokeThickness(float thickness) {
        setThickness(thickness, true);
    }

    private void setThickness(float thickness, boolean requestLayout) {
        mProgressStrokeThickness = thickness;
        mProgressIconThickness = mProgressStrokeThickness / 2;
        mBackgroundPaint.setStrokeWidth(mProgressStrokeThickness);
        mProgressPaint.setStrokeWidth(mProgressStrokeThickness);
        if (mProgressPaintList != null) {
            for (Paint paint : mProgressPaintList) {
                paint.setStrokeWidth(mProgressStrokeThickness);
            }
        }
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

    /**
     * This method will activate the "multiple-arc-progress" and disable the progress thumb, progress round and background.
     * This method disables the "single-arc-progress".
     *
     * @param progressList      - list containing all the progress "step-per-arc". Their sum most be less or equal to {@link #getMax()}.
     * @param progressColorList - list containing the progress "step-per-arc" color. If progressColorList.size() is less than progressList.size(), Color.TRANSPARENT will be used for the missing colors.
     * @throws RuntimeException - will be thrown if progress entities sum is greater than max value.
     */
    public void setProgress(@NonNull List<Float> progressList, @NonNull List<Integer> progressColorList) throws RuntimeException {
        setProgressRounded(false);
        mProgress = mProgressListTotal = 0;
        for (float value : progressList) {
            mProgressListTotal += value;
            if (mProgressListTotal > mMax) {
                throw new RuntimeException(String.format("Progress entities sum (%s) is greater than max value (%s)", mProgressListTotal, mMax));
            }
        }

        mMultipleArcsEnabled = true;
        mProgressList = new ArrayList<>(progressList);
        mProgressPaintList = new ArrayList<>();
        for (int i = 0; i < mProgressList.size(); i++) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(i < progressColorList.size() ? progressColorList.get(i) : Color.TRANSPARENT);
            mProgressPaintList.add(paint);
        }
        setThickness(mProgressStrokeThickness, false);
        invalidate();
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

    public void setProgressAnimationCallback(OnProgressChangeAnimationCallback callback) {
        mCallback = callback;
    }

    private void resetBackgroundPaint() {
        mBackgroundPaint.setColor(mBackgroundAlphaEnabled ? adjustAlpha(mBackgroundColor, DEFAULT_BACKGROUND_ALPHA) : mBackgroundColor);
    }

    /**
     * This method will activate the "single-arc-progress" and enable the progress thumb and background.
     * This method disables the "multiple-arc-progress".
     */
    private void setProgress(float progress, boolean animate, long duration, boolean clockwise) {
        mMultipleArcsEnabled = false;
        if (animate) {
            if (mProgressAnimator != null) {
                mProgressAnimator.cancel();
            }
            mProgressAnimator = getAnimator(getProgress(), clockwise ? progress : 0, duration, new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    setProgressValue(((Float) valueAnimator.getAnimatedValue()));
                    if (mCallback != null) {
                        mCallback.onProgressChanged(mProgress);
                    }
                }
            });
            mProgressAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mCallback != null) {
                        mCallback.onAnimationFinished(mProgress);
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    //not in use
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    //not in use
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    //not in use
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
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED ? MeasureSpec.getSize(heightMeasureSpec) : mDefaultMaxWidth;

        int rawMeasuredDim = Math.max(Math.min(width, height), 0);
        float arcDim = mProgressStrokeThickness + mDefaultViewPadding;
        mProgressRectF.set(arcDim, arcDim, rawMeasuredDim - arcDim, rawMeasuredDim - arcDim);

        //To avoid creating a messy composition
        if (mProgressRectF.width() <= mProgressStrokeThickness) {
            rawMeasuredDim = mLastValidRawMeasuredDim;
            mProgressRectF.set(arcDim, arcDim, rawMeasuredDim - arcDim, rawMeasuredDim - arcDim);
            setThickness(mLastValidStrokeThickness, false);
        }
        mLastValidRawMeasuredDim = rawMeasuredDim;
        mLastValidStrokeThickness = mProgressStrokeThickness;

        mShadowRectF.set(mProgressRectF.left, mDefaultShadowPadding + mProgressRectF.top, mProgressRectF.right, mDefaultShadowPadding + mProgressRectF.bottom);
        setMeasuredDimension(rawMeasuredDim, rawMeasuredDim);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Either we are using "single-arc-progress" or "multiple-arc-progress".
        mValuesToDrawList.clear();
        if (!mMultipleArcsEnabled) {
            mValuesToDrawList.add(mProgress);
            mProgressPaintList.clear();
            mProgressPaintList.add(mProgressPaint);
        } else {
            mValuesToDrawList.addAll(mProgressList);
        }

        float angle;
        float previousAngle = mStartingAngle;
        float radius = getWidth() / 2 - mDefaultViewPadding - mProgressIconThickness - mProgressStrokeThickness / 2;
        double endX, endY;

        //Shadow logic
        if (mShadowEnabled) {
            angle = 360 * (mMultipleArcsEnabled ? mProgressListTotal : mProgress) / mMax;
            if (mReverseEnabled) {
                angle *= -1;
            }
            if (!mMultipleArcsEnabled && mProgressThumbEnabled) {
                //Only in "single-arc-progress", otherwise we'll end up with N thumbs

                //Who doesn't love a bit of math? :)
                //cos(a) = adj / hyp <>cos(angle) = x / radius <>x = cos(angle) * radius
                //sin(a) = opp / hyp <>sin(angle) = y / radius <>y = sin(angle) * radius
                //x = cos(startingAngle + progressAngle) * radius + originX(center)
                //y = sin(startingAngle + progressAngle) * radius + originY(center)
                endX = (Math.cos(Math.toRadians(previousAngle + angle)) * radius);
                endY = (Math.sin(Math.toRadians(previousAngle + angle)) * radius);
                canvas.drawCircle((float) endX + mShadowRectF.centerX(), (float) endY + mShadowRectF.centerY(), mProgressIconThickness, mShadowPaint);
            }
            canvas.drawArc(mShadowRectF, previousAngle, angle, false, mShadowPaint);
        }

        //Progress logic
        for (int i = 0; i < mValuesToDrawList.size(); i++) {
            if (!mMultipleArcsEnabled) {
                //No background will be used when "multiple-arc-progress" is enable because it will be mixed with the "progress-colors"
                canvas.drawOval(mProgressRectF, mBackgroundPaint);
            }

            angle = 360 * mValuesToDrawList.get(i) / mMax;
            if (mReverseEnabled) {
                angle *= -1;
            }
            float offset = !mReverseEnabled && mMultipleArcsEnabled ? ANGLE_OFFSET_FOR_MULTIPLE_ARC_PROGRESS : 0; //to better glue all the "pieces"
            canvas.drawArc(mProgressRectF, previousAngle - offset, angle + offset, false, mProgressPaintList.get(i));
            if (!mMultipleArcsEnabled && mProgressThumbEnabled) {
                //Only in "single-arc-progress", otherwise we'll end up with N thumbs
                endX = (Math.cos(Math.toRadians(previousAngle + angle)) * radius);
                endY = (Math.sin(Math.toRadians(previousAngle + angle)) * radius);
                canvas.drawCircle((float) endX + mProgressRectF.centerX(), (float) endY + mProgressRectF.centerY(), mProgressIconThickness, mProgressPaintList.get(i));
            }
            previousAngle += angle;
        }
    }

    public int dpToPx(float dp) {
        return (int) Math.ceil(dp * Resources.getSystem().getDisplayMetrics().density);
    }
}