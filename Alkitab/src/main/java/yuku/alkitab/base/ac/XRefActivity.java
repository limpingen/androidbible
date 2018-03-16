package yuku.alkitab.base.ac;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import yuku.afw.V;
import yuku.afw.storage.Preferences;
import yuku.alkitab.base.App;
import yuku.alkitab.base.IsiActivity;
import yuku.alkitab.base.storage.Prefkey;
import yuku.alkitab.debug.R;
import yuku.alkitab.base.ac.base.BaseActivity;
import yuku.alkitab.yes2.section.XrefsSection;


public class XRefActivity extends BaseActivity {
    private static String file_url = "http://www.limpingen.org/tb2_xrefs_bt.bt";
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;
    SwitchCompat XRefTypeA;
    SwitchCompat XRefTypeB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xref);
        final Toolbar toolbar = V.get(this, R.id.toolbarxref);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        setTitle("XRef Module");
        XRefTypeA = (SwitchCompat)findViewById(R.id.myXRefA);
        XRefTypeB = (SwitchCompat)findViewById(R.id.myXRefB);

        XRefTypeA.setChecked(Preferences.getBoolean(Prefkey.XRefA, true));
        XRefTypeB.setChecked(Preferences.getBoolean(Prefkey.XRefB, true));

        XRefTypeB.setOnCheckedChangeListener(onCheckedChanged());

        final String fileXRefTypeB = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sabda_india/tb2_xrefs_bt.bt";


        File file = new File(fileXRefTypeB );
        if(file.exists()) {
            XRefTypeB.setVisibility(View.VISIBLE);
        }
        else
        {
            XRefTypeB.setVisibility(View.GONE);
        }

        XRefTypeA.setClickable(false);

        String XRefList[] = {"XRef A - Default", "XRef B"};
        ListView XRef = (ListView) findViewById(R.id.lvXRef);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(XRefActivity.this, android.R.layout.simple_list_item_1, XRefList);
        XRef.setAdapter(adapter);
        XRef.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                String item = ((TextView) view).getText().toString();
                if(item.equals("XRef B"))
                {
                    File file = new File(fileXRefTypeB );
                    if(file.exists()) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(XRefActivity.this);
                        builder.setTitle("Module XRef Type B");
                        builder.setMessage("The file is already exist. Do you want to delete it ?");

                        // add the buttons
                        builder.setPositiveButton("Keep It", null);
                        builder.setNegativeButton("Delete It",  new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Preferences.hold();
                                Preferences.setBoolean(Prefkey.XRefB, false);
                                Preferences.unhold();

                                file.delete();
                                Toast.makeText(getApplicationContext(), "File XRef Type B is deleted !",
                                        Toast.LENGTH_LONG).show();

                                Intent intent = getIntent();
                                startActivity(intent);
                                finish();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    else
                    {
                        new DownloadFileFromURL().execute(file_url);
                    }



                }

            }
        });
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChanged() {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switch (buttonView.getId()) {
                    case R.id.myXRefA:
                        Preferences.hold();
                        Preferences.setBoolean(Prefkey.XRefA, isChecked);
                        Preferences.unhold();
                        break;
                    case R.id.myXRefB:
                        Preferences.hold();
                        Preferences.setBoolean(Prefkey.XRefB, isChecked);
                        Preferences.unhold();

                        Intent intent2 = new Intent(App.context, IsiActivity.class);
                        finishAffinity();
                        startActivity(intent2);

                        break;
                }
            }
        };
    }
    public static Intent createIntent() {

        Intent myIntent = new Intent(App.context,XRefActivity.class);


        return myIntent;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type: // we set this to 0
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    /**
     * Background Async Task to download file
     * */
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                File sdcard = Environment.getExternalStorageDirectory();
// to this path add a new directory path
                File dir = new File(sdcard.getAbsolutePath() + "/sabda_india");
// create this directory if not already created
                dir.mkdir();
                File file = new File(dir,"tb2_xrefs_bt.bt");
                // Output stream
                OutputStream output = new FileOutputStream(file);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e(TAG, "XRef", e);
            }

            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type);

            Intent intent = getIntent();
            startActivity(intent);
            finish();

        }

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

