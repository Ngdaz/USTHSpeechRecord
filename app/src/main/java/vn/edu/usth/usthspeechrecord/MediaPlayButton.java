package vn.edu.usth.usthspeechrecord;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

public class MediaPlayButton extends AppCompatButton {
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
