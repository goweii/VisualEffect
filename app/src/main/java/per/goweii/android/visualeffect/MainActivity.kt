package per.goweii.android.visualeffect

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import per.goweii.android.visualeffect.databinding.ActivityMainBinding
import per.goweii.visualeffect.blur.BlurEffect
import per.goweii.visualeffect.blur.FastBlurEffect
import per.goweii.visualeffect.blur.RSBlurEffect
import per.goweii.visualeffect.core.GroupVisualEffect
import per.goweii.visualeffect.core.VisualEffect
import per.goweii.visualeffect.mosaic.MosaicEffect
import per.goweii.visualeffect.view.VisualEffectView
import per.goweii.visualeffect.watermask.WatermarkEffect
import kotlin.math.max

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val visualEffectArray = arrayOf<Pair<String, () -> VisualEffect>>(
        GroupVisualEffect::class.java.simpleName to {
            GroupVisualEffect(
                RSBlurEffect(applicationContext, blurRadius),
                MosaicEffect(mosaicBoxSize),
                WatermarkEffect(
                    "goweii",
                    Color.BLACK,
                    watermarkTextSize
                ),
            )
        },
        RSBlurEffect::class.java.simpleName to {
            RSBlurEffect(applicationContext, blurRadius)
        },
        FastBlurEffect::class.java.simpleName to {
            FastBlurEffect(blurRadius)
        },
        MosaicEffect::class.java.simpleName to {
            MosaicEffect(mosaicBoxSize)
        },
        WatermarkEffect::class.java.simpleName to {
            WatermarkEffect("goweii", Color.BLACK, watermarkTextSize)
        }
    )

    private val blurRadius: Float get() = binding.sbRadius.progress.toFloat()
    private val mosaicBoxSize: Int get() = binding.sbBoxSize.progress
    private val watermarkTextSize: Float get() = binding.sbWatermarkSize.progress.toFloat()
    private val simpleSize: Float get() = max(binding.sbSimpleSize.progress.toFloat(), 1F)

    private lateinit var visualEffectView: VisualEffectView

    private var currVisualEffectIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        visualEffectView = newVisualEffectCard(false)
        binding.sbRadius.apply {
            max = 100
            progress = 10
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    refreshEffectValue()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
        }
        binding.sbBoxSize.apply {
            max = 40
            progress = 4
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    refreshEffectValue()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
        }
        binding.sbWatermarkSize.apply {
            max = 40
            progress = 10
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    refreshEffectValue()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
        }
        binding.sbSimpleSize.apply {
            max = 20
            progress = 8
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    refreshSimpleSize()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
        }
        refreshEffectValue()
        refreshSimpleSize()
        loadImage("https://imgsa.baidu.com/forum/w%3D580/sign=d5b742f157e736d158138c00ab514ffc/b9afb24543a98226a245270a8982b9014b90eb86.gif")
    }

    private fun showVisualEffectPopupWindow() {
        AlertDialog.Builder(this)
            .setView(newVisualEffectView())
            .show().apply {
                window?.apply {
                    setLayout(
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            300F,
                            resources.displayMetrics
                        ).toInt(),
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            300F,
                            resources.displayMetrics
                        ).toInt()
                    )
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_blur_add_view -> {
                newVisualEffectCard(true)
            }
            R.id.action_blur_show_dialog -> {
                showVisualEffectPopupWindow()
            }
            R.id.action_blur_select_photo -> {
                val intent = Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(intent, REQ_CODE_IMAGE)
            }
            R.id.action_blur_select_effect -> {
                AlertDialog.Builder(this)
                    .setSingleChoiceItems(
                        visualEffectArray.map { it.first }.toTypedArray(),
                        currVisualEffectIndex
                    ) { dialog, which ->
                        currVisualEffectIndex = which
                        visualEffectView.visualEffect =
                            visualEffectArray[currVisualEffectIndex].second.invoke()
                        refreshEffectValue()
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE_IMAGE -> {
                if (resultCode == RESULT_OK) {
                    loadImage(data?.data)
                }
            }
        }
    }

    private fun refreshSimpleSize() {
        binding.tvSimpleSize.text =
            "缩小比例(${binding.sbSimpleSize.progress}/${binding.sbSimpleSize.max})"
        visualEffectView.simpleSize = simpleSize
    }

    private fun refreshEffectValue() {
        binding.llRadius.isVisible = false
        binding.llBoxSize.isVisible = false
        binding.llWatermarkSize.isVisible = false
        visualEffectView.visualEffect.run {
            when (this) {
                is GroupVisualEffect -> {
                    this.getVisualEffects().forEach {
                        refreshEffectValue(it)
                    }
                }
                is BlurEffect -> {
                    refreshEffectValue(this)
                }
                is MosaicEffect -> {
                    refreshEffectValue(this)
                }
                is WatermarkEffect -> {
                    refreshEffectValue(this)
                }
            }
        }
    }

    private fun refreshEffectValue(visualEffect: VisualEffect) {
        when (visualEffect) {
            is BlurEffect -> {
                visualEffect.radius = binding.sbRadius.progress.toFloat()
                binding.llRadius.isVisible = true
                binding.tvRadius.text = "模糊半径(${binding.sbRadius.progress}/${binding.sbRadius.max})"
            }
            is MosaicEffect -> {
                visualEffect.boxSize = binding.sbBoxSize.progress
                binding.llBoxSize.isVisible = true
                binding.tvBoxSize.text =
                    "块尺寸(${binding.sbBoxSize.progress}/${binding.sbBoxSize.max})"
            }
            is WatermarkEffect -> {
                visualEffect.textSize = binding.sbWatermarkSize.progress.toFloat()
                binding.llWatermarkSize.isVisible = true
                binding.tvWatermarkSize.text =
                    "水印大小(${binding.sbWatermarkSize.progress}/${binding.sbWatermarkSize.max})"
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun newVisualEffectView(): VisualEffectView {
        val visualEffectView = VisualEffectView(this)
        visualEffectView.visualEffect = visualEffectArray[currVisualEffectIndex].second.invoke()
        visualEffectView.simpleSize = simpleSize
        return visualEffectView
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun newVisualEffectCard(closeable: Boolean): VisualEffectView {
        val cardView = CardView(this).apply {
            setCardBackgroundColor(Color.TRANSPARENT)
            cardElevation = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10F,
                resources.displayMetrics
            )
            radius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                6F,
                resources.displayMetrics
            )
        }
        val visualEffectView = newVisualEffectView()
        cardView.addView(
            visualEffectView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        val decorView = window.decorView as FrameLayout
        decorView.addView(cardView, FrameLayout.LayoutParams(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                120F,
                resources.displayMetrics
            ).toInt(),
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                120F,
                resources.displayMetrics
            ).toInt()
        ).also {
            it.gravity = Gravity.CENTER
        })
        DragGestureHelper.attach(cardView).apply {
            if (closeable) {
                onDoubleClick = { decorView.removeView(cardView) }
            }
        }
        return visualEffectView
    }

    private fun loadImage(data: Any?) {
        Glide.with(this)
            .load(data)
            .into(binding.ivOriginal)
    }

    companion object {
        private const val REQ_CODE_IMAGE = 1
    }
}