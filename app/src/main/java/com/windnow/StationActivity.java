package com.windnow;

import com.windnow.classes.DownloadStation;
import com.windnow.classes.InterfaceDlUpdate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * 
 * This Class is part of WindNow.
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

public class StationActivity extends AppCompatActivity implements
		InterfaceDlUpdate {

	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_station);
		if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
		mPager = (ViewPager) findViewById(R.id.station_container);
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				setTitle(MainActivity.objects.get(position).getName());
			}
		});
		mPager.setCurrentItem(getIntent().getExtras().getInt("position"));
		mPagerAdapter.notifyDataSetChanged();
		setTitle(MainActivity.objects.get(getIntent().getExtras().getInt("position")).getName());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.station_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			//Intent intent = new Intent(this, MainActivity.class);
			//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Resume saved
			Intent returnIntent = new Intent();
			returnIntent.putExtra("selected", mPager.getCurrentItem());
			setResult(RESULT_OK,returnIntent);
			finish();
			break;
			//NavUtils.navigateUpTo(this, intent);
			//return true;
		case R.id.action_hot_refresh:
			Station st = MainActivity.objects.get(mPager.getCurrentItem());
			if (st.getStatus() == Station.DOWNLOADING) {
				break;
			}
			st.setLoaded(false);
			st.setStatus(Station.DOWNLOADING);
			new DownloadStation(this, st).execute();

		}
		return super.onOptionsItemSelected(item);
	}

	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Bundle bundle = new Bundle();
			if (position >= MainActivity.objects.size())
				position = 0;
			bundle.putInt("position", position);
			StationFragment fragment = new StationFragment();
			fragment.setArguments(bundle);
			return fragment;

		}

		@Override
		public int getCount() {
			return MainActivity.objects.size();
		}
	}

	@Override
	public void onTaskUpdate(Station st) {
		// TODO Left blank for the moment - maybe something fancy one day?
	}

	@Override
	public void onTaskResult(Station st) {
		mPager.setAdapter(mPagerAdapter);
		mPager.setCurrentItem(MainActivity.objects.indexOf(st));
		mPagerAdapter.notifyDataSetChanged();
	}
}