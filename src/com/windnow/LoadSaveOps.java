package com.windnow;

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

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
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
 * @author Florian Hauser Copyright (C) 2014
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
	private static String userDir = PreferenceManager
			.getDefaultSharedPreferences(OnlyContext.getContext()).getString(
					"user_dir", "WindNow");
	private static File localDir = new File(Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/" + userDir);
	private static File stationsFile = new File(Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/"
			+ userDir
			+ "/" + "stations.txt");
	private static File errorLogFile = new File(Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/"
			+ userDir
			+ "/" + "errorlog.txt");

	private static String sep = ";";

	public static ArrayList<Station> loadStations() {
		ArrayList<Station> stations = new ArrayList<Station>();
		try {
			if (!localDir.exists()) {
				localDir.mkdirs();
			}
			BufferedReader br = null;
			boolean saveFile = !stationsFile.exists();
			if (saveFile) {
				AssetManager assetManager = OnlyContext.getContext()
						.getAssets();
				br = new BufferedReader(new InputStreamReader(
						assetManager.open("stations.txt")));
			} else {
				br = new BufferedReader(new FileReader(stationsFile));
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
		} catch (IOException e) {
			printErrorToLog(e);
		}
		return stations;
	}

	public static void saveStations(ArrayList<Station> stations) {
		try {
			if (!localDir.exists()) {
				localDir.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(stationsFile);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			for (Station station : stations) {
				bw.write(station.getName() + sep + station.getUrl());
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			printErrorToLog(e);
		}
	}

	static void printErrorToLog(Exception e) {
		try {
			if (!localDir.exists()) {
				localDir.mkdirs();
			}
			boolean append = false;
			SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
			if (fmt.format(new Date(errorLogFile.lastModified())).equals(fmt.format(new Date()))) {
				append = true;
			}
			String line = Arrays.toString(e.getStackTrace());
			FileOutputStream fos = new FileOutputStream(errorLogFile, append);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
			bw.write(sdf.format(new Date()) + "-------------");
			bw.newLine();
			bw.write(e.toString());
			bw.newLine();
			bw.write(line);
			bw.newLine();
			bw.close();
		} catch (IOException ex) {
			Log.e("Error writing errorLog", ex.toString());
		}
	}
}
