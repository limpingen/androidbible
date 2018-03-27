package org.sabda.alkitabquotegenerator.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.FileProvider;

import android.support.v7.app.ActionBar;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sabda.alkitabquotegenerator.BuildConfig;
import org.sabda.alkitabquotegenerator.R;
import org.sabda.alkitabquotegenerator.data.MemeCategory;
import org.sabda.alkitabquotegenerator.data.MemeFont;
import org.sabda.alkitabquotegenerator.data.MemeLibConfig;
import org.sabda.alkitabquotegenerator.data.MemeOriginAssets;
import org.sabda.alkitabquotegenerator.data.MemeOriginFavorite;
import org.sabda.alkitabquotegenerator.data.MemeOriginInterface;
import org.sabda.alkitabquotegenerator.data.MemeOriginStorage;
import org.sabda.alkitabquotegenerator.ui.GridDecoration;
import org.sabda.alkitabquotegenerator.ui.GridRecycleAdapter;
import org.sabda.alkitabquotegenerator.util.AppSettings;
import org.sabda.alkitabquotegenerator.util.Helpers;
import org.sabda.opoc.util.HelpersA;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TabLayout.OnTabSelectedListener {

    public static final int REQUEST_LOAD_GALLERY_IMAGE = 50;
    public static final int REQUEST_TAKE_CAMERA_PICTURE = 51;
    public static final String IMAGE_PATH = "imagePath";
    private static boolean isShowingFullscreenImage = false;

   public EditText ayat;


   public EditText lokasiayat;

    Menu mMenu;

    //@BindView(R.id.toolbar)
    Toolbar toolbar;


   // @BindView(R.id.main__activity__navview)
    NavigationView navigationView;

    //@BindView(R.id.main__tabs)
    TabLayout tabLayout;

    //@BindView(R.id.main__activity__recycler_view)
    RecyclerView recyclerMemeList;


    private MemeCategory mMemeCategory = null;
    private String cameraPictureFilepath = "";
    public String teks = "";
    public String lokasiteks = "";
    Button ClearButton;
    public AppSettings settings;
    List<MemeCategory> memeCategories;
    List<MemeFont> fonts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main__activity);
         toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        //navigationView = findViewById(R.id.main__activity__navview);

        tabLayout = findViewById(R.id.main__tabs);
        tabLayout.setOnTabSelectedListener(this);
        ayat = (EditText) findViewById(R.id.textViewAyat);
        lokasiayat = (EditText) findViewById(R.id.textViewLokasiAyat);
        Intent intent = getIntent();
        if(intent !=null)
        {
            teks = intent.getStringExtra("ayatteks");
            lokasiteks = intent.getStringExtra("lokasiteks");
        }
        // Bind UI
        loadFonts();
        loadMemeNames();
        settings = new AppSettings(getApplicationContext());

        ;

        if (settings.isAppFirstStart(false)) {
            // Set default values (calculated in getters)
            settings.setGridColumnCountPortrait(settings.getGridColumnCountPortrait());
            settings.setGridColumnCountLandscape(settings.getGridColumnCountLandscape());
        }



        Helpers myhelper = new Helpers(getBaseContext());
        // Setup Floating Action Button
        int gridColumns = myhelper.isInPortraitMode()
                ? settings.getGridColumnCountPortrait()
                : settings.getGridColumnCountLandscape();
        recyclerMemeList = (RecyclerView)findViewById(R.id.main__activity__recycler_view);
        recyclerMemeList.setHasFixedSize(true);
        RecyclerView.LayoutManager recyclerGridLayout = new GridLayoutManager(this, gridColumns);
        recyclerMemeList.setLayoutManager(recyclerGridLayout);
        recyclerMemeList.addItemDecoration(new GridDecoration(10));

        mMemeCategory = getMemeCategory(MemeLibConfig.MEME_CATEGORIES.ALL[settings.getLastSelectedCategory()]);

        for (String cat : getResources().getStringArray(R.array.meme_categories)) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(cat);
            tabLayout.addTab(tab);
        }
        selectTab(settings.getLastSelectedCategory(), settings.getDefaultMainMode());

        //
        // Actions based on build type or version
        //

        if(teks!="")
        {
            ayat.setText(teks.trim());
            lokasiayat.setText(lokasiteks);
        }
        else {
            // Show first start dialog / changelog
            ayat.setText("Segala tulisan yang diilhamkan Allah memang bermanfaat untuk mengajar, untuk menyatakan kesalahan, untuk memperbaiki kelakuan dan untuk mendidik orang dalam kebenaran");
            lokasiayat.setText("2 Timotius 3:16");
        }

        if (BuildConfig.IS_TEST_BUILD) {
            ((ImageView) navigationView.getHeaderView(0).findViewById(R.id.main__activity__navheader__image)).setImageResource(R.drawable.ic_launcher_test);
        }
        ClearButton = (Button) findViewById(R.id.buttonclear);
        ClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ayat.setText("");
                lokasiayat.setText("");
                ayat.setHintTextColor(getResources().getColor(R.color.colorPrimaryDark));
                lokasiayat.setHintTextColor(getResources().getColor(R.color.colorPrimaryDark));
            }

        });
        setTitle("Alkitab Quote Generator");
    }

    public String getAyat()
    {
        return ayat.getText().toString();
    }

    public String getLokasiAyat()
    {
        return lokasiayat.getText().toString();
    }
    @SuppressWarnings("ConstantConditions")
    private void selectTab(int pos, int mainMode) {
        MenuItem navItem = null;
        switch (mainMode) {
            case 0:
                pos = pos >= 0 ? pos : tabLayout.getTabCount() - 1;
                pos = pos < tabLayout.getTabCount() ? pos : 0;
                tabLayout.getTabAt(pos).select();

                break;
            case 1:
                navItem = navigationView.getMenu().findItem(R.id.action_mode_favs);
                break;
            case 2:
                navItem = navigationView.getMenu().findItem(R.id.action_mode_saved);

                break;
        }

        if (navItem != null) {
            navigationView.setCheckedItem(navItem.getItemId());
            onNavigationItemSelected(navItem);
        }
    }

    @Override
    protected void onResume() {
        if (isShowingFullscreenImage) {
            isShowingFullscreenImage = false;
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }
        MemeOriginInterface memeOriginObject = null;
        memeOriginObject = new MemeOriginAssets(mMemeCategory, getAssets());
        setTitle("Alkitab Quote Generator");
        ayat.setVisibility(View.VISIBLE);
        lokasiayat.setVisibility(View.VISIBLE);

        if (memeOriginObject != null) {
            tabLayout.setVisibility(View.VISIBLE);
           ;
            GridRecycleAdapter recyclerMemeAdapter = new GridRecycleAdapter(memeOriginObject, this);
            recyclerMemeList.setAdapter(recyclerMemeAdapter);

        }

        super.onResume();
    }


    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean handleBarClick(MenuItem item) {
        MemeOriginInterface memeOriginObject = null;
        int i = item.getItemId();
        if(i==R.id.home)
        {
            finish();
        }
        else if(i== R.id.action_settings){
                HelpersA.get(this).animateToActivity(SettingsActivity.class, false, SettingsActivity.ACTIVITY_ID);
                return true;
            }
        else if(i == R.id.action_exit) {
                finish();
                return true;
            }
        else if(i == R.id.action_picture_from_gallery ){
                Intent x = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                HelpersA.get(this).animateToActivity(x, false, REQUEST_LOAD_GALLERY_IMAGE);
                return true;
            }
        else if(i == R.id.action_picture_from_camera) {
                showCameraDialog();
                return true;
            }
        else if(i== R.id.action_mode_create) {
                memeOriginObject = new MemeOriginAssets(mMemeCategory, getAssets());
                toolbar.setTitle(R.string.app_name);
                ayat.setVisibility(View.VISIBLE);
                lokasiayat.setVisibility(View.VISIBLE);
                mMenu.findItem(R.id.action_picture_from_camera).setVisible(true);
                mMenu.findItem(R.id.action_picture_from_gallery).setVisible(true);
                ClearButton.setVisibility(View.VISIBLE);

            }
        else if(i == R.id.action_mode_favs) {
                memeOriginObject = new MemeOriginFavorite(settings.getFavoriteMemes(), getAssets());
                toolbar.setTitle(R.string.main__mode__favs);
                ayat.setVisibility(View.VISIBLE);
                lokasiayat.setVisibility(View.VISIBLE);
                mMenu.findItem(R.id.action_picture_from_camera).setVisible(true);
                mMenu.findItem(R.id.action_picture_from_gallery).setVisible(true);
                ClearButton.setVisibility(View.VISIBLE);

            }
        else if(i==  R.id.action_mode_saved) {
                File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getString(R.string.app_name));
                filePath.mkdirs();
                memeOriginObject = new MemeOriginStorage(filePath, getString(R.string.dot_thumbnails));
                toolbar.setTitle(R.string.main__mode__saved);
                ayat.setVisibility(View.GONE);
                lokasiayat.setVisibility(View.GONE);
                mMenu.findItem(R.id.action_picture_from_camera).setVisible(false);
                mMenu.findItem(R.id.action_picture_from_gallery).setVisible(false);
                ClearButton.setVisibility(View.GONE);


        }

        // Change mode
        if (memeOriginObject != null) {
            tabLayout.setVisibility(item.getItemId() == R.id.action_mode_create ? View.VISIBLE : View.GONE);

            GridRecycleAdapter recyclerMemeAdapter = new GridRecycleAdapter(memeOriginObject, this);
            recyclerMemeList.setAdapter(recyclerMemeAdapter);
            return true;
        }

        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOAD_GALLERY_IMAGE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    // String picturePath contains the path of selected Image
                    onImageTemplateWasChosen(picturePath, false);
                }
            } else {
                HelpersA.get(this).showSnackBar(R.string.main__error_no_picture_selected, false);
            }
        }

        if (requestCode == REQUEST_TAKE_CAMERA_PICTURE) {
            if (resultCode == RESULT_OK) {
                onImageTemplateWasChosen(cameraPictureFilepath, false);
            } else {
                HelpersA.get(this).showSnackBar(R.string.main__error_no_picture_selected, false);
            }
        }
    }

    /**
     * Show the camera picker via intent
     * Source: http://developer.android.com/training/camera/photobasics.html
     */
    public void showCameraDialog() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                // Create an image file name
                String imageFileName = getString(R.string.app_name) + "_" + System.currentTimeMillis();
                File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM), "Camera");
                photoFile = File.createTempFile(imageFileName, ".jpg", storageDir);

                // Save a file: path for use with ACTION_VIEW intents
                cameraPictureFilepath = photoFile.getAbsolutePath();

            } catch (IOException ex) {
                HelpersA.get(this).showSnackBar(R.string.main__error_camera_cannot_start, false);
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri uri = FileProvider.getUriForFile(this, getString(R.string.app_fileprovider), photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                } else {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                }
                HelpersA.get(this).animateToActivity(takePictureIntent, false, REQUEST_TAKE_CAMERA_PICTURE);
            }
        }

    }

    public void onImageTemplateWasChosen(String filePath, boolean bIsAsset) {
        final Intent intent = new Intent(this, MemeCreateActivity.class);
        intent.putExtra(MemeCreateActivity.EXTRA_IMAGE_PATH, filePath);
        intent.putExtra(MemeCreateActivity.ASSET_IMAGE, bIsAsset);
        intent.putExtra("ayat", ayat.getText().toString() );
        intent.putExtra("lokasiayat", lokasiayat.getText().toString());


        HelpersA.get(this).animateToActivity(intent, false, MemeCreateActivity.RESULT_MEME_EDITING_FINISHED);
    }


    public void openImageViewActivityWithImage(String imagePath) {
        isShowingFullscreenImage = true;

        Intent intent = new Intent(this, ImageViewActivity.class);
        intent.putExtra(IMAGE_PATH, imagePath);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        HelpersA.get(this).animateToActivity(intent, false, null);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int tabPos = tab.getPosition();
        if (tabPos >= 0 && tabPos < MemeLibConfig.MEME_CATEGORIES.ALL.length) {
            mMemeCategory = getMemeCategory(MemeLibConfig.MEME_CATEGORIES.ALL[tabPos]);
            MemeOriginInterface memeOriginObject = new MemeOriginAssets(mMemeCategory, getAssets());
            GridRecycleAdapter recyclerMemeAdapter = new GridRecycleAdapter(memeOriginObject, this);
            recyclerMemeList.setAdapter(recyclerMemeAdapter);
            settings.setLastSelectedCategory(MemeLibConfig.getIndexOfCategory(mMemeCategory.getCategoryName()));
        }
    }

    private final RectF point = new RectF(0, 0, 0, 0);
    private static final int SWIPE_MIN_DX = 150;
    private static final int SWIPE_MAX_DY = 90;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        return super.dispatchTouchEvent(event);
    }


    //########################
    //## Single line overrides
    //########################
    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        onTabSelected(tab);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main__menu, menu);



        mMenu = menu;

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return handleBarClick(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if(item.getItemId()==android.R.id.home)
        {
            finish();
            return super.onOptionsItemSelected(item);
        }

        return handleBarClick(item);
    }

    public void loadFonts() {
        String FONT_FOLDER = MemeLibConfig.getPath(MemeLibConfig.Assets.FONTS, false);
        try {
            String[] fontFilenames = getAssets().list(FONT_FOLDER);
            FONT_FOLDER = MemeLibConfig.getPath(FONT_FOLDER, true);
            fonts = new ArrayList<>();

            for (int i = 0; i < fontFilenames.length; i++) {
                Typeface tf = Typeface.createFromAsset(getResources().getAssets(), FONT_FOLDER + fontFilenames[i]);
                fonts.add(new MemeFont(FONT_FOLDER + fontFilenames[i], tf));
            }
        } catch (IOException e) {
            log("Could not load fonts");
            fonts = new ArrayList<>();
        }
    }

    public void loadMemeNames() {
        String IMAGE_FOLDER = MemeLibConfig.getPath(MemeLibConfig.Assets.MEMES, false);
        try {
            String[] memeCategories = getAssets().list(IMAGE_FOLDER);
            IMAGE_FOLDER = MemeLibConfig.getPath(IMAGE_FOLDER, true);
            this.memeCategories = new ArrayList<MemeCategory>();

            for (String memeCat : memeCategories) {
                this.memeCategories.add(new MemeCategory(memeCat, getAssets().list(IMAGE_FOLDER + memeCat)));
            }
        } catch (IOException e) {
            log("Could not load images");
            memeCategories = new ArrayList<MemeCategory>();
        }
    }

    public List<MemeFont> getFonts() {
        return this.fonts;
    }

    // Get meme category object (parameter = foldername in assets)
    public MemeCategory getMemeCategory(String category) {
        for (MemeCategory cat : memeCategories) {
            if (cat.getCategoryName().equalsIgnoreCase(category))
                return cat;
        }
        return null;
    }

    public void shareBitmapToOtherApp(Bitmap bitmap, Activity activity) {
        File imageFile = Helpers.get().saveBitmapToFile(getCacheDir().getAbsolutePath(), getString(R.string.cached_picture_filename), bitmap);
        if (imageFile != null) {
            Uri imageUri = FileProvider.getUriForFile(this, getString(R.string.app_fileprovider), imageFile);
            if (imageUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(imageUri, getContentResolver().getType(imageUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                activity.startActivity(Intent.createChooser(shareIntent, getString(R.string.main__share_meme_prompt)));
            }
        }
    }

    public static void log(String text) {
        if (BuildConfig.DEBUG) {
            Log.d("MemeTastic", text);
        }
    }

}
