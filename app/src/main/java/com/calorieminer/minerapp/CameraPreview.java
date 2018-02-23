package com.calorieminer.minerapp;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import static com.calorieminer.minerapp.OpenCVActivity.ratio;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback,Camera.PreviewCallback {
    public static SurfaceHolder mHolder;
    public static int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    public static Camera mCamera;
    private static final String TAG = "Camera";
    public static Camera.Size mDefaultSize;
    private Camera.Size mPictureSize;
    private byte[] FrameData = null;

    private boolean bProcessing = false;
    private int imageFormat;

    public static int lowh = 0;
    public static int maxh = 30;
    public static int lows = 160;
    public static int maxs = 255;
    public static int lowv = 165;
    public static int maxv = 255;

    Mat originalImage;
    public static double xscalefactor = 1;
    public static double yscalefactor = 1;

    public static double scalewid;
    public static double scalehei;

    public boolean detectflag;

    public static Mat circles;

    private List<MatOfPoint> contours;

    private Handler mHandler = new Handler(Looper.getMainLooper());


    public CameraPreview(Context context, Camera camera) {

        super(context);
        mCamera = camera;

        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


    }

    public void surfaceCreated(SurfaceHolder holder) {

        if(mCamera == null){
            return;
        }
        setWillNotDraw(false);
        try {
            if(mCamera != null) {
                mCamera.setPreviewCallback(this);
            }
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        if (mHolder.getSurface() == null){
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e){
            e.printStackTrace();
        }

//        setCameraDisplayOrientation(MainActivity.this, currentCameraId, CameraPreview.mCamera);

        Camera.Parameters parameters = mCamera.getParameters();

//        mDefaultSize = parameters.getSupportedPreviewSizes().get(0);
//        int screenWidth = getResources().getDisplayMetrics().widthPixels;
//        int screenHeight = getResources().getDisplayMetrics().heightPixels;
//
//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            // Notice that width and height are reversed
//            parameters.setPreviewSize(screenHeight, screenWidth);
//            parameters.setPictureSize(screenHeight, screenWidth);
//
//        }else{// LANDSCAPE MODE
//            //No need to reverse width and height
//            parameters.setPreviewSize(screenWidth, screenHeight);
//            parameters.setPictureSize(screenWidth, screenHeight);
//        }

        mDefaultSize = getOptimalPreviewSize();
        parameters.setPreviewSize(mDefaultSize.width, mDefaultSize.height);

        if (null == mCamera) {
            return;
        }

//        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        int rotation = display.getRotation();
//        int degrees = 0;
//        switch (rotation) {
//            case Surface.ROTATION_0:
//                degrees = 0;
//                break; // Natural orientation
//            case Surface.ROTATION_90:
//                degrees = 90;
//                break; // Landscape left
//            case Surface.ROTATION_180:
//                degrees = 180;
//                break;// Upside down
//            case Surface.ROTATION_270:
//                degrees = 270;
//                break;// Landscape right
//        }
//
//        Camera.CameraInfo camInfo = new Camera.CameraInfo();
//        int cameraRotationOffset = camInfo.orientation;
//        int displayRotation;
//        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            displayRotation = (cameraRotationOffset + degrees) % 360;
//            displayRotation = (360 - displayRotation) % 360; // compensate
//            // the
//            // mirror
//        } else { // back-facing
//            displayRotation = (cameraRotationOffset - degrees + 360) % 360;
//        }
//
//        Log.v(TAG, "rotation cam / phone = displayRotation: " + cameraRotationOffset + " / " + degrees + " = "
//                + displayRotation);
//
//        mCamera.setDisplayOrientation(displayRotation);
//
//        int rotate;
        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            rotate = (360 + cameraRotationOffset + degrees) % 360;
            mCamera.setDisplayOrientation(90);
        } else {

//            rotate = (360 + cameraRotationOffset - degrees) % 360;
            mCamera.setDisplayOrientation(90);
        }

        List<String> focusModes = parameters.getSupportedFocusModes();

        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }


        mPictureSize = getOptimalPictureSize();
//        mPictureSize = mDefaultSize;
        parameters.setPictureSize(mPictureSize.width, mPictureSize.height);
        //parameters.setRotation(90);

        imageFormat = parameters.getPreviewFormat();
//*

        mCamera.setParameters(parameters);

        try {
            if(mCamera != null) {
                mCamera.setPreviewCallback(this);
            }
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }

//*
//        try {
//            Log.i(TAG, "starting preview: ");
//
//            // ....
//            Camera.CameraInfo camInfo = new Camera.CameraInfo();
//            Camera.getCameraInfo(currentCameraId, camInfo);
//            int cameraRotationOffset = camInfo.orientation;
//            // ...
//
////            Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
//            Camera.Size previewSize = null;
//            float closestRatio = Float.MAX_VALUE;
//
//            int targetPreviewWidth = isLandscape() ? getWidth() : getHeight();
//            int targetPreviewHeight = isLandscape() ? getHeight() : getWidth();
//            float targetRatio = targetPreviewWidth / (float) targetPreviewHeight;
//
//            Log.v(TAG, "target size: " + targetPreviewWidth + " / " + targetPreviewHeight + " ratio:" + targetRatio);
//            for (Camera.Size candidateSize : previewSizes) {
//                float whRatio = candidateSize.width / (float) candidateSize.height;
//                if (previewSize == null || Math.abs(targetRatio - whRatio) < Math.abs(targetRatio - closestRatio)) {
//                    closestRatio = whRatio;
//                    previewSize = candidateSize;
//                }
//            }
//
//
//            WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
//            Display display = wm.getDefaultDisplay();
//            int rotation = display.getRotation();
//            int degrees = 0;
//            switch (rotation) {
//                case Surface.ROTATION_0:
//                    degrees = 0;
//                    break; // Natural orientation
//                case Surface.ROTATION_90:
//                    degrees = 90;
//                    break; // Landscape left
//                case Surface.ROTATION_180:
//                    degrees = 180;
//                    break;// Upside down
//                case Surface.ROTATION_270:
//                    degrees = 270;
//                    break;// Landscape right
//            }
//            int displayRotation;
//            if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                displayRotation = (cameraRotationOffset + degrees) % 360;
//                displayRotation = (360 - displayRotation) % 360; // compensate
//                // the
//                // mirror
//            } else { // back-facing
//                displayRotation = (cameraRotationOffset - degrees + 360) % 360;
//            }
//
//            Log.v(TAG, "rotation cam / phone = displayRotation: " + cameraRotationOffset + " / " + degrees + " = "
//                    + displayRotation);
//
//            mCamera.setDisplayOrientation(displayRotation);
//
//            int rotate;
//            if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                rotate = (360 + cameraRotationOffset + degrees) % 360;
//            } else {
//                rotate = (360 + cameraRotationOffset - degrees) % 360;
//            }
//
//            Log.v(TAG, "screenshot rotation: " + cameraRotationOffset + " / " + degrees + " = " + rotate);
//
//            Log.v(TAG, "preview size: " + previewSize.width + " / " + previewSize.height);
//            parameters.setPreviewSize(previewSize.width, previewSize.height);
//            parameters.setRotation(rotate);
//            mCamera.setParameters(parameters);
//            if(mCamera != null) {
//                mCamera.setPreviewCallback(this);
//            }
//            mCamera.setPreviewDisplay(mHolder);
//            mCamera.startPreview();
//
//            Log.d(TAG, "preview started");
//
//        } catch (IOException e) {
//            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
//        }
    }

    private boolean isLandscape() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Notice that width and height are reversed
            return false;

        }else{// LANDSCAPE MODE
            //No need to reverse width and height
            return true;
        }
    }

