package it.polito.mad.lab01;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import static android.provider.MediaStore.Images.Media.DATA;
import static android.provider.MediaStore.Images.Media.getBitmap;
import static android.provider.MediaStore.Video;

public class Utilities {

    public static Uri getUriToDrawable(@NonNull Context context, @AnyRes int drawableId) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.getResources().getResourcePackageName(drawableId)
                + '/' + context.getResources().getResourceTypeName(drawableId)
                + '/' + context.getResources().getResourceEntryName(drawableId));
    }

    public static boolean isNullOrWhitespace(String s) {
        if (s == null)
            return true;

        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }

        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();

        if (source != null) {
            destination.transferFrom(source, 0, source.size());
            source.close();
        }

        destination.close();
    }

    public static String getRealPathFromURI(@NonNull Activity activity, @NonNull Uri contentUri) {
        String[] proj = { Video.Media.DATA };
        Cursor cursor = activity.managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private static Bitmap rotateImage(@NonNull Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public static Bitmap loadImage(Uri imageUri, @NonNull Uri defaultUri, int targetWidth, int targetHeight, ContentResolver ctr) {
        ExifInterface ei = null;
        Bitmap bitmap = null;

        if (imageUri != null) {
            try {
                bitmap = Utilities.getImage(imageUri.getPath(), targetWidth, targetHeight);
                ei = new ExifInterface(imageUri.getPath());
            } catch (IOException e) {
                bitmap = null;
            }
        }else{
            try {
                return getBitmap(ctr, defaultUri);
            } catch (IOException e) {
                return null;
            }
        }

        // Use the default image
        if (bitmap == null) {
            return Utilities.getImage(defaultUri.getPath(), targetWidth, targetHeight);
        }

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(bitmap, 270);

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                return bitmap;
        }
    }

    public static Bitmap getImage(String imagePath, int targetWidth, int targetHeight) {

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetWidth, photoH/targetHeight);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        return bitmap;
    }

    public interface TextWatcherValidator {
        boolean validate(String string);
    }

    public static class GenericTextWatcher implements TextWatcher {

        private final EditText textField;
        private final String errorMessage;
        private final TextWatcherValidator validator;

        public GenericTextWatcher(@NonNull EditText textField, @NonNull String errorMessage,
                                  @NonNull TextWatcherValidator validator){
            this.textField = textField;
            this.errorMessage = errorMessage;
            this.validator = validator;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!validator.validate(editable.toString())) {
                textField.setError(errorMessage);
            }
        }
    }
}
