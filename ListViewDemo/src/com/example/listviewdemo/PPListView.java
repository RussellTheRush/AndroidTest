package com.example.listviewdemo;

import java.util.Date;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;  
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ListView;


class PPList extends LinearLayout {

	private OnRefreshListener refreshListener;
	private OnRemoveItemListener removeListener;
	private LayoutInflater inflater;  
	private boolean bIsFirstRefresh = true;
	private boolean isRefreshable;  
	private PPListView mListView;
	View firstRefreshView;
	public interface OnRefreshListener {  
		public void onRefresh();  
		public void onLoadMore();
	}
	
	public interface OnRemoveItemListener {  
		public void removeItem(int position);  
	}
	
	public void setOnRemoveItemListener(OnRemoveItemListener l) {
		removeListener = l;
	}
	
	public PPList(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflater = LayoutInflater.from(context);
		firstRefreshView = inflate(context, R.layout.first_refresh, null);
		firstRefreshView.setVisibility(VISIBLE);
		mListView = new PPListView(context, attrs);
		mListView.setVisibility(GONE);
		addView(firstRefreshView);
		addView(mListView);
	}
	
	public void setAdapter(BaseAdapter adapter) {
		mListView.setAdapter(adapter);
	}
	
	public void setOnRefreshListener(OnRefreshListener refreshListener) {  
		this.refreshListener = refreshListener;  
		isRefreshable = true;  
		if (refreshListener != null) {
			refreshListener.onRefresh();
		}
	}  
	
	public void onRefreshSuccess() {  
		if (bIsFirstRefresh) {
			bIsFirstRefresh = false;
			firstRefreshView.setVisibility(GONE);
			mListView.setVisibility(VISIBLE);
			firstRefreshView = null;
		}
		mListView.onRefreshSuccess();
	} 
	
	public void removeItem(int position) {
		mListView.removeItem(position);
	}
	
	private class PPListView extends ListView implements OnScrollListener{

		private final static int RELEASE_To_REFRESH = 0;
		private final static int PULL_To_REFRESH = 1;
		private final static int REFRESHING = 2;
		private final static int DONE = 3;   
		private final static int LOADING_MORE = 4;

		private final static int RATIO = 3;  

		private LinearLayout headerView; 
		private LinearLayout footerView; 
		private int headerContentHeight;  
		private TextView tipView;
		
		private boolean isBack;
		private int startY;  
		private int state;  
		private int fstate;  
		private boolean isRecored;  
		
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
			Log.e("ListView", "footView:" + footerView + ",parent:" + footerView.getParent());

			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE  
					&& fstate != LOADING_MORE
					&& getLastVisiblePosition() == getCount() - 1) {
				onLvLoadMore();
				fstate = LOADING_MORE;  
			}  
		}

		public void onRefreshSuccess() {  
			state = DONE;  
			fstate = DONE;  
			tipView.setText("最近更新:" + new Date().toLocaleString());  
			changeHeaderViewByState();  
			//Log.w("RRR", "getLastVisiblePosition: " + getLastVisiblePosition() + " getCount: " + getCount());
			if (getLastVisiblePosition() == getCount() - 1 || getLastVisiblePosition() == -1) {
				onLvLoadMore();
				fstate = LOADING_MORE;
			}
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
					if (state != REFRESHING) {  
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
					if (state != REFRESHING && isRecored) {  
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
		
		private void removeItem(final int position) {
			if (position >= 0 && position < getCount()) {
				TranslateAnimation an = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0f, 
						Animation.RELATIVE_TO_PARENT, 1f,
						Animation.RELATIVE_TO_PARENT, 0f,
						Animation.RELATIVE_TO_PARENT, 0f);
				an.setDuration(1000);
				
				final View view = getChildAt(position + 1 - getFirstVisiblePosition());
				Log.w("RRR", "remore:" + (position + 1));
				an.setAnimationListener(new AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						view.clearAnimation();
						removeListener.removeItem(position);
						//nextItemPushUp(position + 1);
					}
				});
				view.startAnimation(an);
			}
		}

		private void nextItemPushUp(int position) {
			TranslateAnimation an = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, 
					Animation.RELATIVE_TO_SELF, 0f,
					Animation.RELATIVE_TO_SELF, 0f,
					Animation.RELATIVE_TO_SELF, -1f);
			an.setDuration(1000);
			final View view = getChildAt(position);
			view.startAnimation(an);
			
		}
		
		private void init(Context context) {
			
			headerView = (LinearLayout) inflater.inflate(R.layout.list_refresh, null);
			tipView = (TextView) headerView.findViewById(R.id.lv_refresh_tv);
			footerView = (LinearLayout)inflater.inflate(R.layout.list_load_more, null);
			addFooterView(footerView);
			measureView(headerView);
			headerContentHeight = headerView.getMeasuredHeight();  
			headerView.setPadding(0, -1 * headerContentHeight, 0, 0);  
			headerView.invalidate(); 
			addHeaderView(headerView, null, false);
			state = DONE;  
			fstate = DONE;  
			isRefreshable = false;
			setOnScrollListener(this);
		}

		private void onLvRefresh() {  
			if (refreshListener != null) {  
				refreshListener.onRefresh();  
			}
		}  

		private void onLvLoadMore() {  
			Log.w("RRR", "MORE");
			fstate = LOADING_MORE;

			if (refreshListener != null) {  
				refreshListener.onLoadMore();  
			}

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
}
