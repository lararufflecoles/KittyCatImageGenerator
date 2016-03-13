package es.rufflecol.lara.kittycatimagegenerator;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.IOException;

public class SetWallpaperService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Uri uri = intent.getExtras().getParcelable(Intent.EXTRA_STREAM);
        setWallpaper(uri);
        return super.onStartCommand(intent, flags, startId);
    }

    private void setWallpaper(Uri uri) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            wallpaperManager.setBitmap(bitmap);
        } catch (IOException exception) {
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}