package com.windnow;

import java.util.ArrayList;

import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableLayout.LayoutParams;

/**
 * 
 * This Class is part of WindNow.
 * 
 * It is the activity representing wetteronline text stations.
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

public class StationTextActivity extends ActionBarActivity {

	@SuppressLint("NewApi")

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_station_text);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		TextView tv = (TextView) findViewById(R.id.textView1);
		tv.setText(getIntent().getExtras().getString("name"));
		ArrayList<String> tabTxt = getIntent().getStringArrayListExtra("tabTxt");
		for (String row:tabTxt) {
			printTableRow(row);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Resume saved
																// State!
			NavUtils.navigateUpTo(this, intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@SuppressLint("RtlHardcoded")
	private void printTableRow(String line) {
		TableLayout tl = (TableLayout) findViewById(R.id.tableLayout);

		String[] words = line.split("&/");
		TableRow tr = new TableRow(this);
		TableRow.LayoutParams params = new TableRow.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.FILL_HORIZONTAL;
		tr.setLayoutParams(params);
		
		TableRow.LayoutParams tvParams = new TableRow.LayoutParams(
				TableRow.LayoutParams.WRAP_CONTENT,
				TableRow.LayoutParams.WRAP_CONTENT);
		tvParams.setMargins(0, 0, 6, 0);
		
		for (int i = words.length-1; i >= 0; i--) {
		TextView txv = new TextView(this);
		txv.setLayoutParams(tvParams);
		txv.setText(words[i]);
		txv.setGravity(i == 0 ? Gravity.LEFT : Gravity.LEFT);
		tr.addView(txv, 0);
		}
		tl.addView(tr);
	}
}
