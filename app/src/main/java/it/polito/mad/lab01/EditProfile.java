package it.polito.mad.lab01;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Calendar;

public class EditProfile extends AppCompatActivity {
    private EditText email, username, location, description;
    private ProfileInfo pi = null;
    private static final int CAMERA = 2;
    private static final int GALLERY = 3;

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.ep_camera_button);
        fab.setOnClickListener(v ->{
            BottomSheetDialog bsd = new BottomSheetDialog(this);
            View sheetView = this.getLayoutInflater().inflate(R.layout.bottom_sheet_picture_dialog, null);
            bsd.setContentView(sheetView);

            LinearLayout camera = (LinearLayout) bsd.findViewById(R.id.bs_camera_option);
            LinearLayout gallery = (LinearLayout) bsd.findViewById(R.id.bs_gallery_option);

            camera.setOnClickListener(r -> {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    File photoDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File image = new File(photoDir.getAbsolutePath(), "profile_picture.jpg");
                    startActivityForResult(cameraIntent, CAMERA);
                }

                bsd.dismiss();
            });

            gallery.setOnClickListener(t -> {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (galleryIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(galleryIntent, GALLERY);
                }

                bsd.dismiss();
            });

            bsd.show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Retrieve elements from layout
        email = (EditText) findViewById(R.id.ep_input_email);
        username = (EditText) findViewById(R.id.ep_input_username);
        location = (EditText) findViewById(R.id.ep_input_location);
        description = (EditText) findViewById(R.id.ep_input_description);

        email.setText(pi.getEmail());
        username.setText(pi.getUsername());
        location.setText(pi.getLocation());
        description.setText(pi.getDescription());
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
                finish();
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
        outState.putString("path_pp", pi.getPath_pp());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Set back the textview
        pi.setEmail(savedInstanceState.getString("email"));
        pi.setUsername(savedInstanceState.getString("username"));
        pi.setLocation(savedInstanceState.getString("location"));
        pi.setDescription(savedInstanceState.getString("description"));

        if(!(pi.getPath_pp() == null)){
            Bitmap image = BitmapFactory.decodeFile(pi.getPath_pp());
            ImageView imageview = findViewById(R.id.ep_profile_picture);
            imageview.setImageBitmap(image);
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

        Intent intent = new Intent(getApplicationContext(), ShowProfile.class);
        intent.putExtra("username", u.getText().toString());
        intent.putExtra("location", l.getText().toString());
        intent.putExtra("description", d.getText().toString());
        intent.putExtra("path_pp", pi.getPath_pp());

        setResult(RESULT_OK, intent);

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView imageview = (ImageView) findViewById(R.id.ep_profile_picture);

        switch (requestCode) {
            case CAMERA:
                if (data != null) {
                    Bitmap bitmap_camera = (Bitmap) data.getExtras().get("data");
                    if (bitmap_camera == null)
                        break;
                    String path = saveImage(bitmap_camera);
                    imageview.setImageBitmap(bitmap_camera);
                    pi.setPath_pp(path);
                }

                break;

            case GALLERY:
                if (data != null) {
                    Uri contentURI = data.getData();
                    try {
                        Bitmap bitmap_gallery = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                        if(bitmap_gallery == null)
                            break;
                        String path = saveImage(bitmap_gallery);
                        imageview.setImageBitmap(bitmap_gallery);
                        pi.setPath_pp(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;

            default:
                break;

        }
    }

    public String saveImage(Bitmap myBitmap) {
        String path = "/img";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File wallpaperDirectory = new File(
                path);

        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, "/img" + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }

}
