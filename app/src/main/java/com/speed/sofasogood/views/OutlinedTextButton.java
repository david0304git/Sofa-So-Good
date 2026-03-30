package com.speed.sofasogood.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

public class OutlinedTextButton extends AppCompatButton {

    private int strokeColor = 0xFF5A3A1A;
    private float strokeWidth = 6f;

    public OutlinedTextButton(Context context) { super(context); }
    public OutlinedTextButton(Context context, AttributeSet attrs) { super(context, attrs); }
    public OutlinedTextButton(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    @Override
    protected void onDraw(Canvas canvas) {
        int textColor = getCurrentTextColor();
        Paint paint = getPaint();

        // Draw stroke
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        setTextColor(strokeColor);
        super.onDraw(canvas);

        // Draw fill
        paint.setStyle(Paint.Style.FILL);
        setTextColor(textColor);
        super.onDraw(canvas);
    }
}