//    public static void setCameraDisplayOrientation(Activity activity,
//                                                   int cameraId, android.hardware.Camera camera) {
//        android.hardware.Camera.CameraInfo info =
//                new android.hardware.Camera.CameraInfo();
//        android.hardware.Camera.getCameraInfo(cameraId, info);
//        int rotation = activity.getWindowManager().getDefaultDisplay()
//                .getRotation();
//        int degrees = 0;
//        switch (rotation) {
//            case Surface.ROTATION_0: degrees = 0; break;
//            case Surface.ROTATION_90: degrees = 90; break;
//            case Surface.ROTATION_180: degrees = 180; break;
//            case Surface.ROTATION_270: degrees = 270; break;
//        }
//
//        int result;
//        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            result = (info.orientation + degrees) % 360;
//            result = (360 - result) % 360;  // compensate the mirror
//        } else {  // back-facing
//            result = (info.orientation - degrees + 360) % 360;
//        }
//        camera.setDisplayOrientation(result);
//    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (imageFormat == ImageFormat.NV21) {
            if (!bProcessing) {
                FrameData = bytes;
                mHandler.post(DoImageProcessing);
            }
        }

    }

    private void convertFrameDataToBitmap(){

        Mat mYuv = new Mat(mDefaultSize.height + mDefaultSize.height / 2,
                mDefaultSize.width, CvType.CV_8UC1);
        mYuv.put(0, 0, FrameData);
        final Mat mRgba = new Mat();
        Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV2BGR_NV12, 3);
        Bitmap bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mRgba, bitmap);

        Bitmap rotatedBitmap = null;
        if (bitmap != null) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        try {
            originalImage = mRgba;
            findCircle(rotatedBitmap,lowh,maxh,lows,maxs,lowv,maxv);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Runnable DoImageProcessing =
            new Runnable() {
                public void run() {

                    bProcessing = true;
                    convertFrameDataToBitmap();
                    bProcessing = false;
                }
            };

    public Camera.Size getOptimalPreviewSize(){
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size optimalsize = null;
        int[] temp = new int[sizes.size()];
        int[] temp1 = new int[sizes.size()];
        for (int i = 0; i < sizes.size(); i++){

            if (sizes.get(i).width + sizes.get(i).height <= 2000) {
                temp[i] = sizes.get(i).width;
                temp1[i] = sizes.get(i).height;
            }
        }

        for (int i = 0; i < sizes.size(); i++){
            if(sizes.get(i).width == getMax(temp)){
                optimalsize = sizes.get(i);
            }
        }

//        int screenWidth = getResources().getDisplayMetrics().widthPixels;
//        int screenHeight = getResources().getDisplayMetrics().heightPixels;
//
//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            // Notice that width and height are reversed
//            optimalsize.width = screenHeight;
//            optimalsize.height = screenWidth;
//
//        }else{// LANDSCAPE MODE
//            //No need to reverse width and height
//            optimalsize.width = screenWidth;
//            optimalsize.height = screenHeight;
//        }

        Log.i(TAG, "Available PreviewSize: "+optimalsize.width+" "+optimalsize.height);
        return optimalsize;

    }


    private Camera.Size getOptimalPictureSize(){
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        Camera.Size optimalsize = null;

        int max = 0;
        for (int i = 0; i < sizes.size(); i++){

            if ((float)(sizes.get(i).height) / (float) (sizes.get(i).width) == ratio && sizes.get(i).width + sizes.get(i).height <= 6000) {
                if(sizes.get(i).width > max ) {
                    optimalsize = sizes.get(i);
                    max = sizes.get(i).width;
                }
            }
        }

        if (optimalsize == null)
        {
            for (int i = 0; i < sizes.size(); i++){

                if ((float)(sizes.get(i).height) / (float) (sizes.get(i).width) == 0.75 && sizes.get(i).width + sizes.get(i).height <= 6000) {
                    if(sizes.get(i).width > max ) {
                        optimalsize = sizes.get(i);
                        max = sizes.get(i).width;
                    }
                }
            }
        }

        Log.i(TAG, "Available PreviewSize: "+optimalsize.width+" "+optimalsize.height);
        return optimalsize;

    }

    public static int getMax(int[] inputArray){
        int maxValue = inputArray[0];
        for(int i=1;i < inputArray.length;i++){
            if(inputArray[i] > maxValue){
                maxValue = inputArray[i];
            }
        }
        return maxValue;
    }

    public void findCircle(Bitmap image,int lh, int hh, int ls, int hs, int lv, int hv) throws Exception {

        Mat imgOriginal = new Mat();
        Mat hsvImg = new Mat();
        Mat threshImg = new Mat();
        Mat morphOutput = new Mat();
        circles = new Mat();

        Utils.bitmapToMat(image, imgOriginal);
        Imgproc.GaussianBlur(imgOriginal, imgOriginal, new org.opencv.core.Size(7, 7), 4, 4);   //Blur Effect
        Imgproc.cvtColor(imgOriginal, hsvImg, Imgproc.COLOR_RGB2HSV);

        Core.inRange(hsvImg, new Scalar(lh, ls, lv, 0), new Scalar(hh, hs, hv, 0), threshImg);

        Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(8, 8));
        Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(10, 10));

        Imgproc.erode(threshImg, morphOutput, erodeElement);
        Imgproc.erode(morphOutput, morphOutput, erodeElement);

        Imgproc.dilate(morphOutput, morphOutput, dilateElement);
        Imgproc.dilate(morphOutput, morphOutput, dilateElement);

        Imgproc.HoughCircles(threshImg, circles, Imgproc.HOUGH_GRADIENT, 2, 60, 100, 25, 10, 30);

        if(circles.cols() > 0){
            detectflag = true;
        }
        else {
            detectflag = false;
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(10);
        mPaint.setStyle(Paint.Style.STROKE);

//        Paint nPaint = new Paint();
//        nPaint.setColor(Color.GREEN);
//        nPaint.setStrokeWidth(10);

        scalewid = canvas.getWidth();
        scalehei = canvas.getHeight();

        xscalefactor = scalewid/mDefaultSize.height;
        yscalefactor = scalehei/mDefaultSize.width;

        if(circles != null){
            for(int i=0; i<circles.cols(); i++) {

                double[] circle = circles.get(0, i);

                float pX = (float) (circle[0] * xscalefactor);

                float pY = (float) (circle[1] * yscalefactor);

                if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
                    pY = (float) (scalehei - pY);

                float r = (float) (circle[2] * xscalefactor);
                if( r > 20 && r < 50 && pX-r > 10 && pX+r < scalewid - 10 &&
                        pY-r > 10 && pY + r < scalehei - 10){
                    canvas.drawCircle(pX, pY, r + 2, mPaint);
                }

//                canvas.drawCircle((float) (circle[0] * xscalefactor), (float) (circle[1] * yscalefactor), 8.0f, nPaint);
            }
        }


        invalidate();
    }

}
