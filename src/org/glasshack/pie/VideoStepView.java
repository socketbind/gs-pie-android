package org.glasshack.pie;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Created by gabriel on 2014.05.25..
 */
public class VideoStepView extends VideoView implements ScrollAware {

    private boolean prepared = false, activated = false;

    public VideoStepView(Context context, String url) {
        super(context);

        /*MediaController mediacontroller = new MediaController(getContext());
        mediacontroller.setAnchorView(this);*/

        Uri videoUri = Uri.parse(url);
        //setMediaController(mediacontroller);

        setVideoURI(videoUri);


        setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                prepared = true;
                if (activated) {
                    start();
                }
            }
        });
    }

    @Override
    public void activated() {
        activated = true;

        if (prepared) {
            start();
        }
    }

    @Override
    public void deactivated() {
        activated = false;

        if (prepared) {
            pause();
        }
    }

    @Override
    public void onTapped() {
        if (prepared) {
            if (isPlaying()) {
                pause();
            } else {
                resume();
            }
        }
    }
}
