package com.apicloud.avSdkControl.multi;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.apicloud.avSdkApp.AvSdkAppDelegate;
import com.apicloud.avSdkControl.MemberInfo;
import com.apicloud.avSdkControl.Util;
//import com.apicloud.pkg.sdk.R;
import com.tencent.av.opengl.GraphicRendererMgr;
import com.tencent.av.opengl.gesturedetectors.MoveGestureDetector;
import com.tencent.av.opengl.gesturedetectors.MoveGestureDetector.OnMoveGestureListener;
import com.tencent.av.opengl.ui.GLRootView;
import com.tencent.av.opengl.ui.GLView;
import com.tencent.av.opengl.ui.GLViewGroup;
import com.tencent.av.opengl.utils.Utils;
import com.tencent.av.sdk.AVConstants;
//import com.tencent.av.sdk.AVEndpoint;
//import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.av.utils.QLog;
import com.uzmap.pkg.uzcore.UZCoreUtil;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzkit.UZUtility;


public class AVUIControl extends GLViewGroup {
	static final String TAG = "VideoLayerUI";

	boolean mIsLocalHasVideo = false;// 自己是否有视频画面

	Context mContext = null;
	GraphicRendererMgr mGraphicRenderMgr = null;

	View mRootView = null;
	int mTopOffset = 0;
	int mBottomOffset = 0;

	GLRootView mGlRootView = null;
	GLVideoView mGlVideoView[] = null;

	int mClickTimes = 0;
	int mTargetIndex = -1;
	OnTouchListener mTouchListener = null;
	GestureDetector mGestureDetector = null;
	MoveGestureDetector mMoveDetector = null;
	ScaleGestureDetector mScaleGestureDetector = null;
	
	private int localViewIndex = -1;
	private int remoteViewIndex = -1;
	private String mRemoteIdentifier = "";

