package com.minee9351gmail.test1;

import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by ehdal on 2017-04-01.
 */

public class Preview implements SurfaceHolder.Callback {

   // CameraDevice camera;
    SurfaceView mCameraView;
    SurfaceHolder mCameraHolder;
    android.hardware.Camera mCamera;
    boolean recording = false;
    MediaRecorder mediaRecorder;

    Preview(Context context, SurfaceView sv){
        mCameraView =sv;

        mCameraHolder = mCameraView.getHolder();
        mCameraHolder.addCallback(this);
        mCameraHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //
    }

    public  void setCamera(Camera camera){
        if(mCamera !=null)
        {
            mCamera.startPreview();
            mCamera.release();

            mCamera = null;
        }

        mCamera =camera;
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();

            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                // set the focus mode
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                // set Camera parameters
                mCamera.setParameters(params);
            }

            try {
                mCamera.setPreviewDisplay(mCameraHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Important: Call startPreview() to start updating the preview
            // surface. Preview must be started before you can take a picture.
            mCamera.startPreview();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try{
            if(mCamera !=null)
            {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if ( mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> allSizes = parameters.getSupportedPreviewSizes();
            Camera.Size size = allSizes.get(0); // get top size
            for (int i = 0; i < allSizes.size(); i++) {
                if (allSizes.get(i).width > size.width)
                    size = allSizes.get(i);
            }
            //set max Preview Size
            parameters.setPreviewSize(size.width, size.height);

            // Important: Call startPreview() to start updating the preview surface.
            // Preview must be started before you can take a picture.
            mCamera.startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
