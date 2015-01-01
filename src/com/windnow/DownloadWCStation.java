package com.windnow;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.res.AssetManager;

/**
 * 
 * This Class is part of WindNow.
 * 
 * It will download text stations or pic stations from web or, in case of a
 * dummy, from assets. Must be handled as as AsyncTask. That is done in
 * MainActivity!
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
class DownloadWCStation {
	private static final int IO_BUFFER_SIZE = 4 * 1024;

	public static boolean downloadWC(Station st) {
		ArrayList<String> patschText = new ArrayList<String>();
		try {
			Document doc;
			if (MainActivity.DUMMY) {
				AssetManager assetManager = OnlyContext.getContext()
						.getAssets();
				InputStream input;
				input = assetManager.open("dummy");
				doc = Jsoup
						.parse(input, "UTF-8", "http://www.wetteronline.de/");
				input.close();
			} else {
				doc = Jsoup.connect(st.getUrl()).get();
			}
			Elements tableElements = doc
					.select("table[class=hourly]:has(th:contains(Spitze))");

			// Headers
			ArrayList<String> headers = new ArrayList<String>();
			ArrayList<String> units = new ArrayList<String>();
			Elements tableHeads = tableElements.select("thead tr").first()
					.select("th");
			for (Element elm : tableHeads) {
				headers.add(elm.text() + ": ");
				if (elm.toString().contains("colspan"))
					headers.add(elm.text());
			}

			String heads = "";
			for (int i = 0; i < headers.size(); i++) {
				if (i != 2 && i != 5) {
					heads += headers.get(i);
					if (i != headers.size() - 1)
						heads += "&/";
				}
			}
			patschText.add(heads);

			Elements tableUnits = tableElements.select("thead tr").last()
					.select("th");
			units.add("");
			for (Element elm : tableUnits) {
				if (!elm.text().contains("Grad"))
					units.add(elm.text() + " ");
				else
					units.add(" ");
			}

			// Rows
			Elements tableRowElements = tableElements.select(":not(thead) tr");
			for (int i = 0; i < tableRowElements.size(); i++) {
				Element row = tableRowElements.get(i);
				Elements rowItems = row.select("td");
				String line = "";
				for (int j = 0; j < rowItems.size(); j++) {
					if (j != 2 && j != 5) {
						line += rowItems.get(j).text() + " " + units.get(j);
						if (j != rowItems.size() - 1)
							line += "&/";
					}
				}
				patschText.add(line);
			}
			saveArray(st.getUrl(), patschText);
		} catch (IOException e) {
			LoadSaveStations.printErrorToLog(e);
			return false;
		}
		st.setTabTxt(patschText);
		return true;
	}

	public static boolean downloadBZ(Station st) {
		ArrayList<String> patschText = new ArrayList<String>();
		try {
			Document doc;

			if (MainActivity.DUMMY) {
				return true;
			}
			doc = Jsoup.connect(st.getUrl()).get();

			Elements tableElements = doc
					.select("table[class=avalanches-stations]:contains(Messstationen)");

			// Headers
			patschText.add("Station, Höhe, Zeit&/Ri&/Wind&/Spitze");

			// Rows
			Elements tableRowElements = tableElements.select(":not(thead) tr");
			outerloop: for (int i = 0; i < tableRowElements.size(); i++) {
				Element row = tableRowElements.get(i);
				Elements rowItems = row.select("th, td");
				String line = "";
				for (int j = 0; j < rowItems.size(); j++) {
					if (j != 2 && j != 3) {
						String el = rowItems.get(j).text();
						if (j == 0 && el.contains("(")) {
							line += toCamelCase(el.split("\\(")[0].trim())
									+ "\n";
							line += el.split("\\(")[1].replace(")", "").trim()
									+ ", ";
						} else if (j == 1) {
							if (el.length() > 5) {
								line += el.substring(el.length() - 5); // no
																		// date,
																		// just
																		// time
							} else {
								continue outerloop;
							}
						} else
							line += rowItems.get(j).text();
						if (j > 4)
							line += " km/h";
						if (j != rowItems.size() - 1 && j != 0)
							line += "&/";
					}
				}
				patschText.add(line);
			}
			saveArray(st.getUrl(), patschText);
		} catch (Exception e) {
			LoadSaveStations.printErrorToLog(e);
			return false;
		}
		st.setTabTxt(patschText);
		return true;
	}

	private static void saveArray(String url, ArrayList<String> ar)
			throws FileNotFoundException {
		String filename = "pic" + url.hashCode();
		PrintWriter pw = null;
		pw = new PrintWriter(OnlyContext.getContext().openFileOutput(filename,
				OnlyContext.MODE_PRIVATE));
		for (String line : ar)
			pw.println(line);
		pw.close();
	}
	
	public static ArrayList<String> loadArray(String url) {
		ArrayList<String> ar = new ArrayList<String>();
		String filename = "pic" + url.hashCode();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(OnlyContext.getContext().getFileStreamPath(filename)));
			String line;
			while ((line = br.readLine()) != null) {
				ar.add(line);
			}
			br.close();
		} catch (Exception e) {
			LoadSaveStations.printErrorToLog(e);
		}
		return ar;
	}

	public static String toCamelCase(String inputString) {
		String result = "";
		if (inputString.length() == 0) {
			return result;
		}
		char firstChar = inputString.charAt(0);
		char firstCharToUpperCase = Character.toUpperCase(firstChar);
		result = result + firstCharToUpperCase;
		for (int i = 1; i < inputString.length(); i++) {
			char currentChar = inputString.charAt(i);
			char previousChar = inputString.charAt(i - 1);
			if (previousChar == ' ') {
				char currentCharToUpperCase = Character
						.toUpperCase(currentChar);
				result = result + currentCharToUpperCase;
			} else {
				char currentCharToLowerCase = Character
						.toLowerCase(currentChar);
				result = result + currentCharToLowerCase;
			}
		}
		return result;
	}

	static boolean downloadPic(Station st) {
		String filename = "pic" + st.getUrl().hashCode();
		try {
			InputStream input;
			OutputStream out;
			if (MainActivity.DUMMY) {
				AssetManager assetManager = OnlyContext.getContext()
						.getAssets();
				input = assetManager.open("windrichtung.png");
			} else {
				input = new BufferedInputStream(new URL(st.getUrl()).openStream(),
						IO_BUFFER_SIZE);
			}
			out = new BufferedOutputStream(OnlyContext.getContext()
					.openFileOutput(filename,
							OnlyContext.MODE_PRIVATE),
					IO_BUFFER_SIZE);
			byte[] b = new byte[IO_BUFFER_SIZE];
			int read;
			while ((read = input.read(b)) != -1) {
				out.write(b, 0, read);
			}
			input.close();
			out.close();

		} catch (Exception e) {
			LoadSaveStations.printErrorToLog(e);
			return false;
		}
		return true;
	}

}