package com.calorieminer.minerapp.VisionPackage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.media.Image;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.google.api.services.vision.v1.model.BoundingPoly;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Landmark;
import com.google.api.services.vision.v1.model.Vertex;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by p1 on 12/15/17.
 */

public class GraphicOverlayView extends android.support.v7.widget.AppCompatImageView {

    private static final float BOX_STROKE_WIDTH = 5.0f;
    private final Object mLock = new Object();

    private int mPreviewWidth = 240;
    private float mWidthScaleFactor = 1.0f;
    private int mPreviewHeight = 320;
    private float mHeightScaleFactor = 1.0f;

    private FaceAnnotation mFaceAnnotation;
    private BoundingPoly mBoundingPoly, mFDBoundingPoly;
    private List<Landmark> mLandMarks;

    private Paint mIdPaint;
    private Paint mBoxPaint;



    public GraphicOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //In versions > 3.0 need to define layer Type
        if (android.os.Build.VERSION.SDK_INT >= 19)
        {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public void startOverlay(FaceAnnotation mFaceAnnotation, int w, int h) {

        this.mFaceAnnotation = mFaceAnnotation;
        if (mFaceAnnotation == null)
        {
            postInvalidate();
            return;
        }

        this.mPreviewWidth = w;
        this.mPreviewHeight = h;

        if (mFaceAnnotation.getBoundingPoly()!=null)
        {
            this.mBoundingPoly = mFaceAnnotation.getBoundingPoly();
            this.mFDBoundingPoly = mFaceAnnotation.getFdBoundingPoly();
        }

        if (mFaceAnnotation.getLandmarks()!=null)
        {
            this.mLandMarks = mFaceAnnotation.getLandmarks();
        }
        //Redraw after defining circle
        postInvalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mFaceAnnotation == null)
        {
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            return;
        }

        mWidthScaleFactor = (float) canvas.getWidth() / mPreviewWidth;// 4
        mHeightScaleFactor = (float) canvas.getHeight()/mPreviewHeight;// 5.5

        mIdPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIdPaint.setColor(getResources().getColor(android.R.color.holo_green_light));
        mIdPaint.setStyle(Paint.Style.FILL);

        mBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBoxPaint.setColor(getResources().getColor(android.R.color.holo_green_light));
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
//        canvas.drawPaint(mIdPaint);

        int[] x = {0, 0, 0, 0};
        int[] y = {0, 0, 0, 0};
        for (int i=0; i< mBoundingPoly.getVertices().size(); i++)
        {
            Vertex mVertex = mBoundingPoly.getVertices().get(i);
            if (mVertex == null)
                continue;

            if (mBoundingPoly.getVertices().get(i).getX() != null)
                x[i] = (int) (mBoundingPoly.getVertices().get(i).getX()*mWidthScaleFactor);

            if (mBoundingPoly.getVertices().get(i).getY() != null)
                y[i] = (int) (mBoundingPoly.getVertices().get(i).getY()*mHeightScaleFactor);
        }

        canvas.drawRect(x[0], y[0], x[2], y[2], mBoxPaint);

        for (Landmark landmark: mLandMarks)
        {
            float cx = landmark.getPosition().getX()*mWidthScaleFactor;
            float cy = landmark.getPosition().getY()*mHeightScaleFactor;
            canvas.drawCircle(cx, cy, 10, mIdPaint);
        }

        for (int i=0; i< mFDBoundingPoly.getVertices().size(); i++)
        {
            if (mFDBoundingPoly.getVertices().get(i).getX() != null)
                x[i] = (int) (mFDBoundingPoly.getVertices().get(i).getX()*mWidthScaleFactor);

            if (mFDBoundingPoly.getVertices().get(i).getY() != null)
                y[i] = (int) (mFDBoundingPoly.getVertices().get(i).getY()*mHeightScaleFactor);
        }

        canvas.drawRect(x[0], y[0], x[2], y[2], mBoxPaint);

    }
}
