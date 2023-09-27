package com.persistent.bionation.ui.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.persistent.bionation.R;

public class OverlayView extends View {

    private Prediction result;
    private Paint textPaint = new Paint();
    private Paint probability = new Paint();
    private Rect textBounds = new Rect();

    public OverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    public OverlayView(Context context){
        super(context);
        initPaints();
    }

    public void clear() {
        textPaint.reset();
        probability.reset();
        invalidate();
        initPaints();
    }

    private void initPaints() {

        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(5f);
        textPaint.setTextSize(80f);
        textPaint.setShadowLayer(10,5,5,Color.BLACK);

        probability = textPaint;

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if(result!=null) {

            String drawableText = result.node.name;
            textPaint.getTextBounds(drawableText,0,drawableText.length(),textBounds);
            canvas.drawText(drawableText, getWidth()*1f/4, getHeight()*1f/4, textPaint);
            Double probabilityText = result.probability;
            canvas.drawText(""+String.valueOf(probabilityText*100).substring(0,5)+"%", getWidth()*1f/4+150, (getHeight()*1f/4)+150, probability);

        }else{
            clear();
        }
    }

    public void setResults(Prediction result){
        this.result = result;
    }
}
