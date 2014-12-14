package com.windnow;

import java.util.ArrayList;

import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

/**
 * 
 * This Class is part of WindNow.
 * 
 * It is the main activity.
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

@SuppressLint({ "InflateParams", "NewApi" })
public class MainActivity extends ActionBarActivity {

	private static final String VERSIONID = "0.1";
	private StationListAdapter stAda;
	public static final int DIALOG_NEW_STAT = -1;
	public static final int STATION = 1;
	public static final int USER_PREF = 3;
	public static final boolean DUMMY = false;
	final ArrayList<Station> objects = new ArrayList<Station>();

	static ArrayList<String> txt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		objects.addAll(LoadSaveStations.loadStations());
		final ListView listview = (ListView) findViewById(R.id.listview);
		stAda = new StationListAdapter(this, R.layout.main_list_item, objects);
		listview.setAdapter(stAda);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				if (objects.get(position).isLoaded()) {
					Intent sText = objects.get(position).isPic() ? new Intent(
							getApplicationContext(), StationPic.class)
							: new Intent(getApplicationContext(),
									StationText.class);
					sText.putExtra("txt", objects.get(position).getUrl());
					sText.putExtra("name", objects.get(position).getName());
					sText.putStringArrayListExtra("tabTxt",
							objects.get(position).getTabTxt());
					startActivityForResult(sText, STATION);
				}
			}
		});

		listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				showDialog(position);
				return true;
			}

		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent userPref = new Intent(getApplicationContext(),
					SettingsActivity.class);
			startActivityForResult(userPref, USER_PREF);
			break;
		case R.id.action_about:
			AboutDialog.makeDialog(this, VERSIONID);
			break;
		case R.id.action_new_station:
			showDialog(DIALOG_NEW_STAT);
			break;
		case R.id.action_refresh:
			for (Station st : objects) {
				st.setLoaded(false);
				stAda.notifyDataSetChanged();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					new DownloadStation(st).executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR, st.getUrl());
				} else {
					new DownloadStation(st).execute(st.getUrl());
				}
			}
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Return here with result from activity
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case STATION:
			break;
		}
	}

	/**
	 * 
	 * AsyncTask to download the content...
	 *
	 */
	private class DownloadStation extends AsyncTask<String, Integer, String> {
		private Station station;

		private DownloadStation(Station station) {
			this.station = station;
		}

		@Override
		protected String doInBackground(String... urls) {
			String response = "";
			if (station.isPic()) {
				station.setTxt(DownloadWCStation.downloadPic(station.getUrl()));
			} else {
				station.setTabTxt(DownloadWCStation.downloadWC(station.getUrl()));
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			station.setLoaded(true);
			stAda.notifyDataSetChanged();
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog dialogDetails = null;
		LayoutInflater inflater = LayoutInflater.from(this);
		AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(this);
		View dialogview = null;
		switch (id) {
		case DIALOG_NEW_STAT:
			dialogview = inflater.inflate(R.layout.dialog_new_station, null);
			dialogbuilder.setTitle("Create the new station:");
			break;
		default:
			dialogview = inflater.inflate(R.layout.dialog_ref_del, null);
			dialogbuilder.setTitle("What do you want to do?");
			break;
		}
		dialogbuilder.setView(dialogview);
		dialogDetails = dialogbuilder.create();

		return dialogDetails;
	}

	@Override
	protected void onPrepareDialog(final int id, Dialog dialog) {
		final AlertDialog alertDialog = (AlertDialog) dialog;
		switch (id) {
		case DIALOG_NEW_STAT:
			final EditText stationName = (EditText) alertDialog
					.findViewById(R.id.newStationName);
			final EditText stationUrl = (EditText) alertDialog
					.findViewById(R.id.newStationUrl);
			final CheckBox stationIsPic = (CheckBox) alertDialog
					.findViewById(R.id.dialog_pic_station);
			Button okButton = (Button) alertDialog
					.findViewById(R.id.btn_confirm);
			okButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Station newStation = new Station(stationName.getText()
							.toString(), stationUrl.getText().toString(),
							stationIsPic.isChecked());
					objects.add(newStation);
					stAda.notifyDataSetChanged();
					LoadSaveStations.saveStations(objects);
					alertDialog.dismiss();
				}
			});

			Button cancelButton = (Button) alertDialog
					.findViewById(R.id.btn_cancel);
			cancelButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					alertDialog.dismiss();
				}
			});
			break;
		default:
			Button refButton = (Button) alertDialog.findViewById(R.id.refresh);
			Button delButton = (Button) alertDialog.findViewById(R.id.delete);
			refButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					objects.get(id).setLoaded(false);
					stAda.notifyDataSetChanged();
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						new DownloadStation(objects.get(id)).executeOnExecutor(
								AsyncTask.THREAD_POOL_EXECUTOR, objects.get(id)
										.getUrl());
					} else {
						new DownloadStation(objects.get(id)).execute("test");
					}
					alertDialog.dismiss();
				}
			});

			delButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case DialogInterface.BUTTON_POSITIVE:
								objects.remove(id);
								LoadSaveStations.saveStations(objects);
								stAda.notifyDataSetChanged();
								break;
							case DialogInterface.BUTTON_NEGATIVE:
								break;
							}
						}
					};
					AlertDialog.Builder builder = new AlertDialog.Builder(
							MainActivity.this);
					builder.setMessage("Really delete this station?")
							.setPositiveButton("Yes", dialogClickListener)
							.setNegativeButton("No", dialogClickListener)
							.show();
					alertDialog.dismiss();
				}
			});
			break;
		}
	}

}
