package yuku.alkitab.base.ac;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import yuku.afw.V;
import yuku.afw.storage.Preferences;
import yuku.alkitab.base.App;
import yuku.alkitab.base.IsiActivity;
import yuku.alkitab.base.S;
import yuku.alkitab.base.ac.base.BaseActivity;
import yuku.alkitab.base.storage.Prefkey;
import yuku.alkitab.base.util.Downloader;
import yuku.alkitab.debug.R;


public class AudioBibleActivity extends BaseActivity {

    int bookposition = 0;
    private Handler handler=new Handler() {
        @Override
        public void handleMessage(Message msg) {


            Toast.makeText(AudioBibleActivity.this, "Download 1 file completed..... Progressing....",
                            Toast.LENGTH_LONG).show();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audiobible);
        final Toolbar toolbar = V.get(this, R.id.toolbaraudiobible);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        Spinner dropdown = findViewById(R.id.spinnerBook);
        String[] items = getResources().getStringArray(R.array.standard_book_names_in);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                bookposition = position;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        Button download = (Button) findViewById(R.id.buttonDownloadAudio);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetAvailable()) {
                    Toast.makeText(AudioBibleActivity.this, "Download Initializing....",
                            Toast.LENGTH_LONG).show();
                    try {
                        String jsontext = loadJSONFromAudioBible();
                        String audio_url = "";
                        String alkitab_audio_url = "";
                        String fileAudio = "";
                        String filePath = "";
                        boolean found = true;
                        JSONObject audio_bible = new JSONObject(jsontext);
                        JSONObject audio_bible_version;
                        if (S.activeVersion().getShortName().equals("TB")) {
                            audio_bible_version = audio_bible.getJSONObject("indonesia");

                        } else if (S.activeVersion().getShortName().equals("NKJV")) {
                            audio_bible_version = audio_bible.getJSONObject("inggris");

                        } else {
                            audio_bible_version = audio_bible.getJSONObject("indonesia");

                        }
                        JSONObject audio_bible_pasal = audio_bible_version.getJSONObject(Integer.toString(bookposition + 1));
                        //audio_url = audio_bible_pasal.getString("audio_bookdir") + "/" + audio_bible_pasal.getString("audio_bookfile");

                        String[] total_chapter = getResources().getStringArray(R.array.standard_number_chapter_book_names_in);
                        for (int i = 0; i < Integer.parseInt(total_chapter[bookposition]); i++) {
                            audio_url = audio_bible_pasal.getString("audio_bookdir") + "/" + audio_bible_pasal.getString("audio_bookfile");

                            if ((bookposition + 1) == 19) {
                                if (Integer.toString(i).length() == 3) {
                                    audio_url += Integer.toString(i + 1);
                                } else if (Integer.toString(i).length() == 2) {
                                    audio_url += "0" + Integer.toString(i + 1);
                                } else if (Integer.toString(i).length() == 1) {
                                    audio_url += "00" + Integer.toString(i + 1);
                                }
                            } else {
                                if (Integer.toString(i).length() == 2) {
                                    audio_url += "" + Integer.toString(i + 1);
                                } else if (Integer.toString(i).length() == 1) {
                                    audio_url += "0" + Integer.toString(i + 1);
                                }
                            }
                            if (S.activeVersion().getShortName().equals("TB")) {
                                if (bookposition + 1 <= 39) {
                                    alkitab_audio_url = "http://media.sabda.org/alkitab_audio/tb_alkitabsuara/" + "pl/mp3/cd/" + audio_url + ".mp3";
                                    fileAudio = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sabda_india/alkitab_audio/tb_alkitabsuara/" + "pl/mp3/cd/" + audio_url + ".mp3";
                                    filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sabda_india/alkitab_audio/tb_alkitabsuara/" + "pl/mp3/cd";

                                } else {
                                    alkitab_audio_url = "http://media.sabda.org/alkitab_audio/tb_alkitabsuara/" + "pb/mp3/cd/" + audio_url + ".mp3";
                                    fileAudio = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sabda_india/alkitab_audio/tb_alkitabsuara/" + "pb/mp3/cd/" + audio_url + ".mp3";
                                    filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sabda_india/alkitab_audio/tb_alkitabsuara/" + "pb/mp3/cd";
                                }


                            } else if (S.activeVersion().getShortName().equals("NKJV")) {
                                if (bookposition + 1 <= 39) {
                                    alkitab_audio_url = "http://media.sabda.org/alkitab_audio/kjv/" + "pl/mp3/cd/" + audio_url + ".mp3";
                                    fileAudio = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sabda_india/alkitab_audio/kjv/" + "pl/mp3/cd/" + audio_url + ".mp3";
                                    filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sabda_india/alkitab_audio/kjv/" + "pl/mp3/cd";

                                } else {
                                    alkitab_audio_url = "http://media.sabda.org/alkitab_audio/kjv/" + "pb/mp3/cd/" + audio_url + ".mp3";
                                    fileAudio = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sabda_india/alkitab_audio/kjv/" + "pb/mp3/cd/" + audio_url + ".mp3";
                                    filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sabda_india/alkitab_audio/kjv/" + "pb/mp3/cd";

                                }

                            }
                            Intent myintent = new Intent(AudioBibleActivity.this, Downloader.class);

                            myintent.setData(Uri.parse(alkitab_audio_url));
                            myintent.putExtra("fileAudio", fileAudio);

                            myintent.putExtra(Downloader.EXTRA_MESSENGER, new Messenger(handler));
                            startService(myintent);


                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                else
                {
                    Toast.makeText(AudioBibleActivity.this, "There is no Internet Connection  !",
                            Toast.LENGTH_LONG).show();
                }
            }

            });

    }
    public boolean isInternetAvailable() {
        try {
            final InetAddress address = InetAddress.getByName("www.google.com");
            return !address.equals("");
        } catch (UnknownHostException e) {
            // Log error
        }
        return false;
    }
    public static Intent createIntent() {

        Intent myIntent = new Intent(App.context,AudioBibleActivity.class);


        return myIntent;
    }
    public String loadJSONFromAudioBible() {
        String json = null;
        try {
            InputStream is = getAssets().open("audio_bible.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);

    }
}

