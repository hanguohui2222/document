package com.demo.preference;
//Gionee <lizhipeng> <2016-08-27> modify for CR01762654 

import com.amigo.widgetdemol.R;

import android.content.Context;
import android.preference.Preference;
import amigoui.preference.AmigoPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class GnCornerPreference extends AmigoPreference {
	
	private View mCorner;
	CornerInterface mInterface;

	public GnCornerPreference(Context context) {
		this(context, null);
	}

	public GnCornerPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GnCornerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setLayoutResource(R.layout.gn_corner_layout);
	}
	
	public void setCornerInterface(CornerInterface mInterface){
		this.mInterface=mInterface;
	}
	
	@Override
    protected void onBindView(View view) {
			super.onBindView(view);
//			mCorner=view.findViewById(R.id.corner);
			updateCorner();
	}
	 
	 
	 public void updateCorner(){
		 if(mCorner!=null&&mInterface!=null){
				boolean displayCorner=mInterface.updateCornerStatus();
				if(displayCorner){
					mCorner.setVisibility(View.VISIBLE);
				}else{
					mCorner.setVisibility(View.GONE);
				}
			}
	 }
	
	//角标显示的逻辑放到这 返回true显示  false不显示
	public interface CornerInterface {
		public boolean updateCornerStatus();
	}
}
