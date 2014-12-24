package com.windnow;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
 * It will download text stations or pic stations from web
 * or, in case of a dummy, from assets. Must be handled as
 * as AsyncTask. That is done in MainActivity!
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
	public static ArrayList<String> downloadWC(String url) {
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
				doc = Jsoup.connect(url).get();
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
		} catch (Exception e) {
			LoadSaveStations.printErrorToLog(e);
		}
		return patschText;
	}

	@SuppressWarnings("static-access")
	static String downloadPic(String url) {
		String filename = "pic" + url.hashCode();
		;
		try {
			InputStream input;
			OutputStream out;
			if (MainActivity.DUMMY) {
				AssetManager assetManager = OnlyContext.getContext()
						.getAssets();
				input = assetManager.open("windrichtung.png");
			} else {
				input = new BufferedInputStream(new URL(url).openStream(),
						IO_BUFFER_SIZE);
			}
			out = new BufferedOutputStream(OnlyContext.getContext()
					.openFileOutput(filename,
							OnlyContext.getContext().MODE_PRIVATE),
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
		}
		return filename;
	}
	
}