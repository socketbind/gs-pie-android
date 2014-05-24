package org.glasshack.pie;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by gabriel on 2014.05.24..
 */
public class TimerStepView extends SimpleStepView implements ScrollAware {

    private TextView tvTimer;

    private int originalTimerSeconds, timerSeconds;

    private Timer countDownTimer;

    private Handler uiHandler;

    private boolean timerStarted = false;

    public TimerStepView(Context context, String stepText, int timerSeconds) {
        super(context, stepText, R.layout.timer_step);

        uiHandler = new Handler(Looper.getMainLooper());

        originalTimerSeconds = timerSeconds;

        tvTimer = (TextView) findViewById(R.id.timer_digits);

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
                    if (timerSeconds == 0) {
                        cancelTimer();
                    } else {
                        updateTimer(--timerSeconds);
                    }
                }
            });
        }
    }

    @Override
    public void activated() {
        countDownTimer = new Timer();

        timerSeconds = originalTimerSeconds;
        updateTimer(timerSeconds);
    }

    @Override
    public void deactivated() {
        cancelTimer();
    }

    private void cancelTimer() {
        countDownTimer.cancel();
        countDownTimer.purge();
        countDownTimer = null;

        timerStarted = false;
    }

    public void startTimer() {
        if (!timerStarted) {
            timerStarted = true;

            countDownTimer.scheduleAtFixedRate(new UpdateTimerTask(), 0, 1000);
        }
    }

    @Override
    public void onTapped() {
        startTimer();
    }
}
