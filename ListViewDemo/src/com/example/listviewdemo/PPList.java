package com.example.listviewdemo;

import java.util.Date;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;  
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ListView;


public class PPList extends LinearLayout {  
	private PPListView mListView;
	private View mFirstRefreshView;
	private boolean mIsFirstRefreshing = true;

	public interface OnRefreshListener {  
		public void onRefresh();  
		public void onLoadMore();
	}

	public interface OnRemoveItemListener {  
		public void removeItem(int position);  
	}

	public PPList(Context context, AttributeSet attrs) {
		super(context, attrs);
		mFirstRefreshView = inflate(context, R.layout.first_refresh, null);
		mFirstRefreshView.setVisibility(VISIBLE);
		mListView = new PPListView(context, attrs);
		mListView.setVisibility(GONE);
		addView(mFirstRefreshView);
		addView(mListView);
	}

	public void setLoadMoreEnable(boolean bLoadMoreEnable) {
		mListView.setLoadMoreEnable(bLoadMoreEnable);
	}
	
	public void setRefreshEnable(boolean bRefreshEnable) {
		mListView.setRefreshEnable(bRefreshEnable);
	}

	public void setAdapter(BaseAdapter adapter) {
		mListView.setAdapter(adapter);
	}
	
	public void setOnRemoveItemListener(OnRemoveItemListener l) {
		mListView.setOnRemoveItemListener(l);
	}

	public void setOnRefreshListener(OnRefreshListener refreshListener) {  
		mListView.setOnRefreshListener(refreshListener);
	}  

	public void onRefreshSuccess() {  
		if (mIsFirstRefreshing) {
			mIsFirstRefreshing = false;
			mFirstRefreshView.setVisibility(GONE);
			mListView.setVisibility(VISIBLE);
			mFirstRefreshView = null;
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
		private final static int ITEM_DELETING = 5;

		private final static int RATIO = 3;  

		private LinearLayout mHeaderView; 
		private LinearLayout mFooterView; 
		private int mHeaderContentHeight;  
		private TextView mTipView;

		private boolean mIsBack;
		private boolean mLoadMoreEnable = true;
		private int mStartY;  
		private int mState;  
		private boolean mIsRecored;  
		private OnRefreshListener mRefreshListener;
		private OnRemoveItemListener mRemoveListener;
		private LayoutInflater mInflater;  
		private boolean mIsRefreshable;
		private boolean mRefreshEnable;

		private Handler mHandler = new Handler();

		private PPListView(Context context, AttributeSet attrs) {
			super(context, attrs);
			init(context);
		}

		public void setRefreshEnable(boolean bRefreshEnable) {
			mRefreshEnable = bRefreshEnable;
		}

		private void setOnRemoveItemListener(OnRemoveItemListener l) {
			mRemoveListener = l;
		}

		private void setLoadMoreEnable(boolean bLoadMoreEnable) {
			this.mLoadMoreEnable = bLoadMoreEnable;
			if (!bLoadMoreEnable && mFooterView != null) {
				removeFooterView(mFooterView);
				mFooterView = null;
			} else if (bLoadMoreEnable && mFooterView == null) {
				mFooterView = (LinearLayout)mInflater.inflate(R.layout.list_load_more, null);
				addFooterView(mFooterView);
			}
			invalidate();
		}

		private void init(Context context) {
			mInflater = LayoutInflater.from(context);
			mState = DONE;  
			mIsRefreshable = false;
			setOnScrollListener(this);
			setLoadMoreEnable(true);
			setRefreshEnable(false);
			mHeaderView = (LinearLayout) mInflater.inflate(R.layout.list_refresh, null);
			mTipView = (TextView) mHeaderView.findViewById(R.id.lv_refresh_tv);

			measureView(mHeaderView);
			mHeaderContentHeight = mHeaderView.getMeasuredHeight();  
			mHeaderView.setPadding(0, -1 * mHeaderContentHeight, 0, 0);  
			addHeaderView(mHeaderView, null, false);
			invalidate();
		}

		private void setOnRefreshListener(OnRefreshListener refreshListener) {  
			this.mRefreshListener = refreshListener;  
			mIsRefreshable = true;  
			if (refreshListener != null) {
				refreshListener.onRefresh();
			}
		}  

		@Override  
		public void onScroll(AbsListView view, int firstVisibleItem,  
				int visibleItemCount, int totalItemCount) {  
			if (firstVisibleItem == 0  && mRefreshListener != null) {  
				mIsRefreshable = true;  
			} else {
				mIsRefreshable = false;  
			}
		} 

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE  
					&& mState == DONE
					&& getLastVisiblePosition() == getCount() - 1) {
				onLvLoadMore();
			}
		}

		public void onRefreshSuccess() {  
			mState = DONE;   
			//mTipView.setText("最近更新:" + new Date().toLocaleString());  
			changeHeaderViewByState();  
			if (getLastVisiblePosition() == getCount() - 1 || getLastVisiblePosition() == -1) {
				onLvLoadMore();
			}
		} 

