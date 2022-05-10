// The present software is not subject to the US Export Administration Regulations (no exportation license required), May 2012
package com.codniv.cookbook1.database;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.codniv.cookbook1.R;

public class DatabaseArrayAdapter extends ArrayAdapter<DatabaseItem>
{
	private Context				c;
	private int					id;
	private List<DatabaseItem>	items;

	public DatabaseArrayAdapter(Context context, int textViewResourceId, List<DatabaseItem> objects)
	{
		super(context, textViewResourceId, objects);
		c = context;
		id = textViewResourceId;
		items = objects;
	}

	public DatabaseItem getItem(int i)
	{
		return items.get(i);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View v = convertView;
//		if (v == null)
//		{
//			LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			v = vi.inflate(id, null);
//		}
//		final DatabaseItem o = items.get(position);
//		if (o != null)
//		{
//			TextView t1 = (TextView) v.findViewById(R.id.numberofdatabasesvalue);
//			TextView t2 = (TextView) v.findViewById(R.id.TextView02);
//			TextView t3 = (TextView) v.findViewById(R.id.TextView03);
//			if (t1 != null)
//			{
//				t1.setText(o.getId());
//				try
//				{
//					int color = Color.TRANSPARENT;
//					View father = (View) t1.getParent();
//					if (o.isSelected())
//					{
//						color = Color.CYAN;
//					}
//					father.setBackgroundColor(color);
//				}
//				catch (Exception e)
//				{
//				}
//			}
//			if (t2 != null)
//			{
//				t2.setText(o.getFirstName());
//			}
//			if (t3 != null)
//			{
//				t3.setText(o.getLastName());
//			}
//		}
		return v;
	}
}
