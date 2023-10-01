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

import java.util.Map;
import java.util.SortedMap;

public class OverlayView extends View {

    private SortedMap<Float,Map<String,SpeciesObject>> result;

    private Paint domainPaint = new Paint();
    private Paint domainProbability = new Paint();
    private Rect domainBounds = new Rect();

    private Paint kingdomPaint = new Paint();
    private Paint kingdomProbability = new Paint();
    private Rect kingdomBounds = new Rect();

    private Paint phylumPaint = new Paint();
    private Paint phylumProbability = new Paint();
    private Rect phylumBounds = new Rect();

    private Paint classPaint = new Paint();
    private Paint classProbability = new Paint();
    private Rect classBounds = new Rect();

    private Paint orderPaint = new Paint();
    private Paint orderProbability = new Paint();
    private Rect orderBounds = new Rect();

    private Paint familyPaint = new Paint();
    private Paint familyProbability = new Paint();
    private Rect familyBounds = new Rect();

    private Paint genusPaint = new Paint();
    private Paint genusProbability = new Paint();
    private Rect genusBounds = new Rect();

    private Paint speciesPaint = new Paint();
    private Paint speciesProbability = new Paint();
    private Rect speciesBounds = new Rect();


    SpeciesObject speciesObject;
    SpeciesObject genusObject;
    SpeciesObject familyObject;
    SpeciesObject orderObject;
    SpeciesObject classObject;
    SpeciesObject phylumObject;
    SpeciesObject kingdomObject;
    SpeciesObject domainObject;

    public OverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    public OverlayView(Context context){
        super(context);
        initPaints();
    }

    public void clear() {
        domainPaint.reset();
        kingdomPaint.reset();
        phylumPaint.reset();
        classPaint.reset();
        orderPaint.reset();
        familyPaint.reset();
        genusPaint.reset();
        speciesPaint.reset();
        invalidate();
        initPaints();
    }

