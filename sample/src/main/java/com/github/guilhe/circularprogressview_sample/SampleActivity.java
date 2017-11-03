package com.github.guilhe.circularprogressview_sample;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import com.github.guilhe.circularprogressview_sample.databinding.ActivitySampleEditorBinding;

/**
 * Created by gdelgado on 30/08/2017.
 */

public class SampleActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

    private ActivitySampleEditorBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_sample_editor);

        mBinding.sizeSeekBar.setOnSeekBarChangeListener(this);
        mBinding.thicknessSeekBar.setOnSeekBarChangeListener(this);
        mBinding.progressSeekBar.setOnSeekBarChangeListener(this);
        mBinding.angleSeekBar.setOnSeekBarChangeListener(this);
        mBinding.colorRSeekBar.setOnSeekBarChangeListener(this);
        mBinding.colorGSeekBar.setOnSeekBarChangeListener(this);
        mBinding.colorBSeekBar.setOnSeekBarChangeListener(this);
        mBinding.bgRSeekBar.setOnSeekBarChangeListener(this);
        mBinding.bgGSeekBar.setOnSeekBarChangeListener(this);
        mBinding.bgBSeekBar.setOnSeekBarChangeListener(this);

        mBinding.colorsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mBinding.sampleCircularProgressView.setBackgroundColor(mBinding.sampleCircularProgressView.getProgressColor());
            }
        });
        mBinding.shadowSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                mBinding.sampleCircularProgressView.setShadowEnabled(checked);
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        switch (seekBar.getId()) {
            case R.id.size_SeekBar:
                mBinding.sampleCircularProgressView.setSize((int) (i * getResources().getDisplayMetrics().density + 0.5f));
                break;
            case R.id.thickness_SeekBar:
                mBinding.sampleCircularProgressView.setProgressStrokeThickness((int) (i * getResources().getDisplayMetrics().density + 0.5f));
                break;
            case R.id.progress_SeekBar:
                mBinding.sampleCircularProgressView.setProgress(i, mBinding.animatedSwitch.isChecked());
                break;
            case R.id.angle_SeekBar:
                mBinding.sampleCircularProgressView.setStartingAngle(i);
                break;
            case R.id.color_r_SeekBar:
                if (mBinding.colorsSwitch.isChecked()) {
                    mBinding.sampleCircularProgressView.setColor(Color.rgb(i, mBinding.colorGSeekBar.getProgress(), mBinding.colorBSeekBar.getProgress()));
                } else {
                    mBinding.sampleCircularProgressView.setProgressColor(Color.rgb(i, mBinding.colorGSeekBar.getProgress(), mBinding.colorBSeekBar.getProgress()));
                }
                break;
            case R.id.color_g_SeekBar:
                if (mBinding.colorsSwitch.isChecked()) {
                    mBinding.sampleCircularProgressView.setColor(Color.rgb(mBinding.colorRSeekBar.getProgress(), i, mBinding.colorBSeekBar.getProgress()));
                } else {
                    mBinding.sampleCircularProgressView.setProgressColor(Color.rgb(mBinding.colorRSeekBar.getProgress(), i, mBinding.colorBSeekBar.getProgress()));
                }
                break;
            case R.id.color_b_SeekBar:
                if (mBinding.colorsSwitch.isChecked()) {
                    mBinding.sampleCircularProgressView.setColor(Color.rgb(mBinding.colorRSeekBar.getProgress(), mBinding.colorGSeekBar.getProgress(), i));
                } else {
                    mBinding.sampleCircularProgressView.setProgressColor(Color.rgb(mBinding.colorRSeekBar.getProgress(), mBinding.colorGSeekBar.getProgress(), i));
                }
                break;
            case R.id.bg_r_SeekBar:
                mBinding.sampleCircularProgressView.setBackgroundColor(Color.rgb(i, mBinding.bgGSeekBar.getProgress(), mBinding.bgBSeekBar.getProgress()));
                break;
            case R.id.bg_g_SeekBar:
                mBinding.sampleCircularProgressView.setBackgroundColor(Color.rgb(mBinding.bgRSeekBar.getProgress(), i, mBinding.bgBSeekBar.getProgress()));
                break;
            case R.id.bg_b_SeekBar:
                mBinding.sampleCircularProgressView.setBackgroundColor(Color.rgb(mBinding.bgRSeekBar.getProgress(), mBinding.bgGSeekBar.getProgress(), i));
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}