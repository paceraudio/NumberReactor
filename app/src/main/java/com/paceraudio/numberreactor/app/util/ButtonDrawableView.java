package com.paceraudio.numberreactor.app.util;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.View;

import com.paceraudio.numberreactor.app.R;

/**
 * Created by jeffwconaway on 1/8/15.
 */
public class ButtonDrawableView extends View {

    public ShapeDrawable mStartTriangleDisengaged;
    public ShapeDrawable mStartTriangleEngaged;
    public ShapeDrawable mStopSquare;

    private static final float ZERO = 0;
    private static final float SQUARE_SIDE_LENGTH = 800;
    private static final float TRIANGLE_SIDE_LENGTH = 300;
    private static final float HALF_TRIANGLE_SIDE_LENGTH = TRIANGLE_SIDE_LENGTH / 2;
    private static final float TRIANGLE_SIDE_SQUARED = (float) Math.pow(TRIANGLE_SIDE_LENGTH, 2);
    private static final float HALF_TRIANGLE_SIDE_SQUARED = (float) Math.pow(TRIANGLE_SIDE_LENGTH / 2, 2);
    private static final float TRIANGLE_WIDTH_SQUARED = TRIANGLE_SIDE_SQUARED - HALF_TRIANGLE_SIDE_SQUARED;
    private static final float TRIANGLE_WIDTH = (float) Math.sqrt(TRIANGLE_WIDTH_SQUARED);
    private static final float TRIANGLE_X_INSET = (SQUARE_SIDE_LENGTH - TRIANGLE_WIDTH) / 2;
    private static final float TRIANGLE_Y_INSET = (SQUARE_SIDE_LENGTH - TRIANGLE_SIDE_LENGTH) / 2;


    public ButtonDrawableView(Context context) {
        super(context);

//        Log.d("jwc", "triangle side squared")

        int grey = context.getResources().getColor(R.color.grey);
        int green = context.getResources().getColor(R.color.green);
        int brown = context.getResources().getColor(R.color.brown);

        Path triangle = new Path();
        triangle.moveTo(ZERO, ZERO);
        triangle.lineTo(SQUARE_SIDE_LENGTH, ZERO);
        triangle.lineTo(SQUARE_SIDE_LENGTH, SQUARE_SIDE_LENGTH);
        triangle.lineTo(ZERO, SQUARE_SIDE_LENGTH);
        triangle.close();
        triangle.moveTo(TRIANGLE_X_INSET, TRIANGLE_Y_INSET);
        triangle.lineTo(TRIANGLE_WIDTH + TRIANGLE_X_INSET, HALF_TRIANGLE_SIDE_LENGTH + TRIANGLE_Y_INSET);
        triangle.lineTo(TRIANGLE_X_INSET, TRIANGLE_SIDE_LENGTH + TRIANGLE_Y_INSET);
        triangle.close();

        RectShape square = new RectShape();

        //mStartTriangleDisengaged = new ShapeDrawable(new PathShape(triangle, TRIANGLE_WIDTH, TRIANGLE_SIDE_LENGTH));
        mStartTriangleDisengaged = new ShapeDrawable(new PathShape(triangle, SQUARE_SIDE_LENGTH, SQUARE_SIDE_LENGTH));
        Paint paint = mStartTriangleDisengaged.getPaint();
        mStartTriangleDisengaged.setIntrinsicHeight(10);
        mStartTriangleDisengaged.setIntrinsicWidth(10);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(brown);
        paint.setStrokeWidth(20);

        mStartTriangleEngaged = new ShapeDrawable(new PathShape(triangle, SQUARE_SIDE_LENGTH, SQUARE_SIDE_LENGTH));
        Paint engagedPaint = mStartTriangleEngaged.getPaint();
        mStartTriangleDisengaged.setIntrinsicHeight(10);
        mStartTriangleDisengaged.setIntrinsicWidth(10);
        engagedPaint.setStyle(Paint.Style.STROKE);
        engagedPaint.setColor(green);
        engagedPaint.setStrokeWidth(20);

        mStopSquare = new ShapeDrawable(new RectShape());
        mStopSquare.setIntrinsicHeight(50);
        mStopSquare.setIntrinsicWidth(50);
        mStopSquare.getPaint().setColor(green);
    }

    public ButtonDrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
