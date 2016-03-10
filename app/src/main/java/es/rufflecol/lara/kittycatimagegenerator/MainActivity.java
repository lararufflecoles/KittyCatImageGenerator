package es.rufflecol.lara.kittycatimagegenerator;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.ByteArrayOutputStream;
import java.io.File;

import es.rufflecol.lara.kittycatimagegenerator.API.KittyCatAPI;
import es.rufflecol.lara.kittycatimagegenerator.API.KittyCatAPIFactory;
import es.rufflecol.lara.kittycatimagegenerator.Model.KittyCatModel;
import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements Callback<KittyCatModel>, com.squareup.picasso.Callback {

    private static final String TWITTER_KEY = "6ztLMhGmShpEmvqwm2QYr7uGX";
    private static final String TWITTER_SECRET = "YEGuJUd7H3OgNGJP7yTPrYDaQIbh76Hi0DqVjOcQ8ddqqbQM07";
    private static final String KEY_URL = "MainActivity.Key_URL";

    private Button buttonFacebook;
    private Button buttonSave;
    private Button buttonTwitter;
    private CallbackManager callbackManager;
    private ImageView imageView;
    private KittyCatAPI api;
    private ProgressBar progressWheel;
    private ShareDialog shareDialog;
    private String url;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(KEY_URL, url);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        url = savedInstanceState.getString(KEY_URL);
        Picasso.with(this)
                .load(url)
                .fit()
                .centerInside()
                .into(imageView, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.Toolbar);
        setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        progressWheel = (ProgressBar) findViewById(R.id.progress_bar);
        imageView = (ImageView) findViewById(R.id.image);

        api = KittyCatAPIFactory.create();

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new TweetComposer(), new Crashlytics());

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        Button buttonKitty = (Button) findViewById(R.id.button_kitty);
        buttonKitty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Call<KittyCatModel> call = api.getRandomKitty();
                call.enqueue(MainActivity.this);
                imageView.setImageResource(android.R.color.transparent);
                progressWheel.setVisibility(View.VISIBLE);
            }
        });

        Button buttonCat = (Button) findViewById(R.id.button_cat);
        buttonCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Call<KittyCatModel> call = api.getRandomCat();
                call.enqueue(MainActivity.this);
                imageView.setImageResource(android.R.color.transparent);
                progressWheel.setVisibility(View.VISIBLE);
            }
        });

        buttonTwitter = (Button) findViewById(R.id.button_twitter);
        buttonTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareToTwitter();
            }
        });

        buttonSave = (Button) findViewById(R.id.button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToPhone();
            }
        });

        buttonFacebook = (Button) findViewById(R.id.button_facebook);
        buttonFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareToFacebook();
            }
        });
    }

    private void shareToTwitter() {
        BitmapDrawable imageDrawable = (BitmapDrawable) imageView.getDrawable();
        if (imageDrawable != null) {
            Bitmap bitmap = imageDrawable.getBitmap();

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "Title", null);
            Uri imageUri = Uri.parse(path);

            TweetComposer.Builder builder = new TweetComposer.Builder(this)
                    .text(getString(R.string.share_caption))
                    .image(imageUri);
            builder.show();
        } else {
            Toast.makeText(MainActivity.this, R.string.share_fail, Toast.LENGTH_LONG).show();
        }
    }

    private void saveToPhone() {
        BitmapDrawable imageDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = imageDrawable.getBitmap();

        ContentResolver contentResolver = getContentResolver();
        String title = "";
        String description = "";
        String path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, title, description);
        Uri imageUri = Uri.parse(path);

        Intent saveIntent = new Intent(Intent.ACTION_SEND);
        saveIntent.setType("image/jpeg");
        saveIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

        Intent chooserIntent = Intent.createChooser(saveIntent, getResources().getText(R.string.notification_image_share));
        PendingIntent pendingSaveIntent = PendingIntent.getActivity(this, 0, chooserIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setPriority(0)
                .setSmallIcon(R.drawable.ic_image_white_24dp)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_image_saved))
                .setContentIntent(pendingSaveIntent)
                .addAction(R.drawable.ic_share_white_24dp, "Share", pendingSaveIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(001, notificationBuilder.build()); // The first parameter allows you to update the notification later on
    }

    private void shareToFacebook() {
        BitmapDrawable imageDrawable = (BitmapDrawable) imageView.getDrawable();
        if (imageDrawable != null) {
            Bitmap bitmap = imageDrawable.getBitmap();
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
//                  .setCaption(getString(R.string.share_caption)) - Not allowed by Facebook
                    .build();
            SharePhotoContent content = new SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .build();
            shareDialog.show(content);
        } else {
            Toast.makeText(MainActivity.this, R.string.share_fail, Toast.LENGTH_LONG).show();
        }
    }

    // Method integral to the Facebook SDK CallbackManager
    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);
        callbackManager.onActivityResult(requestCode, responseCode, data);
    }

    // These two methods are from the retrofit2 Callback interface methods
    @Override
    public void onResponse(Call<KittyCatModel> call, Response<KittyCatModel> response) {
        KittyCatModel model = response.body();
        url = model.getSource();
        Picasso.with(this)
                .load(url)
                .fit()
                .centerInside()
                .into(imageView, this);
    }

    @Override
    public void onFailure(Call<KittyCatModel> call, Throwable throwable) {
        Toast.makeText(this, "Failed to access images, please try again", Toast.LENGTH_LONG).show();
        progressWheel.setVisibility(View.INVISIBLE);
    }

    // These two methods are from the com.squareup.picasso.Callback implementation
    @Override
    public void onSuccess() {
        PackageManager packageManagerTwitter = this.getPackageManager();
        try {
            PackageInfo packageInfo = packageManagerTwitter.getPackageInfo("com.twitter.android", 0);
            String getPackageInfo = packageInfo.toString();
            if (!getPackageInfo.equals("com.twitter.android")) {
                buttonTwitter.setEnabled(true);
            }
        } catch (PackageManager.NameNotFoundException exception) {
            exception.printStackTrace();
            Toast.makeText(this, R.string.install_twitter, Toast.LENGTH_LONG).show();
        }

        PackageManager packageManagerFacebook = this.getPackageManager();
        try {
            PackageInfo packageInfo = packageManagerFacebook.getPackageInfo("com.facebook.katana", 0);
            String getPackageInfo = packageInfo.toString();
            if (!getPackageInfo.equals("com.facebook.katana")) {
                buttonFacebook.setEnabled(true);
            }
        } catch (PackageManager.NameNotFoundException exception) {
            exception.printStackTrace();
            Toast.makeText(this, R.string.install_facebook, Toast.LENGTH_LONG).show();
        }
        buttonSave.setEnabled(true);
        progressWheel.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onError() {
        progressWheel.setVisibility(View.INVISIBLE);
    }

    // Menu code
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                openAbout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openAbout() {
        Intent openAbout = new Intent(this, AboutActivity.class);
        startActivity(openAbout);
    }
}