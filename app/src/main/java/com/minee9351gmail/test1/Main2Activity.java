package com.minee9351gmail.test1;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.icu.util.Calendar;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Build.VERSION_CODES.M;

public class Main2Activity extends AppCompatActivity  {

    final int MY_PERMISSION_REQUEST_CODE = 100;  //camera request code
    CameraDevice camera;
    Preview preview;
    android.hardware.Camera mCamera;
    Button btn_capture;
    boolean recording = false;
    MediaRecorder mediaRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main2);

         btn_capture = (Button) findViewById(R.id.button);
        // Hide the window title.
        if(preview == null)
        {
            preview = new Preview(this, (SurfaceView)findViewById(R.id.surfaceView));
        }

        preview.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        init();
        preview.setCamera(mCamera);

        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // 사진촬영
             //   mCamera.takePicture(null,null,mPicture);
                Toast.makeText(getApplicationContext(),"촬영완료.",Toast.LENGTH_LONG).show();

                //밑에 두줄은 기본 카메라 어플 사용하는거 카메라 렌즈를 따오는게 아니고..
                //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //startActivity(intent);
                preview.mCamera.takePicture(null,null,mPicture);
            }
        });
    }

    Camera.AutoFocusCallback mAoutoFocus = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            btn_capture.setEnabled(success);  //포커싱 성공시 촬영 허가
        }
    };

    //사진 저장
    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            String sd = Environment.getExternalStorageDirectory().getAbsolutePath();

            //String.valueOf(System.currentTimeMillis()); 로 저장할 수 도 있음
            String timeStamp = new SimpleDateFormat( "yyyyMMdd_HHmmss").format( new Date());
            String path = sd + "/"+"DCIM"+"/"+"Camera"+"/"+timeStamp+".jpg";

            File file = new File(path);

            try{
                //찍힌 사진을 갤러리 앱에 추가한다.
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File f = new File(path);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                sendBroadcast(mediaScanIntent);

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(data);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                Toast.makeText(Main2Activity.this,"파일 저장 중 에러 발생: "+ e.getMessage(),Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.parse("file://"+ path);
            sendBroadcast(intent);

            Toast.makeText(Main2Activity.this,"사진 저장 완료: "+ path,Toast.LENGTH_LONG).show();
            preview.mCamera.startPreview();
        }
    };

    private void init(){
        int APIVersion = android.os.Build.VERSION.SDK_INT;
        if(APIVersion >= M){//권한이 이미 허가되어 있는지 확인
            if(checkCAMERAPermission()){
                mCamera = android.hardware.Camera.open(android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
                // 카메라 open()
            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},MY_PERMISSION_REQUEST_CODE);
            }
        }
        else{
            mCamera = android.hardware.Camera.open(android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
            mCamera.setDisplayOrientation(90);
        }
    }


    private boolean checkCAMERAPermission(){
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CAMERA);
            return result == PackageManager.PERMISSION_GRANTED;//카메라권한이 허용되는지 여부
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],@NonNull int[] grantResults){
        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length >0){
                    boolean cameraAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
                            if(cameraAccepted){
                                mCamera = android.hardware.Camera.open(android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK); // 카메라 open()가능
                            }

                            else{ //권한 승인 거절
                                if(Build.VERSION.SDK_INT >= M) {
                                    if (shouldShowRequestPermissionRationale(CAMERA_SERVICE)) {
                                        showMessagePermission("권한 허가를 요청합니다",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if (Build.VERSION.SDK_INT >= M) {
                                                            requestPermissions(new String[]{CAMERA_SERVICE}, MY_PERMISSION_REQUEST_CODE);
                                                        }
                                                    }
                                                });
                                        return;
                                    }
                                }
                            }
                }
                break;
        }
    }

    private  void showMessagePermission(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("허용", okListener)
                .setNegativeButton("거부", null)
                .create()
                .show();
    }
}
