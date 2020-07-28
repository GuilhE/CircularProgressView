package com.github.guilhe.views;

import android.animation.Animator;
import android.animation.FloatEvaluator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.github.guilhe.views.circularprogress.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.guilhe.views.ProgressThumbScaleType.AUTO;
import static com.github.guilhe.views.ProgressThumbScaleType.POINT;
import static com.github.guilhe.views.ProgressThumbScaleType.RATE;
import static com.github.guilhe.views.ProgressThumbScaleType.values;

@SuppressWarnings("unused")
public class CircularProgressView extends View {

    private static final String TAG = CircularProgressView.class.getSimpleName();

    private static final float ANGLE_OFFSET_FOR_MULTIPLE_ARC_PROGRESS = 6;
    private static final float DEFAULT_VIEW_PADDING_DP = 10;
    private static final float DEFAULT_SHADOW_PADDING_DP = 5;
    private static final float DEFAULT_STROKE_THICKNESS_DP = 10;
    private static final float DEFAULT_THUMB_SIZE_DP = 10;
    private static final float DEFAULT_MAXIMUM_THUMB_SIZE_RATE = 2;
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
    private final float mDefaultThumbSize = dpToPx(DEFAULT_THUMB_SIZE_DP);
    private final int mDefaultMaxWidth = dpToPx(DEFAULT_MAX_WIDTH_DP);

    private int mMax;
    private boolean mShadowEnabled;
    private boolean mProgressThumbEnabled;
    private ProgressThumbScaleType mProgressThumbScaleType;
    private float mMaxThumbSizeRate;
    private int mStartingAngle;
    private boolean mMultipleArcsEnabled;
    private float mProgressListTotal;
    private ArrayList<Float> mProgressList = new ArrayList<>();
    private ArrayList<Paint> mProgressPaintList = new ArrayList<>();
    private float mProgress;
    private float mProgressStrokeThickness;
    private float mProgressThumbSize;
    private float mProgressThumbSizeRate;
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
    private Paint mThumbPaint;
    private Paint mShadowPaint;
    private Paint mShadowThumbPaint;
    private float mLastValidRawMeasuredDim;
    private float mLastValidStrokeThickness;
    private float mLastValidThumbSize;
    private float mLastValidThumbSizeRate;

    private TimeInterpolator mInterpolator;
    private Animator mProgressAnimator;
    private OnProgressChangeAnimationCallback mCallback;
    private Shader mShader;
    private int[] mShaderColors;
    private float[] mShaderPositions;
    private boolean mInitShader;
    private boolean mSizeChanged = false;

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
        mLastValidThumbSize = mDefaultThumbSize;
        mLastValidThumbSizeRate = DEFAULT_MAXIMUM_THUMB_SIZE_RATE;
        mInterpolator = DEFAULT_INTERPOLATOR;
        mProgressRectF = new RectF();
        mShadowRectF = new RectF();

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setStyle(Paint.Style.STROKE);
        mShadowThumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowThumbPaint.setStyle(Paint.Style.FILL);
        mThumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mThumbPaint.setStyle(Paint.Style.FILL);

