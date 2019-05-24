package vn.edu.usth.usthspeechrecord;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

public class MediaButton extends AppCompatImageButton {
    int state;
    int numState = 2;

    public MediaButton(Context context) {
        super(context);
        state = 0;
    }

    public MediaButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        state = 0;
    }

    public MediaButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        state = 0;
    }

    public void changeState() {
        state = (state + 1) % numState;
    }

    public int getState() { return state; }
}
