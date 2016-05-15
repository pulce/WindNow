package com.windnow;

import java.io.FileInputStream;

import com.windnow.statics.LoadSaveOps;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.TableLayout.LayoutParams;

/**
 * 
 * This Class is part of WindNow.
 * 
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

public class StationFragment extends Fragment {

	private TableLayout tl;
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	private PointF startPoint = new PointF();
	private PointF midPoint = new PointF();
	private float oldDist = 1f;
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	private Station st;

	public StationFragment() {
	}

	public Station getStation() {
		return st;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments().containsKey("position")) {
			st = MainActivity.getStaticObjects(getActivity()).get(getArguments().getInt("position"));
		}
	}
	
	@SuppressLint("RtlHardcoded")
	private void printTableRow(String line) {
		String[] words = line.split("&/");
		TableRow tr = new TableRow(getActivity());
		TableRow.LayoutParams params = new TableRow.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.FILL_HORIZONTAL;
		tr.setLayoutParams(params);

		TableRow.LayoutParams tvParams = new TableRow.LayoutParams(
				TableRow.LayoutParams.WRAP_CONTENT,
				TableRow.LayoutParams.WRAP_CONTENT);
		tvParams.setMargins(0, 0, 6, 0);

		for (int i = words.length - 1; i >= 0; i--) {
			TextView txv = new TextView(getActivity());
			txv.setLayoutParams(tvParams);
			txv.setText(words[i]);
			txv.setGravity(i == 0 ? Gravity.LEFT : Gravity.LEFT);
			tr.addView(txv, 0);
		}
		tl.addView(tr);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater
				.inflate(R.layout.frag_station, container, false);
		LinearLayout la = (LinearLayout) rootView
				.findViewById(R.id.stationTextLayout);
		if (st.getType() == Station.PIC && st.isvalued()) {
			final ImageView imageDetail = new ImageView(getActivity());
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			imageDetail.setLayoutParams(layoutParams);
			la.addView(imageDetail);
			imageDetail.setScaleType(ScaleType.MATRIX);
			String filename = "pic" + st.getUrl().hashCode();
			Bitmap pic;
			try {
				FileInputStream is = getActivity().openFileInput(filename);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Bitmap.Config.RGB_565;
				options.inJustDecodeBounds = true;
				pic = BitmapFactory.decodeStream(is, null, options);
				is.close();
				is = getActivity().openFileInput(filename);
				int size = Math.max(options.outHeight, options.outWidth);
				if(size>1024)
					options.inSampleSize = Math.round(size / 800);
				options.inJustDecodeBounds = false;
				pic = BitmapFactory.decodeStream(is, null, options);
				is.close();
				imageDetail.setImageBitmap(pic);
			} catch (Exception e) {
				LoadSaveOps.printErrorToLog(e);
			}
			/* Could scale Pic Fullscreen on start. Buggy!
			 * final int startx = pic.getWidth(); final int starty =
			 * pic.getHeight();
			 * 
			 * ViewTreeObserver vto = tv.getViewTreeObserver();
			 * vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			 * 
			 * @Override public void onGlobalLayout() { //LayerDrawable ld =
			 * (LayerDrawable)imageDetail.getBackground(); //ld.setLayerInset(1,
			 * 0, imageDetail.getHeight() / 2, 0, 0); Log.d("Heeeeeeereeeeeee",
			 * "" + startx + " " + imageDetail.getWidth());
			 * savedMatrix.set(matrix); float scalex =
			 * imageDetail.getWidth()/startx; float scaley =
			 * imageDetail.getHeight()/starty; //matrix.postScale(startx,
			 * starty, imageDetail.getWidth(), // imageDetail.getHeight());
			 * matrix.postScale(Math.min(scalex, scaley),Math.min(scalex,
			 * scaley)); imageDetail.setImageMatrix(matrix);
			 * 
			 * ViewTreeObserver obs = imageDetail.getViewTreeObserver();
			 * 
			 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			 * obs.removeOnGlobalLayoutListener(this); } else {
			 * obs.removeGlobalOnLayoutListener(this); } }
			 * 
			 * });
			 */

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
								matrix.postTranslate(event.getX()
										- startPoint.x, event.getY()
										- startPoint.y); // new
							}
						}
						break;

					}
					view.setImageMatrix(matrix);

					return true;
				}

				private float spacing(MotionEvent event) {
					float x = event.getX(0) - event.getX(1);
					float y = event.getY(0) - event.getY(1);
					return (float)Math.sqrt(x * x + y * y);
				}

				private void midPoint(PointF point, MotionEvent event) {
					float x = event.getX(0) + event.getX(1);
					float y = event.getY(0) + event.getY(1);
					point.set(x / 2, y / 2);
				}
			});

		} else if (st.isvalued()){

			ScrollView sv = new ScrollView(getActivity());
			la.addView(sv);
			tl = new TableLayout(getActivity());
			sv.addView(tl);

			for (String row : st.getTabTxt()) {
				printTableRow(row);
			}
		}
		return rootView;
	}
}