        if (attrs != null) {
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircularProgressView, 0, 0);
            try {
                mMax = typedArray.getInt(R.styleable.CircularProgressView_max, DEFAULT_MAX);
                mShadowEnabled = typedArray.getBoolean(R.styleable.CircularProgressView_shadow, true);
                mProgressThumbEnabled = typedArray.getBoolean(R.styleable.CircularProgressView_progressThumb, false);
                mProgressThumbScaleType = values()[typedArray.getInteger(R.styleable.CircularProgressView_progressThumbScaleType, 0)];
                mMaxThumbSizeRate = typedArray.getFloat(R.styleable.CircularProgressView_progressThumbSizeMaxRate, DEFAULT_MAXIMUM_THUMB_SIZE_RATE);
                mStartingAngle = typedArray.getInteger(R.styleable.CircularProgressView_startingAngle, DEFAULT_STARTING_ANGLE);
                mProgress = typedArray.getFloat(R.styleable.CircularProgressView_progress, 0);
                mProgressStrokeThickness = typedArray.getDimension(R.styleable.CircularProgressView_progressBarThickness, mDefaultStrokeThickness);
                mProgressThumbSize = typedArray.getDimension(R.styleable.CircularProgressView_progressThumbSize, mDefaultThumbSize);
                setProgressThumbSizeRate(typedArray.getFloat(R.styleable.CircularProgressView_progressThumbSizeRate, DEFAULT_MAXIMUM_THUMB_SIZE_RATE));
                mProgressColor = typedArray.getInt(R.styleable.CircularProgressView_progressBarColor, DEFAULT_PROGRESS_COLOR);
                mProgressRounded = typedArray.getBoolean(R.styleable.CircularProgressView_progressBarRounded, false);
                mBackgroundColor = typedArray.getInt(R.styleable.CircularProgressView_backgroundColor, mProgressColor);
                mBackgroundAlphaEnabled = typedArray.getBoolean(R.styleable.CircularProgressView_backgroundAlphaEnabled, true);
                mReverseEnabled = typedArray.getBoolean(R.styleable.CircularProgressView_reverse, false);

                int colorsId = typedArray.getResourceId(R.styleable.CircularProgressView_progressBarColorArray, -1);
                boolean duplicate = typedArray.getBoolean(R.styleable.CircularProgressView_duplicateFirstColorInArray, false);
                if (colorsId != -1) {
                    mShaderColors = typedArray.getResources().getIntArray(colorsId);
                    if (duplicate) {
                        mShaderColors = duplicateFirstColor(mShaderColors);
                    }
                    mInitShader = true;
                }
                int positionsId = typedArray.getResourceId(R.styleable.CircularProgressView_progressBarColorArrayPositions, -1);
                if (positionsId != -1) {
                    TypedArray floats = typedArray.getResources().obtainTypedArray(positionsId);
                    mShaderPositions = new float[floats.length()];
                    for (int i = 0; i < floats.length(); i++) {
                        mShaderPositions[i] = floats.getFloat(i, 0f);
                    }
                    floats.recycle();
                }
            } finally {
                typedArray.recycle();
            }
        } else {
            mProgressStrokeThickness = mDefaultStrokeThickness;
            mProgressThumbSize = mDefaultThumbSize;
            mProgressThumbSizeRate = DEFAULT_MAXIMUM_THUMB_SIZE_RATE;
            mProgressThumbScaleType = AUTO;
            mMaxThumbSizeRate = DEFAULT_MAXIMUM_THUMB_SIZE_RATE;
            mShadowEnabled = true;
            mMax = DEFAULT_MAX;
            mStartingAngle = DEFAULT_STARTING_ANGLE;
            mProgressColor = DEFAULT_PROGRESS_COLOR;
            mBackgroundColor = mProgressColor;
            mBackgroundAlphaEnabled = true;
            mReverseEnabled = false;
            mProgressRounded = false;
            mShader = null;
            mShaderColors = null;
            mShaderPositions = null;
        }

        resetBackgroundPaint();
        mProgressPaint.setColor(mProgressColor);
        setShader(mShader);
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
        mSizeChanged = true;
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
        setShader(null);
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

    /**
     * This will create a SweepGradient and use it as progress color. Rainboooowwwww!
     *
     * @param colors         The colors to be distributed between around the center.
     *                       There must be at least 2 colors in the array.
     * @param positions      May be NULL. The relative position of
     *                       each corresponding color in the colors array, beginning
     *                       with 0 and ending with 1.0. If the values are not
     *                       monotonic, the drawing may produce unexpected results.
     *                       If positions is NULL, then the colors are automatically
     *                       spaced evenly.
     * @param duplicateFirst to create a perfect stitch the last color from the array must be equal to the first. If true it will do it for you.
     */
    public void setProgressColors(@NonNull @ColorInt int[] colors, @Nullable float[] positions, boolean duplicateFirst) {
        if (duplicateFirst) {
            colors = duplicateFirstColor(colors);
        }
        mShaderColors = colors;
        mShaderPositions = positions;
        setShader(new SweepGradient(mProgressRectF.centerX(), mProgressRectF.centerY(), colors, positions));
        invalidate();
    }

    public void setProgressColors(@NonNull @ColorInt int[] colors, @Nullable float[] positions) {
        setProgressColors(colors, positions, false);
    }

    private int[] duplicateFirstColor(@ColorInt @NonNull int[] colors) {
        int[] aux = Arrays.copyOf(colors, colors.length + 1);
        aux[colors.length] = colors[0];
        colors = aux;
        return colors;
    }

    private void setShader(Shader shader) {
        mShader = shader;
        mProgressPaint.setShader(shader);
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
        requestLayout();
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

    public void setProgressThumbSize(float size) {
        setThumbSize(size, true);
    }

    private void setThumbSize(float size, boolean requestLayout) {
        mProgressThumbSize = size;

        if (requestLayout) {
            requestLayout();
        }
    }

    public float getProgressThumbSize() {
        return mProgressThumbSize;
    }

    public void setProgressThumbSizeRate(float rate) {
        setThumbSizeRate(rate, true);
    }

    private void setThumbSizeRate(float size, boolean requestLayout) {
        mProgressThumbSizeRate = Math.max(Math.min(size, mMaxThumbSizeRate), 0); // To prevent the Thumb size too big

        if (requestLayout) {
            requestLayout();
        }
    }

    public float getProgressThumbSizeRate() {
        return mProgressThumbSizeRate;
    }

    public void setProgressMaxThumbSizeRate(float maxRate) {
        mMaxThumbSizeRate = maxRate;
    }

    public float getProgressMaxThumbSizeRate() {
        return mMaxThumbSizeRate;
    }

    public void setProgressThumbScaleType(int index) {
        mProgressThumbScaleType = ProgressThumbScaleType.values()[Math.max(Math.min(index, 0), values().length-1)];
    }

    public void setProgressThumbScaleType(ProgressThumbScaleType scaleType) {
        mProgressThumbScaleType = scaleType;
    }

    public ProgressThumbScaleType getProgressThumbScaleType() {
        return mProgressThumbScaleType;
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
        float progressWidth = mProgressStrokeThickness;

        float thumbSize = 0;
        if (mProgressThumbScaleType == POINT) {
            thumbSize = mProgressThumbSize;
        } else if (mProgressThumbScaleType == RATE) {
            thumbSize = (mProgressStrokeThickness / 2) * mProgressThumbSizeRate;
        } else {
            thumbSize = mProgressStrokeThickness;
        }

        // if ThumbSize diameter is thicker than Stroke
        if (mProgressThumbEnabled && mProgressThumbScaleType != AUTO) {
            if (thumbSize * 2 > mProgressStrokeThickness) {
                // increase progressWidth by thumbSize
                progressWidth += thumbSize - mProgressStrokeThickness;
            } else {
                progressWidth = mProgressStrokeThickness / 2;
            }
        }

        float arcDim = Math.max(progressWidth, 0) + mDefaultViewPadding;
        mProgressRectF.set(arcDim, arcDim, rawMeasuredDim - arcDim, rawMeasuredDim - arcDim);

        //To avoid creating a messy composition
        if (mProgressRectF.width() <= (Math.max(progressWidth, thumbSize))) {
            arcDim = mLastValidRawMeasuredDim;
            mProgressRectF.set(arcDim, arcDim, rawMeasuredDim - arcDim, rawMeasuredDim - arcDim);
            setThickness(mLastValidStrokeThickness, false);
            setThumbSize(mLastValidThumbSize, false);
            setThumbSizeRate(mLastValidThumbSizeRate, false);
        } else {
            mLastValidRawMeasuredDim = arcDim;
            mLastValidStrokeThickness = mProgressStrokeThickness;
            mLastValidThumbSize = mProgressThumbSize;
            mLastValidThumbSizeRate = mProgressThumbSizeRate;
        }

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
        float radius = (float) getWidth() / 2 - mDefaultViewPadding;

        float thumbSize = 0;
        if (mProgressThumbScaleType == AUTO) {
            thumbSize = mProgressIconThickness;
            radius -= (mProgressIconThickness + mProgressStrokeThickness / 2);
        } else {
            boolean isThicker = false;
            if (mProgressThumbScaleType == POINT) {
                thumbSize = mProgressThumbSize;
                isThicker = mProgressThumbSize * 2 > mProgressStrokeThickness;
            } else if (mProgressThumbScaleType == RATE) {
                thumbSize = (mProgressStrokeThickness / 2) * mProgressThumbSizeRate;
                isThicker = mProgressThumbSizeRate > 1;
            }

            if (isThicker) {
                radius -= thumbSize;
            } else {
                radius -= mProgressStrokeThickness / 2;
            }
        }
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
                mShadowThumbPaint.set(mShadowPaint); // shadow stroke style copy
                switch(mProgressThumbScaleType) {
                    case POINT:
                    case RATE:
                        mShadowThumbPaint.setStyle(Paint.Style.FILL);
                        break;
                    case AUTO:
                    default:
                        mShadowThumbPaint.setStyle(Paint.Style.STROKE);
                }
                canvas.drawCircle((float) endX + mShadowRectF.centerX(), (float) endY + mShadowRectF.centerY(), thumbSize, mShadowThumbPaint);
            }
            canvas.drawArc(mShadowRectF, previousAngle, angle, false, mShadowPaint);
        }

        //Progress logic
        if (mInitShader) {
            mInitShader = false;
            setShader(new SweepGradient(mProgressRectF.centerX(), mProgressRectF.centerY(), mShaderColors, mShaderPositions));
        } else if (mSizeChanged) {
            mSizeChanged = false;
            setShader(new SweepGradient(mProgressRectF.centerX(), mProgressRectF.centerY(), mShaderColors, mShaderPositions));
        }

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

                mThumbPaint.set(mProgressPaintList.get(i)); // stroke style copy
                switch(mProgressThumbScaleType) {
                    case POINT:
                    case RATE:
                        mThumbPaint.setStyle(Paint.Style.FILL);
                        break;
                    case AUTO:
                    default:
                        mThumbPaint.setStyle(Paint.Style.STROKE);
                }
                canvas.drawCircle((float) endX + mProgressRectF.centerX(), (float) endY + mProgressRectF.centerY(), thumbSize, mThumbPaint);
            }
            previousAngle += angle;
        }
    }

    public int dpToPx(float dp) {
        return (int) Math.ceil(dp * Resources.getSystem().getDisplayMetrics().density);
    }
}