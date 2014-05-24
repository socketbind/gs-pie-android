package org.glasshack.pie;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by gabriel on 2014.05.24..
 */
public class TimerStepView extends FrameLayout implements ScrollAware {

    private TextView tvStepText;
    private TextView tvTimer;

    private int originalTimerSeconds, timerSeconds;

    private Timer countDownTimer;

    private Handler uiHandler;

    private boolean timerStarted = false;

    public TimerStepView(Context context, String stepText, int timerSeconds) {
        super(context);

        uiHandler = new Handler(Looper.getMainLooper());

        originalTimerSeconds = timerSeconds;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.timer_step, this, true);

        tvStepText = (TextView) findViewById(R.id.step_text);
        tvStepText.setText(stepText);

        tvTimer = (TextView) findViewById(R.id.timer_digits);

        countDownTimer = new Timer();

        updateTimer(timerSeconds);
    }

    private void updateTimer(int timerSeconds) {
        this.timerSeconds = timerSeconds;
        int minutes = timerSeconds / 60;
        int seconds = timerSeconds % 60;
        String formatted = String.format("%d:%02d", minutes, seconds);
        tvTimer.setText(formatted);
    }

    private class UpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateTimer(--timerSeconds);
                }
            });
        }
    }

    @Override
    public void activated() {
        timerSeconds = originalTimerSeconds;
        updateTimer(timerSeconds);
    }

    @Override
    public void deactivated() {
        countDownTimer.cancel();
        countDownTimer.purge();

        timerStarted = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && !timerStarted) {
            timerStarted = true;

            updateTimer(timerSeconds);
            countDownTimer.scheduleAtFixedRate(new UpdateTimerTask(), 1000, 1000);

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
