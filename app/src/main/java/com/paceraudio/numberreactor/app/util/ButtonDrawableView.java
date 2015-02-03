package com.paceraudio.numberreactor.app.util;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
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
    public ShapeDrawable mStartFrameDisengaged;
    public ShapeDrawable mStartTriangleEngaged;
    public ShapeDrawable mStartFrameEngaged;
    public LayerDrawable mStartEngagedDrawables;
    public LayerDrawable mStartDisengagedDrawables;
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
    private static final float ICON_STROKE_WIDTH = 30;
    private static final float FRAME_STROKE_WIDTH = 10;


    public ButtonDrawableView(Context context) {
        super(context);

//        Log.d("jwc", "triangle side squared")

        int grey = context.getResources().getColor(R.color.grey);
        int green = context.getResources().getColor(R.color.green);
        int brown = context.getResources().getColor(R.color.brown);

        Path frame = new Path();
        frame.moveTo(ZERO, ZERO);
        frame.lineTo(SQUARE_SIDE_LENGTH, ZERO);
        frame.lineTo(SQUARE_SIDE_LENGTH, SQUARE_SIDE_LENGTH);
        frame.lineTo(ZERO, SQUARE_SIDE_LENGTH);
        frame.close();

        Path triangle = new Path();
        /*triangle.moveTo(ZERO, ZERO);
        triangle.lineTo(SQUARE_SIDE_LENGTH, ZERO);
        triangle.lineTo(SQUARE_SIDE_LENGTH, SQUARE_SIDE_LENGTH);
        triangle.lineTo(ZERO, SQUARE_SIDE_LENGTH);
        triangle.close();*/
        triangle.moveTo(TRIANGLE_X_INSET, TRIANGLE_Y_INSET);
        triangle.lineTo(TRIANGLE_WIDTH + TRIANGLE_X_INSET, HALF_TRIANGLE_SIDE_LENGTH + TRIANGLE_Y_INSET);
        triangle.lineTo(TRIANGLE_X_INSET, TRIANGLE_SIDE_LENGTH + TRIANGLE_Y_INSET);
        triangle.close();


        mStartTriangleDisengaged = new ShapeDrawable(new PathShape(triangle, SQUARE_SIDE_LENGTH, SQUARE_SIDE_LENGTH));
        definePaintTraitsForButtonDrawables(mStartTriangleDisengaged, grey, ICON_STROKE_WIDTH);
        mStartFrameDisengaged = new ShapeDrawable(new PathShape(frame, SQUARE_SIDE_LENGTH, SQUARE_SIDE_LENGTH));
        definePaintTraitsForButtonDrawables(mStartFrameDisengaged, brown, FRAME_STROKE_WIDTH);
        Drawable[] startDisengagedDrawables = {mStartFrameDisengaged, mStartTriangleDisengaged};
        mStartDisengagedDrawables = new LayerDrawable(startDisengagedDrawables);

        mStartTriangleEngaged = new ShapeDrawable(new PathShape(triangle, SQUARE_SIDE_LENGTH, SQUARE_SIDE_LENGTH));
        definePaintTraitsForButtonDrawables(mStartTriangleEngaged, green, ICON_STROKE_WIDTH);
        mStartFrameEngaged = new ShapeDrawable(new PathShape(frame, SQUARE_SIDE_LENGTH, SQUARE_SIDE_LENGTH));
        definePaintTraitsForButtonDrawables(mStartFrameEngaged, green, FRAME_STROKE_WIDTH);
        Drawable[]startEngagedDrawables = {mStartFrameEngaged, mStartTriangleEngaged};
        mStartEngagedDrawables = new LayerDrawable(startEngagedDrawables);


        mStopSquare = new ShapeDrawable(new RectShape());
        mStopSquare.setIntrinsicHeight(50);
        mStopSquare.setIntrinsicWidth(50);
        mStopSquare.getPaint().setColor(green);
    }

    public ButtonDrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void definePaintTraitsForButtonDrawables(ShapeDrawable shapeDrawable, int color, float strokeWidth) {
        Paint paint = shapeDrawable.getPaint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
    }
}
