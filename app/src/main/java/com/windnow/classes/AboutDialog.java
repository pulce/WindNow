package com.windnow.classes;

import com.windnow.R;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * This Class is part of WindNow.
 * <p/>
 * It adds an About Dialog to the given context.
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
public class AboutDialog {

    public static void makeDialog(Context context, String versionID) {
        final AppCompatDialog dialog = new AppCompatDialog(context);
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
