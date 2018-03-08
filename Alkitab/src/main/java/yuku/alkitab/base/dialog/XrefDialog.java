package yuku.alkitab.base.dialog;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import yuku.afw.D;
import yuku.afw.V;
import yuku.alkitab.base.IsiActivity;
import yuku.alkitab.base.S;
import yuku.alkitab.base.dialog.base.BaseDialog;
import yuku.alkitab.base.model.MVersionInternal;
import yuku.alkitab.base.model.VersionImpl;
import yuku.alkitab.base.util.AppLog;
import yuku.alkitab.base.util.Appearances;
import yuku.alkitab.base.util.TargetDecoder;
import yuku.alkitab.base.widget.VerseRenderer;
import yuku.alkitab.base.widget.VersesView;
import yuku.alkitab.base.widget.VersesView.VerseSelectionMode;
import yuku.alkitab.debug.R;
import yuku.alkitab.model.SingleChapterVerses;
import yuku.alkitab.model.Version;
import yuku.alkitab.model.XrefEntry;
import yuku.alkitab.util.Ari;
import yuku.alkitab.util.IntArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class XrefDialog extends BaseDialog {
	public static final String TAG = XrefDialog.class.getSimpleName();

	private static final String EXTRA_arif = "arif";
	private static final String theA = "A";
	private static final String theB = "B";
	private static final String theC = "C";
	public interface XrefDialogListener {
		void onVerseSelected(XrefDialog dialog, int arif_source, int ari_target);
	}
	
	TextView tXrefText;
	VersesView versesView;
	
	XrefDialogListener listener;

	int arif_source;
	XrefEntry xrefEntry;
	int displayedLinkPos = -1; // -1 indicates that we should auto-select the first link
	List<String> displayedVerseTexts;
	List<String> displayedVerseNumberTexts;
	IntArrayList displayedRealAris;

	Version myversion = VersionImpl.getInternalVersion();
	String myversionid = MVersionInternal.getVersionInternalId();
	Version sourceVersion = S.activeVersion();
	String sourceVersionId = S.activeVersionId();
	boolean theXRefA = false;
	boolean  theXRefB = false;
	boolean theXRefC = false;
	boolean [] XRef;


	float textSizeMult = S.getDb().getPerVersionSettings(sourceVersionId).fontSizeMultiplier;  // BY Jeffrey :)
	//float textSizeMult = S.getDb().getPerVersionSettings(myversionid).fontSizeMultiplier;


	public XrefDialog() {
	}
	
	public static XrefDialog newInstance(int arif, boolean [] theXRef) {
		XrefDialog res = new XrefDialog();
		
        Bundle args = new Bundle();
        args.putInt(EXTRA_arif, arif);
        args.putBoolean(theA, theXRef[0]);
		args.putBoolean(theB, theXRef[1]);
		args.putBoolean(theC, theXRef[2]);
        res.setArguments(args);

		return res;
	}

	@Override
	public void onAttach(final Context context) {
		super.onAttach(context);

		if (getParentFragment() instanceof XrefDialogListener) {
			listener = (XrefDialogListener) getParentFragment();
		} else {
			listener = (XrefDialogListener) context;
		}
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);

		theXRefA =  getArguments().getBoolean(theA);
		theXRefB =  getArguments().getBoolean(theB);
		theXRefC =  getArguments().getBoolean(theC);
		arif_source = getArguments().getInt(EXTRA_arif);
		//arif_source = ( Ari.encode(39,1,11) << 8 ) | 1;
		XRef = new boolean[3];
		XRef[0] = theXRefA;
		XRef[1] = theXRefB;
		XRef[2] = theXRefC;

		int field = arif_source & 0xff;


		if(field == 1)
		{
			xrefEntry = myversion.getXrefEntry(arif_source);
			Toast.makeText(getActivity(), Integer.toString(field), Toast.LENGTH_LONG).show();
		}
		else if(field==2)
		{
			xrefEntry = myversion.getXrefEntry2(arif_source);
			Toast.makeText(getActivity(), Integer.toString(field), Toast.LENGTH_LONG).show();
		}
		else if(field==3)
		{
			xrefEntry = myversion.getXrefEntry3(arif_source);
			Toast.makeText(getActivity(), Integer.toString(field), Toast.LENGTH_LONG).show();
		}
		//xrefEntry = sourceVersion.getXrefEntry(arif_source);



	}
	
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View res = inflater.inflate(R.layout.dialog_xref, container, false);

		tXrefText = V.get(res, R.id.tXrefText);
		versesView = V.get(res, R.id.versesView);
		versesView.init2(XRef);
		res.setBackgroundColor(S.applied().backgroundColor);
		versesView.setCacheColorHint(S.applied().backgroundColor);
		versesView.setVerseSelectionMode(VerseSelectionMode.singleClick);
		versesView.setSelectedVersesListener(versesView_selectedVerses);
		tXrefText.setMovementMethod(LinkMovementMethod.getInstance());

		if (xrefEntry != null) {
			renderXrefText();
		} else {
			new MaterialDialog.Builder(getActivity())
				.content(String.format(Locale.US, "Error: xref at arif 0x%08x couldn't be loaded", arif_source))
				.positiveText(R.string.ok)
				.show();
		}

		return res;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (xrefEntry == null) {
			dismiss();
		}
	}

	void renderXrefText() {
		final SpannableStringBuilder sb = new SpannableStringBuilder();
		sb.append(VerseRenderer.XREF_MARK);
		sb.append(" ");
		
		final int[] linkPos = {0};
		findTags(xrefEntry.content, new FindTagsListener() {
			@Override public void onTaggedText(final String tag, int start, int end) {
				final int thisLinkPos = linkPos[0];
				linkPos[0]++;
				
				int sb_len = sb.length();
				sb.append(xrefEntry.content, start, end);
				
				if (tag.startsWith("t")) { // the only supported tag at the moment
					final String encodedTarget = tag.substring(1);
					
					if (thisLinkPos == displayedLinkPos || (displayedLinkPos == -1 && thisLinkPos == 0)) { 
						// just make it bold, because this is the currently displayed link
						sb.setSpan(new StyleSpan(Typeface.BOLD), sb_len, sb.length(), 0); 
						
						if (displayedLinkPos == -1) {
							showVerses(0, encodedTarget);
						}
					} else {
						sb.setSpan(new ClickableSpan() {
							@Override public void onClick(View widget) {
								showVerses(thisLinkPos, encodedTarget);
							}
						}, sb_len, sb.length(), 0);
					}
				}
			}
			
			@Override public void onPlainText(int start, int end) {
				sb.append(xrefEntry.content, start, end);
			}
		});
		
		Appearances.applyTextAppearance(tXrefText, textSizeMult);
		
		tXrefText.setText(sb);
	}

	void showVerses(int linkPos, String encodedTarget) {
		displayedLinkPos = linkPos;
		
		final IntArrayList ranges = decodeTarget(encodedTarget);

		if (D.EBUG) {
			AppLog.d(TAG, "linkPos " + linkPos + " target=" + encodedTarget + " ranges=" + ranges);
		}
		
		displayedVerseTexts = new ArrayList<>();
		displayedVerseNumberTexts = new ArrayList<>();
		displayedRealAris = new IntArrayList();

		//int verse_count = sourceVersion.loadVersesByAriRanges(ranges, displayedRealAris, displayedVerseTexts);
		int verse_count =  sourceVersion.loadVersesByAriRanges(ranges, displayedRealAris, displayedVerseTexts);
		if (verse_count > 0) {
			// set up verse number texts
			for (int i = 0; i < verse_count; i++) {
				int ari = displayedRealAris.get(i);
				displayedVerseNumberTexts.add(Ari.toChapter(ari) + ":" + Ari.toVerse(ari));
			}
		
			class Verses extends SingleChapterVerses {
				@Override public String getVerse(int verse_0) {
					final String res = displayedVerseTexts.get(verse_0);
					// prevent crash if the target xref is not available
					if (res == null) {
						return getString(R.string.generic_verse_not_available_in_this_version);
					}
					return res;
				}
				
				@Override public int getVerseCount() {
					return displayedVerseTexts.size();
				}
				
				@Override public String getVerseNumberText(int verse_0) {


					//Toast.makeText(getActivity(), Integer.toString(verse_0), Toast.LENGTH_LONG).show();

					return displayedVerseNumberTexts.get(verse_0);
				}
			}
	
			int firstAri = displayedRealAris.get(0);
			;


			//versesView.setData(Ari.toBookChapter(firstAri), new Verses(), null, null, 0, sourceVersion, sourceVersionId);
			versesView.setData(Ari.toBookChapter(firstAri), new Verses(), null, null, 0, myversion, myversionid);
		}
		
		renderXrefText();
	}

	private IntArrayList decodeTarget(final String encodedTarget) {
		return TargetDecoder.decode(encodedTarget);
	}

	VersesView.SelectedVersesListener versesView_selectedVerses = new VersesView.DefaultSelectedVersesListener() {
		@Override public void onVerseSingleClick(VersesView v, int verse_1) {
			listener.onVerseSelected(XrefDialog.this, arif_source, displayedRealAris.get(verse_1 - 1));
		}
	};

	interface FindTagsListener {
		void onPlainText(int start, int end);
		void onTaggedText(String tag, int start, int end);
	}
	
	// look for "<@" "@>" "@/"
	void findTags(String s, FindTagsListener listener) {

		if(theXRefA == true)
		{
			s = s.replace("#A##", "").replace("##A#","");
		}
		else
		{
			s = s.replaceAll("#A##.+##A#", "");
		}
		if(theXRefB == true)
		{
			s = s.replace("#B##", "").replace("##B#","");
		}
		else
		{
			s = s.replaceAll("#B##.+##B#", "");
		}
		if(theXRefC == true)
		{
			s = s.replace("#C##", "").replace("##C#","");
		}
		else
		{
			s = s.replaceAll("#C##.+##C#", "");
		}
		int pos = 0;
		while (true) {
			int p = s.indexOf("@<", pos);
			if (p == -1) break;
			
			listener.onPlainText(pos, p);
			
			int q = s.indexOf("@>", p+2);
			if (q == -1) break;
			int r = s.indexOf("@/", q+2);
			if (r == -1) break;
			
			listener.onTaggedText(s.substring(p+2, q), q+2, r);
			
			pos = r+2;
		}
		
		listener.onPlainText(pos, s.length());
	}

	public void setSourceVersion(Version sourceVersion, String sourceVersionId) {
		this.sourceVersion = sourceVersion;
		this.sourceVersionId = sourceVersionId;
		textSizeMult = S.getDb().getPerVersionSettings(sourceVersionId).fontSizeMultiplier;
	}
}