		@Override
		public boolean onTouchEvent(MotionEvent ev) {
			if (mState == ITEM_DELETING) {
				return true;
			}
			if (!mRefreshEnable) {
				return super.onTouchEvent(ev);
			}
			if (mIsRefreshable) {  
				switch (ev.getAction()) {  
				case MotionEvent.ACTION_DOWN:  
					if (!mIsRecored) {  
						mIsRecored = true;  
						mStartY = (int) ev.getY();
					}  
					break;  
				case MotionEvent.ACTION_UP:  
					if (mState != REFRESHING ) {  
						if (mState == PULL_To_REFRESH) {  
							mState = DONE;  
							changeHeaderViewByState();  
						}  
						if (mState == RELEASE_To_REFRESH) {  
							mState = REFRESHING;  
							changeHeaderViewByState();  
							onLvRefresh();  
						}  
					}  
					mIsRecored = false;  
					mIsBack = false;  

					break;  

				case MotionEvent.ACTION_MOVE:  
					int tempY = (int) ev.getY();  
					if (!mIsRecored) {  
						mIsRecored = true;  
						mStartY = tempY;  
					}  
					if (mState != REFRESHING && mIsRecored) {  
						if (mState == RELEASE_To_REFRESH) {  
							setSelection(0);  
							if (((tempY - mStartY) / RATIO < mHeaderContentHeight
									&& (tempY - mStartY) > 0)) {  
								mState = PULL_To_REFRESH;  
								changeHeaderViewByState();  
							}  
							else if (tempY - mStartY <= 0) {
								mState = DONE;  
								changeHeaderViewByState();  
							}  
						}  
						if (mState == PULL_To_REFRESH) {  
							setSelection(0);  
							if ((tempY - mStartY) / RATIO >= mHeaderContentHeight) {
								mState = RELEASE_To_REFRESH;  
								mIsBack = true;  
								changeHeaderViewByState();  
							}  
							else if (tempY - mStartY <= 0) { 
								mState = DONE;  
								changeHeaderViewByState();  
							}  
						}  
						if (mState == DONE) {  
							if (tempY - mStartY > 0) {  
								mState = PULL_To_REFRESH;  
								changeHeaderViewByState();  
							}  
						}  
						if (mState == PULL_To_REFRESH) {  
							mHeaderView.setPadding(0, -1 * mHeaderContentHeight  
									+ (tempY - mStartY) / RATIO, 0, 0);  

						}  
						if (mState == RELEASE_To_REFRESH) {  
							mHeaderView.setPadding(0, (tempY - mStartY) / RATIO  
									- mHeaderContentHeight, 0, 0);  
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
			if (mState == ITEM_DELETING || mRemoveListener == null) {
				return;
			}
			if (position >= 0 && position < getCount()) {
				mState = ITEM_DELETING;
				TranslateAnimation an = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0f, 
						Animation.RELATIVE_TO_PARENT, 1f,
						Animation.RELATIVE_TO_PARENT, 0f,
						Animation.RELATIVE_TO_PARENT, 0f);
				an.setDuration(500);

				final View view = getChildAt(position + 1 - getFirstVisiblePosition());

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
						view.setAlpha(0);
						shrinkItem(view ,position);
					}
				});
				view.startAnimation(an);
			}
		}

		private void shrinkItem(final View view, final int position) {
			final ViewGroup.LayoutParams lp = view.getLayoutParams();//获取item的布局参数  
			final int originalHeight = view.getHeight();//item的高度  
			ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0).setDuration(200);
			animator.addListener(new AnimatorListenerAdapter() {  
				@Override  
				public void onAnimationEnd(Animator animation) {  
					//这段代码很重要，因为我们并没有将item从ListView中移除，而是将item的高度设置为0  
					//所以我们在动画执行完毕之后将item设置回来  
					lp.height = originalHeight;  
					view.setLayoutParams(lp);  
					view.setAlpha(1);
					mRemoveListener.removeItem(position);
					mState = DONE;
				}  
			});  
			animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {  
				@Override  
				public void onAnimationUpdate(ValueAnimator valueAnimator) {  
					//这段代码的效果是ListView删除某item之后，其他的item向上滑动的效果  
					lp.height = (Integer) valueAnimator.getAnimatedValue();  
					view.setLayoutParams(lp);  
				}  
			});  
			animator.start();  
		}

		private void onLvRefresh() {  
			if (mRefreshListener != null) {  
				mRefreshListener.onRefresh();  
			}
		}  

		private void onLvLoadMore() {  
			if (mLoadMoreEnable) {
				mState = LOADING_MORE;

				if (mRefreshListener != null) {  
					mRefreshListener.onLoadMore();  
				}
			}
		}  

		private void changeHeaderViewByState() {  
			switch (mState) {  
			case RELEASE_To_REFRESH:  
				mTipView.setText("松开刷新");  
				break;  
			case PULL_To_REFRESH:  
				mTipView.setText("下拉刷新");  
				break;  

			case REFRESHING:  
				mHeaderView.setPadding(0, 0, 0, 0);  
				mTipView.setText("正在刷新...");  
				break;  
			case DONE:  
				mHeaderView.setPadding(0, -1 * mHeaderContentHeight, 0, 0);  
				mTipView.setText("下拉刷新");  
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
