package ntq.com.barcodedemo;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class CameraActivity extends AppCompatActivity {
    public static final String KEY_MESSAGE = "ScanResult";

    TextView txtResult;
    SurfaceView cameraView;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    boolean firstDetection = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        txtResult = findViewById(R.id.txtResult);
        cameraView = findViewById(R.id.cameraView);
        cameraView.setZOrderMediaOverlay(true);


        barcodeDetector = new BarcodeDetector.Builder(this)
                .build();

        if(!barcodeDetector.isOperational()) {
            Toast.makeText(getApplicationContext(), "Loi, khong scan duoc", Toast.LENGTH_LONG).show();
            this.finish();
        }

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try{
                    if(ContextCompat.checkSelfPermission(CameraActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(cameraView.getHolder());
                    }
                } catch (IOException e) {
                    e.printStackTrace();;
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if(barcodes.size() > 0 && firstDetection) {
//                    cameraSource.stop();
                    firstDetection = false;
                    txtResult.post(new Runnable() {
                        public void run() {
                            Log.d("-------", "run");
                            cameraSource.stop();

                            new AlertDialog.Builder(CameraActivity.this)
                                    .setTitle("Are you sure you want to try again?")
                                    .setMessage(barcodes.valueAt(0).displayValue)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            try{
                                                if(ContextCompat.checkSelfPermission(CameraActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                                    cameraSource.start(cameraView.getHolder());
                                                    firstDetection = true;
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }                                        }
                                    })
                                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    })
                            .show();
                            txtResult.setVisibility(View.VISIBLE);
                            txtResult.setText(    // Update the TextView
                                    barcodes.valueAt(0).displayValue
                            );
                        }
                    });
                } else {
                    txtResult.post(new Runnable() {
                        public void run() {
                            txtResult.setVisibility(View.INVISIBLE);
                        }
                    });
                }

            }
        });
    }
}
