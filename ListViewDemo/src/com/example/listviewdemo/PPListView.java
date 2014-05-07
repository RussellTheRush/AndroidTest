package com.example.listviewdemo;

import java.util.Date;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PPListView extends ListView implements OnScrollListener {

	public final static int ITEM_TYPE_TITLE = 0;
	public final static int ITEM_TYPE_CONTENT = 1;
	public final static int ITEM_TYPE_COUNT = 2;

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
	private TextView mLastInfoView;
	private ImageView mArrowView;
	private ImageView mProgressBar;
	private Animation mArrowViewAnimationTo;
	private Animation mArrowViewAnimationBack;
	private Animation mLoadingAnimation;

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
	private int mPreloadFactor = -1;
	private View mCurTitleView;
	private boolean mIsFirstRefreshing = true;
	private boolean mShouldShowTopTitle;
	private PinnedSectionedHeaderAdapter mAdapter;
	private Handler mHandler = new Handler();
	private int mCurTitleOffset;
	private int mWidthMode;
	private int mHeightMode;
    private boolean mTitleViewDisapear = false;
	private int mCurrentSection;
    
    public static interface PinnedSectionedHeaderAdapter {
        public boolean isSectionHeader(int position);

        public int getSectionForPosition(int position);

        public View getSectionHeaderView(int section, View convertView, ViewGroup parent);

        public int getSectionHeaderViewType(int section);

        public int getCount();

    }

	public interface OnRefreshListener {
		public void onRefresh();
		public void onLoadMore();
	}

	public interface OnRemoveItemListener {
		public void removeItem(int position);
	}

	public PPListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		mState = DONE;
		mIsRefreshable = false;
		mInflater = LayoutInflater.from(context);

		setOnScrollListener(this);

		setLoadMoreEnable(true);
		setRefreshEnable(true);

		invalidate();
	}
	
	public void setShowTopTitle(boolean show) {
		mShouldShowTopTitle = show;
	}
	
	@Override
	public void setAdapter(ListAdapter adapter) {
		mAdapter = (PinnedSectionedHeaderAdapter)adapter;
		super.setAdapter(adapter);
	}

	private void setupHeaderView() {
		mHeaderView = (LinearLayout) mInflater.inflate(
				R.layout.pp_list_header, null);

		mTipView = (TextView) mHeaderView.findViewById(R.id.lv_refresh_tv);
		mLastInfoView = (TextView) mHeaderView
				.findViewById(R.id.lv_refresh_last_info_tv);
		mArrowView = (ImageView) mHeaderView
				.findViewById(R.id.lv_head_arrow_view);
		mArrowView.setVisibility(VISIBLE);
		mProgressBar = (ImageView) mHeaderView
				.findViewById(R.id.lv_head_progressBar);

		initRefreshAnimation();

		measureView(mHeaderView);
		mHeaderContentHeight = mHeaderView.getMeasuredHeight();
		mHeaderView.setPadding(0, -1 * mHeaderContentHeight, 0, 0);
		addHeaderView(mHeaderView, null, false);
	}

	private void destroyHeaderView() {
		removeHeaderView(mHeaderView);
		destroyRefreshAnimation();
		mProgressBar = null;
		mArrowView = null;
		mLastInfoView = null;
		mTipView = null;
		mHeaderView = null;
	}

	private void setupFooterView() {
		mFooterView = (LinearLayout) mInflater.inflate(
				R.layout.list_load_more, null);
		addFooterView(mFooterView);
	}
	
	private void destroyFooterView() {
		removeFooterView(mFooterView);
		mFooterView = null;
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
	}
	
	public void setPreloadFactor(int preloadFactor) {
		mPreloadFactor = preloadFactor;
	}
	
	public void setRefreshEnable(boolean bRefreshEnable) {
		mRefreshEnable = bRefreshEnable;

		if (mRefreshEnable && mHeaderView == null) {
			setupHeaderView();
		} else if (!mRefreshEnable && mHeaderView != null){
			destroyHeaderView();
		}
	}

	public void setOnRemoveItemListener(OnRemoveItemListener l) {
		mRemoveListener = l;
	}

	public void setLoadMoreEnable(boolean bLoadMoreEnable) {
		this.mLoadMoreEnable = bLoadMoreEnable;

		if (!bLoadMoreEnable && mFooterView != null) {
			destroyFooterView();
		} else if (bLoadMoreEnable && mFooterView == null) {
			setupFooterView();
		}
	}

	/**
	 * 初始化箭头旋转动画,加载动画
	 */
	private void initRefreshAnimation() {
		mArrowViewAnimationTo = new RotateAnimation(0f, -180f,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		mArrowViewAnimationTo.setDuration(346);
		mArrowViewAnimationTo.setFillAfter(true);
		mArrowViewAnimationBack = new RotateAnimation(-180f, 0f,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		mArrowViewAnimationBack.setDuration(346);
		mArrowViewAnimationBack.setFillAfter(true);

		// 加载转圈动画
		mLoadingAnimation = new RotateAnimation(0f, 360f,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		mLoadingAnimation.setDuration(850);
		mLoadingAnimation.setRepeatCount(Animation.INFINITE);
		mArrowViewAnimationBack.setFillAfter(true);
	}

	private void destroyRefreshAnimation() {
		mArrowViewAnimationTo = null;
		mLoadingAnimation = null;
		mArrowViewAnimationBack = null;
	}

	public void setOnRefreshListener(OnRefreshListener refreshListener) {
		this.mRefreshListener = refreshListener;
		mIsRefreshable = true;
		mState = REFRESHING;
		if (refreshListener != null) {
			refreshListener.onRefresh();
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mWidthMode = MeasureSpec.getMode(widthMeasureSpec);
		mHeightMode = MeasureSpec.getMode(heightMeasureSpec);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (firstVisibleItem == 0 && mRefreshListener != null) {
			mIsRefreshable = true;
		} else {
			mIsRefreshable = false;
		}
		if (mState == DONE
				&& mPreloadFactor != -1
				&& mLoadMoreEnable 
				&& getLastVisiblePosition() + getHeaderViewsCount() >= getCount()
				- mPreloadFactor) {
			onLvLoadMore();
		}
		
		
		if (!mShouldShowTopTitle || mAdapter == null || mAdapter.getCount() == 0 || (firstVisibleItem < getHeaderViewsCount())) {
			return;
		}
		
        firstVisibleItem -= getHeaderViewsCount();

        int section = mAdapter.getSectionForPosition(firstVisibleItem);
        int viewType = mAdapter.getSectionHeaderViewType(section);
        mCurTitleView = getSectionHeaderView(section, mCurTitleView);
        ensureTitleViewLayout();
        
        mCurTitleOffset = 0;

        for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
            if (mAdapter.isSectionHeader(i)) {
                View header = getChildAt(i - firstVisibleItem);
                int headerTop = header.getTop();
                int pinnedHeaderHeight = mCurTitleView.getMeasuredHeight();
                
                if (pinnedHeaderHeight >= headerTop && headerTop > 0) {
                	mCurTitleOffset = headerTop - header.getHeight();
                }
            }
        }
		
//		mCurTitleOffset = 0;
//        firstVisibleItem -= getHeaderViewsCount();
//
//		for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
//			if () {
//				View titleView = getChildAt(i - firstVisibleItem);	
//				int top = titleView.getTop();
//				int height = mCurTitleView == null ? titleView.getMeasuredHeight() : mCurTitleView.getMeasuredHeight();
//				if (top <= height && top > 0) {
//                    Log.w("RRR", "top: " + top + " height: " + height);
//					View oldView = mCurTitleView;
//					mCurTitleView = mAdapter.getView(i, oldView, this);
//					if (mCurTitleView != oldView) {
//						ensureTitleViewLayout();
//					}
//					mCurTitleOffset = top - height;
//				}
//			}
//		}
		invalidate();
	}
	
    private View getSectionHeaderView(int section, View oldView) {
        boolean shouldLayout = section != mCurrentSection || oldView == null;

        View view = mAdapter.getSectionHeaderView(section, oldView, this);
        if (shouldLayout) {
            // a new section, thus a new header. We should lay it out again
            mCurrentSection = section;
        }
        return view;
    }
	
    private void ensureTitleViewLayout() {
        if (mCurTitleView.isLayoutRequested()) {
            int widthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), mWidthMode);
            
            int heightSpec;
            ViewGroup.LayoutParams layoutParams = mCurTitleView.getLayoutParams();
            if (layoutParams != null && layoutParams.height > 0) {
                heightSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY);
            } else {
                heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            }
            mCurTitleView.measure(widthSpec, heightSpec);
            mCurTitleView.layout(0, 0, mCurTitleView.getMeasuredWidth(), mCurTitleView.getMeasuredHeight());
        }
    }
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mTitleViewDisapear || mCurTitleView == null || !mShouldShowTopTitle || mAdapter == null) {
			return;
		}
		int saveCount = canvas.save();
		canvas.translate(0, mCurTitleOffset);
		canvas.clipRect(0, 0, getWidth(), mCurTitleView.getMeasuredHeight());
		mCurTitleView.draw(canvas);
		canvas.restoreToCount(saveCount);
	}
	

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
				&& mState == DONE 
				&& getLastVisiblePosition() == getCount() - getHeaderViewsCount()) {
			onLvLoadMore();
		}
	}

	public void onRefreshSuccess() {
		if (mIsFirstRefreshing) {
			//showListView();
			mIsFirstRefreshing = false;
		}
		if (mRefreshEnable) {
			mState = DONE;
			mLastInfoView.setText("最后更新时间:" + new Date().toLocaleString());
			changeHeaderViewByState();
		}
//		if (mLoadMoreEnable && 
//				(getLastVisiblePosition() == getCount() - getHeaderViewsCount()
//				|| getLastVisiblePosition() == 0 - getHeaderViewsCount())) {
//			onLvLoadMore();
//		}
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
				if (mState != REFRESHING) {
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
						if (((tempY - mStartY) / RATIO < mHeaderContentHeight && (tempY - mStartY) > 0)) {
							mState = PULL_To_REFRESH;
							changeHeaderViewByState();
						} else if (tempY - mStartY <= 0) {
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
						} else if (tempY - mStartY <= 0) {
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
                    mTitleViewDisapear = true;
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

	public void removeItem(final int position) {
		if (mState == ITEM_DELETING || mRemoveListener == null) {
			return;
		}
		if (position >= 0 && position < getCount()) {
			mState = ITEM_DELETING;
			TranslateAnimation an = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, 0f,
					Animation.RELATIVE_TO_PARENT, 1f,
					Animation.RELATIVE_TO_PARENT, 0f,
					Animation.RELATIVE_TO_PARENT, 0f);
			an.setDuration(300);

			final View view = getChildAt(position + getHeaderViewsCount() - getFirstVisiblePosition());

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
					shrinkItem(view, position);
				}
			});
			view.startAnimation(an);
		}
	}

	private void shrinkItem(final View view, final int position) {
		final ViewGroup.LayoutParams lp = view.getLayoutParams();// 获取item的布局参数
		final int originalHeight = view.getHeight();// item的高度
		ShrinkAnimation animation = new ShrinkAnimation(view,
				originalHeight, 0);
		animation.setDuration(200);

		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				lp.height = originalHeight;
				view.setLayoutParams(lp);
				view.setAlpha(1);
				mRemoveListener.removeItem(position);
				mState = DONE;
			}
		});
		view.setAnimation(animation);
		animation.start();

		// final ViewGroup.LayoutParams lp = view.getLayoutParams();//
		// 获取item的布局参数
		// final int originalHeight = view.getHeight();// item的高度
		// ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0)
		// .setDuration(200);
		// animator.addListener(new AnimatorListenerAdapter() {
		// @Override
		// public void onAnimationEnd(Animator animation) {
		// // 这段代码很重要，因为我们并没有将item从ListView中移除，而是将item的高度设置为0
		// // 所以我们在动画执行完毕之后将item设置回来
		// lp.height = originalHeight;
		// view.setLayoutParams(lp);
		// view.setAlpha(1);
		// mRemoveListener.removeItem(position);
		// mState = DONE;
		// }
		// });
		// animator.addUpdateListener(new
		// ValueAnimator.AnimatorUpdateListener() {
		// @Override
		// public void onAnimationUpdate(ValueAnimator valueAnimator) {
		// // 这段代码的效果是ListView删除某item之后，其他的item向上滑动的效果
		// lp.height = (Integer) valueAnimator.getAnimatedValue();
		// view.setLayoutParams(lp);
		//
		// }
		// });
		// animator.start();
	}

	private void onLvRefresh() {
		if (mRefreshListener != null) {
			mRefreshListener.onRefresh();
		}
	}

	private void onLvLoadMore() {
		if (mLoadMoreEnable) {
			mState = LOADING_MORE;

			if (mRefreshListener != null ) {
				mRefreshListener.onLoadMore();
			}
		}
	}

	private void changeHeaderViewByState() {
		switch (mState) {
		case RELEASE_To_REFRESH:
			mProgressBar.setVisibility(View.GONE);
			mProgressBar.clearAnimation();
			mArrowView.setVisibility(View.VISIBLE);
			mArrowView.clearAnimation();
			mArrowView.startAnimation(mArrowViewAnimationTo);
			mTipView.setText("松开刷新");
			break;
		case PULL_To_REFRESH:
			mProgressBar.setVisibility(GONE);
			mProgressBar.clearAnimation();
			mArrowView.setVisibility(View.VISIBLE);
			mArrowView.clearAnimation();
			if (mIsBack) {
				mIsBack = false;
				mArrowView.startAnimation(mArrowViewAnimationBack);
				mTipView.setText("取消页面更新");
			} else {
				mTipView.setText("下拉刷新");
			}
			break;

		case REFRESHING:
			mArrowView.setVisibility(View.GONE);
			mArrowView.clearAnimation();
			mProgressBar.setVisibility(View.VISIBLE);
			mProgressBar.startAnimation(mLoadingAnimation);
			mTipView.setText("正在刷新");
			if (mHeaderView.getPaddingTop() > 0) {
				ValueAnimator animator = ValueAnimator.ofInt(
						mHeaderView.getPaddingTop(), 0).setDuration(342);
				animator.addUpdateListener(new AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						mHeaderView.setPadding(0,
								(Integer) animation.getAnimatedValue(), 0,
								0);
					}
				});
				animator.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						// 显示正在刷新
						mHeaderView.setPadding(0, 0, 0, 0);

						clearAnimation();
					}
				});
				animator.start();
			}
			break;
		case DONE:
			mArrowView.clearAnimation();
			mArrowView.setImageResource(R.drawable.pp_go_icon);
			if (mHeaderView.getPaddingTop() != -1 * mHeaderContentHeight) {
				final ViewGroup.LayoutParams lp2 = mHeaderView
						.getLayoutParams();
				int height2 = mHeaderView.getHeight();
				ValueAnimator animator2 = ValueAnimator.ofInt(
						mHeaderView.getPaddingTop(),
						-1 * mHeaderContentHeight).setDuration(342);
				System.out.println(height2 + "dd" + mHeaderContentHeight);
				animator2.addUpdateListener(new AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						mHeaderView.setPadding(0,
								(Integer) animation.getAnimatedValue(), 0,
								0);
					}
				});
				animator2.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {

						mHeaderView.setPadding(0,
								-1 * mHeaderContentHeight, 0, 0);
						clearAnimation();
                        mTitleViewDisapear = false;
						mTipView.setText("下拉刷新");
					}
				});
				animator2.start();
			}

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

	private class ShrinkAnimation extends Animation {
		private View mView;
		private ViewGroup.LayoutParams mLP;
		private AnimationListener mListener;
		private int mFrom;
		private int mTo;
		private boolean mStart = false;
		private boolean mEnd = false;

		public ShrinkAnimation(View view, int from, int to) {
			mView = view;
			mFrom = from;
			mTo = to;
			mLP = mView.getLayoutParams();
			mLP.height = from;
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			if ((interpolatedTime == 0f) && !mStart) {
				mStart = true;
				if (mListener != null) {
					mListener.onAnimationStart(this);
				}
			}

			if (mStart && !mEnd) {
				mLP.height = mFrom + (int) (interpolatedTime * (mTo - mFrom));
				mView.requestLayout();
			}

			if ((interpolatedTime != 1.0f) || mEnd) {
				return;
			}

			mEnd = true;
			if (mListener != null) {
				mListener.onAnimationEnd(this);
			}
		}

		@Override
		public void setAnimationListener(AnimationListener listener) {
			mListener = listener;
		}

	}
}
