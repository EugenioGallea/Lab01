package it.polito.mad.lab01;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EditProfile extends AppCompatActivity {
    private EditText email, username, location, description;
    private ImageView image;
    private Uri imageUri;
    private ProfileInfo pi = null;
    //private BottomSheetDialog bsd = null;
    private static final int CAMERA = 2;
    private static final int GALLERY = 3;
    private static final int PERMISSIONS_REQUEST_CAMERA = 4;
    private static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Set the toolbar
        final Toolbar toolbar = findViewById(R.id.ep_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        pi = ProfileInfo.getInstance();

        BottomSheetDialog bsd = new BottomSheetDialog(this);
        View sheetView = this.getLayoutInflater().inflate(R.layout.bottom_sheet_picture_dialog, null);
        bsd.setContentView(sheetView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.ep_camera_button);
        fab.setOnClickListener(v ->{
            LinearLayout camera = (LinearLayout) bsd.findViewById(R.id.bs_camera_option);
            LinearLayout gallery = (LinearLayout) bsd.findViewById(R.id.bs_gallery_option);

            camera.setOnClickListener(v1 -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
                } else {
                    cameraTakePicture();
                }

                bsd.dismiss();
            });

            gallery.setOnClickListener(v2 -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
                } else {
                    galleryLoadPicture();
                }

                bsd.dismiss();
            });

            bsd.show();
        });
    }

    private void galleryLoadPicture() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (galleryIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(galleryIntent, GALLERY);
        }
    }

    private void cameraTakePicture() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            String fileName = "profile_pic";
            File imageFile = new File(this.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);

            if(imageFile != null){
                this.imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID.concat(".fileprovider"), imageFile);

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, this.imageUri);
                startActivityForResult(cameraIntent, CAMERA);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Retrieve elements from layout
        email = (EditText) findViewById(R.id.ep_input_email);
        username = (EditText) findViewById(R.id.ep_input_username);
        location = (EditText) findViewById(R.id.ep_input_location);
        description = (EditText) findViewById(R.id.ep_input_description);
        image = (ImageView) findViewById(R.id.ep_profile_picture);

        email.setText(pi.getEmail());
        username.setText(pi.getUsername());
        location.setText(pi.getLocation());
        description.setText(pi.getDescription());

        this.imageUri = pi.getImageUri();
        if(this.imageUri != null) {
            image.setImageURI(this.imageUri);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.ep_save_profile:
                saveInfo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the values
        outState.putString("email", pi.getEmail());
        outState.putString("username", pi.getUsername());
        outState.putString("location", pi.getLocation());
        outState.putString("description", pi.getDescription());
        if(pi.getImageUri() != null)
            outState.putString("imageUri", pi.getImageUri().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Set back the textview
        pi.setEmail(savedInstanceState.getString("email"));
        pi.setUsername(savedInstanceState.getString("username"));
        pi.setLocation(savedInstanceState.getString("location"));
        pi.setDescription(savedInstanceState.getString("description"));
        pi.setImageUri(Uri.parse(savedInstanceState.getString("imageUri")));

        if(pi.getImageUri() != null){
            ImageView imageview = findViewById(R.id.ep_profile_picture);
            imageview.setImageURI(pi.getImageUri());
        }
    }

    public void saveInfo(){
        EditText e = (EditText) findViewById(R.id.ep_input_email);
        EditText u = (EditText) findViewById(R.id.ep_input_username);
        EditText l = (EditText) findViewById(R.id.ep_input_location);
        EditText d = (EditText) findViewById(R.id.ep_input_description);

        pi.setEmail(e.getText().toString());
        pi.setUsername(u.getText().toString());
        pi.setLocation(l.getText().toString());
        pi.setDescription(d.getText().toString());
        pi.setImageUri(this.imageUri);

        Intent intent = new Intent(getApplicationContext(), ShowProfile.class);
        intent.putExtra("username", u.getText().toString());
        intent.putExtra("location", l.getText().toString());
        intent.putExtra("description", d.getText().toString());
        intent.putExtra("imageUri", pi.getImageUri().toString());

        setResult(RESULT_OK, intent);

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("ON ACTIVITY RES", "Sono entrato da: " + requestCode);
        ImageView imageview = (ImageView) findViewById(R.id.ep_profile_picture);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA:
                    imageview.setImageURI(null);
                    imageview.setImageURI(this.imageUri);

                    break;

                case GALLERY:
                    if (data != null) {
                        this.imageUri = data.getData();
                        imageview.setImageURI(this.imageUri);
                    }
                    break;

                default:
                    break;

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraTakePicture();
                }

                return;
            }

            case PERMISSIONS_REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    galleryLoadPicture();
                }

                return;
            }

            default:
                break;
        }
    }
}
