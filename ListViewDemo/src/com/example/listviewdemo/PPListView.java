package com.example.listviewdemo;

import java.util.Date;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;  
import android.widget.TextView;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

public class PPListView extends ListView implements OnScrollListener {
	private final static int RELEASE_To_REFRESH = 0;
    private final static int PULL_To_REFRESH = 1;
    private final static int REFRESHING = 2;
    private final static int DONE = 3;  
    private final static int LOADING = 4;  
    
    private final static int RATIO = 3;  
    
	private LayoutInflater inflater;  
	private LinearLayout headerView; 
    private int headerContentHeight;  
    private TextView tipView;
    private boolean isRefreshable;  
    private int startY;  
    private int state;  
    private boolean isBack;  
    private boolean isRecored;  
    private OnRefreshListener refreshListener;
    private Handler mHandler = new Handler();
    
    public PPListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
    public PPListView(Context context) {
		super(context);
		init(context);
	}
	public PPListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	public void setOnRefreshListener(OnRefreshListener refreshListener) {  
        this.refreshListener = refreshListener;  
        isRefreshable = true;  
    }  
	
    public interface OnRefreshListener {  
        public void onRefresh();  
        public void onLoadMore();
    }  
	
	@Override  
	public void onScroll(AbsListView view, int firstVisibleItem,  
			int visibleItemCount, int totalItemCount) {  
		if (firstVisibleItem == 0  && refreshListener != null) {  
			isRefreshable = true;  
		} else {  
			isRefreshable = false;  
		}     
	} 
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
	
	public void onRefreshSuccess() {  
        state = DONE;  
        tipView.setText("最近更新:" + new Date().toLocaleString());  
        changeHeaderViewByState();  
    } 
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		 if (isRefreshable) {  
	            switch (ev.getAction()) {  
	            case MotionEvent.ACTION_DOWN:  
	                if (!isRecored) {  
	                    isRecored = true;  
	                    startY = (int) ev.getY();
	                }  
	                break;  
	            case MotionEvent.ACTION_UP:  
	                if (state != REFRESHING && state != LOADING) {  
	                    if (state == PULL_To_REFRESH) {  
	                        state = DONE;  
	                        changeHeaderViewByState();  
	                    }  
	                    if (state == RELEASE_To_REFRESH) {  
	                        state = REFRESHING;  
	                        changeHeaderViewByState();  
	                        onLvRefresh();  
	                    }  
	                }  
	                isRecored = false;  
	                isBack = false;  
	  
	                break;  
	  
	            case MotionEvent.ACTION_MOVE:  
	                int tempY = (int) ev.getY();  
	                if (!isRecored) {  
	                    isRecored = true;  
	                    startY = tempY;  
	                }  
	                if (state != REFRESHING && isRecored && state != LOADING) {  
	                    if (state == RELEASE_To_REFRESH) {  
	                        setSelection(0);  
	                        if (((tempY - startY) / RATIO < headerContentHeight
	                                && (tempY - startY) > 0)) {  
	                            state = PULL_To_REFRESH;  
	                            changeHeaderViewByState();  
	                        }  
	                        else if (tempY - startY <= 0) {
	                            state = DONE;  
	                            changeHeaderViewByState();  
	                        }  
	                    }  
	                    if (state == PULL_To_REFRESH) {  
	                        setSelection(0);  
	                        if ((tempY - startY) / RATIO >= headerContentHeight) {
	                            state = RELEASE_To_REFRESH;  
	                            isBack = true;  
	                            changeHeaderViewByState();  
	                        }  
	                        else if (tempY - startY <= 0) { 
	                            state = DONE;  
	                            changeHeaderViewByState();  
	                        }  
	                    }  
	                    if (state == DONE) {  
	                        if (tempY - startY > 0) {  
	                            state = PULL_To_REFRESH;  
	                            changeHeaderViewByState();  
	                        }  
	                    }  
	                    if (state == PULL_To_REFRESH) {  
	                        headerView.setPadding(0, -1 * headerContentHeight  
	                                + (tempY - startY) / RATIO, 0, 0);  
	  
	                    }  
	                    if (state == RELEASE_To_REFRESH) {  
	                        headerView.setPadding(0, (tempY - startY) / RATIO  
	                                - headerContentHeight, 0, 0);  
	                    }  
	  
	                }  
	                break;  
	  
	            default:  
	                break;  
	            }  
	        }  
		return super.onTouchEvent(ev);
	}
	
	private void init(Context context) {
		inflater = LayoutInflater.from(context);
		headerView = (LinearLayout) inflater.inflate(R.layout.list_refresh, null);
		tipView = (TextView) headerView.findViewById(R.id.lv_refresh_tv);
		measureView(headerView);
		headerContentHeight = headerView.getMeasuredHeight();  
        headerView.setPadding(0, -1 * headerContentHeight, 0, 0);  
        headerView.invalidate(); 
        addHeaderView(headerView, null, false);
        state = DONE;  
        isRefreshable = false;
        setOnScrollListener(this);
	}
	
	private void onLvRefresh() {  
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				if (refreshListener != null) {  
					refreshListener.onRefresh();  
				}  
			}
		});
	}  

    private void changeHeaderViewByState() {  
        switch (state) {  
        case RELEASE_To_REFRESH:  
            tipView.setText("松开刷新");  
            break;  
        case PULL_To_REFRESH:  
            tipView.setText("下拉刷新");  
            break;  
  
        case REFRESHING:  
            headerView.setPadding(0, 0, 0, 0);  
            tipView.setText("正在刷新...");  
            break;  
        case DONE:  
            headerView.setPadding(0, -1 * headerContentHeight, 0, 0);  
            tipView.setText("下拉刷新");  
            break;  
        }  
    }
    
    private void measureView(View child) {
		ViewGroup.LayoutParams params = child.getLayoutParams();
		if (params == null) {  
            params = new ViewGroup.LayoutParams(  
                    ViewGroup.LayoutParams.MATCH_PARENT,  
                    ViewGroup.LayoutParams.WRAP_CONTENT);  
        }  
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0,  
                params.width);
		
		int lpHeight = params.height;  
        int childHeightSpec;  
        if (lpHeight > 0) {  
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,  
                    MeasureSpec.EXACTLY);  
        } else {  
            childHeightSpec = MeasureSpec.makeMeasureSpec(0,  
                    MeasureSpec.UNSPECIFIED);  
        }  
        child.measure(childWidthSpec, childHeightSpec);
	}

}
