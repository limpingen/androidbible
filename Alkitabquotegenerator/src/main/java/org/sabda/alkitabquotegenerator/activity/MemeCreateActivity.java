package org.sabda.alkitabquotegenerator.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.OnSheetDismissedListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.sabda.alkitabquotegenerator.R;
import org.sabda.alkitabquotegenerator.data.MemeCategory;
import org.sabda.alkitabquotegenerator.data.MemeFont;
import org.sabda.alkitabquotegenerator.data.MemeLibConfig;
import org.sabda.alkitabquotegenerator.data.MemeSetting;
import org.sabda.alkitabquotegenerator.ui.FontAdapter;
import org.sabda.alkitabquotegenerator.util.AppSettings;
import org.sabda.alkitabquotegenerator.util.Helpers;
import org.sabda.opoc.util.HelpersA;
import uz.shift.colorpicker.LineColorPicker;

/**
 * Activity for creating memes
 */
public class MemeCreateActivity extends AppCompatActivity
        implements MemeSetting.OnMemeSettingChangedListener,
        BottomSheetLayout.OnSheetStateChangeListener, OnSheetDismissedListener {
    //########################
    //## Static
    //########################
    public final static int RESULT_MEME_EDITING_FINISHED = 150;
    public final static int RESULT_MEME_EDIT_SAVED = 1;
    public final static int RESULT_MEME_NOT_SAVED = 0;
    public final static String EXTRA_IMAGE_PATH = "extraImage";
    public final static String ASSET_IMAGE = "assetImage";

    //########################
    //## UI Binding
    //########################
    //@BindView(R.id.fab)
    FloatingActionButton fab;

    //@BindView(R.id.toolbar)
    Toolbar toolbar;

   // @BindView(R.id.memecreate__activity__bottomsheet_layout)
    BottomSheetLayout bottomSheet;

   // @BindView(R.id.memecreate__activity__image)
    ImageView imageEditView;


    EditText textEditBottomCaption;


    EditText textEditTopCaption;
    public AppSettings settings;
    List<MemeCategory> memeCategories;
    List<MemeFont> fonts;
    //#####################
    //## Members
    //#####################
    private static boolean doubleBackToExitPressedOnce = false;
    private Bitmap lastBitmap = null;
    private long memeSavetime = -1;

    private MemeSetting memeSetting;
    private boolean bFullscreenImage = true;
    private Bundle savedInstanceState = null;

    //#####################
    //## Methods
    //#####################


    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memecreate__activity);
        settings = new AppSettings(getApplicationContext());
        loadFonts();
        loadMemeNames();
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.setVisibility(View.INVISIBLE);
                bottomSheet.showWithSheetView(((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
                        inflate(R.layout.memecreate__bottom_sheet, bottomSheet, false));
                bottomSheet.addOnSheetStateChangeListener(MemeCreateActivity.this);
                bottomSheet.addOnSheetDismissedListener(MemeCreateActivity.this);

                LineColorPicker colorPickerShade = findViewById( R.id.memecreate__bottom_sheet__color_picker_for_border);
                LineColorPicker colorPickerText = findViewById(R.id.memecreate__bottom_sheet__color_picker_for_text);
                Spinner dropdownFont = findViewById(R.id.memecreate__bottom_sheet__dropdown_font);
                SeekBar seekFontSize = findViewById(R.id.memecreate__bottom_sheet__seek_font_size);
                SeekBar seekFontSize2 = findViewById(R.id.memecreate__bottom_sheet__seek_font_size2);
                ToggleButton toggleAllCaps = findViewById( R.id.memecreate__bottom_sheet__toggle_all_caps);
                Button rotateButton = findViewById( R.id.memecreate__bottom_sheet__rotate_plus_90deg);

                colorPickerText.setColors(MemeLibConfig.MEME_COLORS.ALL);
                colorPickerShade.setColors(MemeLibConfig.MEME_COLORS.ALL);

                FontAdapter adapter = new FontAdapter(
                        MemeCreateActivity.this, android.R.layout.simple_list_item_1, getFonts());
                dropdownFont.setAdapter(adapter);


                // Apply existing settings
                colorPickerText.setSelectedColor(memeSetting.getTextColor());
                colorPickerShade.setSelectedColor(memeSetting.getBorderColor());
                dropdownFont.setSelection(memeSetting.getFontId());
                toggleAllCaps.setChecked(memeSetting.isAllCaps());
                ((SeekBar) findViewById(R.id.memecreate__bottom_sheet__seek_font_size)).setProgress(memeSetting.getFontSize() - MemeLibConfig.FONT_SIZES.MIN);
                ((SeekBar) findViewById(R.id.memecreate__bottom_sheet__seek_font_size2)).setProgress(memeSetting.getFontSize2() - MemeLibConfig.FONT_SIZES.MIN);


                //
                //  Add bottom sheet listeners
                //
                colorPickerShade.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        LineColorPicker picker = (LineColorPicker) v;
                        memeSetting.setBorderColor(picker.getColor());
                    }
                });
                colorPickerText.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        LineColorPicker picker = (LineColorPicker) v;
                        memeSetting.setTextColor(picker.getColor());
                    }
                });
                dropdownFont.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onNothingSelected(AdapterView<?> parent) {
                    }

                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        memeSetting.setFont((MemeFont) parent.getSelectedItem());
                        memeSetting.setFontId(parent.getSelectedItemPosition());
                        settings.setLastSelectedFont(memeSetting.getFontId());
                    }
                });
                seekFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        memeSetting.setFontSize(progress + MemeLibConfig.FONT_SIZES.MIN);
                    }
                });

                seekFontSize2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        memeSetting.setFontSize2(progress + MemeLibConfig.FONT_SIZES.MIN);
                    }
                });
                toggleAllCaps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        memeSetting.setAllCaps(isChecked);
                    }
                });
                rotateButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        memeSetting.setRotationDeg((memeSetting.getRotationDeg() + 90) % 360);
                    }
                });
            }
        });
        toolbar = findViewById(R.id.toolbar);
        bottomSheet = findViewById(R.id.memecreate__activity__bottomsheet_layout);
        imageEditView = findViewById(R.id.memecreate__activity__image);
        imageEditView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelpersA.get(MemeCreateActivity.this).hideSoftKeyboard();
            }
        });
        // Quit activity if no image was given
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (!(Intent.ACTION_SEND.equals(action) && type.startsWith("image/")) &&
                (!getIntent().hasExtra(EXTRA_IMAGE_PATH) || !getIntent().hasExtra(ASSET_IMAGE))) {
            finish();
            return;
        }


        // Bind Ui


        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        initMemeSettings(savedInstanceState);
        textEditTopCaption = (EditText) findViewById(R.id.memecreate__activity__edit_caption_top);
        textEditTopCaption.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                memeSetting.setCaptionTop(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        textEditBottomCaption = (EditText) findViewById(R.id.memecreate__activity__edit_caption_bottom);
        textEditBottomCaption.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                memeSetting.setCaptionBottom(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        Bundle extras = intent.getExtras();
        if(extras!=null)
        {
            textEditTopCaption.setText(extras.getString("ayat"));
            textEditBottomCaption.setText(extras.getString("lokasiayat"));
        }
        memeSetting.setMemeSettingChangedListener(this);
        memeSetting.notifyChangedListener();
        memeSetting.setCaptionTop(textEditTopCaption.getText().toString());
        memeSetting.setCaptionBottom(textEditBottomCaption.getText().toString());
        setTitle("Alkitab Quote Generator");
    }

    public void initMemeSettings(Bundle savedInstanceState) {
        Bitmap bitmap = extractBitmapFromIntent(getIntent());
        if (savedInstanceState != null && savedInstanceState.containsKey("memeObj")) {
            memeSetting = (MemeSetting) savedInstanceState.getSerializable("memeObj");
            memeSetting.setImage(bitmap);
            memeSetting.setFont(getFonts().get(settings.getLastSelectedFont()));
        } else {
            memeSetting = new MemeSetting(getFonts().get(settings.getLastSelectedFont()), bitmap);
            memeSetting.setFontId(settings.getLastSelectedFont());
        }
        memeSetting.setDisplayImage(memeSetting.getImage().copy(Bitmap.Config.RGB_565, false));




    }
    public List<MemeFont> getFonts() {
        return this.fonts;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        prepareForSaving();
        outState.putSerializable("memeObj", memeSetting);
        this.savedInstanceState = outState;
    }

    private void prepareForSaving() {
        memeSetting.setMemeSettingChangedListener(null);
        imageEditView.setImageBitmap(null);
        if (lastBitmap != null && !lastBitmap.isRecycled())
            lastBitmap.recycle();
        if (memeSetting.getImage() != null && !memeSetting.getImage().isRecycled())
            memeSetting.getImage().recycle();
        if (memeSetting.getDisplayImage() != null && !memeSetting.getDisplayImage().isRecycled())
            memeSetting.getDisplayImage().recycle();
        lastBitmap = null;
        memeSetting.setDisplayImage(null);
        memeSetting.setImage(null);
        memeSetting.setFont(null);
        memeSetting.setMemeSettingChangedListener(null);
    }

    @Override
    protected void onDestroy() {
        prepareForSaving();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bFullscreenImage) {
            bFullscreenImage = false;
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }
        if (savedInstanceState != null) {
            initMemeSettings(savedInstanceState);
        }



    }

    private Bitmap extractBitmapFromIntent(final Intent intent) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = null;
        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);

        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEND) ) {
            Uri imageURI = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageURI != null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageURI);
                } catch (IOException e) {
                    bitmap = null;
                    e.printStackTrace();
                }
            }

        } else if (intent.getBooleanExtra(ASSET_IMAGE, false)) {
            try {
                //Scale big images down to avoid "out of memory"
                InputStream inputStream = getAssets().open(imagePath);
                BitmapFactory.decodeStream(inputStream, new Rect(0, 0, 0, 0), options);
                Helpers myhelper = new Helpers(this);
                options.inSampleSize = myhelper.calculateInSampleSize(options, settings.getRenderQualityReal());
                options.inJustDecodeBounds = false;
                inputStream.close();
                inputStream = getAssets().open(imagePath);
                bitmap = BitmapFactory.decodeStream(inputStream, new Rect(0, 0, 0, 0), options);
            } catch (IOException e) {
                bitmap = null;
                e.printStackTrace();
            }
        } else {
            //Scale big images down to avoid "out of memory"
            BitmapFactory.decodeFile(imagePath, options);
            Helpers myhelper = new Helpers(this);
            options.inSampleSize = myhelper.calculateInSampleSize(options, settings.getRenderQualityReal());
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(imagePath, options);
        }
        return bitmap;
    }

    @Override
    public void onBackPressed() {
        boolean hasTextInput = !textEditTopCaption.getText().toString().isEmpty() || !textEditBottomCaption.getText().toString().isEmpty();

        // Close views above
        if (bottomSheet.isSheetShowing()) {
            bottomSheet.dismissSheet();
            return;
        }

        // Auto save if option checked
        if (hasTextInput && settings.isAutoSaveMeme()) {
            if (saveMemeToFilesystem(false)) {
                finish();
                return;
            }
        }

        // Close if no input
        if (!hasTextInput) {
            finish();
            return;
        }

        // Else wait for double back-press
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        doubleBackToExitPressedOnce = true;
        Snackbar.make(findViewById(android.R.id.content), R.string.creator__press_back_again_to_exit, Snackbar.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 1000);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.creatememe__menu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if(item.getItemId()==android.R.id.home)
        {
            finish();

        }
        else if(i == R.id.action_share) {
                shareBitmapToOtherApp(lastBitmap, this);
                return true;
            }
        return super.onOptionsItemSelected(item);
    }

    private boolean saveMemeToFilesystem(boolean showDialog) {
        String filepath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getString(R.string.app_name)).getAbsolutePath();
        String thumbnailPath = new File(filepath, getString(R.string.dot_thumbnails)).getAbsolutePath();
        if (memeSavetime < 0) {
            memeSavetime = System.currentTimeMillis();
        }

        String filename = String.format(Locale.getDefault(), "%s_%d.jpg", getString(R.string.app_name), memeSavetime);
        Helpers myhelper = new Helpers(this);
        boolean wasSaved = myhelper.saveBitmapToFile(filepath, filename, lastBitmap) != null && myhelper.saveBitmapToFile(thumbnailPath, filename, myhelper.createThumbnail(lastBitmap)) != null;
        if (wasSaved && showDialog) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(R.string.creator__saved_successfully)
                    .setMessage(R.string.creator__saved_successfully_message)
                    .setNegativeButton(R.string.creator__no_keep_editing, null)
                    .setPositiveButton(R.string.main__yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            dialog.show();
        }
        return wasSaved;
    }

    public void shareBitmapToOtherApp(Bitmap bitmap, Activity activity) {
        Helpers myhelper = new Helpers(this);
        File imageFile = myhelper.saveBitmapToFile(getCacheDir().getAbsolutePath(), getString(R.string.cached_picture_filename), bitmap);
        if (imageFile != null) {
            Uri imageUri = FileProvider.getUriForFile(this, this.getPackageName() + ".file_provider", imageFile);
            // imageUri = FileProvider.getUriForFile(this, getString(R.string.app_fileprovider), imageFile);
            if (imageUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(imageUri, getContentResolver().getType(imageUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

                Intent myintent = getIntent();
                finish();
                startActivity(myintent);
                startActivity(Intent.createChooser(shareIntent, "Send"));
            }
        }
    }

    public Bitmap drawMultilineTextToBitmap(Context c, MemeSetting memeSetting) {
        // prepare canvas
        Resources resources = c.getResources();
        Bitmap bitmap = memeSetting.getDisplayImage();

        if (memeSetting.getRotationDeg() != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(memeSetting.getRotationDeg());
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        Helpers myhelper = new Helpers(this);
        float scale = myhelper.getScalingFactorInPixelsForWritingOnPicture(bitmap.getWidth(), bitmap.getHeight());
        float borderScale;
        Bitmap.Config bitmapConfig = bitmap.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.RGB_565;
        }
        // resource bitmaps are immutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(bitmap);

        // new antialiased Paint
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        borderScale = scale * memeSetting.getFontSize() / MemeLibConfig.FONT_SIZES.DEFAULT;
        paint.setTextSize((int) (memeSetting.getFontSize() * scale));
        paint.setTypeface(memeSetting.getFont().getFont());
        //paint.setStrokeWidth(memeSetting.getFontSize() / 4);
        paint.setStrokeWidth(borderScale);

        String[] textStrings = {memeSetting.getCaptionTop(), memeSetting.getCaptionBottom()};
        if (memeSetting.isAllCaps()) {
            for (int i = 0; i < textStrings.length; i++) {
                textStrings[i] = textStrings[i].toUpperCase();
            }
        }

        for (int i = 0; i < textStrings.length; i++) {

            if(i==0) {
                paint.setColor(memeSetting.getBorderColor());
                paint.setStyle(Paint.Style.FILL_AND_STROKE);

                // set text width to canvas width minus 16dp padding
                int textWidth = canvas.getWidth() - (int) (16 * scale);

                // init StaticLayout for text
                StaticLayout textLayout = new StaticLayout(
                        textStrings[i], paint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

                // get height of multiline text
                int textHeight = textLayout.getHeight();

                // get position of text's top left corner  center: (bitmap.getWidth() - textWidth)/2
                float x = (bitmap.getWidth() - textWidth) / 2;
                float y = 0;
                if (i == 0)
                    y = bitmap.getHeight() / 15;
                else
                    y = bitmap.getHeight() - textHeight;

                // draw text to the Canvas center
                canvas.save();
                canvas.translate(x, y);
                textLayout.draw(canvas);

                // new antialiased Paint
                paint.setColor(memeSetting.getTextColor());
                paint.setStyle(Paint.Style.FILL);

                // init StaticLayout for text
                textLayout = new StaticLayout(
                        textStrings[i], paint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

                // get height of multiline text
                textHeight = textLayout.getHeight();

                // draw text to the Canvas center
                textLayout.draw(canvas);
                canvas.restore();
            }
            else if(i==1)
            {
                borderScale = scale * memeSetting.getFontSize2() / MemeLibConfig.FONT_SIZES.DEFAULT;
                paint.setTextSize((int) (memeSetting.getFontSize2() * scale));
                paint.setTypeface(memeSetting.getFont().getFont());
                //paint.setStrokeWidth(memeSetting.getFontSize() / 4);
                paint.setStrokeWidth(borderScale);
                paint.setColor(memeSetting.getBorderColor());
                paint.setStyle(Paint.Style.FILL_AND_STROKE);

                // set text width to canvas width minus 16dp padding
                int textWidth = canvas.getWidth() - (int) (16 * scale);

                // init StaticLayout for text
                StaticLayout textLayout = new StaticLayout(
                        textStrings[i], paint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

                // get height of multiline text
                int textHeight = textLayout.getHeight();

                // get position of text's top left corner  center: (bitmap.getWidth() - textWidth)/2
                float x = (bitmap.getWidth() - textWidth) / 2;
                float y = 0;
                if (i == 0)
                    y = bitmap.getHeight() / 15;
                else
                    y = bitmap.getHeight() - textHeight;

                // draw text to the Canvas center
                canvas.save();
                canvas.translate(x, y);
                textLayout.draw(canvas);

                // new antialiased Paint
                paint.setColor(memeSetting.getTextColor());
                paint.setStyle(Paint.Style.FILL);

                // init StaticLayout for text
                textLayout = new StaticLayout(
                        textStrings[i], paint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

                // get height of multiline text
                textHeight = textLayout.getHeight();

                // draw text to the Canvas center
                textLayout.draw(canvas);
                canvas.restore();
            }
        }

        return bitmap;
    }


    @Override
    public void onMemeSettingChanged(MemeSetting memeSetting) {
        imageEditView.setImageBitmap(null);
        if (lastBitmap != null)
            lastBitmap.recycle();
        Bitmap bmp = drawMultilineTextToBitmap(this, memeSetting);
        imageEditView.setImageBitmap(bmp);
        lastBitmap = bmp;
    }

    @Override
    public void onSheetStateChanged(BottomSheetLayout.State state) {
        if (state == BottomSheetLayout.State.HIDDEN) {
            fab.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
            textEditBottomCaption.setVisibility(View.VISIBLE);
            textEditTopCaption.setVisibility(View.VISIBLE);
        }
        if (state == BottomSheetLayout.State.EXPANDED || state == BottomSheetLayout.State.PEEKED) {
            textEditBottomCaption.setVisibility(View.GONE);
            textEditTopCaption.setVisibility(View.GONE);
            toolbar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDismissed(BottomSheetLayout bottomSheetLayout) {
        fab.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);
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

            memeCategories = new ArrayList<MemeCategory>();
        }
    }

}
