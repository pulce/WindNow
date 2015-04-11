package com.windnow.preferences;

import com.windnow.MainActivity;
import com.windnow.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

/**
 * 
 * This Class is part of WindNow.
 * 
 * It allows to adjust user settings.
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
public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	SharedPreferences prefs;

	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_type_prefs);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		addPreferencesFromResource(R.xml.pref_general);
		prefs.registerOnSharedPreferenceChangeListener(this);
		Preference q = findPreference("pref_list");
		ListPreference listPref = (ListPreference) q;
		q.setSummary(getString(R.string.maximum_number_summary) + " "
				+ listPref.getValue());

		Preference filePref = (Preference) findPreference("stations_file");
		filePref.setSummary(getString(R.string.current_stations_file) + " "
				+ prefs.getString("stations_file", "stations.txt"));
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			NavUtils.navigateUpTo(this, intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		Preference filePref = (Preference) findPreference("stations_file");
		filePref.setSummary(getString(R.string.current_stations_file) + " "
				+ prefs.getString("stations_file", "stations.txt"));
	}

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Preference p = findPreference(key);
		if (p.getKey().equals("pref_list") && p instanceof ListPreference) {
			ListPreference listPref = (ListPreference) p;
			p.setSummary(getString(R.string.maximum_number_summary) + " "
					+ listPref.getValue());
		}
	}

}
