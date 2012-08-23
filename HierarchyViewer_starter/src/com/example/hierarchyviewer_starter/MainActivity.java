package com.example.hierarchyviewer_starter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	ListView mListView = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mListView = (ListView) findViewById(R.id.list);
		mListView.setAdapter(new MyListAdapter(this));
	}

	private class MyListAdapter extends BaseAdapter {
		private LayoutInflater li = null;

		public MyListAdapter(Context context) {
			li = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mImgIds.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout weatherView = null;
			if (null == convertView) {
				weatherView = (LinearLayout) li.inflate(
						R.layout.weather_adapter, null);
			} else {
				weatherView = (LinearLayout) convertView;
			}

			ImageView pic = (ImageView) weatherView.findViewById(R.id.pic);
			pic.setImageResource(mImgIds[position]);

			TextView title = (TextView) weatherView.findViewById(R.id.title);
			title.setText(mTitle[position]);

			TextView date = (TextView) weatherView.findViewById(R.id.date);
			date.setText((position + 1) + " APR 2012");
			
			TextView info = (TextView) weatherView.findViewById(R.id.info);
			info.setText(mInfo[position]);

			return weatherView;
		}
	}
	
	private int[] mImgIds = { R.drawable.w_c_night_rain,
			R.drawable.w_c_night_snow, R.drawable.w_chance_storm_n,
			R.drawable.w_cloudy_night, R.drawable.w_night,
			R.drawable.w_night_rain, R.drawable.w_night_snow,
			R.drawable.w_smoke, R.drawable.w_night_rain,
			R.drawable.w_cloudy_night, R.drawable.w_smoke };

	private String[] mTitle = { "Night Storm", "Night Snow", "Chance Storm",
			"Cloudy Night", "Regular Night", "Night Rain", "Night Snow",
			"Smoke", "Night Rain", "Cloudy Night", "Smoke" };

	private String[] mInfo = {
			"This is a stormy night. It is good time to stay back at home :)",
			"It will be a snow night..enjoy the first snow of this season",
			"There might be a storm tonight!",
			"The night is gonna be cloudy. Take an umbrella when you go out.",
			"It is a good weather tonight..go out and catch a movie :)",
			"It is going to rain cats & dogs.. better stay @ home",
			"It will be a snow night..enjoy the first snow of this season",
			"Smoke Smoke Smoke..it's gonna be smokie night",
			"It is going to rain cats & dogs.. better stay @ home",
			"The night is gonna be cloudy. Take an umbrella when you go out.",
			"Smoke Smoke Smoke..it's gonna be smokie night" };
}
