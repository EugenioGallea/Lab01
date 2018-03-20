package it.polito.mad.lab01;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class EditProfile extends AppCompatActivity {
    private EditText email, username, location, description;
    private ProfileInfo pi = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Set the toolbar
        final Toolbar toolbar = findViewById(R.id.ep_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FloatingActionButton cameraButton = (FloatingActionButton) findViewById(R.id.sp_camera_button);
        cameraButton.bringToFront();

        pi = ProfileInfo.getInstance();
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
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Set back the textview
        pi.setEmail(savedInstanceState.getString("email"));
        pi.setUsername(savedInstanceState.getString("username"));
        pi.setLocation(savedInstanceState.getString("location"));
        pi.setDescription(savedInstanceState.getString("description"));
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

        Log.v("Inserted username", ProfileInfo.getUsername());

        Intent intent = new Intent(getApplicationContext(), ShowProfile.class);
        intent.putExtra("username", u.getText().toString());
        intent.putExtra("location", l.getText().toString());
        intent.putExtra("description", d.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }

}
