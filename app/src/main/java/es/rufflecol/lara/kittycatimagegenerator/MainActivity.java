package es.rufflecol.lara.kittycatimagegenerator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.squareup.picasso.Picasso;
import com.facebook.FacebookSdk;

import java.util.Arrays;
import java.util.List;

import es.rufflecol.lara.kittycatimagegenerator.API.KittyCatAPI;
import es.rufflecol.lara.kittycatimagegenerator.API.KittyCatAPIFactory;
import es.rufflecol.lara.kittycatimagegenerator.Model.KittyCatModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements Callback<KittyCatModel>, com.squareup.picasso.Callback {

    private static final String KEY_URL = "MainActivity.Key_URL";

    private KittyCatAPI api;
    private ImageView imageView;
    private ProgressBar progress;
    private String url;
    private CallbackManager callbackManager;
    private LoginManager loginManager;
    private Bitmap bitmap;

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
        setTitle(R.string.main_activity_toolbar_name);
        setSupportActionBar(toolbar);

        FacebookSdk.sdkInitialize(getApplicationContext());

        progress = (ProgressBar) findViewById(R.id.progress_bar);
        imageView = (ImageView) findViewById(R.id.image);

        api = KittyCatAPIFactory.create();

        Button buttonKitty = (Button) findViewById(R.id.button_kitty);
        buttonKitty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Call<KittyCatModel> call = api.getRandomKitty();
                call.enqueue(MainActivity.this);
                progress.setVisibility(View.VISIBLE);
            }
        });

        Button buttonCat = (Button) findViewById(R.id.button_cat);
        buttonCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Call<KittyCatModel> call = api.getRandomCat();
                call.enqueue(MainActivity.this);
                progress.setVisibility(View.VISIBLE);
            }
        });

        Button buttonFacebook = (Button) findViewById(R.id.button_facebook);
        buttonFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareToFacebook();
            }
        });
    }

    private void shareToFacebook() {
        callbackManager = CallbackManager.Factory.create();
        List<String> permissionNeeds = Arrays.asList("publish_actions");
        loginManager = LoginManager.getInstance(); // loginManager helps you eliminate adding a LoginButton to your UI
        loginManager.logInWithPublishPermissions(MainActivity.this, permissionNeeds);
        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                BitmapDrawable imageDrawable = (BitmapDrawable) imageView.getDrawable();
                bitmap = imageDrawable.getBitmap();
                SharePhoto photo = new SharePhoto.Builder()
                        .setBitmap(bitmap)
                        .setCaption("Test")
                        .build();
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                ShareApi.share(content, null);
                Toast.makeText(MainActivity.this, "Share successful", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                System.out.println("onCancel");
                Toast.makeText(MainActivity.this, "Share cancelled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException exception) {
                System.out.println("onError");
                Toast.makeText(MainActivity.this, "Share errored", Toast.LENGTH_LONG).show();
            }
        });
    }

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
        Toast.makeText(this, "Failed to access images", Toast.LENGTH_LONG).show();
        progress.setVisibility(View.INVISIBLE);
    }

    // These two methods are from the com.squareup.picasso.Callback implementation
    @Override
    public void onSuccess() {
        progress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onError() {
        progress.setVisibility(View.INVISIBLE);
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