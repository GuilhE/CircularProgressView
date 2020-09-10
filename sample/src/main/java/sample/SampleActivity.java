package sample;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.github.guilhe.circularprogressview.sample.R;
import com.github.guilhe.circularprogressview.sample.databinding.ActivitySampleEditorBinding;
import com.github.guilhe.views.CircularProgressView;
import com.github.guilhe.views.ProgressThumbScaleType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SampleActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, RadioGroup.OnCheckedChangeListener {

    private ActivitySampleEditorBinding binding;
    private boolean transparent;
    private Toast toast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sample_editor);

        binding.sizeSeekBar.setOnSeekBarChangeListener(this);
        binding.thicknessSeekBar.setOnSeekBarChangeListener(this);
        binding.thumbsizeSeekBar.setOnSeekBarChangeListener(this);
        binding.progressSeekBar.setOnSeekBarChangeListener(this);
        binding.angleSeekBar.setOnSeekBarChangeListener(this);
        binding.colorRSeekBar.setOnSeekBarChangeListener(this);
        binding.colorGSeekBar.setOnSeekBarChangeListener(this);
        binding.colorBSeekBar.setOnSeekBarChangeListener(this);
        binding.bgRSeekBar.setOnSeekBarChangeListener(this);
        binding.bgGSeekBar.setOnSeekBarChangeListener(this);
        binding.bgBSeekBar.setOnSeekBarChangeListener(this);
        binding.thumbScaleGroup.setOnCheckedChangeListener(this);

        binding.roundedSwitch.setOnCheckedChangeListener((compoundButton, checked) -> binding.sampleCircularProgressView.setProgressRounded(checked));
        binding.shadowSwitch.setOnCheckedChangeListener((compoundButton, checked) -> binding.sampleCircularProgressView.setShadowEnabled(checked));
        binding.thumbSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            binding.sampleCircularProgressView.setProgressThumbEnabled(checked);
            binding.thumbScaleAuto.setEnabled(checked);
            binding.thumbScalePoint.setEnabled(checked);
            binding.thumbScaleRate.setEnabled(checked);
        });
        binding.reverseSwitch.setOnCheckedChangeListener((compoundButton, checked) -> binding.sampleCircularProgressView.setReverseEnabled(checked));
        binding.alphaSwitch.setOnCheckedChangeListener(((compoundButton, checked) -> binding.sampleCircularProgressView.setBackgroundAlphaEnabled(checked)));
        binding.colorsSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (!transparent) {
                binding.sampleCircularProgressView.setProgressBackgroundColor(binding.sampleCircularProgressView.getProgressColor());
            }
        });
        binding.transparentSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            transparent = checked;
            binding.sampleCircularProgressView.setProgressBackgroundColor(Color.TRANSPARENT);
        });

        binding.sampleCircularProgressView.setActionCallback(new CircularProgressView.CircularProgressViewActionCallback() {
            @Override
            public void onProgressChanged(float progress) {
            }

            @Override
            public void onAnimationFinished(float progress) {
                if (toast != null) {
                    toast.cancel(); //Prevent toasts from overlapping.
                }
                toast = Toast.makeText(SampleActivity.this, progress + "%", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        binding.sampleFloatingActionButton.setOnClickListener(v -> {
            List<Float> values = new ArrayList<Float>() {{
                add(12.5f);
//                add(12.5f);
                add(25f);
                add(50f);
            }};
            binding.sampleCircularProgressView.setProgress(values, new ArrayList<Integer>() {{
                for (Float ignored : values) {
                    add(Color.rgb(new Random().nextInt(), new Random().nextInt(), new Random().nextInt()));
                }
            }});
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.size_SeekBar:
                binding.sampleCircularProgressView.setSize((int) (progress * getResources().getDisplayMetrics().density + 0.5f));
                break;
            case R.id.thickness_SeekBar:
                binding.sampleCircularProgressView.setProgressStrokeThickness((int) (progress * getResources().getDisplayMetrics().density + 0.5f));
                break;
            case R.id.thumbsize_SeekBar:
                float size;
                if (binding.sampleCircularProgressView.getProgressThumbScaleType() == ProgressThumbScaleType.RATE) {
                    size = progress / ((float) seekBar.getMax() / 2);
                    binding.sampleCircularProgressView.setProgressMaxThumbSizeRate(size);
                } else {
                    size = (progress * getResources().getDisplayMetrics().density + 0.5f);
                    binding.sampleCircularProgressView.setProgressThumbSize(size);
                }

                break;
            case R.id.progress_SeekBar:
                binding.sampleCircularProgressView.setProgress(progress, binding.animatedSwitch.isChecked());
                break;
            case R.id.angle_SeekBar:
                binding.sampleCircularProgressView.setStartingAngle(progress);
                break;
            case R.id.color_r_SeekBar:
                if (binding.colorsSwitch.isChecked()) {
                    binding.bgRSeekBar.setProgress(progress);
                }
                binding.sampleCircularProgressView.setProgressColor(Color.rgb(progress, binding.colorGSeekBar.getProgress(), binding.colorBSeekBar.getProgress()));
                break;
            case R.id.color_g_SeekBar:
                if (binding.colorsSwitch.isChecked()) {
                    binding.bgGSeekBar.setProgress(progress);
                }
                binding.sampleCircularProgressView.setProgressColor(Color.rgb(binding.colorRSeekBar.getProgress(), progress, binding.colorBSeekBar.getProgress()));
                break;
            case R.id.color_b_SeekBar:
                if (binding.colorsSwitch.isChecked()) {
                    binding.bgBSeekBar.setProgress(progress);
                }
                binding.sampleCircularProgressView.setProgressColor(Color.rgb(binding.colorRSeekBar.getProgress(), binding.colorGSeekBar.getProgress(), progress));
                break;
            case R.id.bg_r_SeekBar:
                if (binding.colorsSwitch.isChecked()) {
                    binding.colorRSeekBar.setProgress(progress);
                }
                if (!transparent) {
                    binding.sampleCircularProgressView.setProgressBackgroundColor(Color.rgb(progress, binding.bgGSeekBar.getProgress(), binding.bgBSeekBar.getProgress()));
                }
                break;
            case R.id.bg_g_SeekBar:
                if (binding.colorsSwitch.isChecked()) {
                    binding.colorGSeekBar.setProgress(progress);
                }
                if (!transparent) {
                    binding.sampleCircularProgressView.setProgressBackgroundColor(Color.rgb(binding.bgRSeekBar.getProgress(), progress, binding.bgBSeekBar.getProgress()));
                }
                break;
            case R.id.bg_b_SeekBar:
                if (binding.colorsSwitch.isChecked()) {
                    binding.colorBSeekBar.setProgress(progress);
                }
                if (!transparent) {
                    binding.sampleCircularProgressView.setProgressBackgroundColor(Color.rgb(binding.bgRSeekBar.getProgress(), binding.bgGSeekBar.getProgress(), progress));
                }
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.thumb_scale_point:
                binding.sampleCircularProgressView.setProgressThumbScaleType(ProgressThumbScaleType.POINT);
                binding.sampleCircularProgressView.setProgressThumbSize((binding.thumbsizeSeekBar.getProgress() * getResources().getDisplayMetrics().density + 0.5f));
                break;
            case R.id.thumb_scale_rate:
                binding.sampleCircularProgressView.setProgressThumbScaleType(ProgressThumbScaleType.RATE);
                float rate = (binding.thumbsizeSeekBar.getProgress() / (binding.thumbsizeSeekBar.getMax() / binding.sampleCircularProgressView.getProgressMaxThumbSizeRate()));
                binding.sampleCircularProgressView.setProgressMaxThumbSizeRate(rate);
                break;
            case R.id.thumb_scale_auto:
            default:
                binding.sampleCircularProgressView.setProgressThumbScaleType(ProgressThumbScaleType.AUTO);
        }
        binding.sampleCircularProgressView.requestLayout();
    }
}