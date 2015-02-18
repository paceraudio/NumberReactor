package com.paceraudio.numberreactor.app.util;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.AttributeSet;
import android.view.View;

import com.paceraudio.numberreactor.app.R;

/**
 * Created by jeffwconaway on 1/8/15.
 */
public class ButtonDrawableView extends View {

    public LayerDrawable mStartEngagedDrawables;
    public LayerDrawable mStartDisengagedDrawables;
    public LayerDrawable mStartArmed;
    public LayerDrawable mStopEngagedDrawables;
    public LayerDrawable mStopDisengagedDrawables;
    public LayerDrawable mStopArmed;

    private static final float ZERO = 0;
    private static final float FRAME_SIDE_LENGTH = 800;
    private static final float TRIANGLE_SIDE_LENGTH = 350;
    private static final float SQUARE_SIDE_LENGTH = 300;
    private static final float HALF_TRIANGLE_SIDE_LENGTH = TRIANGLE_SIDE_LENGTH / 2;
    private static final float TRIANGLE_SIDE_SQUARED = (float) Math.pow(TRIANGLE_SIDE_LENGTH, 2);
    private static final float HALF_TRIANGLE_SIDE_SQUARED = (float) Math.pow(TRIANGLE_SIDE_LENGTH / 2, 2);
    private static final float TRIANGLE_WIDTH_SQUARED = TRIANGLE_SIDE_SQUARED - HALF_TRIANGLE_SIDE_SQUARED;
    private static final float TRIANGLE_WIDTH = (float) Math.sqrt(TRIANGLE_WIDTH_SQUARED);
    private static final float TRIANGLE_X_INSET = (FRAME_SIDE_LENGTH - TRIANGLE_WIDTH) / 2;
    private static final float TRIANGLE_Y_INSET = (FRAME_SIDE_LENGTH - TRIANGLE_SIDE_LENGTH) / 2;
    private static final float SQUARE_X_INSET = (FRAME_SIDE_LENGTH - SQUARE_SIDE_LENGTH) / 2;
    private static final float SQUARE_Y_INSET = SQUARE_X_INSET;
    private static final float ICON_STROKE_WIDTH = 22;
    private static final float FRAME_STROKE_WIDTH = 10;


    public ButtonDrawableView(Context context) {
        super(context);

//        Log.d("jwc", "triangle side squared")

        int lightBrown = context.getResources().getColor(R.color.lightBrown);
        int brown = context.getResources().getColor(R.color.brown);
        int darkGrey = context.getResources().getColor(R.color.darkGrey);
        int grey = context.getResources().getColor(R.color.grey);
        int glowGreen = context.getResources().getColor(R.color.glowGreen);
        int green = context.getResources().getColor(R.color.green);
        int red = context.getResources().getColor(R.color.red);
        int darkRed = context.getResources().getColor(R.color.darkRed);
        int lightBlackRed = context.getResources().getColor(R.color.lightBlackRed);
        int darkBlackRed = context.getResources().getColor(R.color.darkBlackRed);

        PathShape frame = makeButtonFrame();
        PathShape startTriangle = makeButtonTriangleIcon();
        PathShape stopSquare = makeButtonSquareIcon();

        mStartDisengagedDrawables = layerButtonDrawables(new ShapeDrawable(frame), new ShapeDrawable(startTriangle), brown, darkGrey);
        mStartArmed = layerButtonDrawables(new ShapeDrawable(frame), new ShapeDrawable(startTriangle), lightBrown, grey);
        mStartEngagedDrawables = layerButtonDrawables(new ShapeDrawable(frame), new ShapeDrawable(startTriangle), glowGreen, glowGreen);
        mStopDisengagedDrawables = layerButtonDrawables(new ShapeDrawable(frame), new ShapeDrawable(stopSquare), brown, darkGrey);
        mStopArmed = layerButtonDrawables(new ShapeDrawable(frame), new ShapeDrawable(stopSquare), lightBrown, grey);
        mStopEngagedDrawables = layerButtonDrawables(new ShapeDrawable(frame), new ShapeDrawable(stopSquare), red, red);
    }

    public ButtonDrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private PathShape makeButtonFrame() {
        Path path = new Path();
        path.moveTo(ZERO, ZERO);
        path.lineTo(FRAME_SIDE_LENGTH, ZERO);
        path.lineTo(FRAME_SIDE_LENGTH, FRAME_SIDE_LENGTH);
        path.lineTo(ZERO, FRAME_SIDE_LENGTH);
        path.close();
        return new PathShape(path, FRAME_SIDE_LENGTH, FRAME_SIDE_LENGTH);
    }

    private PathShape makeButtonTriangleIcon() {
        Path path = new Path();
        path.moveTo(TRIANGLE_X_INSET, TRIANGLE_Y_INSET);
        path.lineTo(TRIANGLE_WIDTH + TRIANGLE_X_INSET, HALF_TRIANGLE_SIDE_LENGTH + TRIANGLE_Y_INSET);
        path.lineTo(TRIANGLE_X_INSET, TRIANGLE_SIDE_LENGTH + TRIANGLE_Y_INSET);
        path.close();
        return new PathShape(path, FRAME_SIDE_LENGTH, FRAME_SIDE_LENGTH);
    }

    private PathShape makeButtonSquareIcon() {
        Path path = new Path();
        path.moveTo(SQUARE_X_INSET, SQUARE_Y_INSET);
        path.lineTo(SQUARE_X_INSET + SQUARE_SIDE_LENGTH, SQUARE_Y_INSET);
        path.lineTo(SQUARE_X_INSET + SQUARE_SIDE_LENGTH, SQUARE_Y_INSET + SQUARE_SIDE_LENGTH);
        path.lineTo(SQUARE_X_INSET, SQUARE_Y_INSET + SQUARE_SIDE_LENGTH);
        path.close();
        return new PathShape(path, FRAME_SIDE_LENGTH, FRAME_SIDE_LENGTH);
    }

    private void definePaintTraitsForButtonDrawables(ShapeDrawable shapeDrawable, int color, float strokeWidth) {
        Paint paint = shapeDrawable.getPaint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
    }

    private LayerDrawable layerButtonDrawables(ShapeDrawable frame, ShapeDrawable icon, int frameColor, int iconColor ) {
        definePaintTraitsForButtonDrawables(frame, frameColor, FRAME_STROKE_WIDTH);
        definePaintTraitsForButtonDrawables(icon, iconColor, ICON_STROKE_WIDTH);
        Drawable[] drawables = {frame, icon};
        return new LayerDrawable(drawables);
    }
}
