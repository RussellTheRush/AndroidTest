package com.example.hello;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.Toast;
/**
 * 
 * @Description:TODO

 * @author:who

 * @time:2014年4月22日 下午2:56:01
 */
public class MainActivity extends Activity {
	//	commit by xiaoluo third
	//  commit by laotan third
	//  commit by ziqi 
	//  commit by russell
	//	commit by xiaoluo luo 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toast.makeText(this, "Hello", 0);
        //AAAA
        //BBBB
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
