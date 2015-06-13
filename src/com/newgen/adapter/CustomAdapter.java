package com.newgen.adapter;



import com.newgen.bluchat.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class CustomAdapter<String> extends ArrayAdapter<String> {

	private Context context;
	private String[] values;
	public CustomAdapter(Context context, String[] values) 
	{
		    super(context,R.layout.row, values);
		    this.context = context;
		    this.values = values;
	}
	@Override
	  public View getView(int position, View convertView, ViewGroup parent) 
	{
	    LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View row = inflater.inflate(R.layout.row, parent, false);
	    TextView textView = (TextView) row.findViewById(R.id.label);
	    ImageView imageView = (ImageView) row.findViewById(R.id.icon);
	    textView.setText(""+values[position]);
	    String s = values[position];
	    imageView.setImageResource(R.drawable.ic_launcher);
	    return row;
	  }
}
