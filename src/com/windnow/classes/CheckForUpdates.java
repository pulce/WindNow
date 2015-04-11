package com.windnow.classes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.windnow.MainActivity;
import com.windnow.R;

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
public class CheckForUpdates extends AsyncTask<String, Void, Boolean> {
	private String answer;
	private Context context;
	
	public CheckForUpdates(Context context) {
		this.context = context;
		this.answer = context.getString(
					R.string.check_for_updates_failed);
	}
	
	@Override
	protected Boolean doInBackground(String... org) {
		String tag;
		try {
			URLConnection con = new URL(MainActivity.APPURL).openConnection();
			con.connect();
			InputStream is = con.getInputStream();
			String gt = con.getURL().toString();
			if (gt == null) {
				return false;
			}
			String[] spl = gt.split("/");
			tag = spl[spl.length - 1];
			is.close();
		} catch (IOException e) {
			return false;
		}
		if (tag.equals(MainActivity.VERSIONID)) {
			answer = context.getString(
					R.string.already_latest_version)
					+ " " + MainActivity.VERSIONID + ".";
			return false;
		}
		answer = "Version "
				+ tag
				+ " "
				+ context.getString(
						R.string.new_version_available) + " "
				+ MainActivity.VERSIONID + ".";
		return true;
	}
	
	@Override
	protected void onPostExecute(Boolean newVersion) {
		Toast.makeText(context.getApplicationContext(), answer, Toast.LENGTH_LONG)
				.show();
		if (newVersion) {
			Intent browse = new Intent(Intent.ACTION_VIEW,
					Uri.parse(MainActivity.APPURL));
			context.startActivity(browse);
		}
	}

}