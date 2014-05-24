package org.glasshack.pie;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by gabriel on 2014.05.24..
 */
public class SimpleStepView extends FrameLayout implements ScrollAware {

    protected TextView tvStepText;

    protected TextView tvFooter;

    protected TextView tvTimestamp;

    protected ImageView ivStepImage;

    public SimpleStepView(Context context, String stepText) {
        this(context, stepText, R.layout.simple_step);
    }

    public SimpleStepView(Context context, String stepText, int layout) {
        super(context);

        setFocusable(true);
        setFocusableInTouchMode(true);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layout, this, true);

        tvStepText = (TextView) findViewById(R.id.step_text);
        tvStepText.setText(stepText);

        tvFooter = (TextView) findViewById(R.id.footer);
        tvTimestamp = (TextView) findViewById(R.id.timestamp);

        ivStepImage = (ImageView) findViewById(R.id.step_image);
    }

    @Override
    public void activated() {
    }

    @Override
    public void deactivated() {
    }

    @Override
    public void onTapped() {
    }

    public void setFooter(String text) {
        tvFooter.setText(text);
    }

    public void setTimestamp(String text) {
        tvTimestamp.setText(text);
    }

    public void setImageResource(int resId) {
        ivStepImage.setImageResource(resId);
        ivStepImage.setAlpha(0.4f);
    }
}
