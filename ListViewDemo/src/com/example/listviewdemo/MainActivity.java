package com.example.listviewdemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends Activity {

	private Bitmap bitmap;
	private Context mContext;
	private PPListView lv;

	private List<String> mList = new ArrayList<String>();
	private TestSectionedAdapter adapter = new TestSectionedAdapter();

	private Handler mHandler = new Handler();

	private static int mID;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lv = (PPListView)findViewById(R.id.pp_listview);
		mContext = this;

		lv.setOnRefreshListener(new PPListView.OnRefreshListener() {

			@Override
			public void onRefresh() {
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(3000);

							mHandler.post(new Runnable() {

								@Override
								public void run() {
									for (int i=0; i<30; i++) {
										mList.add("origin item: " + (mID++));
									}
									lv.onRefreshSuccess();
									adapter.notifyDataSetChanged();
								}
							});
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}

			@Override
			public void onLoadMore() {
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(3000);


							mHandler.post(new Runnable() {

								@Override
								public void run() {
									for (int i=0; i<30; i++) {
										mList.add("item: " + (mID++));
									}
									lv.onRefreshSuccess();
									adapter.notifyDataSetChanged();
								}
							});

						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		});

		lv.setAdapter(adapter);
		lv.setPreloadFactor(3);
		lv.setShowTopTitle(true);
		lv.setOnRemoveItemListener(new PPListView.OnRemoveItemListener() {

			@Override
			public void removeItem(int position) {
				mList.remove(position);
				adapter.notifyDataSetChanged();
			}
		});

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void loadBitmap() {
		if (bitmap == null) {
			try {
				AssetManager asset = getAssets();
				bitmap = BitmapFactory.decodeStream(asset.open("icon.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public class TestSectionedAdapter extends SectionedBaseAdapter {

	    @Override
	    public Object getItem(int section, int position) {
	        // TODO Auto-generated method stub
	        return null;
	    }

	    @Override
	    public long getItemId(int section, int position) {
	        // TODO Auto-generated method stub
	        return 0;
	    }

	    @Override
	    public int getSectionCount() {
	        return 7;
	    }

	    @Override
	    public int getCountForSection(int section) {
	        return 15;
	    }

	    @Override
	    public View getItemView(int section, final int position, View convertView, ViewGroup parent) {
	    	if(convertView==null){
				convertView=LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
			}
			ImageView iv = (ImageView)convertView.findViewById(R.id.pp_app_img);
			TextView tv = (TextView)convertView.findViewById(R.id.pp_app_name);
			loadBitmap();
			iv.setImageBitmap(bitmap);
			tv.setText(mList.get(position));
			Button btn = (Button)convertView.findViewById(R.id.pp_d_del_btn);
			
			btn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					lv.removeItem(position);
				}
			});
			convertView.setTag("BBB, position: " + position);
			return convertView;
	    }

	    @Override
	    public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
	    	if(convertView==null){
				convertView=LayoutInflater.from(mContext).inflate(R.layout.list_item_title, parent, false);
			}
			TextView tv = (TextView)convertView.findViewById(R.id.tv_item_title);
			tv.setText("POSITION: " + section);
			convertView.setTag("AAA, position: " + section);
			return convertView;
	    }
	}

}
