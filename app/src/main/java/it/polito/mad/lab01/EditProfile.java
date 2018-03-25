package it.polito.mad.lab01;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;

public class EditProfile extends AppCompatActivity {

    private static final String ORIGINAL_PROFILE_KEY = "original_profile";
    private static final String CURRENT_PROFILE_KEY = "current_profile";
    private static final String IMAGE_URI_KEY = "image_uri";
    private static final String IMAGE_CHANGED_KEY = "image_changed";
    private static final String IMAGE_PATH = "profile_pic";
    private static final String IMAGE_PATH_TMP = "profile_pic_tmp";

    private static final int CAMERA = 2;
    private static final int GALLERY = 3;
    private static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 4;

    private EditText email, username, location, biography;
    private ImageView imageView;
    private BottomSheetDialog bottomSheetDialog;
    //private Uri tmpImageUri;
    private boolean imageChanged;
    private UserProfile originalProfile, currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        email = findViewById(R.id.ep_input_email);
        username = findViewById(R.id.ep_input_username);
        location = findViewById(R.id.ep_input_location);
        biography = findViewById(R.id.ep_input_biography);
        imageView = findViewById(R.id.ep_profile_picture);

        // Set the toolbar
        final Toolbar toolbar = findViewById(R.id.ep_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize the Profile instances
        if (savedInstanceState != null) {
            // If they was saved, load them
            originalProfile = (UserProfile) savedInstanceState.getSerializable(ORIGINAL_PROFILE_KEY);
            currentProfile = (UserProfile) savedInstanceState.getSerializable(CURRENT_PROFILE_KEY);
            imageChanged = savedInstanceState.getBoolean(IMAGE_CHANGED_KEY, false);
        } else {
            // Otherwise, obtain them through the intent
            originalProfile = (UserProfile) this.getIntent().getSerializableExtra(UserProfile.PROFILE_INFO_KEY);
            currentProfile = new UserProfile(originalProfile);
            imageChanged = false;
        }

        // Fill the views with the data
        fillViews(currentProfile);

        bottomSheetDialog = new BottomSheetDialog(this);
        final View sheetView = this.getLayoutInflater().inflate(R.layout.bottom_sheet_picture_dialog, null);
        bottomSheetDialog.setContentView(sheetView);

        final FloatingActionButton floatingActionButton = findViewById(R.id.ep_camera_button);
        floatingActionButton.setOnClickListener(v -> {
            LinearLayout camera = bottomSheetDialog.findViewById(R.id.bs_camera_option);
            LinearLayout gallery = bottomSheetDialog.findViewById(R.id.bs_gallery_option);
            LinearLayout reset = bottomSheetDialog.findViewById(R.id.bs_reset_option);

            camera.setOnClickListener(v1 -> {
                cameraTakePicture();
                bottomSheetDialog.dismiss();
            });

            gallery.setOnClickListener(v2 -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
                } else {
                    galleryLoadPicture();
                }

                bottomSheetDialog.dismiss();
            });

            reset.setOnClickListener(v3 -> {
                this.imageChanged = false;
                currentProfile.update(null);
                imageView.setImageURI(currentProfile.getImageUriOrDefault(this));
                bottomSheetDialog.dismiss();
            });

            bottomSheetDialog.show();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
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
                if (commitChanges()) {
                    finish();
                }
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
    public void onBackPressed() {

        updateProfileInfo(currentProfile);
        if (!currentProfile.equals(originalProfile) || this.imageChanged) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.discard_changes))
                    .setPositiveButton(getString(android.R.string.yes), (dialog, which) -> cleanupAndFinish())
                    .setNegativeButton(getString(android.R.string.no), null)
                    .show();
        } else {
            cleanupAndFinish();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        updateProfileInfo(currentProfile);
        outState.putSerializable(ORIGINAL_PROFILE_KEY, originalProfile);
        outState.putSerializable(CURRENT_PROFILE_KEY, currentProfile);
        outState.putBoolean(IMAGE_CHANGED_KEY, imageChanged);
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

            File imageFile = new File(this.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), IMAGE_PATH_TMP);
            Uri imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID.concat(".fileprovider"), imageFile);

            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(cameraIntent, CAMERA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ImageView imageView = findViewById(R.id.ep_profile_picture);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA:

                    File imageFileCamera = new File(this.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), IMAGE_PATH_TMP);
                    Uri imageUriCamera = Uri.fromFile(imageFileCamera);

                    // Reset the image even if the filename is the same
                    imageView.setImageURI(null);
                    imageView.setImageURI(imageUriCamera);
                    currentProfile.update(imageUriCamera);

                    this.imageChanged = true;
                    break;

                case GALLERY:
                    if (data != null && data.getData() != null) {

                        // Move the image to a temporary location
                        File imageFileGallery = new File(this.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), IMAGE_PATH_TMP);
                        try {
                            Utilities.copyFile(new File(Utilities.getRealPathFromURI(this, data.getData())), imageFileGallery);
                        } catch (IOException e) {
                            showErrorMessage(getString(R.string.failed_obtain_picture));
                        }

                        Uri imageUriGallery = Uri.fromFile(imageFileGallery);

                        // Reset the image even if the filename is the same
                        imageView.setImageURI(null);
                        imageView.setImageURI(imageUriGallery);
                        currentProfile.update(imageUriGallery);

                        this.imageChanged = true;
                    }
                    break;

                default:
                    break;

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

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

    private void fillViews(UserProfile profile) {
        email.setText(profile.getEmail());
        username.setText(profile.getUsername());
        location.setText(profile.getLocation());
        biography.setText(profile.getBiography());

        // Reset the image even if the filename is the same
        imageView.setImageURI(null);
        imageView.setImageURI(profile.getImageUriOrDefault(this));
    }

    private void updateProfileInfo(UserProfile profile) {
        String emailStr = email.getText().toString();
        String usernameStr = username.getText().toString();
        String locationStr = location.getText().toString();
        String biographyStr = biography.getText().toString();

        profile.update(emailStr, usernameStr, locationStr, biographyStr);
    }

    private boolean commitChanges() {
        updateProfileInfo(currentProfile);

        if(!currentProfile.isValid()) {
            showErrorMessage(getString(R.string.incorrect_values));
            return false;
        }

        if(!currentProfile.isEmailValid()) {
            showErrorMessage(getString(R.string.incorrect_email));
            return false;
        }

        // Save the image permanently
        if (this.imageChanged) {
            File sourceFile = new File(this.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), IMAGE_PATH_TMP);
            File destinationFile = new File(this.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), IMAGE_PATH);
            if (!sourceFile.renameTo(destinationFile)) {
                showErrorMessage(getString(R.string.failed_obtain_picture));
                return false;
            }
            currentProfile.update(Uri.fromFile(destinationFile));
        }

        if (!originalProfile.equals(currentProfile) || this.imageChanged) {
            Intent intent = new Intent(getApplicationContext(), ShowProfile.class);
            intent.putExtra(UserProfile.PROFILE_INFO_KEY, currentProfile);
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED);
        }
        return true;
    }

    private void showErrorMessage(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(android.R.string.ok), null)
                .show();
    }

    private void cleanupAndFinish() {
        File tmpImageFile = new File(this.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), IMAGE_PATH_TMP);
        if (tmpImageFile.exists()) {
            tmpImageFile.deleteOnExit();
        }
        finish();
    }
}
