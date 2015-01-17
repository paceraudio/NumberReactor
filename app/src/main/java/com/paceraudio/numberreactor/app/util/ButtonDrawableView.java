package com.paceraudio.numberreactor.app.util;

import android.content.Context;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.paceraudio.numberreactor.app.R;

/**
 * Created by jeffwconaway on 1/8/15.
 */
public class ButtonDrawableView extends View {

    public ShapeDrawable mStartTriangle;
    public ShapeDrawable mStopSquare;



    private static final float TRIANGLE_SIDE_LENGTH = 400;
    private static final float HALF_TRIANGLE_SIDE_LENGTH = TRIANGLE_SIDE_LENGTH / 2;
    private static final float TRIANGLE_SIDE_SQUARED = (float) Math.pow(TRIANGLE_SIDE_LENGTH, 2);
    private static final float HALF_TRIANGLE_SIDE_SQUARED = (float) Math.pow(TRIANGLE_SIDE_LENGTH / 2, 2);
    private static final float TRIANGLE_WIDTH_SQUARED = TRIANGLE_SIDE_SQUARED - HALF_TRIANGLE_SIDE_SQUARED;
    private static final float TRIANGLE_WIDTH = (float) Math.sqrt(TRIANGLE_WIDTH_SQUARED);

    public ButtonDrawableView(Context context) {
        super(context);

//        Log.d("jwc", "triangle side squared")

        int color = context.getResources().getColor(R.color.green);

        Path triangle = new Path();
        triangle.moveTo(0, 0);
        triangle.lineTo(TRIANGLE_WIDTH, HALF_TRIANGLE_SIDE_LENGTH);
        triangle.lineTo(0, TRIANGLE_SIDE_LENGTH);
        triangle.close();

        RectShape square = new RectShape();

        mStartTriangle = new ShapeDrawable(new PathShape(triangle, TRIANGLE_WIDTH, TRIANGLE_SIDE_LENGTH));
        mStartTriangle.setIntrinsicHeight(10);
        mStartTriangle.setIntrinsicWidth(10);
        mStartTriangle.getPaint().setColor(color);
        mStartTriangle.getPaint().setStrokeWidth(2);


        mStopSquare = new ShapeDrawable(new RectShape());
        mStopSquare.setIntrinsicHeight(50);
        mStopSquare.setIntrinsicWidth(50);
        mStopSquare.getPaint().setColor(color);
    }

    public ButtonDrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
