package com.example.endlessrunner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CubeCharacterView extends androidx.appcompat.widget.AppCompatImageView {

    private static int color;
    private Paint characterPaint;


    public CubeCharacterView(Context context) {
        super(context);
        init();
    }

    public CubeCharacterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CubeCharacterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        characterPaint = new Paint();
        characterPaint.setColor(Color.RED); // Set the default color

        // Set the color based on the obstacle type
        setColor(Color.RED);
    }

    public static int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        characterPaint.setColor(color);
        invalidate(); // Redraw the character with the new color
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the character on the canvas
        int width = getWidth();
        int height = getHeight();
        int halfWidth = width / 2;
        int halfHeight = height / 2;
        float left = halfWidth - 50;
        float top = halfHeight - 50;
        float right = halfWidth + 50;
        float bottom = halfHeight + 50;

        canvas.drawRect(left, top, right, bottom, characterPaint);
        canvas.drawRect(halfWidth - 20, halfHeight - 20, halfWidth + 20, halfHeight + 20, characterPaint);
    }

    public CubeCharacterView get() {
        return null;
    }
}

