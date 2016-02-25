package es.rufflecol.lara.kittycatimagegenerator;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import static android.support.v7.app.AlertDialog.*;

public class MainActivity extends AppCompatActivity implements Callback<KittyCatModel>, com.squareup.picasso.Callback {

    private static final String KEY_URL = "MainActivity.Key_URL";

    private Button buttonFacebook;
    private CallbackManager callbackManager;
    private EditText alertDialogShareCaption;
    private ImageView imageView;
    private KittyCatAPI api;
    private ProgressBar progressWheel;
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
        setTitle(R.string.main_activity_toolbar_name);
        setSupportActionBar(toolbar);

        progressWheel = (ProgressBar) findViewById(R.id.progress_bar);
        imageView = (ImageView) findViewById(R.id.image);

        api = KittyCatAPIFactory.create();

        FacebookSdk.sdkInitialize(getApplicationContext());

        Button buttonKitty = (Button) findViewById(R.id.button_kitty);
        buttonKitty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Call<KittyCatModel> call = api.getRandomKitty();
                call.enqueue(MainActivity.this);
                progressWheel.setVisibility(View.VISIBLE);
            }
        });

        Button buttonCat = (Button) findViewById(R.id.button_cat);
        buttonCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Call<KittyCatModel> call = api.getRandomCat();
                call.enqueue(MainActivity.this);
                progressWheel.setVisibility(View.VISIBLE);
            }
        });

        buttonFacebook = (Button) findViewById(R.id.button_facebook);
        buttonFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareToFacebookAlertDialog();
            }
        });
    }

    private void shareToFacebookAlertDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.alert_dialog, null);

        alertDialogShareCaption = (EditText) alertLayout.findViewById(R.id.caption_facebook);

        AlertDialog.Builder alertDialogBuilder = new Builder(MainActivity.this);
        alertDialogBuilder
                .setView(alertLayout)
                .setCancelable(true) // Allows user to use back button to exit dialog
                .setNegativeButton(R.string.alert_dialog_back, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(R.string.alert_dialog_share, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        shareToFacebookContent();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

//        Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
//        negativeButton.setBackgroundColor(getResources().getColor(R.color.colorBackground));
//
//        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
//        positiveButton.setBackgroundColor(getResources().getColor(R.color.colorBackground));
    }

    private void shareToFacebookContent() {
        callbackManager = CallbackManager.Factory.create();
        List<String> permissionNeeds = Arrays.asList("publish_actions");
        LoginManager loginManager = LoginManager.getInstance();
        loginManager.logInWithPublishPermissions(MainActivity.this, permissionNeeds);
        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                BitmapDrawable imageDrawable = (BitmapDrawable) imageView.getDrawable();
                if (imageDrawable != null) {
                    Bitmap bitmap = imageDrawable.getBitmap();
                    SharePhoto photo = new SharePhoto.Builder()
                            .setBitmap(bitmap)
                            .setCaption(alertDialogShareCaption.getText().toString())
                            .build();
                    SharePhotoContent content = new SharePhotoContent.Builder()
                            .addPhoto(photo)
                            .build();
                    ShareApi.share(content, null);
                } else {
                    Toast.makeText(MainActivity.this, R.string.share_to_facebook_on_success_fail, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancel() {
                System.out.println("onCancel");
                Toast.makeText(MainActivity.this, R.string.share_to_facebook_on_cancel, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException exception) {
                System.out.println("onError");
                Toast.makeText(MainActivity.this, R.string.share_to_facebook_on_error, Toast.LENGTH_LONG).show();
            }
        });
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
        Toast.makeText(this, "Failed to access images", Toast.LENGTH_LONG).show();
        progressWheel.setVisibility(View.INVISIBLE);
    }

    // These two methods are from the com.squareup.picasso.Callback implementation
    @Override
    public void onSuccess() {
        buttonFacebook.setEnabled(true);
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