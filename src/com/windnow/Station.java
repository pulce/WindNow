package com.windnow;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * This Class is part of WindNow.
 * 
 * It provides the sceleton for a station.
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

public class Station implements Comparable<Station> {
	private String name;
	private String url;
	private int type;
	private int status;
	private int progress;
	private Date date;
	private ArrayList<String> tabTxt;
	private int position;
	private boolean loaded = false;
	private boolean valued = false;

	// Type of station
	public static final int PIC = 1;
	public static final int WC = 2;
	public static final int BZ = 3;
	public static final int WD = 4;

	// Status
	public static final int NOT_LOADED = 1;
	public static final int DOWNLOADING = 2;
	public static final int LOADED = 3;
	public static final int DOWNLOAD_ERROR = 4;
	public static final int PARSE_ERROR = 5;

	public static DateFormat sdf = SimpleDateFormat.getDateTimeInstance();

	public Station(String name, String url) {
		this.name = name;
		this.url = url;
		if (!url.startsWith("http")) {
			this.url = "http://" + this.url;
		}
		if (url.contains("wetteronline") && (url.contains("aktuelles-wetter") || url.contains("wetter-aktuell")))
			this.type = WC;
		else if (url.contains("provinz.bz.it") && url.endsWith(".asp"))
			this.type = BZ;
		else if (url.contains("wetterdienst.de") && url.contains("Aktuell")) {
			this.type = WD;
		} else {
			this.type = PIC;
		}
		// Check loaded
		String filename = "pic" + this.url.hashCode();
		File file = OnlyContext.getContext().getFileStreamPath(filename);
		if (file != null && file.exists()) {
			this.date = new Date(file.lastModified());
			this.status = LOADED;
			if (this.type != PIC) {
				parseCache();
			}
			this.valued = true;
		} else {
			this.status = NOT_LOADED;
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSecLine() {
		if (this.status == NOT_LOADED) {
			return OnlyContext.getContext().getString(R.string.not_loaded);
		} else if (this.status == DOWNLOADING) {
			return OnlyContext.getContext().getString(R.string.downloading)
					+ "..." + this.progress + "%";
		} else if (this.status == LOADED) {
			return OnlyContext.getContext().getString(R.string.downloaded_at)
					+ sdf.format(this.date);
		} else if (this.status == DOWNLOAD_ERROR) {
			return OnlyContext.getContext().getString(R.string.download_error);
		} else {
			return OnlyContext.getContext().getString(R.string.could_not_parse);
		}
	}

	public int getType() {
		return type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public ArrayList<String> getTabTxt() {
		return tabTxt;
	}

	public void setTabTxt(ArrayList<String> tabTxt) {
		this.tabTxt = tabTxt;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	@Override
	public int compareTo(Station o) {
		if (this.url != null)
			return this.url.toLowerCase(Locale.getDefault()).compareTo(
					o.getUrl().toLowerCase(Locale.getDefault()));
		else
			throw new IllegalArgumentException();
	}

	public boolean isvalued() {
		return valued;
	}

	public void setValued(boolean valued) {
		this.valued = valued;
	}

	public void parseCache() {
		String filename = "pic" + this.url.hashCode();
		try {
			Document doc = Jsoup.parse(OnlyContext.getContext()
					.getFileStreamPath(filename), null);
			if (this.type == WC) {
				this.tabTxt = parseWC(doc);
			} else if (this.type == BZ) {
				this.tabTxt = parseBZ(doc);
			} else if (this.type == WD) {
				this.tabTxt = parseWD(doc);
			}
		} catch (IOException e) {
			LoadSaveOps.printErrorToLog(e);
		} catch (Exception e) {
			LoadSaveOps.printErrorToLog(e);
			this.status = PARSE_ERROR;
		}
	}

	private static ArrayList<String> parseWC(Document doc) throws Exception {
		ArrayList<String> patschText = new ArrayList<String>();
		Elements tableElements = doc
				.select("table[class=hourly]:has(th:contains(Spitze))");
		// Handle Stations that deliver data only every 6 hours
		if (tableElements.size() == 0) {
			tableElements = doc
					.select("table[class=sixhourly]:has(th:contains(Spitze))");
		}
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
		return patschText;
	}

	private static ArrayList<String> parseBZ(Document doc) throws Exception {
		ArrayList<String> patschText = new ArrayList<String>();
		Elements tableElements = doc
				.select("table[class=avalanches-stations]:contains(Messstationen)");
		// Headers
		ArrayList<String> searchers = new ArrayList<String>();
		searchers.add("dd");
		searchers.add("ff");
		searchers.add("bb");
		ArrayList<Integer> posis = new ArrayList<Integer>();
		posis.add(0);// Zeit
		ArrayList<String> headers = new ArrayList<String>();
		Elements tableHeadElements = tableElements.select("tr");
		Element trow = tableHeadElements.get(0);
		Elements trowItems = trow.select("th, td");
		for (int j = 2; j < trowItems.size(); j++) {
			if (searchers.contains(trowItems.get(j).text().substring(0, 2))) {
				headers.add(trowItems.get(j).text());
				posis.add(j);
			}
		}
		String head = "Station, Höhe, Zeit";
		for (String line : headers) {
			head += "&/" + line;
		}
		patschText.add(head);

		Elements tableRowElements = tableElements.select(":not(thead) tr");
		// Rows
		outerloop: for (int i = 0; i < tableRowElements.size(); i++) {
			Element row = tableRowElements.get(i);
			Elements rowItems = row.select("th, td");
			String line = "";
			for (int j = 0; j < rowItems.size(); j++) {
				String el = rowItems.get(j).text();
				if (j == 0 && el.contains("(")) {
					line += toCamelCase(el.split("\\(")[0].trim()) + "\n";
					line += el.split("\\(")[1].replace(")", "").trim() + ", ";
				} else if (j == 1) {
					if (el.length() > 5) {
						line += el.substring(el.length() - 5); // no date,
																// just time
						line += "&/";
					} else {
						continue outerloop;
					}
				} else if (posis.contains(j)) {
					line += rowItems.get(j).text();
					if (j != rowItems.size() - 1 && j != 0)
						line += "&/";
				}

			}
			patschText.add(line);
		}
		return patschText;
	}

	// Wetterdienst.de
	public static ArrayList<String> parseWD(Document doc) throws Exception {
		ArrayList<String> patschText = new ArrayList<String>();
		Elements tableElements = doc.select("table[class=weather-table]");
		// Headers
		ArrayList<String> searchers = new ArrayList<String>();
		searchers.add("Luftdruck");
		searchers.add("Wind");
		ArrayList<Integer> posis = new ArrayList<Integer>();
		posis.add(0);// Zeit
		ArrayList<String> headers = new ArrayList<String>();
		headers.add("Zeit");
		// Rows
		Elements tableRowElements = tableElements.select(":not(thead) tr");
		Element trow = tableRowElements.get(0);
		Elements trowItems = trow.select("th, td");
		for (int j = 1; j < trowItems.size(); j++) {
			if (searchers.contains(trowItems.get(j).text())) {
				headers.add(trowItems.get(j).text());
				posis.add(j);
			}
		}
		// Rows
		for (int i = 0; i < tableRowElements.size(); i++) {
			Element row = tableRowElements.get(i);
			Elements rowItems = row.select("th, td");
			String line = "";
			for (int j = 0; j < rowItems.size(); j++) {
				if (posis.contains(j)) {
					line += rowItems.get(j).text();
					if (j != rowItems.size() - 1)
						line += "&/";
				}
			}
			patschText.add(line);
		}
		return patschText;
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

}
