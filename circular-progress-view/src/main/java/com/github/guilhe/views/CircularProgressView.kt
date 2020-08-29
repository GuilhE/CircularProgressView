package com.github.guilhe.views

import android.animation.Animator
import android.animation.FloatEvaluator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.github.guilhe.views.ProgressThumbScaleType.*
import com.github.guilhe.views.circularprogress.R
import java.util.*
import kotlin.math.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
class CircularProgressView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val defaultViewPadding = dpToPx(DEFAULT_VIEW_PADDING_DP).toFloat()
    private val defaultShadowPadding = dpToPx(DEFAULT_SHADOW_PADDING_DP).toFloat()
    private val defaultStrokeThickness = dpToPx(DEFAULT_STROKE_THICKNESS_DP).toFloat()
    private val defaultThumbSize = dpToPx(DEFAULT_THUMB_SIZE_DP).toFloat()
    private val defaultMaxWidth = dpToPx(DEFAULT_MAX_WIDTH_DP.toFloat())
    private val valuesToDrawList: MutableList<Float> = ArrayList()

    private lateinit var progressRectF: RectF
    private lateinit var shadowRectF: RectF
    private lateinit var backgroundPaint: Paint
    private lateinit var progressPaint: Paint
    private lateinit var shadowPaint: Paint
    private lateinit var shadowThumbPaint: Paint
    private lateinit var thumbPaint: Paint
    private lateinit var progressAnimator: ValueAnimator

    private var multipleArcsEnabled = false
    private var progressListTotal = 0f
    private var progressList = ArrayList<Float>()
    private var progressPaintList: ArrayList<Paint> = ArrayList()
    private var progress = 0f
    private var progressThumbSizeRate = DEFAULT_MAXIMUM_THUMB_SIZE_RATE
    private var progressIconThickness = 0f
    private var progressStrokeThickness = defaultStrokeThickness
    private var lastValidRawMeasuredDim = 0f
    private var lastValidStrokeThickness = defaultStrokeThickness
    private var lastValidThumbSize = defaultThumbSize
    private var lastValidThumbSizeRate = DEFAULT_MAXIMUM_THUMB_SIZE_RATE
    private var progressInterpolator: TimeInterpolator = DEFAULT_INTERPOLATOR
    private var shader: Shader? = null
    private var shaderColors: IntArray = intArrayOf()
    private var shaderPositions: FloatArray = floatArrayOf()
    private var initShader = false
    private var sizeChanged = false

    var progressThumbScaleType: ProgressThumbScaleType = AUTO
    var progressMaxThumbSizeRate = DEFAULT_MAXIMUM_THUMB_SIZE_RATE
    var progressThumbSize = defaultThumbSize
    var actionCallback: CircularProgressViewActionCallback? = null

    var isBackgroundAlphaEnabled: Boolean = false
        set(enabled) {
            field = enabled
            resetBackgroundPaint()
            invalidate()
        }

    var isReverseEnabled: Boolean = false
        set(enabled) {
            field = enabled
            invalidate()
        }

    var isProgressRounded: Boolean = false
        set(enabled) {
            field = enabled
            progressPaint.strokeCap = if (isProgressRounded) Paint.Cap.ROUND else Paint.Cap.SQUARE
            shadowPaint.strokeCap = progressPaint.strokeCap
            invalidate()
        }

    var isShadowEnabled: Boolean = true
        set(enable) {
            field = enable
            invalidate()
        }

    var isProgressThumbEnabled: Boolean = false
        set(enable) {
            field = enable
            invalidate()
            requestLayout()
        }

    var progressColor: Int = DEFAULT_PROGRESS_COLOR
        set(color) {
            field = color
            if (color == -1) {
                progressBackgroundColor = color
            }
            progressPaint.color = color
            setShader(null)
            invalidate()
        }

    var progressBackgroundColor: Int = DEFAULT_PROGRESS_COLOR
        set(color) {
            field = color
            resetBackgroundPaint()
            invalidate()
        }

    /**
     * This method changes the progress bar starting angle.
     * The default value is 270 and it's equivalent to 12 o'clock.
     */
    var startingAngle: Int = DEFAULT_STARTING_ANGLE
        set(angle) {
            field = angle
            invalidate()
        }

    /**
     * Sets progress bar max value (100%)
     */
    var max: Int = DEFAULT_MAX
        set(value) {
            field = value
            invalidate()
        }

    interface CircularProgressViewActionCallback {
        fun onProgressChanged(progress: Float)
        fun onAnimationFinished(progress: Float)
    }

    init {
        setupAttr(context, attrs)
    }

    private fun setupAttr(context: Context, attrs: AttributeSet?) {
        progressRectF = RectF()
        shadowRectF = RectF()
        backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backgroundPaint.style = Paint.Style.STROKE
        progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        progressPaint.style = Paint.Style.STROKE
        shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        shadowPaint.style = Paint.Style.STROKE
        shadowThumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        shadowThumbPaint.style = Paint.Style.FILL
        thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        thumbPaint.style = Paint.Style.FILL

        if (attrs != null) {
            val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.CircularProgressView, 0, 0)
            try {
                max = typedArray.getInt(R.styleable.CircularProgressView_max, DEFAULT_MAX)
                isShadowEnabled = typedArray.getBoolean(R.styleable.CircularProgressView_shadow, true)
                isProgressThumbEnabled = typedArray.getBoolean(R.styleable.CircularProgressView_progressThumb, false)
                progressThumbScaleType = values()[typedArray.getInteger(R.styleable.CircularProgressView_progressThumbScaleType, 0)]
                progressMaxThumbSizeRate = typedArray.getFloat(R.styleable.CircularProgressView_progressThumbSizeMaxRate, DEFAULT_MAXIMUM_THUMB_SIZE_RATE)
                startingAngle = typedArray.getInteger(R.styleable.CircularProgressView_startingAngle, DEFAULT_STARTING_ANGLE)
                progress = typedArray.getFloat(R.styleable.CircularProgressView_progress, 0f)
                progressStrokeThickness = typedArray.getDimension(R.styleable.CircularProgressView_progressBarThickness, defaultStrokeThickness)
                progressThumbSize = typedArray.getDimension(R.styleable.CircularProgressView_progressThumbSize, defaultThumbSize)
                progressThumbSizeRate = typedArray.getFloat(R.styleable.CircularProgressView_progressThumbSizeRate, DEFAULT_MAXIMUM_THUMB_SIZE_RATE)
                progressColor = typedArray.getInt(R.styleable.CircularProgressView_progressBarColor, DEFAULT_PROGRESS_COLOR)
                isProgressRounded = typedArray.getBoolean(R.styleable.CircularProgressView_progressBarRounded, false)
                progressBackgroundColor = typedArray.getInt(R.styleable.CircularProgressView_progressBackgroundColor, progressColor)
                isBackgroundAlphaEnabled = typedArray.getBoolean(R.styleable.CircularProgressView_progressBackgroundAlphaEnabled, true)
                isReverseEnabled = typedArray.getBoolean(R.styleable.CircularProgressView_reverse, false)
                val colorsId = typedArray.getResourceId(R.styleable.CircularProgressView_progressBarColorArray, -1)
                val duplicate = typedArray.getBoolean(R.styleable.CircularProgressView_duplicateFirstColorInArray, false)
                if (colorsId != -1) {
                    shaderColors = typedArray.resources.getIntArray(colorsId)
                    if (duplicate) {
                        shaderColors = duplicateFirstColor(shaderColors)
                    }
                    initShader = true
                }
                val positionsId = typedArray.getResourceId(R.styleable.CircularProgressView_progressBarColorArrayPositions, -1)
                if (positionsId != -1) {
                    val floats = typedArray.resources.obtainTypedArray(positionsId)
                    shaderPositions = FloatArray(floats.length())
                    for (i in 0 until floats.length()) {
                        shaderPositions[i] = floats.getFloat(i, 0f)
                    }
                    floats.recycle()
                }
            } finally {
                typedArray.recycle()
            }
        }

        resetBackgroundPaint()
        progressPaint.color = progressColor
        setShader(shader)
        progressPaint.strokeCap = if (isProgressRounded) Paint.Cap.ROUND else Paint.Cap.SQUARE
        shadowPaint.color = adjustAlpha(Color.BLACK, 0.2f)
        shadowPaint.strokeCap = progressPaint.strokeCap
        setThickness(progressStrokeThickness, false)
    }

    /**
     * Either width or height, this view will use Math.min(width, height) value.
     * If an invalid size is set it won't take effect and a last valid size will be used.
     * Check [.onMeasure]
     *
     * @param size in pixels
     */
    fun setSize(size: Int) {
        layoutParams.height = size
        sizeChanged = true
        requestLayout()
    }

    /**
     * Changes progress and background color
     *
     * @param color - Color
     */
    fun setColor(color: Int) {
        progressColor = color
        progressBackgroundColor = color
    }

    /**
     * You can simulate the use of this method with by calling [.setColor] with ContextCompat:
     * setBackgroundColor(ContextCompat.getColor(resId));
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun setColorResource(@ColorRes resId: Int) {
        setColor(context.getColor(resId))
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun setColor(color: Color) {
        setColor(color.toArgb())
    }

    /**
     * You can simulate the use of this method with by calling [.setProgressColor] with ContextCompat:
     * setProgressColor(ContextCompat.getColor(resId));
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun setProgressColorResource(@ColorRes resId: Int) {
        progressColor = context.getColor(resId)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun setProgressColor(color: Color) {
        progressColor = color.toArgb()
    }

    /**
     * This will create a SweepGradient and use it as progress color. Rainboooowwwww!
     *
     * @param colors - The colors to be distributed between around the center. There must be at least 2 colors in the array.
     * @param positions - May be NULL. The relative position of each corresponding color in the colors array, beginning with 0 and ending with 1.0.
     * If the values are not monotonic, the drawing may produce unexpected results. If positions is NULL, then the colors are automatically spaced evenly.
     * @param duplicateFirst to create a perfect stitch the last color from the array must be equal to the first. If true it will do it for you.
     */
    @JvmOverloads
    fun setProgressColors(@ColorInt colors: IntArray, positions: FloatArray, duplicateFirst: Boolean = false) {
        shaderColors = if (duplicateFirst) duplicateFirstColor(colors) else colors
        shaderPositions = positions
        setShader(SweepGradient(progressRectF.centerX(), progressRectF.centerY(), colors, positions))
        invalidate()
    }

    private fun duplicateFirstColor(@ColorInt colors: IntArray): IntArray {
        return colors.copyOf(colors.size + 1).also {
            it[colors.size] = colors[0]
        }
    }

    private fun setShader(shader: Shader?) {
        progressPaint.shader = shader
    }

    /**
     * You can simulate the use of this method with by calling [.setBackgroundColor] with ContextCompat:
     * setBackgroundColor(ContextCompat.getColor(resId));
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun setShadowColorResource(@ColorRes resId: Int) = setBackgroundColor(context.getColor(resId))

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun setBackgroundColor(color: Color) = setBackgroundColor(color.toArgb())

    /**
     * Changes progressBar & progressIcon, background and shadow line width. Thickness in pixels.
     */
    fun setProgressStrokeThickness(value: Float) = setThickness(value, true)

    private fun setThickness(thickness: Float, requestLayout: Boolean) {
        progressStrokeThickness = thickness
        progressIconThickness = progressStrokeThickness / 2
        backgroundPaint.strokeWidth = progressStrokeThickness
        progressPaint.strokeWidth = progressStrokeThickness
        for (paint in progressPaintList) {
            paint.strokeWidth = progressStrokeThickness
        }
        shadowPaint.strokeWidth = progressStrokeThickness
        if (requestLayout) {
            requestLayout()
        }
    }

    @JvmOverloads
    fun setProgress(progress: Float, animate: Boolean = false, duration: Long = DEFAULT_ANIMATION_MILLIS.toLong()) {
        setProgress(progress, animate, duration, true)
    }

    /**
     * This method will activate the "multiple-arc-progress" and disable the progress thumb, progress round and background.
     * This method disables the "single-arc-progress".
     *
     * @param progressList      - list containing all the progress "step-per-arc". Their sum most be less or equal to [.getMax].
     * @param progressColorList - list containing the progress "step-per-arc" color. If progressColorList.size() is less than progressList.size(), Color.TRANSPARENT will be used for the missing colors.
     * @throws RuntimeException - will be thrown if progress entities sum is greater than max value.
     */
    @Throws(RuntimeException::class)
    fun setProgress(progressList: List<Float>, progressColorList: List<Int>) {
        isProgressRounded = false
        progressListTotal = 0f
        progress = progressListTotal
        for (value in progressList) {
            progressListTotal += value
            if (progressListTotal > max) {
                throw RuntimeException(String.format("Progress entities sum (%s) is greater than max value (%s)", progressListTotal, max))
            }
        }
        multipleArcsEnabled = true
        this.progressList = ArrayList(progressList)
        progressPaintList = ArrayList()
        for (i in progressList.indices) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.style = Paint.Style.STROKE
            paint.color = if (i < progressColorList.size) progressColorList[i] else Color.TRANSPARENT
            progressPaintList.add(paint)
        }
        setThickness(progressStrokeThickness, false)
        invalidate()
    }

    @JvmOverloads
    fun resetProgress(animate: Boolean = false, duration: Long = DEFAULT_ANIMATION_MILLIS.toLong()) {
        setProgress(0f, animate, duration, false)
    }

    fun setAnimationInterpolator(interpolator: TimeInterpolator?) {
        progressInterpolator = interpolator ?: DEFAULT_INTERPOLATOR
    }

    private fun resetBackgroundPaint() {
        backgroundPaint.color = if (isBackgroundAlphaEnabled) adjustAlpha(progressBackgroundColor, DEFAULT_BACKGROUND_ALPHA) else progressBackgroundColor
    }

    /**
     * This method will activate the "single-arc-progress" and enable the progress thumb and background.
     * This method disables the "multiple-arc-progress".
     */
    private fun setProgress(progress: Float, animate: Boolean, duration: Long, clockwise: Boolean) {
        multipleArcsEnabled = false
        if (animate) {
            if (::progressAnimator.isInitialized) {
                progressAnimator.cancel()
            }
            progressAnimator = getAnimator(this.progress.toDouble(), if (clockwise) progress.toDouble() else 0.toDouble(), duration) { valueAnimator ->
                setProgressValue(valueAnimator.animatedValue as Float)
                actionCallback?.onProgressChanged(progress)
            }
            progressAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator) {
                    actionCallback?.onAnimationFinished(progress)
                }

                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })
            progressAnimator.start()
        } else {
            setProgressValue(progress)
        }
    }

    private fun setProgressValue(value: Float) {
        progress = value
        invalidate()
    }

    private fun getAnimator(current: Double, next: Double, duration: Long, updateListener: AnimatorUpdateListener) = ValueAnimator().apply {
        this.interpolator = progressInterpolator
        this.duration = duration
        this.setObjectValues(current, next)
        this.setEvaluator(object : FloatEvaluator() {
            fun evaluate(fraction: Float, startValue: Float, endValue: Float): Int {
                return (startValue + (endValue - startValue) * fraction).roundToInt()
            }
        })
        this.addUpdateListener(updateListener)
    }

    /**
     * Changes color's alpha by the factor
     *
     * @param color  The color to change alpha
     * @param factor 1.0f (solid) to 0.0f (transparent)
     * @return int - A color with modified alpha
     */
    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).roundToInt()
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED) MeasureSpec.getSize(heightMeasureSpec) else defaultMaxWidth
        val rawMeasuredDim = max(min(width, height), 0)
        var progressWidth = progressStrokeThickness
        val thumbSize = when (progressThumbScaleType) {
            POINT -> progressThumbSize
            RATE -> progressStrokeThickness / 2 * progressThumbSizeRate
            else -> progressStrokeThickness
        }

        // if ThumbSize diameter is thicker than Stroke
        if (isProgressThumbEnabled && progressThumbScaleType != AUTO) {
            if (thumbSize * 2 > progressStrokeThickness) {
                // increase progressWidth by thumbSize
                progressWidth += thumbSize - progressStrokeThickness
            } else {
                progressWidth = progressStrokeThickness / 2
            }
        }
        var arcDim = max(progressWidth, 0f) + defaultViewPadding
        progressRectF[arcDim, arcDim, rawMeasuredDim - arcDim] = rawMeasuredDim - arcDim

        //To avoid creating a messy composition
        if (progressRectF.width() <= max(progressWidth, thumbSize)) {
            arcDim = lastValidRawMeasuredDim
            progressRectF[arcDim, arcDim, rawMeasuredDim - arcDim] = rawMeasuredDim - arcDim
            setThickness(lastValidStrokeThickness, false)
            progressThumbSize = lastValidThumbSize
            progressThumbSizeRate = max(min(lastValidThumbSizeRate, progressMaxThumbSizeRate), 0f) // To prevent the Thumb size too big
        } else {
            lastValidRawMeasuredDim = arcDim
            lastValidStrokeThickness = progressStrokeThickness
            lastValidThumbSize = progressThumbSize
            lastValidThumbSizeRate = progressThumbSizeRate
        }
        shadowRectF[progressRectF.left, defaultShadowPadding + progressRectF.top, progressRectF.right] = defaultShadowPadding + progressRectF.bottom
        setMeasuredDimension(rawMeasuredDim, rawMeasuredDim)
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //Either we are using "single-arc-progress" or "multiple-arc-progress".
        valuesToDrawList.clear()
        if (!multipleArcsEnabled) {
            valuesToDrawList.add(progress)
            progressPaintList.clear()
            progressPaintList.add(progressPaint)
        } else {
            valuesToDrawList.addAll(progressList)
        }

        var angle: Float
        var previousAngle = startingAngle.toFloat()
        var radius = width.toFloat() / 2 - defaultViewPadding
        var thumbSize = 0f
        if (progressThumbScaleType == AUTO) {
            thumbSize = progressIconThickness
            radius -= progressIconThickness + progressStrokeThickness / 2
        } else {
            var isThicker = false
            if (progressThumbScaleType == POINT) {
                thumbSize = progressThumbSize
                isThicker = progressThumbSize * 2 > progressStrokeThickness
            } else if (progressThumbScaleType == RATE) {
                thumbSize = progressStrokeThickness / 2 * progressThumbSizeRate
                isThicker = progressThumbSizeRate > 1
            }
            radius -= if (isThicker) thumbSize else progressStrokeThickness / 2
        }
        var endX: Double
        var endY: Double

        //Shadow logic
        if (isShadowEnabled) {
            angle = 360 * (if (multipleArcsEnabled) progressListTotal else progress) / max
            if (isReverseEnabled) {
                angle *= -1f
            }
            if (!multipleArcsEnabled && isProgressThumbEnabled) {
                //Only in "single-arc-progress", otherwise we'll end up with N thumbs

                //Who doesn't love a bit of math? :)
                //cos(a) = adj / hyp <>cos(angle) = x / radius <>x = cos(angle) * radius
                //sin(a) = opp / hyp <>sin(angle) = y / radius <>y = sin(angle) * radius
                //x = cos(startingAngle + progressAngle) * radius + originX(center)
                //y = sin(startingAngle + progressAngle) * radius + originY(center)
                endX = cos(Math.toRadians(previousAngle + angle.toDouble())) * radius
                endY = sin(Math.toRadians(previousAngle + angle.toDouble())) * radius
                shadowThumbPaint.set(shadowPaint) // shadow stroke style copy
                when (progressThumbScaleType) {
                    POINT, RATE -> shadowThumbPaint.style = Paint.Style.FILL
                    else -> shadowThumbPaint.style = Paint.Style.STROKE
                }
                canvas.drawCircle(endX.toFloat() + shadowRectF.centerX(), endY.toFloat() + shadowRectF.centerY(), thumbSize, shadowThumbPaint)
            }
            canvas.drawArc(shadowRectF, previousAngle, angle, false, shadowPaint)
        }

        //Progress logic
        if (initShader) {
            initShader = false
            setShader(SweepGradient(progressRectF.centerX(), progressRectF.centerY(), shaderColors, if (shaderPositions.isEmpty()) null else shaderPositions))
        } else if (sizeChanged) {
            sizeChanged = false
            setShader(SweepGradient(progressRectF.centerX(), progressRectF.centerY(), shaderColors, if (shaderPositions.isEmpty()) null else shaderPositions))
        }
        for (i in valuesToDrawList.indices) {
            if (!multipleArcsEnabled) {
                //No background will be used when "multiple-arc-progress" is enable because it will be mixed with the "progress-colors"
                canvas.drawOval(progressRectF, backgroundPaint)
            }
            angle = 360 * valuesToDrawList[i] / max
            if (isReverseEnabled) {
                angle *= -1f
            }
            val offset: Float = if (!isReverseEnabled && multipleArcsEnabled) ANGLE_OFFSET_FOR_MULTIPLE_ARC_PROGRESS else 0f //to better glue all the "pieces"
            canvas.drawArc(progressRectF, previousAngle - offset, angle + offset, false, progressPaintList[i])
            if (!multipleArcsEnabled && isProgressThumbEnabled) {
                //Only in "single-arc-progress", otherwise we'll end up with N thumbs
                endX = cos(Math.toRadians(previousAngle + angle.toDouble())) * radius
                endY = sin(Math.toRadians(previousAngle + angle.toDouble())) * radius
                thumbPaint.set(progressPaintList[i]) // stroke style copy
                when (progressThumbScaleType) {
                    POINT, RATE -> thumbPaint.style = Paint.Style.FILL
                    else -> thumbPaint.style = Paint.Style.STROKE
                }
                canvas.drawCircle(endX.toFloat() + progressRectF.centerX(), endY.toFloat() + progressRectF.centerY(), thumbSize, thumbPaint)
            }
            previousAngle += angle
        }
    }

    private fun dpToPx(dp: Float) = ceil(dp * Resources.getSystem().displayMetrics.density.toDouble()).toInt()

    companion object {
        private const val ANGLE_OFFSET_FOR_MULTIPLE_ARC_PROGRESS = 6f
        private const val DEFAULT_VIEW_PADDING_DP = 10f
        private const val DEFAULT_SHADOW_PADDING_DP = 5f
        private const val DEFAULT_STROKE_THICKNESS_DP = 10f
        private const val DEFAULT_THUMB_SIZE_DP = 10f
        private const val DEFAULT_MAXIMUM_THUMB_SIZE_RATE = 2f
        private const val DEFAULT_MAX_WIDTH_DP = 100
        private const val DEFAULT_MAX = 100
        private const val DEFAULT_STARTING_ANGLE = 270
        private const val DEFAULT_ANIMATION_MILLIS = 1000
        private const val DEFAULT_PROGRESS_COLOR = Color.BLACK
        private const val DEFAULT_BACKGROUND_ALPHA = 0.3f
        private val DEFAULT_INTERPOLATOR: TimeInterpolator = DecelerateInterpolator()
    }
}

enum class ProgressThumbScaleType { AUTO, POINT, RATE }

@Suppress("unused")
inline fun CircularProgressView.addActionListener(
        crossinline onProgressChanged: (progress: Float) -> Unit = { _ -> },
        crossinline onAnimationFinished: (progress: Float) -> Unit = { _ -> },
): CircularProgressView.CircularProgressViewActionCallback {
    val callback = object : CircularProgressView.CircularProgressViewActionCallback {
        override fun onProgressChanged(progress: Float) {
            onProgressChanged.invoke(progress)
        }

        override fun onAnimationFinished(progress: Float) {
            onAnimationFinished.invoke(progress)
        }
    }
    actionCallback = callback
    return callback
}