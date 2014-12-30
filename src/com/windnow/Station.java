package com.windnow;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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

public class Station implements Comparable<Station>{
	private String name;
	private String url;
	private int type;
	private Date date;
	private String txt;
	private ArrayList<String> tabTxt;
	private int position;
	private boolean loaded = false;
	private boolean valued = false;
	
	public static final int PIC = 1;
	public static final int WC = 2;
	public static final int BZ = 3;
	
	public Station(String name, String url) {
		this.name = name;
		this.url = url;
		if (!url.startsWith("http")) {
			if (!url.startsWith("www.")) {
				this.url = "www." + this.url;
			}
			this.url = "http://" + this.url;
		}
		if (url.contains("wetteronline") && url.contains("aktuelles-wetter")) this.type = WC;
		else if (url.contains("provinz.bz.it") && url.contains("hoehenwindstationen")) this.type = BZ;
		else this.type = PIC;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTxt() {
		return txt;
	}

	public void setTxt(String txt) {
		this.txt = txt;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
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

}
