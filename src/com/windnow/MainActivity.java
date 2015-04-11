package com.windnow;

import java.util.ArrayList;

import com.windnow.classes.AboutDialog;
import com.windnow.classes.CheckForUpdates;
import com.windnow.classes.DownloadStation;
import com.windnow.classes.InterfaceDlUpdate;
import com.windnow.preferences.SettingsActivity;
import com.windnow.statics.LoadSaveOps;

import android.support.v4.app.FragmentActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * 
 * This Class is part of WindNow.
 * 
 * It is the main activity.
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

@SuppressLint({ "InflateParams", "NewApi" })
public class MainActivity extends FragmentActivity implements
		InterfaceDlUpdate, MainFragment.Callbacks {

	public static final String VERSIONID = "2.0.0";
	public static final String APPURL = "https://github.com/pulce/WindNow/releases/latest";

	private String sharedUrl = null;
	private static int stationToEdit;
	public static final int DIALOG_NEW_STAT = -1;
	public static final int DIALOG_EDIT_STAT = -2;
	public static final int DIALOG_SHARE_STAT = -3;
	public static final int ACT_PREF = 1;
	public static final int ACT_STATION = 2;
	public static final ArrayList<Station> objects = new ArrayList<Station>();

	private static final String LIST_STATE = "listState";

	private boolean mTwoPane;
	private Station activeStation;
	private Parcelable savedListState = null;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_list);

		if (findViewById(R.id.station_container) != null) {
			mTwoPane = true;
		}

		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			sharedUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
			showDialog(DIALOG_SHARE_STAT);
		}
	}

	/**
	 * Method needed to restore scroll position
	 */
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		savedListState = state.getParcelable(LIST_STATE);
	}

	/**
	 * Method needed to restore scroll position
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (savedListState != null) {
			((MainFragment) getSupportFragmentManager().findFragmentById(
					R.id.main_list)).getListView().onRestoreInstanceState(
					savedListState);
		}
		savedListState = null;
	}

	/**
	 * Method needed to restore scroll position
	 */
	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		savedListState = ((MainFragment) getSupportFragmentManager()
				.findFragmentById(R.id.main_list)).getListView()
				.onSaveInstanceState();
		state.putParcelable(LIST_STATE, savedListState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void refreshView() {
		MainFragment fragment = (MainFragment) getSupportFragmentManager()
				.findFragmentById(R.id.main_list);
		if (fragment != null && fragment.isInLayout()) {
			fragment.refreshAdapter();
		}
	}
	
	private void refreshDetail(Station st) {
		StationFragment stfragment = (StationFragment) getSupportFragmentManager()
				.findFragmentById(R.id.station_container);
		if (stfragment != null && st == activeStation && mTwoPane) {
			Bundle bundle = new Bundle();
			bundle.putInt("position", objects.indexOf(st));
			Log.d("Replacing....", activeStation.getName());
			StationFragment frag = new StationFragment();
			frag.setArguments(bundle);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.station_container, frag).commit();
		}

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACT_PREF) {
			refreshView();
		} else if (requestCode == ACT_STATION) {
			
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivityForResult(new Intent(getApplicationContext(),
					SettingsActivity.class), ACT_PREF);
			break;
		case R.id.action_help:
			startActivity(new Intent(getApplicationContext(),
					HelpActivity.class));
			break;
		case R.id.action_about:
			AboutDialog.makeDialog(this, VERSIONID);
			break;
		case R.id.action_new_station:
			showDialog(DIALOG_NEW_STAT);
			break;
		case R.id.action_refresh:
			for (Station st : getObjects()) {
				initiateDl(st);
			}
			break;
		case R.id.action_update:
			new CheckForUpdates(this).execute(VERSIONID);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initiateDl(Station st) {
		if (st.getStatus() == Station.DOWNLOADING) {
			return;
		}
		st.setLoaded(false);
		st.setStatus(Station.DOWNLOADING);
		refreshView();
		// stAda.notifyDataSetChanged();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			new DownloadStation(this, st)
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			new DownloadStation(this, st).execute();
		}
	}

	@Override
	public void onTaskUpdate(Station st) {
		refreshView();
	}

	@Override
	public void onTaskResult(Station st) {
		refreshView();
		refreshDetail(st);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog dialogDetails = null;
		LayoutInflater inflater = LayoutInflater.from(this);
		AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(this);
		View dialogview = null;
		switch (id) {
		case DIALOG_SHARE_STAT:
		case DIALOG_NEW_STAT:
			dialogview = inflater.inflate(R.layout.dialog_new_station, null);
			dialogbuilder.setTitle(R.string.create_the_new_station);
			break;
		case DIALOG_EDIT_STAT:
			dialogview = inflater.inflate(R.layout.dialog_new_station, null);
			dialogbuilder.setTitle(R.string.edit_the_station);
			break;
		default:
			dialogview = inflater.inflate(R.layout.dialog_ref_del, null);
			dialogbuilder.setTitle(R.string.what_to_do);
			break;
		}
		dialogbuilder.setView(dialogview);
		dialogDetails = dialogbuilder.create();
		return dialogDetails;
	}

	@Override
	protected void onPrepareDialog(final int id, Dialog dialog) {
		final AlertDialog alertDialog = (AlertDialog) dialog;
		final boolean edit = id == DIALOG_EDIT_STAT;
		final boolean share = id == DIALOG_SHARE_STAT;
		int position = getObjects().size();
		switch (id) {
		case DIALOG_SHARE_STAT:
		case DIALOG_EDIT_STAT:
		case DIALOG_NEW_STAT:
			final EditText stationName = (EditText) alertDialog
					.findViewById(R.id.newStationName);
			final EditText stationUrl = (EditText) alertDialog
					.findViewById(R.id.newStationUrl);
			if (edit) {
				stationName.setText(getObjects().get(stationToEdit).getName());
				stationUrl.setText(getObjects().get(stationToEdit).getUrl());
				position = stationToEdit;
			}
			if (share) {
				stationName.setText("");
				stationUrl.setText(sharedUrl);
			}
			final Spinner dropdown = (Spinner) alertDialog
					.findViewById(R.id.spinner1);
			ArrayList<Integer> items = new ArrayList<Integer>();
			for (int i = 1; i <= (edit ? getObjects().size() : getObjects()
					.size() + 1); i++) {
				items.add(i);
			}
			ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
					android.R.layout.simple_spinner_item, items);
			dropdown.setAdapter(adapter);
			dropdown.setSelection(position);
			Button okButton = (Button) alertDialog
					.findViewById(R.id.btn_confirm);
			okButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Station newStation;
					if (edit) {
						newStation = getObjects().get(stationToEdit);
						newStation.setName(stationName.getText().toString());
						newStation.setUrl(stationUrl.getText().toString());
						getObjects().remove(newStation);
					} else {
						newStation = new Station(stationName.getText()
								.toString(), stationUrl.getText().toString());
					}
					getObjects().add((Integer) dropdown.getSelectedItem() - 1,
							newStation);
					refreshView();
					// stAda.notifyDataSetChanged();
					LoadSaveOps.saveStations(getObjects());
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
			Button editButton = (Button) alertDialog.findViewById(R.id.edit_st);
			Button delButton = (Button) alertDialog.findViewById(R.id.delete);
			Button fullButton = (Button) alertDialog.findViewById(R.id.fullscreen);
			refButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					alertDialog.dismiss();
					initiateDl(getObjects().get(id));
				}
			});
			editButton.setOnClickListener(new View.OnClickListener() {
				@SuppressWarnings("deprecation")
				@Override
				public void onClick(View v) {
					alertDialog.dismiss();
					stationToEdit = id;
					showDialog(DIALOG_EDIT_STAT);
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
								getObjects().remove(id);
								LoadSaveOps.saveStations(getObjects());
								refreshView();
								break;
							case DialogInterface.BUTTON_NEGATIVE:
								break;
							}
						}
					};
					AlertDialog.Builder builder = new AlertDialog.Builder(
							MainActivity.this);
					builder.setMessage(R.string.really_delete)
							.setPositiveButton(android.R.string.yes,
									dialogClickListener)
							.setNegativeButton(android.R.string.no,
									dialogClickListener).show();
					alertDialog.dismiss();
				}
			});
			if (fullButton != null) {
			fullButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					alertDialog.dismiss();
					Bundle bundle = new Bundle();
					bundle.putInt("position", id);
					Intent sText = new Intent(getApplicationContext(),
							StationActivity.class);
					sText.putExtras(bundle);
					startActivity(sText);
				}
			});
			}
			break;
		}
	}

	@Override
	public void onItemSelected(Station st) {
		if (st.isvalued()) {
			activeStation = st;
			Bundle bundle = new Bundle();
			bundle.putInt("position", getObjects().indexOf(st));
			if (mTwoPane) {
				StationFragment fragment = new StationFragment();
				fragment.setArguments(bundle);
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.station_container, fragment).commit();
			} else {
				Intent sText = new Intent(getApplicationContext(),
						StationActivity.class);
				sText.putExtras(bundle);
				startActivityForResult(sText, 0);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void OnItemLongClicked(int position) {
		showDialog(position);
	}

	@Override
	public ArrayList<Station> getObjects() {
		if (objects.size() == 0) {
			try {
				objects.addAll(LoadSaveOps.loadStations());
			} catch (Exception e) {
				LoadSaveOps.printErrorToLog(e);
				Toast.makeText(this,
						getString(R.string.error_loading_stations_file),
						Toast.LENGTH_SHORT).show();
			}
		}
		return objects;
	}
}
