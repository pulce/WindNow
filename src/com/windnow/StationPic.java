package com.windnow;

import java.io.FileInputStream;

import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * This Class is part of WindNow.
 * 
 * It is the activity representing pure picture stations.
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

public class StationPic extends ActionBarActivity {

	private ImageView imageDetail;
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	private PointF startPoint = new PointF();
	private PointF midPoint = new PointF();
	private float oldDist = 1f;
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_station_pic);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		TextView tv = (TextView) findViewById(R.id.textViewPicName);
		tv.setText(getIntent().getExtras().getString("name"));
		imageDetail = (ImageView) findViewById(R.id.imageView1);
		String filename = "pic"
				+ getIntent().getExtras().getString("txt").hashCode();
		Bitmap pic = null;
		try {
			FileInputStream is = this.openFileInput(filename);
			pic = BitmapFactory.decodeStream(is);
			is.close();
		} catch (Exception e) {
			LoadSaveStations.printErrorToLog(e);
		}
		imageDetail.setImageBitmap(pic);
		imageDetail.setOnTouchListener(new View.OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				ImageView view = (ImageView) v;
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:

					savedMatrix.set(matrix);
					startPoint.set(event.getX(), event.getY());
					mode = DRAG;
					break;

				case MotionEvent.ACTION_POINTER_DOWN:

					oldDist = spacing(event);

					if (oldDist > 10f) {
						savedMatrix.set(matrix);
						midPoint(midPoint, event);
						mode = ZOOM;
					}
					break;

				case MotionEvent.ACTION_UP:

				case MotionEvent.ACTION_POINTER_UP:
					mode = NONE;

					break;

				case MotionEvent.ACTION_MOVE:
					if (mode == DRAG) {
						matrix.set(savedMatrix);
						matrix.postTranslate(event.getX() - startPoint.x,
								event.getY() - startPoint.y);
					} else if (mode == ZOOM) {
						float newDist = spacing(event);
						if (newDist > 10f) {
							matrix.set(savedMatrix);
							float scale = newDist / oldDist;
							matrix.postScale(scale, scale, midPoint.x,
									midPoint.y);
							matrix.postTranslate(event.getX() - startPoint.x,
									event.getY() - startPoint.y); //new
						}
					}
					break;

				}
				view.setImageMatrix(matrix);

				return true;
			}

			@SuppressLint("FloatMath")
			private float spacing(MotionEvent event) {
				float x = event.getX(0) - event.getX(1);
				float y = event.getY(0) - event.getY(1);
				return FloatMath.sqrt(x * x + y * y);
			}

			private void midPoint(PointF point, MotionEvent event) {
				float x = event.getX(0) + event.getX(1);
				float y = event.getY(0) + event.getY(1);
				point.set(x / 2, y / 2);
			}
		});

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
}
