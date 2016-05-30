package com.windnow.statics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.windnow.OnlyContext;
import com.windnow.Station;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 
 * This Class is part of WindNow.
 * 
 * It is responsible for writing and loading the stations file. Also for writing
 * the error log.
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

@SuppressLint("SimpleDateFormat")
public class LoadSaveOps {

	private static SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(OnlyContext.getContext());
	private static String sep = ";";

	public static File getStationsFile() {
		return new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ "/"
				+ prefs.getString("user_dir", "WindNow")
				+ "/"
				+ prefs.getString("stations_file", "stations.txt"));
	}

	static File getErrorFile() {
		return new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ "/"
				+ prefs.getString("user_dir", "WindNow") + "/errorlog.txt");
	}

	public static ArrayList<Station> loadStations() throws Exception {
		mkLocalDir();
		//Manage Cache
		if (OnlyContext.getContext().getFilesDir() != null) {
			File[] files = OnlyContext.getContext().getFilesDir().listFiles();
			long now = System.currentTimeMillis();
			for (File fl : files) {
				if (now - fl.lastModified() > 1000L * 60 * 60 * 24 * 90) {
					//noinspection ResultOfMethodCallIgnored
					fl.delete();
				}
			}
		}
		ArrayList<Station> stations = new ArrayList<>();
		BufferedReader br;
		boolean saveFile = !getStationsFile().exists();
		if (saveFile) {
			AssetManager assetManager = OnlyContext.getContext().getAssets();
			br = new BufferedReader(new InputStreamReader(
					assetManager.open("stations.txt")));
		} else {
			br = new BufferedReader(new FileReader(getStationsFile()));
		}

		String line;
		while ((line = br.readLine()) != null) {
			String[] arr = line.split(sep);
			if (arr.length > 1)
				stations.add(new Station(arr[0], arr[1]));
		}
		br.close();
		if (saveFile) {
			saveStations(stations);
		}
		return stations;
	}

	public static void saveStations(ArrayList<Station> stations) {
		try {
			mkLocalDir();
			FileOutputStream fos = new FileOutputStream(getStationsFile());
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			for (Station station : stations) {
				bw.write(station.getName() + sep + station.getUrl());
				bw.newLine();
			}
			bw.close();
			MediaScannerConnection.scanFile(OnlyContext.getContext(),
					new String[] { getStationsFile().getAbsolutePath() }, null,
					null);
		} catch (IOException e) {
			printErrorToLog(e);
		}
	}

	public static void printErrorToLog(Exception e) {
		try {
			mkLocalDir();
			boolean append = false;
			SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
			if (fmt.format(new Date(getErrorFile().lastModified())).equals(
					fmt.format(new Date()))) {
				append = true;
			}
			String line = Arrays.toString(e.getStackTrace());
			FileOutputStream fos = new FileOutputStream(getErrorFile(), append);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
			bw.write(sdf.format(new Date()) + "-------------");
			bw.newLine();
			bw.write(e.toString());
			bw.newLine();
			bw.write(line);
			bw.newLine();
			bw.close();
			MediaScannerConnection.scanFile(OnlyContext.getContext(),
					new String[] { getErrorFile().getAbsolutePath() }, null,
					null);
		} catch (IOException ex) {
			Log.e("Error writing errorLog", ex.toString());
		}

	}

	private static void mkLocalDir() {
		File localDir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ "/"
				+ prefs.getString("user_dir", "WindNow"));
		if (!localDir.exists()) {
			//noinspection ResultOfMethodCallIgnored
			localDir.mkdirs();
		}
		MediaScannerConnection.scanFile(OnlyContext.getContext(),
				new String[] { localDir.getAbsolutePath() }, null, null);
	}

}
