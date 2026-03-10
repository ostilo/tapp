package com.tapp.spinwheel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.animation.ValueAnimator
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.animation.DecelerateInterpolator
import androidx.core.graphics.withRotation
import com.tapp.spinwheel.core.SpinWheelLogger
import com.tapp.spinwheel.data.SpinWheelRepository
import com.tapp.spinwheel.core.SpinWheelColors
import com.tapp.spinwheel.ui.model.WidgetConfig
import com.tapp.spinwheel.ui.model.SpinWheelBitmaps
import com.tapp.spinwheel.ui.state.SpinWheelState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Custom view that renders the spin wheel widget end-to-end.
 *
 * Responsibilities:
 * - Requests and caches config + assets via [SpinWheelRepository].
 * - Renders loading, ready and error states based on [SpinWheelState].
 * - Handles user input:
 *   - tap while `Ready` => starts a spin animation
 *   - tap while `Error` => retries loading config/assets
 */
class SpinWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val loadingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = SpinWheelColors.LoaderBase
    }
    private val scrimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
        alpha = 160
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 42f * resources.displayMetrics.scaledDensity
    }
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var repository: SpinWheelRepository? = null
    private var config: WidgetConfig? = null
    private var configUrl: String? = null

    private var bgBitmap: Bitmap? = null
    private var wheelBitmap: Bitmap? = null
    private var frameBitmap: Bitmap? = null
    private var spinBitmap: Bitmap? = null

    private var currentAngle: Float = 0f
    private var isSpinning: Boolean = false
    private var state: SpinWheelState = SpinWheelState.Loading
    private var loadingAnimator: ValueAnimator? = null
    private var spinAnimator: ValueAnimator? = null
    private var loadingPhase: Float = 0f
    private var lastErrorMessage: String? = null

    // Cached drawing geometry to avoid per-frame allocations
    private val bgDstRect = Rect()
    private val bgSrcRect = Rect()
    private val frameRect = Rect()
    private val wheelRect = Rect()
    private val spinRect = Rect()
    private val frameRectF = RectF()
    private val wheelRectF = RectF()

    fun setConfigUrl(url: String) {
        configUrl = url
        repository = SpinWheelRepository(context.applicationContext, url)
        retryLoad(force = false)
    }

    private fun setState(newState: SpinWheelState) {
        state = newState
        if (newState == SpinWheelState.Loading) {
            if (loadingAnimator == null) {
                loadingAnimator =
                    ValueAnimator.ofFloat(0f, 1f).apply {
                        duration = 1200L
                        repeatCount = ValueAnimator.INFINITE
                        repeatMode = ValueAnimator.RESTART
                        addUpdateListener {
                            loadingPhase = it.animatedFraction
                            postInvalidateOnAnimation()
                        }
                        start()
                    }
            } else if (loadingAnimator?.isStarted != true) {
                loadingAnimator?.start()
            }
        } else {
            loadingAnimator?.cancel()
            loadingAnimator = null
            postInvalidateOnAnimation()
        }
    }

    private fun retryLoad(force: Boolean = true) {
        val url = configUrl ?: return
        val repo = repository ?: SpinWheelRepository(context.applicationContext, url).also { repository = it }

        lastErrorMessage = null
        setState(SpinWheelState.Loading)

        scope.launch {
            try {
                val cfg = repo.loadConfig(force = force)
                if (cfg == null) {
                    setError("Unable to load config")
                    return@launch
                }
                config = cfg
                loadAssets(cfg)
            } catch (t: Throwable) {
                setError(t.message ?: "Unknown error")
            }
        }
    }

    private fun setError(message: String) {
        lastErrorMessage = message
        SpinWheelLogger.e("SpinWheel", "Error: $message")
        setState(SpinWheelState.Error)
    }

    private fun loadAssets(cfg: WidgetConfig) {
        val repo = repository ?: return
        val host = cfg.network.assets.host.trimEnd('/')
        val path = "wheel/Assets"
        val assets = cfg.wheel.assets
        SpinWheelLogger.d("SpinWheel", "Loading assets from host=$host")

        scope.launch {
            val bitmaps: SpinWheelBitmaps = withContext(Dispatchers.IO) {
                val bgFile = repo.fetchImageToFile("$host/$path/${assets.bg}")
                val wheelFile = repo.fetchImageToFile("$host/$path/${assets.wheel}")
                val frameFile = repo.fetchImageToFile("$host/$path/${assets.wheelFrame}")
                val spinFile = repo.fetchImageToFile("$host/$path/${assets.wheelSpin}")

                val bg = bgFile?.let { BitmapFactory.decodeFile(it.absolutePath) }
                val wheel = wheelFile?.let { BitmapFactory.decodeFile(it.absolutePath) }
                val frame = frameFile?.let { BitmapFactory.decodeFile(it.absolutePath) }
                val spin = spinFile?.let { BitmapFactory.decodeFile(it.absolutePath) }

                SpinWheelBitmaps(bg = bg, wheel = wheel, frame = frame, spin = spin)
            }

            bgBitmap = bitmaps.bg
            wheelBitmap = bitmaps.wheel
            frameBitmap = bitmaps.frame
            spinBitmap = bitmaps.spin
            updateBackgroundCrop()

            // Ready only when we have the key visuals.
            if (bgBitmap == null || wheelBitmap == null || frameBitmap == null) {
                setError("Missing assets")
            } else {
                setState(SpinWheelState.Ready)
            }
            postInvalidateOnAnimation()
        }
    }

    private fun startSpin() {
        val cfg = config ?: return
        val rotation = cfg.wheel.rotation
        val spins = Random.nextInt(rotation.minimumSpins, rotation.maximumSpins + 1)
        val targetAngle = currentAngle + spins * 360f + Random.nextFloat() * 360f

        val startAngle = currentAngle
        val delta = targetAngle - startAngle
        val durationMs = rotation.duration

        isSpinning = true
        spinAnimator?.cancel()
        spinAnimator =
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = durationMs
                interpolator = DecelerateInterpolator()
                addUpdateListener { animator ->
                    val eased = animator.animatedValue as Float
                    currentAngle = startAngle + eased * delta
                    postInvalidateOnAnimation()
                }
                addListener(
                    object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            isSpinning = false
                        }

                        override fun onAnimationCancel(animation: Animator) {
                            isSpinning = false
                        }
                    },
                )
                start()
            }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return super.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_UP -> {
                // Only treat taps that land inside the spin button as a spin / retry gesture.
                if (spinRect.contains(event.x.toInt(), event.y.toInt())) {
                    if (isSpinning) return true
                    when (state) {
                        is SpinWheelState.Error -> retryLoad()
                        SpinWheelState.Ready -> startSpin()
                        SpinWheelState.Loading -> Unit
                    }
                    performClick()
                    return true
                }
            }
        }

        // Consume events so we can see the corresponding ACTION_UP.
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bgDstRect.set(0, 0, w, h)

        val centerX = w / 2f
        val centerY = h / 2f

        // All drawing geometry is derived from the shortest screen edge so
        // the wheel remains perfectly circular and centered across devices.
        val baseSize = (0.84f * w.coerceAtMost(h)).roundToInt()
        val frameSize = baseSize
        val wheelSize = (frameSize * 0.88f).roundToInt()
        val spinSize = (wheelSize * 0.34f).roundToInt()

        setCenteredSquareRect(frameRect, centerX, centerY, frameSize)
        setCenteredSquareRect(wheelRect, centerX, centerY, wheelSize)
        setCenteredSquareRect(spinRect, centerX, centerY, spinSize)

        frameRectF.set(frameRect)
        wheelRectF.set(wheelRect)

        updateBackgroundCrop()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width
        val h = height
        if (w == 0 || h == 0) return

        // Background fills widget (center-crop)
        bgBitmap?.let { bmp ->
            drawCenterCrop(canvas, bmp)
        } ?: run {
            canvas.drawColor(Color.BLACK)
        }
        
       // Soften the background so the wheel + frame stand out more
        canvas.drawColor(0x66000000) // semi‑transparent black overlay
        
        val centerX = w / 2f
        val centerY = h / 2f

        if (state == SpinWheelState.Loading || wheelBitmap == null || frameBitmap == null) {
            drawLoadingState(canvas, centerX, centerY)
            return
        }

        if (state is SpinWheelState.Error) {
            drawErrorState(canvas, centerX, centerY)
            return
        }

        // Rotating wheel
        wheelBitmap?.let { bmp ->
            canvas.withRotation(currentAngle, centerX, centerY) {
                drawBitmap(bmp, null, wheelRect, paint)
            }
        }

        // Frame overlay
        frameBitmap?.let { bmp ->
            canvas.drawBitmap(bmp, null, frameRect, paint)
        }

        // Spin button – drawn at center; click is on whole view
        spinBitmap?.let { bmp ->
            canvas.drawBitmap(bmp, null, spinRect, paint)
        }
    }

    private fun drawErrorState(canvas: Canvas, centerX: Float, centerY: Float) {
        // Dim overlay
        canvas.drawRect(bgDstRect, scrimPaint)

        val title = "Tap to retry"
        val subtitle = lastErrorMessage

        val titleY = centerY - 10f * resources.displayMetrics.density
        canvas.drawText(title, centerX, titleY, textPaint)

        if (!subtitle.isNullOrBlank()) {
            val oldSize = textPaint.textSize
            textPaint.textSize = 14f * resources.displayMetrics.scaledDensity
            textPaint.alpha = 220
            canvas.drawText(subtitle, centerX, titleY + (22f * resources.displayMetrics.density), textPaint)
            textPaint.alpha = 255
            textPaint.textSize = oldSize
        }
    }

    private fun drawLoadingState(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
    ) {
        val t = loadingPhase
        val pulse =
            (0.55f + 0.35f * kotlin.math.sin(t * 2f * Math.PI).toFloat()).coerceIn(0f, 1f)
        val baseAlpha = (80 + (120 * pulse)).roundToInt().coerceIn(0, 255)

        // Frame skeleton
        loadingPaint.alpha = baseAlpha
        canvas.drawOval(frameRectF, loadingPaint)

        // Wheel skeleton
        loadingPaint.alpha = (baseAlpha + 40).coerceIn(0, 255)
        canvas.drawOval(wheelRectF, loadingPaint)

        // Fun: small orbiting dot
        val r = wheelRect.width() * 0.48f
        val a = t * 360f
        val rad = Math.toRadians(a.toDouble())
        val dotX = centerX + (kotlin.math.cos(rad) * r).toFloat()
        val dotY = centerY + (kotlin.math.sin(rad) * r).toFloat()
        loadingPaint.alpha = 220
        loadingPaint.color = SpinWheelColors.LoaderAccent
        canvas.drawCircle(dotX, dotY, wheelRect.width() * 0.03f, loadingPaint)
        loadingPaint.color = SpinWheelColors.LoaderBase
    }

    private fun setCenteredSquareRect(out: Rect, centerX: Float, centerY: Float, size: Int) {
        val left = (centerX - size / 2f).roundToInt()
        val top = (centerY - size / 2f).roundToInt()
        out.set(left, top, left + size, top + size)
    }

    private fun updateBackgroundCrop() {
        val bitmap = bgBitmap ?: return
        val bw = bitmap.width.toFloat()
        val bh = bitmap.height.toFloat()
        val dw = bgDstRect.width().toFloat()
        val dh = bgDstRect.height().toFloat()
        if (bw <= 0f || bh <= 0f || dw <= 0f || dh <= 0f) return

        val scale = maxOf(dw / bw, dh / bh)
        val srcW = dw / scale
        val srcH = dh / scale
        val srcLeft = ((bw - srcW) / 2f).roundToInt().coerceAtLeast(0)
        val srcTop = ((bh - srcH) / 2f).roundToInt().coerceAtLeast(0)
        val srcRight = (srcLeft + srcW).roundToInt().coerceAtMost(bitmap.width)
        val srcBottom = (srcTop + srcH).roundToInt().coerceAtMost(bitmap.height)
        bgSrcRect.set(srcLeft, srcTop, srcRight, srcBottom)
    }

    private fun drawCenterCrop(canvas: Canvas, bitmap: Bitmap) {
        if (bgSrcRect.isEmpty || bgDstRect.isEmpty) {
            updateBackgroundCrop()
        }
        canvas.drawBitmap(bitmap, bgSrcRect, bgDstRect, paint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        spinAnimator?.cancel()
        spinAnimator = null
        loadingAnimator?.cancel()
        loadingAnimator = null
        scope.cancel()
    }
}

