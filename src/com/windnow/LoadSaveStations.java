package com.windnow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 
 * This Class is part of WindNow.
 * 
 * It is responsible for writing and loading the stations file.
 * Also for writing the error log.
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
public class LoadSaveStations {
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
		if (!localDir.exists()) {
			localDir.mkdirs();
		}
		if (!stationsFile.exists()) {
			saveStandardStations();
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(stationsFile));
			String line;
			while ((line = br.readLine()) != null) {
				String[] arr = line.split(sep);
				if (arr.length > 2)
				stations.add(new Station(arr[0], arr[1], arr[2].equals("true")));
			}
			br.close();
		} catch (Exception e) {
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
				bw.write(station.getName() + sep + station.getUrl() + sep
						+ station.isPic());
				bw.newLine();
			}
			bw.close();
		} catch (Exception e) {
			printErrorToLog(e);
		}
	}
	
	public static void saveStandardStations() {
		ArrayList<Station> stations = new ArrayList<Station>();
		stations.add(new Station("Patscherkofel", "11126", false));
		stations.add(new Station("Wallberg", "http://80.157.135.194/wetter_uebersicht_tegernsee/windrichtung24.php", true));
		saveStations(stations);
	}
	
	static void printErrorToLog(Exception e) {
		try {
			if (!localDir.exists()) {
				localDir.mkdirs();
			}
			String line = Arrays.toString(e.getStackTrace());
			FileOutputStream fos = new FileOutputStream(errorLogFile);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
			bw.write(sdf.format(new Date()) + "-------------");
			bw.newLine();
			bw.write(line);
			bw.newLine();
			bw.close();
		} catch (Exception ex) {
			Log.e("Error writing errorLog", ex.toString());
		}
	}
}
