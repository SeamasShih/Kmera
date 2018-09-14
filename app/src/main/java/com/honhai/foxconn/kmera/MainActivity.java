package com.honhai.foxconn.kmera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.honhai.foxconn.kmera.Tools.DirectionVerifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private final String TAG = "DEBUG";
    private final int REQUEST_CODE_CAMERA = 1;
    private final int REQUEST_CODE_ACCESS_STORAGE = 2;
    private static final SparseIntArray ORIENTATION = new SparseIntArray();

    static {
        ORIENTATION.append(0, 90);
        ORIENTATION.append(90, 180);
        ORIENTATION.append(180, 270);
        ORIENTATION.append(270, 0);
    }

    TextView azimuthText , pitchText , rollText;

    int azimuth,pitch,roll;
    int rotation;
    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];
    private boolean isCameraPermissionGrant = false;
    private boolean isStoragePermissionGrant = false;
    SensorManager sensorManager;
    private AutoFitTextureView mTextureView;
    private Handler mCameraHandler;
    private String mCameraId;
    private Size mPreViewSize;
    private Size mCaptureSize;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CaptureRequest mCaptureRequest;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;
    private TextureView.SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setupCamera(width, height);
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_main);

        findViews();
        mTextureView.setOnClickListener(this::takePicture);
    }

    private void findViews() {
        mTextureView = findViewById(R.id.textureView);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        azimuthText = findViewById(R.id.one);
        pitchText = findViewById(R.id.two);
        rollText = findViewById(R.id.three);
    }

    private boolean isPermissionGranted(String permission, int requestCode) {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{permission},
                    requestCode
            );
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    finish();
                else
                    isCameraPermissionGrant = true;
                break;
            case REQUEST_CODE_ACCESS_STORAGE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    finish();
                else
                    isStoragePermissionGrant = true;
                break;
        }

        if (isCameraPermissionGrant && isStoragePermissionGrant) {
            onResume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isPermissionGranted(Manifest.permission.CAMERA, REQUEST_CODE_CAMERA) &&
                isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_CODE_ACCESS_STORAGE) &&
                isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_CODE_ACCESS_STORAGE)) {
            startCameraThread();

            if (mTextureView.isAvailable()) {
                if (mCameraId == null) {
                    setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
                }
                openCamera();
            } else {
                mTextureView.setSurfaceTextureListener(mTextureListener);
            }

            sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void startPreview() {
        SurfaceTexture mSurfaceTexture = mTextureView.getSurfaceTexture();
        mSurfaceTexture.setDefaultBufferSize(mPreViewSize.getWidth(), mPreViewSize.getHeight());
        Surface previewSurface = new Surface(mSurfaceTexture);
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        mCaptureRequest = mCaptureRequestBuilder.build();
                        mCameraCaptureSession = session;
                        mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) return;
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CODE_CAMERA);
                return;
            }
            manager.openCamera(mCameraId, mStateCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setupCamera(int width, int height) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) return;

        try {
            for (String id : manager.getCameraIdList()) {

                CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
                Integer face = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (face != null && face == CameraCharacteristics.LENS_FACING_FRONT)
                    continue;

                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) throw new NullPointerException();
                mPreViewSize = getOptimumSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                mCaptureSize = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        (o1, o2) -> Long.signum(o1.getWidth() * o1.getHeight() - o2.getHeight() * o2.getWidth()));

                mTextureView.setAspectRatio(mPreViewSize.getHeight(), mPreViewSize.getWidth());
                setupImageReader();
                mCameraId = id;
            }
        } catch (CameraAccessException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void takePicture(View view) {
        lockFocus();
    }

    private void lockFocus() {
        try {
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            mCameraCaptureSession.capture(mCaptureRequestBuilder.build(), mCaptureCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            capture();
        }
    };

    private void capture() {
        try {
            CaptureRequest.Builder mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCaptureBuilder.addTarget(mImageReader.getSurface());
            mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(rotation));
            CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    Toast.makeText(getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
                    unLockFocus();
                }
            };
            mCameraCaptureSession.stopRepeating();
            mCameraCaptureSession.capture(mCaptureBuilder.build(), captureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unLockFocus() {
        try {
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setupImageReader() {
        mImageReader = ImageReader.newInstance(mCaptureSize.getWidth(), mCaptureSize.getHeight(),
                ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(reader -> mCameraHandler.post(new imageSaver(reader.acquireNextImage())), mCameraHandler);
    }

    private Size getOptimumSize(Size[] outputSizes, int width, int height) {
        List<Size> sizeList = new ArrayList<>();

        if (width > height)
            for (Size option : outputSizes) {
                if (option.getWidth() > width && option.getHeight() > width)
                    sizeList.add(option);
            }
        else
            for (Size opt : outputSizes)
                if (opt.getWidth() > height && opt.getHeight() > width)
                    sizeList.add(opt);

        if (sizeList.size() > 0) {
            return Collections.min(sizeList, (lhs, rhs) -> Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight()));
        }

        return outputSizes[0];
    }

    private void startCameraThread() {
        HandlerThread mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
    }

    private void setFullScreen() {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        );
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = new float[3];
        float[] R = new float[9];
        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerValues = event.values;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticFieldValues = event.values;
                break;
        }
        SensorManager.getRotationMatrix(R,null,accelerometerValues,magneticFieldValues);
        SensorManager.getOrientation(R,values);

        azimuth = (int)Math.toDegrees(values[0]);
        pitch = (int)Math.toDegrees(values[1]);
        roll = (int)Math.toDegrees(values[2]);

        int orientation = DirectionVerifier.getOrientation(values);

        if (DirectionVerifier.mask(orientation,DirectionVerifier.MASK_ROTATION)
                == DirectionVerifier.ROTATION_CLOCKWISE)
            rotation = 90;
        else if (DirectionVerifier.mask(orientation,DirectionVerifier.MASK_ROTATION)
                == DirectionVerifier.ROTATION_ANTI_CLOCKWISE)
            rotation = 270;
        else if (DirectionVerifier.mask(orientation,DirectionVerifier.MASK_ROTATION)
                == DirectionVerifier.ROTATION_REVERSE)
            rotation = 180;
        else if (DirectionVerifier.mask(orientation,DirectionVerifier.MASK_ROTATION)
                == DirectionVerifier.ROTATION_NORMAL)
            rotation = 0;

//        azimuthText.setText(String.valueOf(azimuth));
//        pitchText.setText(String.valueOf(pitch));
//        rollText.setText(String.valueOf(roll));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class imageSaver implements Runnable {

        private Image mImage;

        imageSaver(Image image) {
            mImage = image;
        }

        @SuppressLint("SimpleDateFormat")
        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String path = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/";
            File mImageFile = new File(path);
            if (!mImageFile.exists())
                mImageFile.mkdir();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = path + "IMG_" + timeStamp + ".jpg";
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(filename);
                fos.write(data, 0, data.length);
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.parse(filename);
                mediaScanIntent.setData(contentUri);
                sendBroadcast(mediaScanIntent);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
//            values.put(MediaStore.Images.Media.ORIENTATION, ORIENTATION.get(rotation));
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATA, filename);
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + timeStamp);
            ContentResolver cr = MainActivity.this.getContentResolver();
            cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
    }
}
