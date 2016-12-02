package com.apicloud.avSdkApi;

import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.apicloud.avSdkApp.AvSdkAppDelegate;
import com.apicloud.avSdkApp.LbbAvSingleton;
import com.apicloud.avSdkControl.Util;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.annotation.UzJavascriptMethod;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
//UZUtility.dipToPix：css-》设备像素，UZCoreUtil.pixToDip：设备像素-》css，
public class LbbQQVideo  extends UZModule {
	private UZModuleContext mJsbstartContext;
	private Context mContext;
	private View videoPair = null;
	private View videoPairM = null;
	private  LbbAvSingleton mLbbAvSingleton = null;
	
	
	
	public LbbQQVideo(UZWebView webView) {
		super(webView);
		// TODO Auto-generated constructor stub
		mContext = getContext();
		Util.modifyAppid = super.getFeatureValue("lbbQQVideo", "appId");
		Util.modifyUid = super.getFeatureValue("lbbQQVideo", "accountType");
		mLbbAvSingleton = LbbAvSingleton.getLbbAvSingleton(mContext);
	}

	
	/**
	 * 登陆视频聊天服务器
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_login(final UZModuleContext moduleContext) {
		mJsbstartContext = moduleContext;
		mLbbAvSingleton.startAvContextPair(moduleContext);
	}
	
	/**
	 * 退出视频聊天登陆
	 * @param moduleContext
	 */
	public void jsmethod_logout(final UZModuleContext moduleContext){
		mLbbAvSingleton.stopContext(moduleContext);
		moduleContext.interrupt();
	}
	
	/**
	 * 邀请加入双人房间
	 * @param moduleContext
	 */
	public void jsmethod_invitePair(UZModuleContext moduleContext) {
		mLbbAvSingleton.invitePair(moduleContext);
		moduleContext.interrupt();
	}
	
	/**
	 * 接受双人房间邀请
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_pairInviteAccept(UZModuleContext moduleContext) {
		mLbbAvSingleton.pairInviteAccept(moduleContext);
		moduleContext.interrupt();
	}

	/**
	 * 拒绝双人房间邀请
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_pairInviteRefuse(UZModuleContext moduleContext) {
		mLbbAvSingleton.pairInviteRefuse(moduleContext);
		moduleContext.interrupt();
	}
	
	/**
	 * 打开双人对话视频页面
	 */
	@UzJavascriptMethod
	public void jsmethod_openPairVideoView(UZModuleContext moduleContext) {
		boolean lbool = mLbbAvSingleton.openPairVideoView(moduleContext);
		
		int x = moduleContext.optInt("x");
		int y = moduleContext.optInt("y");
		int w = moduleContext.optInt("w");
		int h = moduleContext.optInt("h");
		if(w == 0){
			w = ViewGroup.LayoutParams.MATCH_PARENT;
		}
		if(h == 0){
			h = ViewGroup.LayoutParams.MATCH_PARENT;
		}

		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(w, h);
		rlp.leftMargin = x;
		rlp.topMargin = y;
		if(lbool){
			videoPair = (View) LayoutInflater
					.from(mContext)
					.inflate(UZResourcesIDFinder.getResLayoutID("qav_video_layer_ui"),null);
			insertViewToCurWindow(videoPair, rlp);
			AvSdkAppDelegate.getQavsdkControlPair().onCreate(mContext,
					videoPair);
			String retstr = "{action:success}";
			jscallback(moduleContext,retstr,true);
			videoPair.invalidate();
			getContext().sendStickyBroadcast(new Intent( Util.ACTION_PEER_MIC_OPEN));		
			getContext().sendStickyBroadcast(new Intent(Util.ACTION_PEER_CAMERA_OPEN));			
		}else{
			String retstr = "{action:error}";
			jscallback(moduleContext,retstr,true);
		}
	}
	
	
	/**
	 * 刷新界面
	 */
	public void jsmethod_invalidate(UZModuleContext moduleContext) {
		if(null != videoPair){
			videoPair.invalidate();
			//Log.v("test", "--------------mReceiveIdentifier-------jsmethod_invalidate---" + mReceiveIdentifier);
		}
	}
	//=====================================================common==============================================================================
	/**
	 * 退出房间
	 * 双人和多人合并
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_exitRoom(UZModuleContext moduleContext) {
		mLbbAvSingleton.exitRoom(moduleContext);
		//moduleContext.interrupt();
		String retstr = "{action:success}";
		jscallback(moduleContext,retstr,true);
	}
	/**
	 * 打开关闭 mic麦克风  是否禁用话筒
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_enableMic(UZModuleContext moduleContext) {
		mLbbAvSingleton.enableMicPair(moduleContext);
	}
	
	/**
	 * 打开关闭 Speak  是否静音
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_enableSpeaker(UZModuleContext moduleContext) {
		mLbbAvSingleton.enableSpeakerPair(moduleContext);
	}
	
	
	/**
	 * 打开关闭照相机
	 */
	@UzJavascriptMethod
	public void jsmethod_toggleEnableCamera(UZModuleContext moduleContext) {
		mLbbAvSingleton.toggleEnableCameraPair(moduleContext);
	}
	
