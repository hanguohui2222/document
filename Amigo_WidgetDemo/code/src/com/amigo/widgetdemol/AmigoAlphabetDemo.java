package com.amigo.widgetdemol;

import java.util.ArrayList;
import java.util.List;

import amigoui.app.AmigoActivity;
import amigoui.widget.AmigoAlphabetIndexView;
import amigoui.widget.AmigoListView;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class AmigoAlphabetDemo extends AmigoActivity {
	private AmigoAlphabetIndexView mAmigoAlphabetIndexView;
	private AmigoListView mListView;
	private BaseAdapter mListViewAdapter;
//	private String[] ALPHABET = new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
//			"M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

	private String[] ALPHABET = new String[] {"A", "B", "C", "F", "G", "H", "I", "K", "L", "M", "N", "P",
			"Q", "R", "S", "T", "U", "X", "Y", "Z"};

	private OnScrollListener mOnScrollListener = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if (mAmigoAlphabetIndexView == null || mAmigoAlphabetIndexView.getVisibility() == View.GONE) {
				return;
			}

			// 根据listview的滚动位置，更新字母表
			mAmigoAlphabetIndexView.invalidateShowingLetterIndex();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mView = LayoutInflater.from(this).inflate(R.layout.activity_alphabet_demo, null);
		setContentView(mView);
		initAdapter();
		initViews();
		repositionScrollbar();
	}
	private View mView;
	
	private void repositionScrollbar() {
        final int sbWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                /*getListView()*/mListView.getScrollBarSize(),
                getResources().getDisplayMetrics());
        final View parent = (View)mView.getParent();
        final int eat = Math.min(sbWidthPx, parent.getPaddingEnd());
        if (eat <= 0) return;
        Log.d("maxw", String.format("Eating %dpx into %dpx padding for %dpx scroll, ld=%d",
                eat, parent.getPaddingEnd(), sbWidthPx, /*getListView()*/mListView.getLayoutDirection()));
        parent.setPaddingRelative(parent.getPaddingStart(), parent.getPaddingTop(),
                parent.getPaddingEnd() - eat, parent.getPaddingBottom());
    }

	private void initAdapter() {
		List<String> names = new ArrayList<String>();
		for (String c : ALPHABET) {
			for (int i = 0; i < 2; i++) {
				names.add(c + i);
			}
		}
		mListViewAdapter = new MyAdapter(names);
	}

	private Handler myhanlder = new MyHanlder();
	class MyHanlder extends Handler{

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			mAmigoAlphabetIndexView.setList(mListView, mOnScrollListener);
		}
		
	}
	private void initViews() {
		mAmigoAlphabetIndexView = (AmigoAlphabetIndexView) findViewById(R.id.alphabet_indexer);
		mListView = (AmigoListView) findViewById(R.id.listview);
		mListView.setAdapter(mListViewAdapter); // 此adapter需要实现sectionIndexer接口
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				myhanlder.sendEmptyMessage(0);
			}
		}).start();
//		mAmigoAlphabetIndexView.setList(mListView, mOnScrollListener);
		mAmigoAlphabetIndexView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {

				}
				return false;
			}
		});
		// 如果adpater没有实现sectionIndexer接口，必须写一个sectionIndexer的实现类，并通过updateIndexer设置进去，如下
		// mAmigoAlphabetIndexView.updateIndexer(new MySectionIndexer());
	}

	class MyAdapter extends BaseAdapter implements SectionIndexer {
		private List<String> names;

		public MyAdapter(List<String> names) {
			this.names = names;
		}

		@Override
		public int getCount() {
			return names.size();
		}

		@Override
		public Object getItem(int position) {
			return names.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(AmigoAlphabetDemo.this).inflate(R.layout.listview_item,
						null);
			}

			TextView tv = ViewHolder.get(convertView, R.id.tv);
			tv.setText(names.get(position));

			return convertView;
		}

		@Override
		public Object[] getSections() {
			List<String> existLetters = new ArrayList<String>();
			for (String name : names) {
				String firstLetter = name.substring(0, 1);
				if (existLetters.contains(firstLetter)) {
					continue;
				}
				existLetters.add(firstLetter);
			}
			return existLetters.toArray();
			// return ALPHABET;
		}

		@Override
		public int getPositionForSection(int sectionIndex) {
			String section = (String) (getSections())[sectionIndex];
			int position = 0;
			for (String name : names) {
				String firstLetter = name.substring(0, 1);
				if (firstLetter.equalsIgnoreCase(section)) {
					return position;
				}
				position++;
			}
			return -1;
		}

		@Override
		public int getSectionForPosition(int position) {
			String name = names.get(position);
			Object[] alphabet = getSections();
			int i = 0;
			for (Object obj : alphabet) {
				String letter = (String) obj;
				if (letter.equalsIgnoreCase(name.substring(0, 1))) {
					return i;
				}
				i++;
			}
			return -1;
		}

	}

	public static class ViewHolder {
		@SuppressWarnings("unchecked")
		public static <T extends View> T get(View view, int id) {
			SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
			if (viewHolder == null) {
				viewHolder = new SparseArray<View>();
				view.setTag(viewHolder);
			}
			View childView = viewHolder.get(id);
			if (childView == null) {
				childView = view.findViewById(id);
				viewHolder.put(id, childView);
			}
			return (T) childView;
		}
	}

}
