package org.sabda.alkitabquotegenerator.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

import org.sabda.alkitabquotegenerator.App;
import org.sabda.alkitabquotegenerator.R;
import org.sabda.alkitabquotegenerator.util.Helpers;

public class ImageViewActivity extends AppCompatActivity {
    //########################
    //## UI Binding
    //########################
   // @BindView(R.id.imageview_activity__expanded_image)
    ImageView expandedImageView;

   // @BindView(R.id.toolbar)
    Toolbar toolbar;

    //#####################
    //## Members
    //#####################
    private String imagePath;
    private Bitmap mBitmap = null;
    private App app;

    //#####################
    //## Methods
    //#####################
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageview__activity);

        app = (App) getApplication();
        //Helpers.get().enableImmersiveMode(getWindow().getDecorView());
        expandedImageView = findViewById(R.id.imageview_activity__expanded_image);
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            ActionBar ab = getSupportActionBar();
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        imagePath = getIntent().getStringExtra(MainActivity.IMAGE_PATH);
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            // Thumbnail
            imagePath = imagePath.replace(getString(R.string.app_name) + "_",
                    ".thumbnails" + File.separator + getString(R.string.app_name) + "_");
        }

        mBitmap = Helpers.get().loadImageFromFilesystem(imagePath);
        expandedImageView.setImageBitmap(mBitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.imageview__menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        expandedImageView.setImageBitmap(null);
        if (mBitmap != null && !mBitmap.isRecycled())
            mBitmap.recycle();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_share) {
                app.shareBitmapToOtherApp(mBitmap, this);
                return true;
            }
        else if(item.getItemId() == R.id.action_delete) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("Hapus Rekaman")
                        .setMessage("Apakah anda ingin menghapus rekaman ini ?")
                        .setNegativeButton("Tidak", null)
                        .setPositiveButton(R.string.main__yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                File file = new File(imagePath);
                                if (file.exists()) {
                                    file.delete();
                                }
                                finish();
                            }
                        });
                dialog.show();

                return true;
            }

        return super.onOptionsItemSelected(item);
    }

    /**
     * The image was clicked
     */

}
