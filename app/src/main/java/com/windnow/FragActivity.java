package com.windnow;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.windnow.preferences.FilePreferenceFragment;
import com.windnow.preferences.UserPreferencesFragment;

/**
 * This Class is part of WindNow.
 * <p/>
 * It provides the Help.
 *
 * @author Florian Hauser Copyright (C) 2014
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

public class FragActivity extends AppCompatActivity {

    public static final int HELPFRAG = 1;
    public static final int FILEFRAG = 2;
    public static final int USERPREFFRAG = 3;

    private int thisType;
    private Fragment frag = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        thisType = getIntent().getIntExtra("FragType", 1);
        switch (thisType) {
            case HELPFRAG:
                frag = new HelpFragment();
                break;
            case FILEFRAG:
                frag = new FilePreferenceFragment();
                 break;
            case USERPREFFRAG:
                frag = new UserPreferencesFragment();
                setTitle(R.string.action_userprefs);
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, frag).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (thisType == FILEFRAG) {
                    FilePreferenceFragment fpf = (FilePreferenceFragment) frag;
                    if (fpf.tryToMoveUp())
                        return true;
                }
                startActivity(new Intent(getApplicationContext(),
                        MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
