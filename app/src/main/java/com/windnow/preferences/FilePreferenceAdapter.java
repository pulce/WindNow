package com.windnow.preferences;

import java.util.List;

import com.windnow.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * 
 * @author Florian Hauser
 * 
 *         Part of the code comes from H3R3T1C File Chooser from
 *         http://www.dreamincode.net/forums/topic/190013-creating-simple-file-
 *         chooser/
 * 
 */
public class FilePreferenceAdapter extends ArrayAdapter<FilePreferenceAdapterOption> {

	private Context context;
	private int id;
	private List<FilePreferenceAdapterOption> files;

	public FilePreferenceAdapter(Context context, int textViewResourceId,
			List<FilePreferenceAdapterOption> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		id = textViewResourceId;
		files = objects;
	}

	public FilePreferenceAdapterOption getItem(int i) {
		return files.get(i);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflator = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflator.inflate(id, null);
		}
		final FilePreferenceAdapterOption filePreferenceAdapterOption = files.get(position);
		if (filePreferenceAdapterOption != null) {
			TextView header = (TextView) view.findViewById(R.id.TextView01);
			TextView body = (TextView) view.findViewById(R.id.TextView02);
			if (header != null)
				header.setText(filePreferenceAdapterOption.getName());
			if (body != null)
				body.setText(filePreferenceAdapterOption.getData());
		}
		return view;
	}
}
