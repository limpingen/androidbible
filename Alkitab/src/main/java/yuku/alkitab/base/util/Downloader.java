package yuku.alkitab.base.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import java.net.URL;
import java.net.URLConnection;
import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class Downloader extends IntentService {
	public static final String EXTRA_MESSENGER="EXTRA_MESSENGER";

	URLConnection conection;
	public Downloader() {
		super("Downloader");
	}

	@Override
	public void onCreate() {
		super.onCreate();


	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		conection = null;
	}

	@Override
	public void onHandleIntent(Intent i) {
		try {
			int result=Activity.RESULT_CANCELED;
			int count;
			String fileAudio = "";
			Bundle extras = i.getExtras();

			if (extras != null) {
				fileAudio = extras.getString("fileAudio");

			}

			URL url = new URL(i.getData().toString());


			try {


				conection = url.openConnection();
				conection.connect();

				// this will be useful so that you can show a tipical 0-100%
				// progress bar
				int lenghtOfFile = conection.getContentLength();

				// download the file
				InputStream input = new BufferedInputStream(url.openStream(),
						8192);


				File dir = new File(fileAudio.replaceAll("/[A-Za-z0-9_]+\\.mp3", ""));

				dir.mkdirs();
				File file = new File(fileAudio);

				if (file.exists()) {
					file.delete();
				}
				OutputStream output = new FileOutputStream(file);

				byte data[] = new byte[1024];

				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					output.write(data, 0, count);
				}

				output.flush();

				output.close();
				input.close();



				result = Activity.RESULT_OK;
				if (extras != null) {
					Messenger messenger = (Messenger) extras.get(EXTRA_MESSENGER);
					Message msg = Message.obtain();

					msg.arg1 = result;

					try {
						messenger.send(msg);
					} catch (android.os.RemoteException e1) {
						Log.w(getClass().getName(), "Exception sending message", e1);
					}
				}

			} catch (IOException e2) {
				Log.e(getClass().getName(), "Exception in download", e2);
			}


		} catch (Exception e) {

		}


	}


}