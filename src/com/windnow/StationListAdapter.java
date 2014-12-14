package com.windnow;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * This Class is part of WindNow.
 * 
 * It is the list adapter for the main class.
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

public class StationListAdapter extends ArrayAdapter<Station> {

	private Context context;
	private int id;
	private List<Station> objects;
	private static Bitmap greySock = BitmapFactory.decodeResource(OnlyContext
			.getContext().getResources(), R.drawable.greysocksmall);
	private static Bitmap colSock = BitmapFactory.decodeResource(OnlyContext
			.getContext().getResources(), R.drawable.colsocksmall);

	public StationListAdapter(Context context, int textViewResourceId,
			List<Station> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		id = textViewResourceId;
		this.objects = objects;
	}

	public Station getItem(int i) {
		return objects.get(i);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(id, null);
		}
		final Station o = objects.get(position);

		if (o != null) {

			TextView header = (TextView) v.findViewById(R.id.secondLine);
			header.setText(o.getName());

		}

		ImageView view = (ImageView) v.findViewById(R.id.sockIcon);
		if (o.isLoaded())
			view.setImageDrawable(scaleImage(view, colSock));
		else
			view.setImageDrawable(scaleImage(view, greySock));

		return v;
	}

	private BitmapDrawable scaleImage(View v, Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int bounding = dpToPx(100);
		float xScale = ((float) bounding) / width;
		float yScale = ((float) bounding) / height;
		float scale = (xScale <= yScale) ? xScale : yScale;
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		width = scaledBitmap.getWidth(); // re-use
		height = scaledBitmap.getHeight(); // re-use
		@SuppressWarnings("deprecation")
		BitmapDrawable result = new BitmapDrawable(scaledBitmap);
		return result;
	}

	private int dpToPx(int dp) {
		float density = OnlyContext.getContext().getResources()
				.getDisplayMetrics().density;
		return Math.round((float) dp * density);
	}
}
