package vn.edu.usth.usthspeechrecord;

import android.content.Context;

import androidx.appcompat.widget.AppCompatImageButton;
import android.util.AttributeSet;


public class StateButton extends AppCompatImageButton {
    private int state;
    private int numState = 3;

    public StateButton(Context context) {
        super(context);
        state = 0;
    }

    public StateButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        state = 0;
    }

    public StateButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        state = 0;
    }

    public void changeState() {
        state = (state + 1) % numState;
    }

    public int getState() {
        return state;
    }
}
