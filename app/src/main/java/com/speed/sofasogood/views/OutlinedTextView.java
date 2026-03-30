package com.speed.sofasogood.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class OutlinedTextView extends AppCompatTextView {

    private int strokeColor = 0xFF5A3A1A;
    private float strokeWidth = 6f;

    public OutlinedTextView(Context context) { super(context); }
    public OutlinedTextView(Context context, AttributeSet attrs) { super(context, attrs); }
    public OutlinedTextView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    @Override
    protected void onDraw(Canvas canvas) {
        int textColor = getCurrentTextColor();
        Paint paint = getPaint();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        setTextColor(strokeColor);
        super.onDraw(canvas);

        paint.setStyle(Paint.Style.FILL);
        setTextColor(textColor);
        super.onDraw(canvas);
    }
}
