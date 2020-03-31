package vn.edu.usth.usthspeechrecord.views;

import android.content.Context;

import androidx.appcompat.widget.AppCompatImageButton;
import android.util.AttributeSet;

public class MediaPlayButton extends AppCompatImageButton {
    private int state;
    private int numState = 2;

    public MediaPlayButton(Context context) {
        super(context);
        state = 0;
    }

    public MediaPlayButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        state = 0;
    }

    public MediaPlayButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        state = 0;
    }

    public void changeState() {
        state = (state + 1) % numState;
    }

    public int getState() { return state; }

}
