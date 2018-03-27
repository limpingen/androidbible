package org.sabda.alkitabquotegenerator.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import org.sabda.alkitabquotegenerator.R;
import org.sabda.alkitabquotegenerator.util.Helpers;
import org.sabda.opoc.util.HelpersA;
import org.sabda.opoc.util.SimpleMarkdownParser;

public class InfoActivity extends AppCompatActivity {
    //####################
    //##  Ui Binding
    //####################
    //@BindView(R.id.toolbar)
    Toolbar toolbar;

   // @BindView(R.id.info__activity__text_app_version)
    TextView textAppVersion;

   // @BindView(R.id.info__activity__text_team)
    TextView textTeam;

   // @BindView(R.id.info__activity__text_contributors)
    TextView textContributors;

    //@BindView(R.id.info__activity__text_license)
    TextView textLicense;

    //####################
    //##  Methods
    //####################
    @Override
    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info__activity);
        toolbar = findViewById(R.id.toolbar);
        textAppVersion = findViewById(R.id.info__activity__text_app_version);
        textAppVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        textTeam = findViewById(R.id.info__activity__text_team);
        textContributors = findViewById(R.id.info__activity__text_contributors);
        textLicense = findViewById(R.id.info__activity__text_license);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        textTeam.setText(new SpannableString(Html.fromHtml(
                Helpers.get().loadMarkdownForTextViewFromRaw(R.raw.maintainers, ""))));
        textTeam.setMovementMethod(LinkMovementMethod.getInstance());

        textContributors.setText(new SpannableString(Html.fromHtml(
                Helpers.get().loadMarkdownForTextViewFromRaw(R.raw.contributors, "* ")
        )));
        textContributors.setMovementMethod(LinkMovementMethod.getInstance());

        // License text MUST be shown
        try {
            textLicense.setText(new SpannableString(Html.fromHtml(
                    SimpleMarkdownParser.get().parse(getString(R.string.copyright_license_text_official).replace("\n", "  \n"),
                            SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW, "").getHtml()
            )));
        } catch (IOException e) {
            e.printStackTrace();
        }


        // App version
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            textAppVersion.setText(getString(R.string.app_version_v, info.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }




}