	private SurfaceView mSurfaceView = null;
	private SurfaceHolder.Callback mSurfaceHolderListener = new SurfaceHolder.Callback() {
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			mContext.sendBroadcast(new Intent(Util.ACTION_MULTI_SURFACE_CREATED));
			mCameraSurfaceCreated = true;

			QavsdkControlMulti qavsdk = AvSdkAppDelegate.getQavsdkControlMulti();
			qavsdk.getAVContext().setRenderMgrAndHolder(mGraphicRenderMgr, holder);
			Log.e("memoryLeak", "memoryLeak surfaceCreated");
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			if (holder.getSurface() == null) {
				return;
			}
			holder.setFixedSize(width, height);
			Log.e("memoryLeak", "memoryLeak surfaceChanged");				
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.e("memoryLeak", "memoryLeak surfaceDestroyed");			
		}
	};

	public AVUIControl(Context context, View rootView) {
		mContext = context;
		mRootView = rootView;
		mGraphicRenderMgr = new GraphicRendererMgr();
		initQQGlView();
		initCameraPreview();
	}

	@Override
	protected void onLayout(boolean flag, int left, int top, int right, int bottom) {
		if (QLog.isColorLevel()) {
			QLog.d(TAG, QLog.CLR, "onLayout|left: " + left + ", top: " + top + ", right: " + right + ", bottom: " + bottom);
		}
		layoutVideoView(false);
	}

	public void showGlView() {
		if (mGlRootView != null) {
			mGlRootView.setVisibility(View.VISIBLE);
		}
	}

	public void hideGlView() {
		if (mGlRootView != null) {
			mGlRootView.setVisibility(View.GONE);
		}
	}

	public void onResume() {
		if (mGlRootView != null) {
			mGlRootView.onResume();
		}

		setRotation(mCacheRotation);
	}

	public void onPause() {
		if (mGlRootView != null) {
			mGlRootView.onPause();
		}
	}

	public void onDestroy() {
		Log.e("memoryLeak", "memoryLeak AVUIControl onDestroy");		
		unInitCameraaPreview();
		mContext = null;
		mRootView = null;

		removeAllView();
		for (int i = 0; i < mGlVideoView.length; i++) {
			mGlVideoView[i].flush();
			mGlVideoView[i].clearRender();
			mGlVideoView[i] = null;
		}
		mGlRootView.setOnTouchListener(null);
		mGlRootView.setContentPane(null);

		mTouchListener = null;
		mGestureDetector = null;
		mMoveDetector = null;
		mScaleGestureDetector = null;

		mGraphicRenderMgr = null;

		mGlRootView = null;
		mGlVideoView = null;
	}

	public void enableDefaultRender() {
		QavsdkControlMulti qavsdk = AvSdkAppDelegate.getQavsdkControlMulti();
		//qavsdk.getAVContext().setRenderFunctionPtr(mGraphicRenderMgr.getRecvDecoderFrameFunctionptr()); 
	}
	
	public boolean setLocalHasVideo(boolean isLocalHasVideo, boolean forceToBigView, String identifier) {
		if (mContext == null)
			return false;

		if (Utils.getGLVersion(mContext) == 1) {
			return false;
		}
		
				
		if (isLocalHasVideo) {// 打开摄像头
			GLVideoView view = null;
			int index = getViewIndexById(identifier, AVConstants.VIDEO_SRC_CAMERA);		
			if (index < 0) {
				//lbbniu   index = getIdleViewIndex(0);				
				index = getIdleViewIndex(1);				
				if (index >= 0) {
					view = mGlVideoView[index];
					view.setRender(identifier, AVConstants.VIDEO_SRC_CAMERA);
					localViewIndex = index;
				}
			} else {
				view = mGlVideoView[index];
			}
			if (view != null) {
				view.setIsPC(false);
				view.enableLoading(false);
				// if (isFrontCamera()) {
				// view.setMirror(true);
				// } else {
				// view.setMirror(false);
				// }
				view.setVisibility(GLView.VISIBLE);
			}
			if (forceToBigView && index > 0) {
				// lbbniu
				//switchVideo(0, index);	
				
			}
		} else if (!isLocalHasVideo) {// 关闭摄像头
			int index = getViewIndexById(identifier, AVConstants.VIDEO_SRC_CAMERA);			
			if (index >= 0) {
				closeVideoView(index);
				localViewIndex = -1;
			}
		}
		mIsLocalHasVideo = isLocalHasVideo;

		return true;
	}

	public void setRemoteHasVideo(String identifier, int videoSrcType, boolean isRemoteHasVideo, boolean forceToBigView, boolean isPC) {
		boolean needForceBig = forceToBigView;
		if (mContext == null)
			return;
		if (Utils.getGLVersion(mContext) == 1) {
			isRemoteHasVideo = false;
			return;
		}
		if (!forceToBigView && !isLocalFront()) {
			forceToBigView = true;
		}
		Log.v("test","-------------lbbniu---------========"+identifier+"---------");		
		if (isRemoteHasVideo) {// 打开对方画面
			GLVideoView view = null;
			int index = getViewIndexById(identifier, videoSrcType);			
			if (index < 0) {
				//lbbniu   index = getIdleViewIndex(0);			
				index = getIdleViewIndex(1);			
				if (index >= 0) {
					view = mGlVideoView[index];
					view.setRender(identifier, videoSrcType);
					remoteViewIndex = index;
					mRemoteIdentifier = identifier;
				}
			} else {
				view = mGlVideoView[index];
			}
			if (view != null) {
				view.setIsPC(isPC);
				view.setMirror(false);
				if (needForceBig && (videoSrcType == AVConstants.VIDEO_SRC_PPT || videoSrcType == AVConstants.VIDEO_SRC_SHARESCREEN)) {
					view.enableLoading(false);
				} else {
					view.enableLoading(true);
				}
				view.setVisibility(GLView.VISIBLE);
			}
			if (forceToBigView && index > 0) {
				//lbbniu
				//switchVideo(0, index);
			}
		} else {// 关闭对方画面
			int index = getViewIndexById(identifier, videoSrcType);		
			if (index >= 0) {
				closeVideoView(index);
				remoteViewIndex = -1;
			}
		}
	}

	int mRotation = 0;
	int mCacheRotation = 0;

	public void setRotation(int rotation) {
		// 在没有画面前，ratation可能计算有误，此时的值如果设置下去会使用错误角度值，直到ratation大角度变化，新的值才能生效
		// 这种情况下把当前rotation缓存起来，等到RefreshUI时再设置
//		if (!mIsLocalHasVideo && !isRemoteHasVideo()) {
//			mCacheRotation = rotation;
//			return;
//		}
//
//		if (rotation == mRotation && rotation != mCacheRotation) {
//			return;
//		}
		if (mContext == null) {
			return;
		}

		if ((rotation % 90) != (mRotation % 90)) {
			mClickTimes = 0;
		}

		mRotation = rotation;
		mCacheRotation = rotation;
		
		// layoutVideoView(true);
		QavsdkControlMulti qavsdk = AvSdkAppDelegate.getQavsdkControlMulti();
		if ((qavsdk != null) && (qavsdk.getAVVideoControl() != null)) {		
			qavsdk.getAVVideoControl().setRotation(rotation);			
		}
		switch (rotation) {
		case 0:
			for (int i = 0; i < getChildCount(); i++) {
				GLView view = getChild(i);
				if(view != null)
					view.setRotation(0);
			}
			break;
		case 90:
			for (int i = 0; i < getChildCount(); i++) {
				GLView view = getChild(i);
				if(view != null)				
					view.setRotation(90);
			}
			break;
		case 180:
			for (int i = 0; i < getChildCount(); i++) {
				GLView view = getChild(i);
				if(view != null)				
					view.setRotation(180);
			}
			break;
		case 270:
			for (int i = 0; i < getChildCount(); i++) {
				GLView view = getChild(i);
				if(view != null)				
					view.setRotation(270);
			}
			break;
		default:
			break;
		}
	}
	public String getQualityTips() {
		QavsdkControlMulti qavsdk = AvSdkAppDelegate.getQavsdkControlMulti();
		String audioQos = "";
		String videoQos = "";
		String roomQos = "";
		
		if (qavsdk != null) {
			if (qavsdk.getAVAudioControl() != null) {
				audioQos = qavsdk.getAVAudioControl().getQualityTips();
			}
			if (qavsdk.getAVVideoControl() != null) {
				videoQos = qavsdk.getAVVideoControl().getQualityTips();
			}
			
			if (qavsdk.getAVVideoControl() != null) {
				roomQos = qavsdk.getRoom().getQualityTips();
			}
		}
		
		return audioQos + videoQos + roomQos;
	}
	public void setOffset(int topOffset, int bottomOffset) {
		if (QLog.isColorLevel()) {
			QLog.d(TAG, QLog.CLR, "setOffset topOffset: " + topOffset + ", bottomOffset: " + bottomOffset);
		}
		mTopOffset = topOffset;
		mBottomOffset = bottomOffset;
		// refreshUI();
		layoutVideoView(true);
	}

	public void setText(String identifier, int videoSrcType, String text, float textSize, int color) {
		int index = getViewIndexById(identifier, videoSrcType);
		if (index < 0) {
			index = getIdleViewIndex(0);
			if (index >= 0) {
				GLVideoView view = mGlVideoView[index];
				view.setRender(identifier, videoSrcType);
			}
		}
		if (index >= 0) {
			GLVideoView view = mGlVideoView[index];
			view.setVisibility(GLView.VISIBLE);
			view.setText(text, textSize, color);
		}
		if (QLog.isColorLevel()) {
			QLog.d(TAG, QLog.CLR, "setText identifier: " + identifier + ", videoSrcType: " + videoSrcType + ", text: " + text + ", textSize: " + textSize + ", color: " + color + ", index: " + index);
		}
	}

	public void setBackground(String identifier, int videoSrcType, Bitmap bitmap, boolean needRenderVideo) {
		int index = getViewIndexById(identifier, videoSrcType);
		if (index < 0) {
			index = getIdleViewIndex(0);
			if (index >= 0) {
				GLVideoView view = mGlVideoView[index];
				view.setVisibility(GLView.VISIBLE);
				view.setRender(identifier, videoSrcType);
			}
		}
		if (index >= 0) {
			GLVideoView view = mGlVideoView[index];
			view.setBackground(bitmap);
			view.setNeedRenderVideo(needRenderVideo);
			if (!needRenderVideo) {
				view.enableLoading(false);
			}
		}
		if (QLog.isColorLevel()) {
			QLog.d(TAG, QLog.CLR, "setBackground identifier: " + identifier + ", videoSrcType: " + videoSrcType + ", index: " + index + ", needRenderVideo: " + needRenderVideo);
		}
	}

	public void onVideoSrcTypeChanged(String identifier, int oldVideoSrcType, int newVideoSrcType) {
		int index = getViewIndexById(identifier, oldVideoSrcType);
		if (index >= 0) {
			GLVideoView view = mGlVideoView[index];
			view.clearRender();
			view.setRender(identifier, newVideoSrcType);
			if (index == 0 && (newVideoSrcType == AVConstants.VIDEO_SRC_PPT || newVideoSrcType == AVConstants.VIDEO_SRC_SHARESCREEN)) {
				view.enableLoading(false);
			} else {
				view.enableLoading(true);
			}
		}
		if (QLog.isColorLevel()) {
			QLog.d(TAG, QLog.CLR, "onVideoSrcTypeChanged identifier: " + identifier + ", oldVideoSrcType: " + oldVideoSrcType + ", newVideoSrcType: " + newVideoSrcType + ", index: " + index);
		}
	}

	boolean isLocalFront() {
		boolean isLocalFront = true;
		String selfIdentifier = "";
		GLVideoView view = mGlVideoView[0];
		if (view.getVisibility() == GLView.VISIBLE && selfIdentifier.equals(view.getIdentifier())) {
			isLocalFront = false;
		}
		return isLocalFront;
	}

	boolean isLocalHasVideo(String selfIdentifier) {
		boolean isLocalHasVideo = false;
		for (int i = 0; i < mGlVideoView.length; i++) {
			GLVideoView view = mGlVideoView[i];
			if (view.getVisibility() == GLView.VISIBLE && selfIdentifier.equals(view.getIdentifier())) {
				isLocalHasVideo = true;
				break;
			}
		}
		return isLocalHasVideo;
	}

	boolean isRemoteHasVideo() {
		boolean isRemoteHasVideo = false;
		String selfIdentifier = "";
		for (int i = 0; i < mGlVideoView.length; i++) {
			GLVideoView view = mGlVideoView[i];
			if (view.getVisibility() == GLView.VISIBLE && !selfIdentifier.equals(view.getIdentifier())) {
				isRemoteHasVideo = true;
				break;
			}
		}
		return isRemoteHasVideo;
	}

	int getViewCount() {
		int count = 0;
		for (int i = 0; i < mGlVideoView.length; i++) {
			GLVideoView view = mGlVideoView[i];
			if (view.getVisibility() == GLView.VISIBLE && null != view.getIdentifier()) {
				count++;
			}
		}
		return count;
	}

	int getIdleViewIndex(int start) {
		int index = -1;
		for (int i = start; i < mGlVideoView.length; i++) {
			GLVideoView view = mGlVideoView[i];
			if ((null == view.getIdentifier() || view.getVisibility() == GLView.INVISIBLE)&&i!=AvSdkAppDelegate.index) {//lbbniu
				index = i;
				break;
			}
		}
		return index;
	}

	int getViewIndexById(String identifier, int videoSrcType) {
		int index = -1;
		if (null == identifier) {
			return index;
		}
		for (int i = 0; i < mGlVideoView.length; i++) {
			GLVideoView view = mGlVideoView[i];
			if ((identifier.equals(view.getIdentifier()) && view.getVideoSrcType() == videoSrcType) && view.getVisibility() == GLView.VISIBLE) {
				index = i;
				break;
			}
		}
		return index;
	}

	void layoutVideoView(boolean virtical) {
		if (QLog.isColorLevel()) {
			QLog.d(TAG, QLog.CLR, "layoutVideoView virtical: " + virtical);
		}
		if (mContext == null)
			return;

		int width = getWidth();
		int height = getHeight();
		mGlVideoView[0].layout(0, 0, width, height);
		mGlVideoView[0].setBackgroundColor(Color.BLACK);
		//mGlVideoView[0].setBackgroundColor(Color.TRANSPARENT);
		//
		int edgeX = mContext.getResources().getDimensionPixelSize(UZResourcesIDFinder.getResDimenID("video_small_view_offsetX"));
		int edgeY = edgeX;
		if (mBottomOffset != 0) {
			edgeY = mContext.getResources().getDimensionPixelSize(UZResourcesIDFinder.getResDimenID("video_small_view_offsetY"));
		}
		/**
	    final int w = (width - edgeX * 2) / 4;
		final int h = w;
		//
		int left = 0;
		int right = 0;
		int top = height - h - edgeY - mBottomOffset;
		int bottom = height - edgeY - mBottomOffset;
		if (virtical) {
			left = mGlVideoView[4].getBounds().left;
			right = mGlVideoView[4].getBounds().right;
		} else {
			left = width - w - edgeX;
			right = width - edgeX;
		}
		mGlVideoView[4].layout(left, top, right, bottom);
		if (virtical) {
			left = mGlVideoView[3].getBounds().left;
			right = mGlVideoView[3].getBounds().right;
		} else {
			right = left;
			left = right - w;
		}
		mGlVideoView[3].layout(left, top, right, bottom);
		if (virtical) {
			left = mGlVideoView[2].getBounds().left;
			right = mGlVideoView[2].getBounds().right;
		} else {
			right = left;
			left = right - w;
		}
		mGlVideoView[2].layout(left, top, right, bottom);
		*/
		//UZUtility.dipToPix(10)
		//lbbniu  设置各个视频的位置
		//final int w = (width - 25) / 4;
		final int w = (width - UZUtility.dipToPix(10)) / 4-UZUtility.dipToPix(10);
		final int h = (int)(w*1.25);
		
		
		
		//
		int left = 0;
		int right = 0;
		edgeY = 0;
		int top = height - h - edgeY - UZUtility.dipToPix(10);//mBottomOffset;
		int bottom = height - edgeY - UZUtility.dipToPix(10);//mBottomOffset;
		
		if (virtical) {
			left = mGlVideoView[4].getBounds().left;
			right = mGlVideoView[4].getBounds().right;
		} else {
			//left = width - w;
			//right = width - 5;
			//new
			left = w*3 + UZUtility.dipToPix(40);
			right = left + w;
		}
		
		mGlVideoView[4].layout(left, top, right, bottom);
		Log.v("lbbniu", "------------"+left+"-----"+top+"----------"+right+"-----------"+bottom+"------------");
		if (virtical) {
			left = mGlVideoView[3].getBounds().left;
			right = mGlVideoView[3].getBounds().right;
		} else {
			//right = left-5;
			//left = right - w;
			//new
			left = w*2 + UZUtility.dipToPix(30);
			right = left + w;
		}
		mGlVideoView[3].layout(left, top, right, bottom);
		if (virtical) {
			left = mGlVideoView[2].getBounds().left;
			right = mGlVideoView[2].getBounds().right;
		} else {
			//right = left-5;
			//left = right - w;
			//new
			left = w + UZUtility.dipToPix(20);
			right = left + w;
		}
		mGlVideoView[2].layout(left, top, right, bottom);
		
		if (virtical) {
			left = mGlVideoView[2].getBounds().left;
			right = mGlVideoView[2].getBounds().right;
		} else {
			//right = left-5;
			//left = right - w;
			//new
			left = UZUtility.dipToPix(10);
			right = left + w;
		}
		mGlVideoView[1].layout(left, top, right, bottom);
		
		mGlVideoView[0].layout(0, 0, width, top-edgeY-UZUtility.dipToPix(10));
		/* lbbniu
		int wRemote = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_width);
		int hRemote = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_height);
		int edgeXRemote = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetX);
		int edgeYRemote = mContext.getResources().getDimensionPixelSize(R.dimen.video_small_view_offsetY);
		left = edgeXRemote;
		right = left + wRemote;
		top = edgeYRemote + mTopOffset;
		bottom = top + hRemote;
		
		mGlVideoView[1].layout(left, top, right, bottom);
		*/	

		mGlVideoView[1].setBackgroundColor(0xFF111111);
		mGlVideoView[2].setBackgroundColor(0xFF111111);
		mGlVideoView[3].setBackgroundColor(0xFF111111);
		mGlVideoView[4].setBackgroundColor(0xFF111111);
		
		mGlVideoView[1].setPaddings(2, 3, 3, 3);
		mGlVideoView[2].setPaddings(2, 3, 2, 3);
		mGlVideoView[3].setPaddings(2, 3, 2, 3);
		mGlVideoView[4].setPaddings(3, 3, 2, 3);
		invalidate();
	}

	void closeVideoView(int index) {
		if (QLog.isColorLevel()) {
			QLog.d(TAG, QLog.CLR, "closeVideoView index: " + index);
		}

	
		GLVideoView view = mGlVideoView[index];
		//view.setVisibility(GLView.INVISIBLE); //lbbniu
		view.setNeedRenderVideo(true);
		view.enableLoading(false);
		view.setIsPC(false);
		view.clearRender();

//		for (int i = 0; i < mGlVideoView.length - 1; i++) {
//			GLVideoView view1 = mGlVideoView[i];
//			for (int j = i + 1; j < mGlVideoView.length; j++) {
//				GLVideoView view2 = mGlVideoView[j];
//				if (view1.getVisibility() == GLView.INVISIBLE && view2.getVisibility() == GLView.VISIBLE) {
//					String openId = view2.getOpenId();
//					int videoSrcType = view2.getVideoSrcType();
//					boolean isPC = view2.isPC();
//					boolean isMirror = view2.isMirror();
//					boolean isLoading = view2.isLoading();
//					view1.setRender(openId, videoSrcType);
//					view1.setIsPC(isPC);
//					view1.setMirror(isMirror);
//					view1.enableLoading(isLoading);
//					view1.setVisibility(GLView.VISIBLE);
//					view2.setVisibility(GLView.INVISIBLE);
//				}
//			}
//		}

		layoutVideoView(false);
	}

	void initQQGlView() {
		if (QLog.isColorLevel()) {
			QLog.d(TAG, QLog.CLR, "initQQGlView");
		}
		this.setBackgroundColor(Color.BLACK);
		mRootView.setBackgroundColor(Color.BLACK);
		mGlRootView = (GLRootView) mRootView.findViewById(UZResourcesIDFinder.getResIdID("av_video_glview"));
		mGlVideoView = new GLVideoView[5];
		// for (int i = 0; i < mGlVideoView.length; i++) {
		// mGlVideoView[i] = new GLVideoView(mVideoController, mContext.getApplicationContext());
		// mGlVideoView[i].setVisibility(GLView.INVISIBLE);
		// addView(mGlVideoView[i]);
		// }
		mGlVideoView[0] = new GLVideoView(mContext.getApplicationContext(), mGraphicRenderMgr);
		//mGlVideoView[0].setVisibility(GLView.INVISIBLE);  //lbbniu
		addView(mGlVideoView[0]);
		for (int i = 4; i >= 1; i--) {
			mGlVideoView[i] = new GLVideoView(mContext.getApplicationContext(), mGraphicRenderMgr);
			//mGlVideoView[i].setVisibility(GLView.INVISIBLE);  //lbbniu
			addView(mGlVideoView[i]);
		}
		mGlRootView.setContentPane(this);
		// set bitmap ,reuse the backgroud BitmapDrawable,mlzhong
		// setBackground(UITools.getBitmapFromResourceId(mContext, R.drawable.qav_gaudio_bg));

		mScaleGestureDetector = new ScaleGestureDetector(mContext, new ScaleGestureListener());
		mGestureDetector = new GestureDetector(mContext, new GestureListener());
		mMoveDetector = new MoveGestureDetector(mContext, new MoveListener());
		mTouchListener = new TouchListener();
		setOnTouchListener(mTouchListener);
	}

	boolean mCameraSurfaceCreated = false;

	void initCameraPreview() {
		
//		SurfaceView localVideo = (SurfaceView) mRootView.findViewById(R.id.av_video_surfaceView);
//		SurfaceHolder holder = localVideo.getHolder();
//		holder.addCallback(mSurfaceHolderListener);
//		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 3.0以下必须在初始化时调用，否则不能启动预览
//		localVideo.setZOrderMediaOverlay(true);
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.width = 1;
        layoutParams.height = 1;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        // layoutParams.flags |= LayoutParams.FLAG_NOT_TOUCHABLE;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.windowAnimations = 0;// android.R.style.Animation_Toast;
        layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        //layoutParams.setTitle("Toast");
        try {
        	mSurfaceView = new SurfaceView(mContext);
            SurfaceHolder holder = mSurfaceView.getHolder();
            holder.addCallback(mSurfaceHolderListener);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 3.0以下必须在初始化时调用，否则不能启动预览
            mSurfaceView.setZOrderMediaOverlay(true);
            windowManager.addView(mSurfaceView, layoutParams);
        } catch (IllegalStateException e) {
            windowManager.updateViewLayout(mSurfaceView, layoutParams);
            if (QLog.isColorLevel()) {
                QLog.d(TAG, QLog.CLR, "add camera surface view fail: IllegalStateException." + e);
            }
        } catch (Exception e) {
            if (QLog.isColorLevel()) {
                QLog.d(TAG, QLog.CLR, "add camera surface view fail." + e);
            }
        }		
		Log.e("memoryLeak", "memoryLeak initCameraPreview");
	}
	
	void unInitCameraaPreview() {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        try {
            windowManager.removeView(mSurfaceView);
            mSurfaceView = null;
        } catch(Exception e) {
            if (QLog.isColorLevel()) {
                QLog.e(TAG, QLog.CLR, "remove camera view fail.", e);
            }
        }
	}

	void switchVideo(int index1, int index2) {
		if (QLog.isColorLevel()) {
			QLog.d(TAG, QLog.CLR, "switchVideo index1: " + index1 + ", index2: " + index2);
		}
		if (index1 == index2 || index1 < 0 || index1 >= mGlVideoView.length || index2 < 0 || index2 >= mGlVideoView.length) {
			return;
		}

		String identifier1 = mGlVideoView[index1].getIdentifier();
		int videoSrcType1 = mGlVideoView[index1].getVideoSrcType();
		boolean isPC1 = mGlVideoView[index1].isPC();
		boolean isMirror1 = mGlVideoView[index1].isMirror();
		boolean isLoading1 = mGlVideoView[index1].isLoading();
		String identifier2 = mGlVideoView[index2].getIdentifier();
		int videoSrcType2 = mGlVideoView[index2].getVideoSrcType();
		boolean isPC2 = mGlVideoView[index2].isPC();
		boolean isMirror2 = mGlVideoView[index2].isMirror();
		boolean isLoading2 = mGlVideoView[index2].isLoading();

		mGlVideoView[index1].setRender(identifier2, videoSrcType2);
		mGlVideoView[index1].setIsPC(isPC2);
		mGlVideoView[index1].setMirror(isMirror2);
		mGlVideoView[index1].enableLoading(isLoading2);
		mGlVideoView[index2].setRender(identifier1, videoSrcType1);
		mGlVideoView[index2].setIsPC(isPC1);
		mGlVideoView[index2].setMirror(isMirror1);
		mGlVideoView[index2].enableLoading(isLoading1);
		
		int temp = localViewIndex;
		localViewIndex = remoteViewIndex;
		remoteViewIndex = temp;
	}
	void switchVideo( int index2) {
		int index1 = 0;
		if (QLog.isColorLevel()) {
			QLog.d(TAG, QLog.CLR, "switchVideo index1: " + index1 + ", index2: " + index2);
		}
		
		if (index1 == index2 || index1 < 0 || index1 >= mGlVideoView.length || index2 < 0 || index2 >= mGlVideoView.length) {
			return;
		}
		
		GLVideoView view = mGlVideoView[index1];
		if (view == null)
			return;
		String viewIdentifier = view.getIdentifier();
		QavsdkControlMulti qavsdk = AvSdkAppDelegate.getQavsdkControlMulti();
		ArrayList<MemberInfo> memberList = qavsdk.getMemberList();
		if (TextUtils.isEmpty(viewIdentifier) || memberList.size()==4)
			return;
		
		GLVideoView view2 = mGlVideoView[index2];
		if(!TextUtils.isEmpty(view2.getIdentifier())){
			for (int i = 4; i >= 1; i--) {
				view2 = mGlVideoView[i];
				if(TextUtils.isEmpty(view2.getIdentifier())){
					index2 = i;
					break;
				}
			}
		}
		
		String identifier1 = mGlVideoView[index1].getIdentifier();
		int videoSrcType1 = mGlVideoView[index1].getVideoSrcType();
		boolean isPC1 = mGlVideoView[index1].isPC();
		boolean isMirror1 = mGlVideoView[index1].isMirror();
		boolean isLoading1 = mGlVideoView[index1].isLoading();
		String identifier2 = mGlVideoView[index2].getIdentifier();
		int videoSrcType2 = mGlVideoView[index2].getVideoSrcType();
		boolean isPC2 = mGlVideoView[index2].isPC();
		boolean isMirror2 = mGlVideoView[index2].isMirror();
		boolean isLoading2 = mGlVideoView[index2].isLoading();

		mGlVideoView[index1].setRender(identifier2, videoSrcType2);
		mGlVideoView[index1].setIsPC(isPC2);
		mGlVideoView[index1].setMirror(isMirror2);
		mGlVideoView[index1].enableLoading(isLoading2);
		mGlVideoView[index2].setRender(identifier1, videoSrcType1);
		mGlVideoView[index2].setIsPC(isPC1);
		mGlVideoView[index2].setMirror(isMirror1);
		mGlVideoView[index2].enableLoading(isLoading1);
		
		int temp = localViewIndex;
		localViewIndex = remoteViewIndex;
		remoteViewIndex = temp;
	}

	class Position {
		final static int CENTER = 0;
		final static int LEFT_TOP = 1;
		final static int RIGHT_TOP = 2;
		final static int RIGHT_BOTTOM = 3;
		final static int LEFT_BOTTOM = 4;
	}

	public void setSmallVideoViewLayout(boolean isRemoteHasVideo, String remoteIdentifier) {
		if (QLog.isColorLevel()) {
			QLog.d(TAG, QLog.CLR, "setSmallVideoViewLayout position: " + mPosition);
		}
		if (mContext == null) {
			return;
		}
		
		int left = 0;
		int top = 0;
		int right = 0;
		int bottom = 0;
		
		int width = getWidth();
		int height = getHeight();
		
		int w = mContext.getResources().getDimensionPixelSize(UZResourcesIDFinder.getResDimenID("video_small_view_width"));
		int h = mContext.getResources().getDimensionPixelSize(UZResourcesIDFinder.getResDimenID("video_small_view_height"));
		int edgeX = mContext.getResources().getDimensionPixelSize(UZResourcesIDFinder.getResDimenID("video_small_view_offsetX"));
		int edgeY = mContext.getResources().getDimensionPixelSize(UZResourcesIDFinder.getResDimenID("video_small_view_offsetY"));
		if (mBottomOffset == 0) {
			edgeY = edgeX;
		}

		switch (mPosition) {
		case Position.LEFT_TOP:
			left = edgeX;
			right = left + w;
			// if (mBottomOffset != 0) {
			// top = height - h - edgeY - mBottomOffset;
			// bottom = top + h;
			// } else {
			top = edgeY + mTopOffset;
			bottom = top + h;
			// }
			break;
		case Position.RIGHT_TOP:
			left = width - w - edgeX;
			right = left + w;
			// if (mBottomOffset != 0) {
			// top = height - h - edgeY - mBottomOffset;
			// bottom = top + h;
			// } else {
			top = edgeY + mTopOffset;
			bottom = top + h;
			// }
			break;
		case Position.LEFT_BOTTOM:
			left = edgeX;
			right = left + w;
			top = height - h - edgeY - mBottomOffset;
			bottom = top + h;
			break;
		case Position.RIGHT_BOTTOM:
			left = width - w - edgeX;
			top = height - h - edgeY - mBottomOffset;
			right = left + w;
			bottom = top + h;
			break;
		}
		
		
		if (isRemoteHasVideo) {// 打开摄像头
			GLVideoView view = null;
			mRemoteIdentifier = remoteIdentifier;
			int index = getViewIndexById(remoteIdentifier, AVConstants.VIDEO_SRC_CAMERA);	
			
			//请求多路画面用这个测试
//			if (remoteViewIndex != -1 && !mRemoteIdentifier.equals("") && !mRemoteIdentifier.equals(remoteIdentifier)) {
//				closeVideoView(remoteViewIndex);
//			}
			if (remoteViewIndex != -1) {
				closeVideoView(remoteViewIndex);
			}
			if (index < 0) {
				index = getIdleViewIndex(0);					
				if (index >= 0) {
					view = mGlVideoView[index];
					view.setRender(remoteIdentifier, AVConstants.VIDEO_SRC_CAMERA);
					remoteViewIndex = index;
				}
			} else {
				view = mGlVideoView[index];
			}
			if (view != null) {
				view.setIsPC(false);
				view.enableLoading(false);
				view.setVisibility(GLView.VISIBLE);
			}

		} else {// 关闭摄像头
			int index = getViewIndexById(remoteIdentifier, AVConstants.VIDEO_SRC_CAMERA);			
			if (index >= 0) {
				closeVideoView(index);
				remoteViewIndex = -1;
			}
		}		
		
		
//		if (null != mGlVideoView[1].getOpenId()) {
//			mGlVideoView[1].clearRender();
//		}
//		
//				
//		mGlVideoView[1].layout(left, top, right, bottom);
//		mGlVideoView[1].setRender(remoteOpenid, AVConstants.VIDEO_SRC_CAMERA);
//		mGlVideoView[1].setIsPC(false);
//		mGlVideoView[1].enableLoading(false);	
//		mGlVideoView[1].setVisibility(View.VISIBLE);
	}

	int mPosition = Position.LEFT_TOP;
	boolean mDragMoving = false;

	public int getPosition() {
		return mPosition;
	}

	void checkAndChangeMargin(int deltaX, int deltaY) {
		if (mContext == null) {
			return;
		}
		int width = mContext.getResources().getDimensionPixelSize(UZResourcesIDFinder.getResDimenID("video_small_view_width"));
		int height = mContext.getResources().getDimensionPixelSize(UZResourcesIDFinder.getResDimenID("video_small_view_height"));

		Rect outRect = getBounds();
		int minOffsetX = 0;
		int minOffsetY = 0;
		int maxOffsetX = outRect.width() - width;
		int maxOffsetY = outRect.height() - height;

		int left = mGlVideoView[1].getBounds().left + deltaX;
		int top = mGlVideoView[1].getBounds().top + deltaY;
		if (left < minOffsetX) {
			left = minOffsetX;
		} else if (left > maxOffsetX) {
			left = maxOffsetX;
		}
		if (top < minOffsetY) {
			top = minOffsetY;
		} else if (top > maxOffsetY) {
			top = maxOffsetY;
		}
		int right = left + width;
		int bottom = top + height;
		mGlVideoView[1].layout(left, top, right, bottom);
	}

	int getSmallViewPosition() {
		int position = Position.CENTER;
		Rect visableRect = getBounds();
		int screenCenterX = visableRect.centerX();
		int screenCenterY = visableRect.centerY();
		int viewCenterX = mGlVideoView[1].getBounds().centerX();
		int viewCenterY = mGlVideoView[1].getBounds().centerY();
		if (viewCenterX < screenCenterX && viewCenterY < screenCenterY) {
			position = Position.LEFT_TOP;
		} else if (viewCenterX < screenCenterX && viewCenterY > screenCenterY) {
			position = Position.LEFT_BOTTOM;
		} else if (viewCenterX > screenCenterX && viewCenterY < screenCenterY) {
			position = Position.RIGHT_TOP;
		} else if (viewCenterX > screenCenterX && viewCenterY > screenCenterY) {
			position = Position.RIGHT_BOTTOM;
		}

		return position;
	}

	class TouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(GLView view, MotionEvent event) {
			if (view == mGlVideoView[0]) {
				mTargetIndex = 0;
			} else if (view == mGlVideoView[1]) {
				mTargetIndex = 1;
			} else if (view == mGlVideoView[2]) {
				mTargetIndex = 2;
			} else if (view == mGlVideoView[3]) {
				mTargetIndex = 3;
			} else if (view == mGlVideoView[4]) {
				mTargetIndex = 4;
			} else {
				mTargetIndex = -1;
			}
			if (mGestureDetector != null) {
				mGestureDetector.onTouchEvent(event);
			}
			if (mTargetIndex == 1 && mMoveDetector != null) {
				//mMoveDetector.onTouchEvent(event);
			} else if (mTargetIndex == 0 && mGlVideoView[0].getVideoSrcType() == AVConstants.VIDEO_SRC_SHARESCREEN) {
				if (mScaleGestureDetector != null) {
					mScaleGestureDetector.onTouchEvent(event);
				}
				if (mMoveDetector != null) {
					//mMoveDetector.onTouchEvent(event);
				}
			}
			return true;
		}
	};
	class GestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onSingleTapConfirmed(MotionEvent event) {
			if (QLog.isColorLevel())
				QLog.d(TAG, QLog.CLR, "GestureListener-->mTargetIndex=" + mTargetIndex);
			if (mTargetIndex <= 0) {
				// 显示控制层
				mContext.sendBroadcast(new Intent(Util.VIDEO_CLICK).putExtra("VIDEO_INDEX", mTargetIndex));
			} else {
				switchVideo(0, mTargetIndex);
				UZCoreUtil.parseCssPixel(""+UZUtility.dipToPix(mGlVideoView[mTargetIndex].getBounds().left));
				int left = UZCoreUtil.pixToDip(mGlVideoView[mTargetIndex].getBounds().left);
				int top = UZCoreUtil.pixToDip(mGlVideoView[mTargetIndex].getBounds().top);
				int right = UZCoreUtil.pixToDip(mGlVideoView[mTargetIndex].getBounds().right);
				int bottom = UZCoreUtil.pixToDip(mGlVideoView[mTargetIndex].getBounds().bottom);
				Log.v("lbbniu", "------------left＝"+left+"-----top＝"+top+"----------right＝"+right+"-----------bottom＝"+bottom+"------------");
				mContext.sendBroadcast(new Intent(Util.VIDEO_CLICK).putExtra("VIDEO_INDEX", mTargetIndex)
						.putExtra("x", left).putExtra("y", top).putExtra("w", right-left).putExtra("h", bottom-top));
			}
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (mTargetIndex == 0 && mGlVideoView[0].getVideoSrcType() == AVConstants.VIDEO_SRC_SHARESCREEN) {
				mClickTimes++;
				if (mClickTimes % 2 == 1) {
					mGlVideoView[0].setScale(GLVideoView.MAX_SCALE + 1, 0, 0, true);
				} else {
					mGlVideoView[0].setScale(GLVideoView.MIN_SCALE, 0, 0, true);
				}
				return true;
			}
			return super.onDoubleTap(e);
		}
	};

	class MoveListener implements OnMoveGestureListener {
		int startX = 0;
		int startY = 0;
		int endX = 0;
		int endY = 0;
		int startPosition = 0;

		@Override
		public boolean onMoveBegin(MoveGestureDetector detector) {
			if (mTargetIndex == 0) {

			} else if (mTargetIndex == 1) {
				startX = (int) detector.getFocusX();
				startY = (int) detector.getFocusY();
				startPosition = getSmallViewPosition();
			}
			return true;
		}

		@Override
		public boolean onMove(MoveGestureDetector detector) {
			PointF delta = detector.getFocusDelta();
			int deltaX = (int) delta.x;
			int deltaY = (int) delta.y;
			if (mTargetIndex == 0) {
				mGlVideoView[0].setOffset(deltaX, deltaY, false);
			} else if (mTargetIndex == 1) {
				if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5) {
					mDragMoving = true;
				}
				// 修改拖动窗口的位置
				checkAndChangeMargin(deltaX, deltaY);
			}
			return true;
		}

		@Override
		public void onMoveEnd(MoveGestureDetector detector) {
			PointF delta = detector.getFocusDelta();
			int deltaX = (int) delta.x;
			int deltaY = (int) delta.y;
			if (mTargetIndex == 0) {
				mGlVideoView[0].setOffset(deltaX, deltaY, true);
			} else if (mTargetIndex == 1) {
				// 修改拖动窗口的位置
				checkAndChangeMargin(deltaX, deltaY);
				endX = (int) detector.getFocusX();
				endY = (int) detector.getFocusY();
				mPosition = getSmallViewDstPosition(startPosition, startX, startY, endX, endY);
				afterDrag(mPosition);
			}
		}
	};

	class ScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float x = detector.getFocusX();
			float y = detector.getFocusY();
			float scale = detector.getScaleFactor();
			float curScale = mGlVideoView[0].getScale();
			mGlVideoView[0].setScale(curScale * scale, (int) x, (int) y, false);
			return true;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			float x = detector.getFocusX();
			float y = detector.getFocusY();
			float scale = detector.getScaleFactor();
			float curScale = mGlVideoView[0].getScale();
			mGlVideoView[0].setScale(curScale * scale, (int) x, (int) y, true);
		}

	}

	enum MoveDistanceLevel {
		e_MoveDistance_Min, e_MoveDistance_Positive, e_MoveDistance_Negative
	};

	int getSmallViewDstPosition(int startPosition, int nStartX, int nStartY, int nEndX, int nEndY) {
		int thresholdX = mContext.getApplicationContext().getResources().getDimensionPixelSize(UZResourcesIDFinder.getResDimenID("video_smallview_move_thresholdX"));
		int thresholdY = mContext.getApplicationContext().getResources().getDimensionPixelSize(UZResourcesIDFinder.getResDimenID("video_smallview_move_thresholdY"));
		int xMoveDistanceLevelStandard = thresholdX;
		int yMoveDistanceLevelStandard = thresholdY;

		MoveDistanceLevel eXMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Min;
		MoveDistanceLevel eYMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Min;

		if (nEndX - nStartX > xMoveDistanceLevelStandard) {
			eXMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Positive;
		} else if (nEndX - nStartX < -xMoveDistanceLevelStandard) {
			eXMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Negative;
		} else {
			eXMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Min;
		}

		if (nEndY - nStartY > yMoveDistanceLevelStandard) {
			eYMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Positive;
		} else if (nEndY - nStartY < -yMoveDistanceLevelStandard) {
			eYMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Negative;
		} else {
			eYMoveDistanceLevel = MoveDistanceLevel.e_MoveDistance_Min;
		}

		int eBeginPosition = startPosition;
		int eEndPosition = Position.LEFT_TOP;
		int eDstPosition = Position.LEFT_TOP;
		eEndPosition = getSmallViewPosition();

		if (eEndPosition == Position.RIGHT_BOTTOM) {
			if (eBeginPosition == Position.LEFT_TOP) {
				eDstPosition = Position.RIGHT_BOTTOM;
			} else if (eBeginPosition == Position.RIGHT_TOP) {
				eDstPosition = Position.RIGHT_BOTTOM;
			} else if (eBeginPosition == Position.LEFT_BOTTOM) {
				eDstPosition = Position.RIGHT_BOTTOM;
			} else if (eBeginPosition == Position.RIGHT_BOTTOM) {
				if (eXMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Negative) {
					if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Negative) {
						eDstPosition = Position.LEFT_TOP;
					} else {
						eDstPosition = Position.LEFT_BOTTOM;
					}
				} else {
					if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Negative) {
						eDstPosition = Position.RIGHT_TOP;
					} else {
						eDstPosition = Position.RIGHT_BOTTOM;
					}
				}
			}
		} else if (eEndPosition == Position.RIGHT_TOP) {
			if (eBeginPosition == Position.LEFT_TOP) {
				eDstPosition = Position.RIGHT_TOP;
			} else if (eBeginPosition == Position.RIGHT_BOTTOM) {
				eDstPosition = Position.RIGHT_TOP;
			} else if (eBeginPosition == Position.LEFT_BOTTOM) {
				eDstPosition = Position.RIGHT_TOP;
			} else if (eBeginPosition == Position.RIGHT_TOP) {
				if (eXMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Negative) {
					if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Positive) {
						eDstPosition = Position.LEFT_BOTTOM;
					} else {
						eDstPosition = Position.LEFT_TOP;
					}
				} else {
					if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Positive) {
						eDstPosition = Position.RIGHT_BOTTOM;
					} else {
						eDstPosition = Position.RIGHT_TOP;
					}
				}
			}
		} else if (eEndPosition == Position.LEFT_TOP) {
			if (eBeginPosition == Position.RIGHT_TOP) {
				eDstPosition = Position.LEFT_TOP;
			} else if (eBeginPosition == Position.RIGHT_BOTTOM) {
				eDstPosition = Position.LEFT_TOP;
			} else if (eBeginPosition == Position.LEFT_BOTTOM) {
				eDstPosition = Position.LEFT_TOP;
			} else if (eBeginPosition == Position.LEFT_TOP) {
				if (eXMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Positive) {
					if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Positive) {
						eDstPosition = Position.RIGHT_BOTTOM;
					} else {
						eDstPosition = Position.RIGHT_TOP;
					}
				} else {
					if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Positive) {
						eDstPosition = Position.LEFT_BOTTOM;
					} else {
						eDstPosition = Position.LEFT_TOP;
					}
				}
			}
		} else if (eEndPosition == Position.LEFT_BOTTOM) {
			if (eBeginPosition == Position.LEFT_TOP) {
				eDstPosition = Position.LEFT_BOTTOM;
			} else if (eBeginPosition == Position.RIGHT_TOP) {
				eDstPosition = Position.LEFT_BOTTOM;
			} else if (eBeginPosition == Position.RIGHT_BOTTOM) {
				eDstPosition = Position.LEFT_BOTTOM;
			} else if (eBeginPosition == Position.LEFT_BOTTOM) {
				if (eXMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Positive) {
					if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Negative) {
						eDstPosition = Position.RIGHT_TOP;
					} else {
						eDstPosition = Position.RIGHT_BOTTOM;
					}
				} else {
					if (eYMoveDistanceLevel == MoveDistanceLevel.e_MoveDistance_Negative) {
						eDstPosition = Position.LEFT_TOP;
					} else {
						eDstPosition = Position.LEFT_BOTTOM;
					}
				}
			}
		}
		return eDstPosition;
	}

	void afterDrag(int position) {
		int width = mContext.getResources().getDimensionPixelSize(UZResourcesIDFinder.getResDimenID("video_small_view_width"));
		int height = mContext.getResources().getDimensionPixelSize(UZResourcesIDFinder.getResDimenID("video_small_view_height"));
		int edgeX = mContext.getResources().getDimensionPixelSize(UZResourcesIDFinder.getResDimenID("video_small_view_offsetX"));
		int edgeY = mContext.getResources().getDimensionPixelSize(UZResourcesIDFinder.getResDimenID("video_small_view_offsetY"));
		if (mBottomOffset == 0) {
			edgeY = edgeX;
		}
		Rect visableRect = getBounds();

		int fromX = mGlVideoView[1].getBounds().left;
		int fromY = mGlVideoView[1].getBounds().top;
		int toX = 0;
		int toY = 0;

		switch (position) {
		case Position.LEFT_TOP:
			toX = edgeX;
			toY = edgeY;
			break;
		case Position.RIGHT_TOP:
			toX = visableRect.width() - edgeX - width;
			toY = edgeY;
			break;
		case Position.RIGHT_BOTTOM:
			toX = visableRect.width() - edgeX - width;
			toY = visableRect.height() - edgeY - height;
			break;
		case Position.LEFT_BOTTOM:
			toX = edgeX;
			toY = visableRect.height() - edgeY - height;
			break;
		default:
			break;
		}
	}
	public void setSelfId(String key) {
		if (mGraphicRenderMgr != null) {
			mGraphicRenderMgr.setSelfId(key + "_" + AVConstants.VIDEO_SRC_CAMERA);
		}
	}
	void onMemberChange() {
		Log.d(TAG, "WL_DEBUG onMemberChange start");
		QavsdkControlMulti qavsdk = AvSdkAppDelegate.getQavsdkControlMulti();
		//lbbniu
		
		
		
		
		
		ArrayList<MemberInfo> memberList = qavsdk.getMemberList();

		for (MemberInfo memberInfo : memberList) {
			int index = getViewIndexById(memberInfo.identifier, AVConstants.VIDEO_SRC_CAMERA);
			if (index >= 0) {
				Log.d(TAG, "WL_DEBUG onMemberChange memberInfo.isVideoIn = " + memberInfo.isVideoIn);

				if (!memberInfo.isVideoIn && !memberInfo.isSpeaking) {
					closeVideoView(index);
				}
			}
		}
		
		// 去掉已经不再memberlist中的view
		if (!memberList.isEmpty()) {
			for (int i = 0; i < mGlVideoView.length; i++) {
				GLVideoView view = mGlVideoView[i];
				if (view == null)
					continue;
				String viewIdentifier = view.getIdentifier();
				if (TextUtils.isEmpty(viewIdentifier))
					continue;
				
				
				//start   lbbniu
				/*AVEndpoint endpoint = ((AVRoomMulti) qavsdk.getRoom()).getEndpointById(viewIdentifier);
				if (endpoint != null)
				{
					AVEndpoint.View  viewAVEndpoint= new AVEndpoint.View();
					viewAVEndpoint.videoSrcType = AVEndpoint.View.VIDEO_SRC_TYPE_CAMERA;//SDK1.2版本只支持摄像头视频源，所以当前只能设置为VIDEO_SRC_TYPE_CAMERA。
					viewAVEndpoint.viewSizeType = AVEndpoint.View.VIEW_SIZE_TYPE_BIG;
					
					endpoint.requestView(viewAVEndpoint, new AVEndpoint.RequestViewCompleteCallback() {
						protected void OnComplete(String identifier, int result) {
							// TODO
							Log.d(TAG, "RequestViewCompleteCallback.OnComplete");
						}
					});
					AvSdkAppDelegate.getQavsdkControlMulti().setRemoteHasVideo(viewIdentifier, AVConstants.VIDEO_SRC_CAMERA, true);
				}*/
				//end   lbbniu
				
				
				boolean memberExist = false;
				for (int j=0; j<memberList.size(); j++) {
					if (!TextUtils.isEmpty(memberList.get(j).identifier)) {
						if (viewIdentifier.equals(memberList.get(j).identifier)) {
							memberExist = true;
							break;
						}			
					}
				}
				
				if (!memberExist) {
					closeVideoView(i);
				}else{
					AvSdkAppDelegate.getQavsdkControlMulti().setRemoteHasVideo(viewIdentifier, AVConstants.VIDEO_SRC_CAMERA, true);
				}
			}	
		}

		Log.d(TAG, "WL_DEBUG onMemberChange end");
	}
}