	/**
	 * 查看照相机是否打开
	 */
	@UzJavascriptMethod
	public void jsmethod_IsEnableCameraPair(UZModuleContext moduleContext) {
		mLbbAvSingleton.IsEnableCameraPair(moduleContext);
	}
	/**
	 * 设置是否免提
	 */
	@UzJavascriptMethod
	public void jsmethod_setHandfree(UZModuleContext moduleContext) {
		mLbbAvSingleton.setHandfreePair(moduleContext);
		moduleContext.interrupt();
	}
	
	
	/**
	 * 查看照相机是否免提
	 */
	public void jsmethod_IsHandfree(UZModuleContext moduleContext) {
		mLbbAvSingleton.IsHandfreePair(moduleContext);
	}
	
	
	/**
	 * 切换前后摄像头
	 */
	public void jsmethod_toggleSwitchCamera(UZModuleContext moduleContext) {
		mLbbAvSingleton.toggleSwitchCamera(moduleContext);
		moduleContext.interrupt();
	}
	/**
	 * 挂断
	 */
	@UzJavascriptMethod
	public void jsmethod_hangup(UZModuleContext moduleContext) {
		mLbbAvSingleton.hangupPair(moduleContext);
		if (videoPairM != null) {
			removeViewFromCurWindow(videoPairM);
		}
		if (videoPair != null) {
			removeViewFromCurWindow(videoPair);
		}
		//moduleContext.interrupt();
		String retstr = "{action:success}";
		jscallback(moduleContext,retstr,true);
	}
	//==========================================================================================================================
	/**
	 * 创建多人房间
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_createMultiRoom(UZModuleContext moduleContext) {
		mLbbAvSingleton.createMultiRoom(moduleContext);
		moduleContext.interrupt();
	}
	
	/**
	 * 打开多人对话视频页面
	 */
	@UzJavascriptMethod
	public void jsmethod_openMultiVideoView(UZModuleContext moduleContext) {
		
		boolean lbool = mLbbAvSingleton.openMultiVideoView(moduleContext);
		
		int x = moduleContext.optInt("x");
		int y = moduleContext.optInt("y");
		int w = moduleContext.optInt("w");
		int h = moduleContext.optInt("h");
		if(w == 0){
			w = ViewGroup.LayoutParams.MATCH_PARENT;
		}
		if(h == 0){
			h = ViewGroup.LayoutParams.MATCH_PARENT;
		}

		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(w, h);
		rlp.leftMargin = x;
		rlp.topMargin = y;
		if(lbool){
			if(videoPairM==null){
				videoPairM = (View) LayoutInflater
						.from(mContext)
						.inflate(UZResourcesIDFinder.getResLayoutID("av_activity"),null);
			}
			insertViewToCurWindow(videoPairM, rlp);

			AvSdkAppDelegate.getQavsdkControlMulti().onCreate(mContext,
					videoPairM);
			String retstr = "{action:success}";
			jscallback(moduleContext,retstr,true);
		}else{
			String retstr =  "{action:error}";
			jscallback(moduleContext,retstr,true);
		}
		Log.v("test", "--------jsmethod_openMultiVideoView----11111-----");
	}
	
	/**
	 * 取得自己用户id
	 */
	@UzJavascriptMethod
	public void jsmethod_getSelfIdentifier(UZModuleContext moduleContext) {
		mLbbAvSingleton.getSelfIdentifier(moduleContext);
	}
	/**
	 * 取得成员列表
	 */
	@UzJavascriptMethod
	public void jsmethod_getMemberList(UZModuleContext moduleContext) {
		mLbbAvSingleton.getMemberList(moduleContext);
	}
	/**
	 * 课件与视频交换
	 */
	@UzJavascriptMethod
	public void jsmethod_switchVideo(UZModuleContext moduleContext) {
		mLbbAvSingleton.switchVideo(moduleContext);
		moduleContext.interrupt();
	}
	
	/**
	 * 课件位置索引
	 */
	@UzJavascriptMethod
	public void jsmethod_setIndex(UZModuleContext moduleContext) {
		mLbbAvSingleton.setIndex(moduleContext);
		moduleContext.interrupt();
	}
	
	public void jscallback(UZModuleContext moduleContext,String json,boolean del){
		if(moduleContext != null){
			try {
				JSONObject obj = new JSONObject(json);
				moduleContext.success(obj, del);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
