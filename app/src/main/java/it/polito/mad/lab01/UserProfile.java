package it.polito.mad.lab01;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.Serializable;

public class UserProfile implements Serializable {

    public static final String PROFILE_INFO_KEY = "profile_info_key";
    private static final String EMAIL_PREFERENCE_KEY = "email";
    private static final String USERNAME_PREFERENCE_KEY = "username";
    private static final String LOCATION_PREFERENCE_KEY = "location";
    private static final String BIOGRAPHY_PREFERENCE_KEY = "biography";
    private static final String IMAGE_PREFERENCE_KEY = "image";

    private String email;
    private String username;
    private String location;
    private String biography;
    private String imageUri;

    private float rating;
    private int lentBooks;
    private int borrowedBooks;
    private int toBeReturnedBooks;

    public UserProfile(@NonNull UserProfile other) {
        this.email = other.getEmail();
        this.username = other.getUsername();
        this.location = other.getLocation();
        this.biography = other.getBiography();

        this.imageUri = other.imageUri;

        this.rating = other.getRating();
        this.lentBooks = other.getLentBooks();
        this.borrowedBooks = other.getBorrowedBooks();
        this.toBeReturnedBooks = other.getToBeReturnedBooks();
    }

    public UserProfile(@NonNull Context ctx, @NonNull String id, @NonNull SharedPreferences sharedPref) {

        this.email = sharedPref.getString(id + "_" + EMAIL_PREFERENCE_KEY, ctx.getString(R.string.default_email));
        this.username = sharedPref.getString(id + "_" + USERNAME_PREFERENCE_KEY, ctx.getString(R.string.default_username));
        this.location = sharedPref.getString(id + "_" + LOCATION_PREFERENCE_KEY, ctx.getString(R.string.default_city));
        this.biography = sharedPref.getString(id + "_" + BIOGRAPHY_PREFERENCE_KEY, ctx.getString(R.string.default_biography));

        this.imageUri = null;
        this.imageUri = sharedPref.getString(id + "_" + IMAGE_PREFERENCE_KEY, null);

        this.rating = 4.5f;
        this.lentBooks = 18;
        this.borrowedBooks = 24;
        this.toBeReturnedBooks = 2;
    }

    public void update(@NonNull String email, @NonNull String username, @NonNull String location, @NonNull String biography) {
        this.email = email;
        this.username = username;
        this.location = location;
        this.biography = biography;
    }

    public void update(Uri imageUri) {
        this.imageUri = imageUri == null ? null : imageUri.toString();
    }

    public void save(@NonNull String id, @NonNull SharedPreferences.Editor sharedPrefEditor) {
        sharedPrefEditor.putString(id + "_" + EMAIL_PREFERENCE_KEY, this.getEmail());
        sharedPrefEditor.putString(id + "_" + USERNAME_PREFERENCE_KEY, this.getUsername());
        sharedPrefEditor.putString(id + "_" + LOCATION_PREFERENCE_KEY, this.getLocation());
        sharedPrefEditor.putString(id + "_" + BIOGRAPHY_PREFERENCE_KEY, this.getBiography());
        if (this.getImageUriOrNull() != null) {
            sharedPrefEditor.putString(id + "_" + IMAGE_PREFERENCE_KEY, this.imageUri);
        }
    }

    public boolean isValid() {
        return !Utilities.isNullOrWhitespace(email) &&
                !Utilities.isNullOrWhitespace(username) &&
                !Utilities.isNullOrWhitespace(location);
    }

    public boolean isEmailValid() {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public String getEmail() {
        return this.email;
    }

    public String getUsername() {
        return this.username;
    }

    public String getLocation() {
        return this.location;
    }

    public String getBiography() {
        return this.biography;
    }

    public Bitmap getImageBitmapOrDefault(@NonNull Context ctx) {

        Uri uri = getImageUriOrNull();
        return  Utilities.loadImage(uri, getDefaultImageUri(ctx), ctx.getContentResolver());
    }

    public float getRating() {
        return this.rating;
    }

    public int getLentBooks() {
        return this.lentBooks;
    }

    public int getBorrowedBooks() {
        return this.borrowedBooks;
    }

    public int getToBeReturnedBooks() {
        return this.toBeReturnedBooks;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof UserProfile)) {
            return false;
        }

        UserProfile otherUP = (UserProfile) other;

        String thisUri = this.imageUri;
        String otherUri = otherUP.imageUri;

        return this.getEmail().equals(otherUP.getEmail()) &&
                this.getUsername().equals(otherUP.getUsername()) &&
                this.getLocation().equals(otherUP.getLocation()) &&
                this.getBiography().equals(otherUP.getBiography()) &&
                Float.compare(this.getRating(), otherUP.getRating()) == 0 &&
                this.getLentBooks() == otherUP.getLentBooks() &&
                this.getBorrowedBooks() == otherUP.getBorrowedBooks() &&
                this.getToBeReturnedBooks() == otherUP.getToBeReturnedBooks() &&
                (thisUri == null && otherUri == null ||
                        thisUri != null && thisUri.equals(otherUri));
    }

    private Uri getImageUriOrNull() {
        return this.imageUri != null ? Uri.parse(this.imageUri) : null;
    }

    private static Uri getDefaultImageUri(Context context) {
        return Utilities.getUriToDrawable(context, R.drawable.default_header);
    }
}
