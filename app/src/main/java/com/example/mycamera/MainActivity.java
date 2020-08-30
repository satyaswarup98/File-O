package com.example.mycamera;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    final int PERMISSION_REQUEST_CODE = 1240;
    private Uri fileUri;
    SurfaceView cameraView;
    TextView textView;
    CameraSource cameraSource;
    TextView b;
    String[] appPermissions = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Camera Permission Denied")
                            .setMessage("Application Can't run without Camera Permissions !")
                            .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }

                            })
                            .show();
                }
                if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                }

            }
            break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b = (TextView) findViewById(R.id.text_read);
        b.setX(Resources.getSystem().getDisplayMetrics().widthPixels - 200);
        b.setY(0);
        cameraView = (SurfaceView) findViewById(R.id.surface_view);
        textView = (TextView) findViewById(R.id.text_view);

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {
            Log.w("MainActivity", "Detector dependencies are not yet available");
        } else {

            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(4608, 2304)
                    .setRequestedFps(20.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                                ActivityCompat.checkSelfPermission(getApplicationContext(),
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(MainActivity.this,
                                    appPermissions,
                                    PERMISSION_REQUEST_CODE);
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    cameraSource.stop();
                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    SparseArray<TextBlock> textBlocks = detections.getDetectedItems();
                    TextBlock tBlock = textBlocks.valueAt(0);
                    b.setText("");
                    if (tBlock != null) {
                        for (Text line : tBlock.getComponents()) {
//                            float top = line.getBoundingBox().top;
//                            textBlocks2.(0,line);
//                            TextBlock tBlock2 = tBlock.;
                            for (Text word : line.getComponents()) {
                                Rect rect = new Rect(word.getBoundingBox());
                                Rect myRect = new Rect(Resources.getSystem().getDisplayMetrics().widthPixels - 200, 0, Resources.getSystem().getDisplayMetrics().widthPixels, 200);
//                            textView.setText(Float.toString(top));

                                if (rect.intersect(myRect)) {
                                    b.setText(word.getValue());
//                                    if (word.getValue().equals("A") || word.getValue().equals("a")) {
//                                        //takePicture789();
////                                        b.setText("");
//                                    }
                                    if (word.getValue() == null) {
                                        b.setText("");
                                    }

//                                    Float s = getResources().getDimension(R.dimen.image_width);
//                                    b.setText(Float.toString(b.getX()) + "  "+ Float.toString(b.getY())+ " "+ Resources.getSystem().getDisplayMetrics().widthPixels);


                                }

                            }
                        }
                    } else {
                        b.setText("");
                    }

                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0) {
                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i = 0; i < 1; ++i) {

                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
//                                    stringBuilder.append("\n");
                                }
                                textView.setText(stringBuilder.toString());
                            }
                        });
                    }
                }
            });
        }
    }

    public void captureImage(View v) {
        if (b.getText().equals("A") || b.getText().equals("a")) {
            takePicture789();
            b.setText("");


        }
    }


    public void takePicture789() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    appPermissions,
                    PERMISSION_REQUEST_CODE);
            return;
        }
        cameraSource.takePicture(new CameraSource.ShutterCallback() {
            @Override
            public void onShutter() {

            }
        }, new CameraSource.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] bytes) {
                File picture_file = getOutputMediaFile("MyCamera");
                if (picture_file == null) {
                    return;
                } else {
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(picture_file);
                        fileOutputStream.write(bytes);
                        fileOutputStream.close();

//                        camera.startPreview();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                int REQUEST_CODE = 99;
                int preference = ScanConstants.OPEN_MEDIA;
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
                intent.putExtra("imageUri", fileUri+"");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 99 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                getContentResolver().delete(uri, null, null);
                File pic_file = getOutputMediaFile("MyScanner");
                if (pic_file == null) {
                    return;
                } else {
                    try (FileOutputStream out = new FileOutputStream(pic_file)) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getOutputMediaFile(String folder) {
        String state = Environment.getExternalStorageState();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        } else {
            File MyCam = new File(Environment.getExternalStorageDirectory() + File.separator + folder);
            if (!MyCam.exists()) {
                MyCam.mkdir();
            }

            File outputFile = new File(MyCam, "IMG_" + timeStamp + ".jpg");
            fileUri = Uri.fromFile(outputFile);
            return outputFile;
        }
    }

}