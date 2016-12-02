package com.apicloud.moduleDemo;


import android.widget.RelativeLayout;

import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

public class ModuleScrollPicture extends UZModule {

	ScrollPictureView mInnerGroup;
	
	public ModuleScrollPicture(UZWebView webView) {
		super(webView);
	}
	
	
	public void jsmethod_open(final UZModuleContext moduleContext){
		if(null == mInnerGroup){
			mInnerGroup = new ScrollPictureView(mContext);
		}
		if(null == mInnerGroup.getParent()){
			int x = moduleContext.optInt("x");
			int y = moduleContext.optInt("y");
			int w = moduleContext.optInt("w");
			int h = moduleContext.optInt("h");
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(w, h);
			lp.leftMargin = x;
			lp.topMargin = y;
			String fixedOn = moduleContext.optString("fixedOn");
			boolean fixed = moduleContext.optBoolean("fixed", true);
			fixed = false;
			insertViewToCurWindow(mInnerGroup, lp, fixedOn, fixed, true);
		}
		mInnerGroup.initialize();
	}
	
	public void jsmethod_close(final UZModuleContext moduleContext){
		if(null != mInnerGroup){
			removeViewFromCurWindow(mInnerGroup);
		}
	}

}
