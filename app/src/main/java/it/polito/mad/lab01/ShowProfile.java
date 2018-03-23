package it.polito.mad.lab01;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;

public class ShowProfile extends AppCompatActivity {

    // Just temp data
    private final String emailAddress = "user@provider.com";
    private final String city = "Turin";
    private ProfileInfo pi = null;
    private static final int EDIT_PROFILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);

        // Set the toolbar
        final Toolbar toolbar = findViewById(R.id.sp_toolbar);
        setSupportActionBar(toolbar);

        // MailTo button
        final AppCompatImageButton mailToButton = findViewById(R.id.sp_mail_icon);
        mailToButton.setOnClickListener(v -> {
            Uri uri = Uri.parse("mailto:" + emailAddress);
            Intent mailTo = new Intent(Intent.ACTION_SENDTO, uri);
            if (mailTo.resolveActivity(getPackageManager()) != null) {
                startActivity(mailTo);
            }
        });

        // ShowCity button
        final AppCompatImageButton showCityButton = findViewById(R.id.sp_locate_icon);
        showCityButton.setOnClickListener(v -> {
            Uri uri = Uri.parse("http://maps.google.co.in/maps?q=" + city);
            Intent showCity = new Intent(Intent.ACTION_VIEW, uri);
            if (showCity.resolveActivity(getPackageManager()) != null) {
                startActivity(showCity);
            }
        });

        pi = ProfileInfo.getInstance();

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        AppCompatTextView username = (AppCompatTextView) findViewById(R.id.sp_username);
        AppCompatTextView location = (AppCompatTextView) findViewById(R.id.sp_location);
        AppCompatTextView description = (AppCompatTextView) findViewById(R.id.sp_description);
        ImageView image = (ImageView) findViewById(R.id.sp_profile_picture);

        String email = sharedPref.getString(getString(R.string.preference_email), "email@default.com");
        username.setText(sharedPref.getString(getString(R.string.preference_username), "Default Username"));
        location.setText(sharedPref.getString(getString(R.string.preference_location), "Default Location"));
        description.setText(sharedPref.getString(getString(R.string.preference_description), "Default Description"));

        String imageUriString = sharedPref.getString(getString(R.string.preference_imageUri), null);
        Uri imageUri = null;
        if(imageUriString != null) {
            imageUri = Uri.parse(imageUriString);
            image.setImageURI(imageUri);
        }


        pi.setEmail(email);
        pi.setUsername(username.getText().toString());
        pi.setLocation(location.getText().toString());
        pi.setDescription(description.getText().toString());
        pi.setImageUri(imageUri);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_show_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sp_edit_profile:
                Intent toEdit = new Intent(getApplicationContext(), EditProfile.class);
                startActivityForResult(toEdit, EDIT_PROFILE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(getString(R.string.preference_email), pi.getEmail());
        editor.putString(getString(R.string.preference_username), pi.getUsername());
        editor.putString(getString(R.string.preference_location), pi.getLocation());
        editor.putString(getString(R.string.preference_description), pi.getDescription());
        if(pi.getImageUri() != null)
            editor.putString(getString(R.string.preference_imageUri), pi.getImageUri().toString());

        editor.commit();
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
            ImageView imageview = findViewById(R.id.sp_profile_picture);
            imageview.setImageURI(pi.getImageUri());
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case EDIT_PROFILE:
                AppCompatTextView username = (AppCompatTextView) findViewById(R.id.sp_username);
                AppCompatTextView location = (AppCompatTextView) findViewById(R.id.sp_location);
                AppCompatTextView description = (AppCompatTextView) findViewById(R.id.sp_description);
                ImageView imageview = (ImageView) findViewById(R.id.sp_profile_picture);

                if(resultCode == RESULT_OK) {
                    pi.setUsername(data.getStringExtra("username"));
                    pi.setLocation(data.getStringExtra("location"));
                    pi.setDescription(data.getStringExtra("description"));
                    pi.setImageUri(Uri.parse(data.getStringExtra("imageUri")));

                    // Set back the textview
                    username.setText(pi.getUsername());
                    location.setText(pi.getLocation());
                    description.setText(pi.getDescription());

                    if(pi.getImageUri() != null) {
                        imageview.setImageURI(null);
                        imageview.setImageURI(pi.getImageUri());
                    }
                }

                break;

            default:
                break;

        }
    }
}
