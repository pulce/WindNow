package com.windnow.classes;

import com.windnow.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * 
 * This Class is part of WindNow.
 * 
 * It adds an About Dialog to the given context.
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
public class AboutDialog {

	public static void makeDialog(Context context, String versionID) {
		final Dialog dialog = new Dialog(context);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setContentView(R.layout.about);
		dialog.setTitle(dialog.getContext().getString(R.string.about));

		TextView text = (TextView) dialog.findViewById(R.id.text);
		text.setText("WindNow Version " + versionID);

		Button confirmButton = (Button) dialog.findViewById(R.id.confButton);
		confirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
}
