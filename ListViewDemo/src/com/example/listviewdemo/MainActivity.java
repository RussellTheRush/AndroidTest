package com.example.listviewdemo;

import java.io.IOException;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends Activity {

	private Bitmap bitmap;
	private Context mContext;
	private PPListView lv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lv = (PPListView)findViewById(R.id.lv);
		mContext = this;
		lv.setOnRefreshListener(new PPListView.OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				try {
					Thread.sleep(3000);
					lv.onRefreshSuccess();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onLoadMore() {
			}
		});
		lv.setAdapter(new ListAdapter() {
			
			@Override
			public void unregisterDataSetObserver(DataSetObserver observer) {
				// TODO Auto-generated method stub

			}
			
			@Override
			public void registerDataSetObserver(DataSetObserver observer) {
				// TODO Auto-generated method stub

			}
			
			@Override
			public boolean isEmpty() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean hasStableIds() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public int getViewTypeCount() {
				// TODO Auto-generated method stub
				return 1;
			}
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				//�Ż�ListView  
				if(convertView==null){
					convertView=LayoutInflater.from(mContext).inflate(R.layout.list_item, null);  
					ImageView iv = (ImageView)convertView.findViewById(R.id.lv_item_img);
					TextView tv = (TextView)convertView.findViewById(R.id.lv_item_tv);
					loadBitmap();
					iv.setImageBitmap(bitmap);
					tv.setText("AAAAA");
				}
				return convertView;
			}
			
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return 100;
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
			public int getItemViewType(int position) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public boolean areAllItemsEnabled() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isEnabled(int position) {
				// TODO Auto-generated method stub
				return false;
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
}
