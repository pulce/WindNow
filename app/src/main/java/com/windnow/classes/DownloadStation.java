package com.windnow.classes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import android.os.AsyncTask;

import com.windnow.Station;
import com.windnow.statics.LoadSaveOps;
import com.windnow.statics.OnlyContext;

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
	
	public DownloadStation(InterfaceDlUpdate update, Station station) {
		this.station = station;
		//this.maxRetries = Integer.parseInt(PreferenceManager
		//		.getDefaultSharedPreferences(OnlyContext.getContext())
		//		.getString("pref_list", "5"));
		this.update = update;
	}

	@Override
	protected Void doInBackground(Void... v) {
		for (int dlTry = 1; dlTry <= maxRetries; dlTry++) {
			try {
				station.setProgress(0);
				String filename = "pic" + station.getUrl().hashCode();
				int IO_BUFFER_SIZE = 4 * 1024;
				URLConnection uc = new URL(station.getUrl())
						.openConnection();
				int contentLength = uc.getContentLength();
				InputStream input = new BufferedInputStream(
						uc.getInputStream(), IO_BUFFER_SIZE);
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

				station.setLoaded(true);
				station.setValued(true);
				station.setStatus(Station.LOADED);
				station.setDate(Calendar.getInstance().getTime());

				if (station.getType() != Station.PIC) {
					station.parseCache();
				}
				break;
			} catch (IOException e) {
				if (dlTry == maxRetries) {
					station.setStatus(Station.DOWNLOAD_ERROR);
				}
				LoadSaveOps.printErrorToLog(e);
				e.printStackTrace();
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
