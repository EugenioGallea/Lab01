package it.polito.mad.lab01;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class ShowProfile extends AppCompatActivity {

    // Just temp data
    private final String emailAddress = "user@provider.com";
    private final String city = "Turin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showprofile);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_showprofile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sp_edit_profile:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
