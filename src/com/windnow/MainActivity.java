package com.windnow;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;

import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

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

	private static final String VERSIONID = "1.1.0";
	private StationListAdapter stAda;
	private String sharedUrl = null;
	public static int maxRetries;

	private static int stationToEdit;
	public static final int DIALOG_NEW_STAT = -1;
	public static final int DIALOG_EDIT_STAT = -2;
	public static final int DIALOG_SHARE_STAT = -3;
	public static final boolean DUMMY = false;
	final ArrayList<Station> objects = new ArrayList<Station>();

	static ArrayList<String> txt;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		maxRetries = Integer.parseInt(PreferenceManager
				.getDefaultSharedPreferences(OnlyContext.getContext())
				.getString("pref_list", "5"));
		objects.addAll(LoadSaveOps.loadStations());
		final ListView listview = (ListView) findViewById(R.id.listview);
		stAda = new StationListAdapter(this, R.layout.main_list_item, objects);
		listview.setAdapter(stAda);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				if (objects.get(position).isvalued()) {
					Intent sText = objects.get(position).getType() == Station.PIC ? new Intent(
							getApplicationContext(), StationPicActivity.class)
							: new Intent(getApplicationContext(),
									StationTextActivity.class);
					sText.putExtra("txt", objects.get(position).getUrl());
					sText.putExtra(
							"name",
							objects.get(position).getName()
									+ "\n"
									+ getString(R.string.downloaded_at)
									+ Station.sdf.format(objects.get(position)
											.getDate()));
					sText.putStringArrayListExtra("tabTxt",
							objects.get(position).getTabTxt());
					startActivity(sText);
				}
			}
		});

		listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				showDialog(position);
				return true;
			}

		});

		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			sharedUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
			showDialog(DIALOG_SHARE_STAT);
		}
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
			startActivity(new Intent(getApplicationContext(),
					SettingsActivity.class));
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
			for (Station st : objects) {
				initiateDl(st);
			}
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
		stAda.notifyDataSetChanged();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			new DownloadStation(st)
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			new DownloadStation(st).execute();
		}
	}

	/**
	 * 
	 * AsyncTask to download the content...
	 *
	 */
	private class DownloadStation extends AsyncTask<Void, Void, Void> {
		private Station station;

		private DownloadStation(Station station) {
			this.station = station;
		}

		@Override
		protected Void doInBackground(Void... v) {
			for (int dlTry = 1; dlTry <= maxRetries; dlTry++) {
				try {
					station.setProgress(0);
					String filename = "pic" + station.getUrl().hashCode();
					int IO_BUFFER_SIZE = 4 * 1024;
					URLConnection uc = new URL(station.getUrl())
							.openConnection();
					int contentLength = uc.getContentLength();
					InputStream input = new BufferedInputStream(
							uc.getInputStream(), IO_BUFFER_SIZE);
					OutputStream out = new BufferedOutputStream(OnlyContext
							.getContext().openFileOutput(filename,
									OnlyContext.MODE_PRIVATE), IO_BUFFER_SIZE);
					byte[] b = new byte[IO_BUFFER_SIZE];
					int read;
					long total = 0;
					while ((read = input.read(b)) != -1) {
						total += read;
						if (contentLength > 0) {
							station.setProgress((int) ((total * 100) / contentLength));
							publishProgress();
						}
						out.write(b, 0, read);
					}
					input.close();
					out.close();

					station.setLoaded(true);
					station.setValued(true);
					station.setStatus(Station.LOADED);
					station.setDate(Calendar.getInstance().getTime());

					if (station.getType() == Station.PIC) {
					} else if (station.getType() == Station.BZ) {
						station.parseCache();
					} else {
						station.parseCache();
					}
					break;
				} catch (IOException e) {
					if (dlTry == maxRetries) {
						station.setStatus(Station.DOWNLOAD_ERROR);
					}
					LoadSaveOps.printErrorToLog(e);
					e.printStackTrace();
				}
			}
			return null;
		}

		protected void onProgressUpdate(Void... p) {
			stAda.notifyDataSetChanged();
		}

		@Override
		protected void onPostExecute(Void v) {
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
		int position = objects.size();
		switch (id) {
		case DIALOG_SHARE_STAT:
		case DIALOG_EDIT_STAT:
		case DIALOG_NEW_STAT:
			final EditText stationName = (EditText) alertDialog
					.findViewById(R.id.newStationName);
			final EditText stationUrl = (EditText) alertDialog
					.findViewById(R.id.newStationUrl);
			if (edit) {
				stationName.setText(objects.get(stationToEdit).getName());
				stationUrl.setText(objects.get(stationToEdit).getUrl());
				position = stationToEdit;
			}
			if (share) {
				stationName.setText("");
				stationUrl.setText(sharedUrl);
			}
			final Spinner dropdown = (Spinner) alertDialog
					.findViewById(R.id.spinner1);
			ArrayList<Integer> items = new ArrayList<Integer>();
			for (int i = 1; i <= (edit ? objects.size() : objects.size() + 1); i++) {
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
						newStation = objects.get(stationToEdit);
						newStation.setName(stationName.getText().toString());
						newStation.setUrl(stationUrl.getText().toString());
						objects.remove(newStation);
					} else {
						newStation = new Station(stationName.getText()
								.toString(), stationUrl.getText().toString());
					}
					objects.add((Integer) dropdown.getSelectedItem() - 1,
							newStation);
					stAda.notifyDataSetChanged();
					LoadSaveOps.saveStations(objects);
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
			refButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					alertDialog.dismiss();
					initiateDl(objects.get(id));
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
								objects.remove(id);
								LoadSaveOps.saveStations(objects);
								stAda.notifyDataSetChanged();
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
			break;
		}
	}
}
