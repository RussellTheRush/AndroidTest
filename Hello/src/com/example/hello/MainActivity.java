package com.example.hello;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
/**
 * 
 * @Description:TODO

 * @author:caican

 * @time:2014年4月22日 下午2:56:01
 */
public class MainActivity extends Activity {
	//	commit by xiaoluo
	//  commit by laotan
	//  commit by who ? you guess? test
	//  commit by i don't know who you r
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
