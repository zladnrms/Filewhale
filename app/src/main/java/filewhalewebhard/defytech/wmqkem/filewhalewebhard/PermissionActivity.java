package filewhalewebhard.defytech.wmqkem.filewhalewebhard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import filewhalewebhard.defytech.wmqkem.filewhalewebhard.user.LoginActivity;

public class PermissionActivity extends AppCompatActivity {


    private LinearLayout layoutCameraCol, layoutStorageCol, layoutCameraDet, layoutStorageDet;
    private ImageView ivCamera, ivStorage;
    private Button btnAccept;

    /* collaspe variable */
    private boolean showCamera = true;
    private boolean showStorage = true;

    public static final int PERMISSIONS_MULTIPLE_REQUEST = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_check);

        layoutCameraCol = (LinearLayout) findViewById(R.id.layout_camera_collapse);
        layoutStorageCol = (LinearLayout) findViewById(R.id.layout_storage_collapse);
        layoutCameraDet = (LinearLayout) findViewById(R.id.layout_camera_detail);
        layoutStorageDet = (LinearLayout) findViewById(R.id.layout_storage_detail);
        ivCamera = (ImageView) findViewById(R.id.iv_camera);
        ivStorage = (ImageView) findViewById(R.id.iv_storage);
        btnAccept = (Button) findViewById(R.id.btn_accept);

        layoutCameraCol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showCamera) {
                    showCamera = false;
                    ivCamera.setImageResource(R.drawable.ic_chevron_down);
                    layoutCameraDet.setVisibility(View.GONE);
                } else {
                    showCamera = true;
                    ivCamera.setImageResource(R.drawable.ic_chevron_up);
                    layoutCameraDet.setVisibility(View.VISIBLE);
                }
            }
        });

        layoutStorageCol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showStorage) {
                    showStorage = false;
                    ivStorage.setImageResource(R.drawable.ic_chevron_down);
                    layoutStorageDet.setVisibility(View.GONE);
                } else {
                    showStorage = true;
                    ivStorage.setImageResource(R.drawable.ic_chevron_up);
                    layoutStorageDet.setVisibility(View.VISIBLE);
                }
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             checkPermission();
                                         }
                                     }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkAndroidVersion();
    }

    /* Check Android Version*/
    private void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    + ContextCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    + ContextCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {

                Intent intent = new Intent(PermissionActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();

            }
        } else {
            Intent intent = new Intent(PermissionActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /* Permission Check */
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(PermissionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(PermissionActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(PermissionActivity.this, Manifest.permission.CAMERA)) {

                Snackbar.make(PermissionActivity.this.findViewById(android.R.id.content),
                        "방송 송출, 시청의 원활한 진행을 위해 권한을 허용해주세요",
                        Snackbar.LENGTH_INDEFINITE).setAction("설정", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(PermissionActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                                                , Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                , Manifest.permission.CAMERA},
                                        PERMISSIONS_MULTIPLE_REQUEST);
                            }
                        }

                ).show();
            } else {
                ActivityCompat.requestPermissions(PermissionActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        PERMISSIONS_MULTIPLE_REQUEST);
            }
        } else {
            // if permission already granted
            Intent intent = new Intent(PermissionActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_MULTIPLE_REQUEST:
                if (grantResults.length > 0) {
                    boolean readExternalFile = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeExternalFile = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraPermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (readExternalFile && writeExternalFile && cameraPermission) {
                        // if permission granted
                        Intent intent = new Intent(PermissionActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Snackbar.make(PermissionActivity.this.findViewById(android.R.id.content),
                                "방송 송출, 시청의 원활한 진행을 위해 권한을 허용해주세요",
                                Snackbar.LENGTH_INDEFINITE).setAction("설정", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(PermissionActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                                                , Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                , Manifest.permission.CAMERA},
                                        PERMISSIONS_MULTIPLE_REQUEST);
                            }
                        });

                    }
                }
                break;
        }
    }
}