    private void initPaints() {

        domainPaint.setStyle(Paint.Style.FILL);
        domainPaint.setStrokeWidth(5f);
        domainPaint.setTextSize(50f);
        domainPaint.setShadowLayer(10,5,5,Color.BLACK);
        domainPaint.setColor(Color.rgb(255,0,0));
        domainPaint.setColor(Color.WHITE);

        kingdomPaint.setStyle(Paint.Style.FILL);
        kingdomPaint.setStrokeWidth(5f);
        kingdomPaint.setTextSize(50f);
        kingdomPaint.setShadowLayer(10,5,5,Color.BLACK);
        kingdomPaint.setColor(Color.rgb(230,145,56));
        kingdomPaint.setColor(Color.WHITE);

        phylumPaint.setStyle(Paint.Style.FILL);
        phylumPaint.setStrokeWidth(5f);
        phylumPaint.setTextSize(50f);
        phylumPaint.setShadowLayer(10,5,5,Color.BLACK);
        phylumPaint.setColor(Color.rgb(241,194,50));
        phylumPaint.setColor(Color.WHITE);

        classPaint.setStyle(Paint.Style.FILL);
        classPaint.setStrokeWidth(5f);
        classPaint.setTextSize(50f);
        classPaint.setShadowLayer(10,5,5,Color.BLACK);
        classPaint.setColor(Color.rgb(106,168,79));
        classPaint.setColor(Color.WHITE);

        orderPaint.setStyle(Paint.Style.FILL);
        orderPaint.setStrokeWidth(5f);
        orderPaint.setTextSize(50f);
        orderPaint.setShadowLayer(10,5,5,Color.BLACK);
        orderPaint.setColor(Color.rgb(61,133,198));
        orderPaint.setColor(Color.WHITE);

        familyPaint.setStyle(Paint.Style.FILL);
        familyPaint.setStrokeWidth(5f);
        familyPaint.setTextSize(50f);
        familyPaint.setShadowLayer(10,5,5,Color.BLACK);
        familyPaint.setColor(Color.rgb(103,78,167));
        familyPaint.setColor(Color.WHITE);

        genusPaint.setStyle(Paint.Style.FILL);
        genusPaint.setStrokeWidth(5f);
        genusPaint.setTextSize(50f);
        genusPaint.setShadowLayer(10,5,5,Color.BLACK);
        genusPaint.setColor(Color.rgb(166,77,121));
        genusPaint.setColor(Color.WHITE);

        speciesPaint.setStyle(Paint.Style.FILL);
        speciesPaint.setStrokeWidth(5f);
        speciesPaint.setTextSize(50f);
        speciesPaint.setShadowLayer(10,5,5,Color.BLACK);
        speciesPaint.setColor(Color.rgb(183,118,173));
        speciesPaint.setColor(Color.WHITE);

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if(result!=null) {

            float width = getWidth()*1f/16;
            float height = getHeight()*1f/4;

            if(result.get(100.0f) != null && result.get(100.0f).get("stateofmatter") != null){
                domainObject = result.get(100.0f).get("stateofmatter");
                String drawableText = "Domain: " + domainObject.name + " ("+(Math.round(domainObject.score * 10000.0) / 100.0)+"%)";
                domainPaint.getTextBounds(drawableText,0,drawableText.length(),domainBounds);
                canvas.drawText(drawableText, width, height, domainPaint);
            }

            if(result.get(70.0f) != null && result.get(70.0f).get("kingdom") != null){
                kingdomObject = result.get(70.0f).get("kingdom");
                height = height + 100;
                String drawableText = "Kingdom: " + kingdomObject.name + " ("+(Math.round(kingdomObject.score * 10000.0) / 100.0)+"%)";
                kingdomPaint.getTextBounds(drawableText,0,drawableText.length(),kingdomBounds);
                canvas.drawText(drawableText, width, height, kingdomPaint);
            }

            if(result.get(60.0f) != null && result.get(60.0f).get("phylum") != null){
                phylumObject = result.get(60.0f).get("phylum");
                height = height + 100;
                String drawableText = "Phylum: " + phylumObject.name + " ("+(Math.round(phylumObject.score * 10000.0) / 100.0)+"%)";
                phylumPaint.getTextBounds(drawableText,0,drawableText.length(),phylumBounds);
                canvas.drawText(drawableText, width, height, phylumPaint);
            }

            if(result.get(50.0f) != null && result.get(50.0f).get("class") != null){
                classObject = result.get(50.0f).get("class");
                height = height + 100;
                String drawableText = "Class: " + classObject.name + " ("+(Math.round(classObject.score * 10000.0) / 100.0)+"%)";
                classPaint.getTextBounds(drawableText,0,drawableText.length(),classBounds);
                canvas.drawText(drawableText, width, height, classPaint);
            }

            if(result.get(40.0f) != null && result.get(40.0f).get("order") != null){
                orderObject = result.get(40.0f).get("order");
                height = height + 100;
                String drawableText = "Order: " + orderObject.name + " ("+(Math.round(orderObject.score * 10000.0) / 100.0)+"%)";
                orderPaint.getTextBounds(drawableText,0,drawableText.length(),orderBounds);
                canvas.drawText(drawableText, width, height, orderPaint);
            }

            if(result.get(30.0f) != null && result.get(30.0f).get("family") != null){
                familyObject = result.get(30.0f).get("family");
                height = height + 100;
                String drawableText = "Family: " + familyObject.name + " ("+(Math.round(familyObject.score * 10000.0) / 100.0)+"%)";
                familyPaint.getTextBounds(drawableText,0,drawableText.length(),familyBounds);
                canvas.drawText(drawableText, width, height, familyPaint);
            }

            if(result.get(20.0f) != null && result.get(20.0f).get("genus") != null){
                genusObject = result.get(20.0f).get("genus");
                height = height + 100;
                String drawableText = "Genus: " + genusObject.name + " ("+(Math.round(genusObject.score * 10000.0) / 100.0)+"%)";
                genusPaint.getTextBounds(drawableText,0,drawableText.length(),genusBounds);
                canvas.drawText(drawableText, width, height, genusPaint);
            }

            if(result.get(10.0f) != null && result.get(10.0f).get("species") != null){
                speciesObject = result.get(10.0f).get("species");
                height = height + 100;
                String drawableText = "Species: " + speciesObject.name + " ("+(Math.round(speciesObject.score * 10000.0) / 100.0)+"%)";
                speciesPaint.getTextBounds(drawableText,0,drawableText.length(),speciesBounds);
                canvas.drawText(drawableText, width, height, speciesPaint);
            }

        }else{
            clear();
        }
    }

    public void setResults(SortedMap<Float, Map<String,SpeciesObject>> result){
        this.result = result;
    }
}
