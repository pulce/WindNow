package com.windnow.classes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Calendar;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.windnow.Station;
import com.windnow.statics.LoadSaveOps;
import com.windnow.OnlyContext;

//import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * 
 * This Class is part of WindNow.
 * 
 * @author Florian Hauser Copyright (C) 2015
 * 
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or (at
 *         your option) any later version.
 * 
 *         This program is distributed in the hope that it will be useful, but
 *         WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *         General Public License for more details.
 * 
 *         You should have received a copy of the GNU General Public License
 *         along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
public class DownloadStation extends AsyncTask<Void, Void, Void> {
	private Station station;
	private int maxRetries = 5;
	private InterfaceDlUpdate update;
	private static SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(OnlyContext.getContext());

	public DownloadStation(InterfaceDlUpdate update, Station station) {
		this.station = station;
		this.update = update;
	}

	@Override

	protected Void doInBackground(Void... v) {
		for (int dlTry = 1; dlTry <= maxRetries; dlTry++) {
			try {
				Log.d("DownloadStation", "Download");
				//System.setProperty( "http.proxyUserName", prefs.getString("user_name_pref", "") );
				//System.setProperty( "http.proxyPassword", prefs.getString("password_pref", "") );
				station.setProgress(0);
				String filename = "pic" + station.getUrl().hashCode();
				int IO_BUFFER_SIZE = 4 * 1024;
				//System.setProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
				URLConnection uc = new URL(station.getUrl())
						.openConnection();
				uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
				uc.setRequestProperty("Authorization", "Basic " + Base64.encodeToString((prefs.getString("user_name_pref", "") + ":" + prefs.getString("password_pref", "")).getBytes(), Base64.DEFAULT));
				/*Authenticator.setDefault (new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication (prefs.getString("user_name_pref", ""), prefs.getString("password_pref", "").toCharArray());
					}
				});*/
				int contentLength = uc.getContentLength();
				InputStream input = new BufferedInputStream(
						uc.getInputStream(), IO_BUFFER_SIZE);
				//InputStream input = new BufferedInputStream(
				//		new URL(station.getUrl()).openStream(), IO_BUFFER_SIZE);
				//logLongString(IOUtils.toString(input));
				OutputStream out = new BufferedOutputStream(OnlyContext
						.getContext().openFileOutput(filename,
								OnlyContext.MODE_PRIVATE), IO_BUFFER_SIZE);
				byte[] b = new byte[IO_BUFFER_SIZE];
				int read;
				long total = 0;
				while ((read = input.read(b)) != -1) {
					total += read;
					if (contentLength > 0) {
						station.setProgress((int) ((total * 100) / contentLength));
						publishProgress();
					}
					out.write(b, 0, read);
				}

				input.close();
				out.close();
				Log.d("DownloadStation", "Size: " + total);
				station.setLoaded(true);
				station.setValued(true);
				station.setStatus(Station.LOADED);
				station.setDate(Calendar.getInstance().getTime());

				if (station.getType() != Station.PIC) {
					station.parseCache();
				}
				//Debugging
				/*
				URLConnection uc2 = (HttpURLConnection) new URL("https://www.austrocontrol.at/flugwetter/start.php?back=https://www.austrocontrol.at/flugwetter/products/chartloop/barbs_012.png").openConnection();
				uc2.setDoOutput(true);
				PrintWriter wr = new PrintWriter(uc2.getOutputStream(), true);
				StringBuilder parameters = new StringBuilder();
				parameters.append("username=" +prefs.getString("user_name_pref", ""));
				parameters.append("&");
				parameters.append("password=" + prefs.getString("password_pref", ""));
				wr.println(parameters);
				wr.close();


				int contentLength2 = uc2.getContentLength();
				InputStream input2 = new BufferedInputStream(
						uc2.getInputStream(), IO_BUFFER_SIZE);
				OutputStream out2 = new BufferedOutputStream(OnlyContext
						.getContext().openFileOutput(filename,
								OnlyContext.MODE_PRIVATE), IO_BUFFER_SIZE);
				byte[] b2 = new byte[IO_BUFFER_SIZE];
				int read2;
				long total2 = 0;
				while ((read2 = input2.read(b2)) != -1) {
					total2 += read2;
					if (contentLength2 > 0) {
						station.setProgress((int) ((total2 * 100) / contentLength2));
						publishProgress();
					}
					out2.write(b2, 0, read2);
				}
				Log.d("DownloadStation2", uc2.getURL().toString());
				input2.close();
				out2.close();
				Document doc = Jsoup.parse(OnlyContext.getContext()
						.getFileStreamPath(filename), null);
				Log.d("Document", doc.toString());
				Log.d("DownloadStation3", uc2.getURL().toString());
				*/

				break;
			} catch (Exception e) {
				if (dlTry == maxRetries) {
					station.setStatus(Station.DOWNLOAD_ERROR);
				}
				LoadSaveOps.printErrorToLog(e);
				//e.printStackTrace();
				Log.e("Downloadstation", "exception", e);
			}
		}
		return null;
	}

	protected void onProgressUpdate(Void... p) {
		update.onTaskUpdate(station);
	}

	@Override
	protected void onPostExecute(Void v) {
		update.onTaskResult(station);
	}
}
