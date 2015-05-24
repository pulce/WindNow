package com.windnow;

import java.util.ArrayList;

import com.windnow.classes.AboutDialog;
import com.windnow.classes.CheckForUpdates;
import com.windnow.classes.DownloadStation;
import com.windnow.classes.DrawerListAdapter;
import com.windnow.classes.InterfaceDlUpdate;
import com.windnow.classes.NavItem;
import com.windnow.preferences.FilePreferenceFragment;
import com.windnow.statics.LoadSaveOps;

import android.content.res.Configuration;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * This Class is part of WindNow.
 * <p/>
 * It is the main activity.
 *
 * @author Florian Hauser Copyright (C) 2015
 *         <p/>
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or (at
 *         your option) any later version.
 *         <p/>
 *         This program is distributed in the hope that it will be useful, but
 *         WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *         General Public License for more details.
 *         <p/>
 *         You should have received a copy of the GNU General Public License
 *         along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

public class MainActivity extends AppCompatActivity implements
        InterfaceDlUpdate, MainFragment.Callbacks {

    public static final String VERSIONID = "2.1.0"; 
    public static final String APPURL = "https://github.com/pulce/WindNow/releases/latest";

    private String sharedUrl = null;
    private static int stationToEdit;
    public static final int DIALOG_NEW_STAT = -1;
    public static final int DIALOG_EDIT_STAT = -2;
    public static final int DIALOG_SHARE_STAT = -3;
    public static final ArrayList<Station> objects = new ArrayList<>();

    private static final String LIST_STATE = "listState";

    private boolean mTwoPane;
    private Station activeStation;
    private Parcelable savedListState = null;

    ListView mDrawerList;
    RelativeLayout mDrawerPane;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    ArrayList<NavItem> mNavItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        setTitle(R.string.app_name);
        if (findViewById(R.id.station_container) != null) {
            mTwoPane = true;
            Fragment frag = getSupportFragmentManager().findFragmentById(R.id.station_container);
            if (frag != null) {
                if (frag instanceof StationFragment) {
                    StationFragment stf = (StationFragment) frag;
                    setTitle(stf.getStation().getName());
                } else if (frag instanceof HelpFragment) {
                    setTitle(R.string.title_activity_help);
                }
            }
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        mDrawerList = (ListView) findViewById(R.id.navList);
        final DrawerListAdapter adapter = new DrawerListAdapter(this, mNavItems);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onMenuItemSelected(mNavItems.get(position).getId());
            }
        });

        mNavItems.add(new NavItem(R.id.action_new_station, R.string.action_new_station, R.drawable.ic_add_circle_outline_grey600_36dp));
        mNavItems.add(new NavItem(R.id.action_file, R.string.menu_sel_stationsfile, R.drawable.ic_input_grey600_36dp));
        mNavItems.add(new NavItem(R.id.action_help, R.string.action_help, R.drawable.ic_help_grey600_36dp));
        mNavItems.add(new NavItem(R.id.action_about, R.string.action_about, R.drawable.ic_info_outline_grey600_36dp));
        mNavItems.add(new NavItem(R.id.action_update, R.string.check_for_updates, R.drawable.ic_file_download_grey600_36dp));

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            sharedUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
            //noinspection deprecation
            showDialog(DIALOG_SHARE_STAT);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle state) {
        super.onRestoreInstanceState(state);
        savedListState = state.getParcelable(LIST_STATE);
    }

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
        getMenuInflater().inflate(R.menu.main_menu, menu);
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
            StationFragment frag = new StationFragment();
            frag.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.station_container, frag).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onMenuItemSelected(item.getItemId());
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private boolean onMenuItemSelected(int id) {
        mDrawerLayout.closeDrawer(Gravity.START);
        switch (id) {
            case R.id.action_file:
                if (mTwoPane) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.station_container, new FilePreferenceFragment()).commit();

                } else {
                    startActivity(new Intent(getApplicationContext(),
                            FragActivity.class).putExtra("FragType", FragActivity.FILEFRAG));
                }
                break;
            case R.id.action_help:
                if (mTwoPane) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.station_container, new HelpFragment()).commit();
                    setTitle(R.string.title_activity_help);
                } else {
                    startActivity(new Intent(getApplicationContext(),
                            FragActivity.class).putExtra("FragType", FragActivity.HELPFRAG));
                }
                break;
            case R.id.action_about:
                AboutDialog.makeDialog(this, VERSIONID);
                break;
            case R.id.action_new_station:
                //noinspection deprecation
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
        }
        return true;
    }

    private void initiateDl(Station st) {
        if (st.getStatus() == Station.DOWNLOADING) {
            return;
        }
        st.setLoaded(false);
        st.setStatus(Station.DOWNLOADING);
        refreshView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new DownloadStation(this, st)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new DownloadStation(this, st).execute();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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

    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog dialogDetails;
        LayoutInflater inflater = LayoutInflater.from(this);
        AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(this);
        View dialogview;
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
                dialogbuilder.setTitle(getObjects().get(id).getName());
                break;
        }
        dialogbuilder.setView(dialogview);
        dialogDetails = dialogbuilder.create();
        return dialogDetails;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPrepareDialog(final int id, @NonNull Dialog dialog) {
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
                ArrayList<Integer> items = new ArrayList<>();
                for (int i = 1; i <= (edit ? getObjects().size() : getObjects()
                        .size() + 1); i++) {
                    items.add(i);
                }
                ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this,
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
                        android.support.v7.app.AlertDialog.Builder builder = new AlertDialog.Builder(
                                MainActivity.this, R.style.AppCompatAlertDialogStyle);
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
                setTitle(st.getName());
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
