package com.windnow;

import java.util.Locale;

/**
 * 
 * @author Florian Hauser Part of the code comes from H3R3T1C File Chooser from
 *         http://www.dreamincode.net/forums/topic/190013-creating-simple-file-
 *         chooser/
 * 
 */
public class FilePreferenceAdapterOption implements Comparable<FilePreferenceAdapterOption> {
	private String name;
	private String data;
	private String path;

	public FilePreferenceAdapterOption(String n, String d, String p) {
		name = n;
		data = d;
		path = p;
	}

	public String getName() {
		return name;
	}

	public String getData() {
		return data;
	}

	public String getPath() {
		return path;
	}

	@Override
	public int compareTo(FilePreferenceAdapterOption o) {
		if (this.name != null)
			return this.name.toLowerCase(Locale.ENGLISH).compareTo(o.getName().toLowerCase(Locale.ENGLISH));
		else
			throw new IllegalArgumentException();
	}
}
