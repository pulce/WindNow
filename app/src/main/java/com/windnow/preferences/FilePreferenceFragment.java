package com.windnow.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.windnow.MainActivity;
import com.windnow.R;
import com.windnow.Station;
import com.windnow.statics.LoadSaveOps;

import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * This Class is part of WindNow.
 * <p/>
 * It provides an Fragment to select the stations file.
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
public class FilePreferenceFragment extends ListFragment {

    private File currentDir;
    private FilePreferenceAdapter adapter;
    private Stack<File> dirStack = new Stack<>();
    SharedPreferences prefs;

    public FilePreferenceFragment() {
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String rootDir = Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + "/"
                + prefs.getString("user_dir", "WindNow");
        currentDir = new File(rootDir);
        String[] comps = rootDir.split("/");
        String addDir = "";
        int layers = Environment.getExternalStorageDirectory()
                .getAbsolutePath().split("/").length - 2;
        for (int i = 0; i < comps.length - 1; i++) {
            addDir += "/" + comps[i];
            if (i > layers) dirStack.push(new File(addDir));
        }
        fill(currentDir);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.file_pref, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_saveas:
                final EditText input = new EditText(getActivity());
                input.getBackground().setColorFilter(getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_ATOP);
                new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                        .setTitle(getString(R.string.menu_save_as))
                        .setMessage(getString(R.string.filechooser_save_hint))
                        .setView(input)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        SharedPreferences.Editor prefEditor = prefs
                                                .edit();
                                        prefEditor.putString("stations_file", input
                                                .getText().toString());
                                        prefEditor.putString("user_dir",
                                                getRelativePath(currentDir));
                                        prefEditor.commit();
                                        LoadSaveOps
                                                .saveStations(MainActivity.objects);
                                        Toast.makeText(getActivity(), getString(R.string.successfully_saved) + input
                                                        .getText().toString(),
                                                Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getActivity().getApplicationContext(),
                                                MainActivity.class));
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                }).show();
        }
        return super.onOptionsItemSelected(item);
    }


    public boolean tryToMoveUp() {
        if (dirStack.size() == 0)
            return false;
        currentDir = dirStack.pop();
        fill(currentDir);
        return true;
    }

    private String getRelativePath(File recentDir) {
        String folderPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        if (recentDir.getPath().length() < folderPath.length() + 1) return "";
        return recentDir.getPath().substring(folderPath.length() + 1);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        FilePreferenceAdapterOption o = adapter.getItem(position);
        if (o.getData()
                .equalsIgnoreCase(getString(R.string.filechooser_folder))) {
            dirStack.push(currentDir);
            currentDir = new File(o.getPath());
            fill(currentDir);
        } else if (o.getData().equalsIgnoreCase(
                getString(R.string.filechooser_parent_directory))) {
            currentDir = dirStack.pop();
            fill(currentDir);
        } else {
            onFileClick(o);
        }
    }

    private void onFileClick(FilePreferenceAdapterOption o) {
        SharedPreferences.Editor prefEditor = prefs.edit();
        String oldFile = prefs.getString("stations_file", "stations.txt");
        String oldDir = prefs.getString("user_dir", "stations.txt");
        prefEditor.putString("stations_file", o.getName());
        prefEditor.putString("user_dir", getRelativePath(currentDir));
        prefEditor.commit();
        ArrayList<Station> newStations = null;
        try {
            newStations = LoadSaveOps.loadStations();
        } catch (Exception e) {
            LoadSaveOps.printErrorToLog(e);
            Toast.makeText(getActivity(), R.string.filechooser_load_failed,
                    Toast.LENGTH_SHORT).show();
            prefEditor.putString("stations_file", oldFile);
            prefEditor.putString("user_dir", oldDir);
            prefEditor.commit();
            startActivity(new Intent(getActivity().getApplicationContext(),
                    MainActivity.class));
        }
        //noinspection CollectionAddedToSelf
        MainActivity.objects.removeAll(MainActivity.objects);
        MainActivity.objects.addAll(newStations);
        startActivity(new Intent(getActivity().getApplicationContext(),
                MainActivity.class));
    }

    public void fill(File f) {
        File[] dirs = f.listFiles();
        getActivity().setTitle("/" + getRelativePath(currentDir));
        List<FilePreferenceAdapterOption> dir = new ArrayList<>();
        List<FilePreferenceAdapterOption> fls = new ArrayList<>();
        try {
            for (File ff : dirs) {
                if (ff.isDirectory()) {
                    dir.add(new FilePreferenceAdapterOption(ff.getName(),
                            getString(R.string.filechooser_folder), ff
                            .getAbsolutePath()));
                } else {
                    fls.add(new FilePreferenceAdapterOption(ff.getName(),
                            getString(R.string.filechooser_file_size)
                                    + ff.length(), ff.getAbsolutePath()));
                }
            }
        } catch (Exception e) {
            LoadSaveOps.printErrorToLog(e);
        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if (dirStack.size() > 0)
            dir.add(0,
                    new FilePreferenceAdapterOption("..",
                            getString(R.string.filechooser_parent_directory), f
                            .getParent()));
        adapter = new FilePreferenceAdapter(getActivity(), R.layout.file_view, dir);
        this.setListAdapter(adapter);
    }
}
