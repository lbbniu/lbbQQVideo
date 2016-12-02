package com.apicloud.avSdkApi;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.apicloud.avSdkApp.AvSdkAppDelegate;
import com.apicloud.avSdkControl.MemberInfo;
import com.apicloud.avSdkControl.Util;
import com.apicloud.avSdkControl.multi.QavsdkControlMulti;
import com.tencent.av.sdk.AVAudioCtrl;
import com.tencent.av.sdk.AVConstants;
import com.tencent.av.sdk.AVEndpoint;
import com.tencent.av.sdk.AVError;
import com.tencent.av.sdk.AVRoomMulti;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.annotation.UzJavascriptMethod;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

public class AvSdkMultiApi extends UZModule {

	private UZModuleContext mJsbstartContext;
	private Context mContext;

	private int mLoginErrorCode = AVError.AV_OK;
	private int mCreateRoomErrorCode = AVError.AV_OK;
	private int mCloseRoomErrorCode = AVError.AV_OK;
	private int mOnOffCameraErrorCode = AVError.AV_OK;
	private int mSwitchCameraErrorCode = AVError.AV_OK;
	private int mEnableExternalCaptureErrorCode = AVError.AV_OK;
	public String mSelfIdentifier = "";
	private String mRecvIdentifier = "";
	private boolean mIsPaused = false;

	private View videoPair;
	public static final int MAX_TIMEOUT = 5*1000;
	public static final int MSG_CREATEROOM_TIMEOUT = 1;
	
	
   private Handler handler = new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_CREATEROOM_TIMEOUT:
				if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
					AvSdkAppDelegate.getQavsdkControlMulti().setCreateRoomStatus(false);
					AvSdkAppDelegate.getQavsdkControlMulti().setCloseRoomStatus(false);
					String retstr = "{action:createroom_timeout,retcode:"+9999+"}";
					jscallback(mJsbstartContext,retstr,false);
				}
				break;

			default:
				break;
			}
			return false;
		}
	});	

	public BroadcastReceiver mBroadcastReceiverMulti = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.v("test", "--------mBroadcastReceiverMulti---------" + action);
			if (action.equals(Util.ACTION_START_CONTEXT_COMPLETE)) {			
				mLoginErrorCode = intent.getIntExtra(
						Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);
				String retstr = "{action:"+Util.ACTION_START_CONTEXT_COMPLETE+",retcode:"+mLoginErrorCode+"}";
				jscallback(mJsbstartContext,retstr,false);
				if (mLoginErrorCode == AVError.AV_OK) {
					//createroom
				} else {
					
				}
			} else if (action.equals(Util.ACTION_CLOSE_CONTEXT_COMPLETE)) {
				AvSdkAppDelegate.getQavsdkControlMulti().setIsInStopContext(false);
				String retstr = "{action:"+Util.ACTION_CLOSE_CONTEXT_COMPLETE+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);
			} else if (action.equals(Util.ACTION_MULTI_ROOM_CREATE_COMPLETE)) {
				handler.removeMessages(MSG_CREATEROOM_TIMEOUT);
				mCreateRoomErrorCode = intent.getIntExtra(
						Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);
				//create videoview getRelationId mSelfIdentifier
				if (mCreateRoomErrorCode == AVError.AV_OK) {
					
				} else {
					
				}
				String retstr = "{action:"+Util.ACTION_MULTI_ROOM_CREATE_COMPLETE+",retcode:"+mCreateRoomErrorCode+"}";
				jscallback(mJsbstartContext,retstr,false);
			} else if (action.equals(Util.ACTION_MULTI_CLOSE_ROOM_COMPLETE)) {
				String retstr = "{action:"+Util.ACTION_MULTI_CLOSE_ROOM_COMPLETE+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);
			} else if (action.equals(Util.ACTION_MULTI_SURFACE_CREATED)) {
				
			} else if (action.equals(Util.ACTION_VIDEO_CLOSE)) {
				String identifier = intent.getStringExtra(Util.EXTRA_IDENTIFIER);
				if (!TextUtils.isEmpty(mRecvIdentifier)) {
					AvSdkAppDelegate.getQavsdkControlMulti().setRemoteHasVideo(false, mRecvIdentifier);			
				}
					
				mRecvIdentifier = identifier;				
			} else if (action.equals(Util.ACTION_VIDEO_SHOW)) {
				String identifier = intent.getStringExtra(Util.EXTRA_IDENTIFIER);
				mRecvIdentifier = identifier;
				//单路视频
//				AvSdkAppDelegate.getQavsdkControlMulti().setRemoteHasVideo(true, mRecvIdentifier);
				//多路视频
				AvSdkAppDelegate.getQavsdkControlMulti().setRemoteHasVideo(identifier, AVConstants.VIDEO_SRC_CAMERA, true);
			} else if (action.equals(Util.ACTION_MULTI_ENABLE_CAMERA_COMPLETE)) {
				mOnOffCameraErrorCode = intent.getIntExtra(Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);
				boolean isEnable = intent.getBooleanExtra(Util.EXTRA_IS_ENABLE, false);
						
				if (mOnOffCameraErrorCode == AVError.AV_OK) {
					if (!mIsPaused) {
						AvSdkAppDelegate.getQavsdkControlMulti().setSelfId(mSelfIdentifier);
						AvSdkAppDelegate.getQavsdkControlMulti().setLocalHasVideo(isEnable, mSelfIdentifier);							
					}
					
				}else{
					
				}
				//开启渲染回调的接口
				//mQavsdkControl.setRenderCallback();
			} else if (action.equals(Util.ACTION_ENABLE_EXTERNAL_CAPTURE_COMPLETE)) {
				
			} else if (action.equals(Util.ACTION_MULTI_SWITCH_CAMERA_COMPLETE)) {
				mSwitchCameraErrorCode = intent.getIntExtra(Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);
				boolean isFront = intent.getBooleanExtra(Util.EXTRA_IS_FRONT, false);
				if (mSwitchCameraErrorCode != AVError.AV_OK) {
									}
			} else if (action.equals(Util.ACTION_MEMBER_CHANGE)) {
				String retstr = "{action:"+Util.ACTION_MEMBER_CHANGE+",retcode:"+0+"}";
				AvSdkAppDelegate.getQavsdkControlMulti().onMemberChange();
			} else if (action.equals(Util.ACTION_MULTI_OUTPUT_MODE_CHANGE)) {
				
			}
		}
	};

	public AvSdkMultiApi(UZWebView webView) {
		super(webView);
		mContext = getContext();
	}

	/**
	 * 启动sdk avcontext
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_startAvContextMulti(final UZModuleContext moduleContext) {
		mJsbstartContext = moduleContext;
		
		final IntentFilter intentFilter = new IntentFilter();
		// 启动关闭sdk
		intentFilter.addAction(Util.ACTION_START_CONTEXT_COMPLETE);
		intentFilter.addAction(Util.ACTION_CLOSE_CONTEXT_COMPLETE);
		// 多人对话创建房间
		intentFilter.addAction(Util.ACTION_MULTI_ROOM_CREATE_COMPLETE);  //xuyao
		intentFilter.addAction(Util.ACTION_MULTI_CLOSE_ROOM_COMPLETE);   //xuyao

		// 双人对话视频view
		intentFilter.addAction(Util.ACTION_MULTI_SURFACE_CREATED);       //xuyao
		intentFilter.addAction(Util.ACTION_VIDEO_SHOW);            //=====
		intentFilter.addAction(Util.ACTION_VIDEO_CLOSE);		   //=====
		intentFilter.addAction(Util.ACTION_MULTI_ENABLE_CAMERA_COMPLETE); //xuyao
		intentFilter.addAction(Util.ACTION_ENABLE_EXTERNAL_CAPTURE_COMPLETE);//======
		intentFilter.addAction(Util.ACTION_MULTI_SWITCH_CAMERA_COMPLETE);//xuyao
		intentFilter.addAction(Util.ACTION_MEMBER_CHANGE);//====
		intentFilter.addAction(Util.ACTION_MULTI_OUTPUT_MODE_CHANGE);//xuyao

		// 网络切换
//		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mContext.registerReceiver(mBroadcastReceiverMulti, intentFilter);
		

		final String identifier = moduleContext.optString("identifier");
		final String usersig = moduleContext.optString("usersig");
		AvSdkAppDelegate.getQavsdkControlMulti().selfindentify = identifier;
		if (AvSdkAppDelegate.mAVContext == null) {
		   if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {		
			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					 AvSdkAppDelegate.getQavsdkControlMulti().startContext(identifier,usersig);
				}
			});
		   }
		}else{
			String retstr = "{action:"+Util.ACTION_START_CONTEXT_COMPLETE+",retcode:0}";
			jscallback(mJsbstartContext,retstr,false);
		}
	}

	/**
	 * 关闭avsdk
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_stopContextMulti(UZModuleContext moduleContext) {
		if (null != AvSdkAppDelegate.getQavsdkControlMulti()) {
			AvSdkAppDelegate.getQavsdkControlMulti().stopContext();
		}
	}


	/**
	 * 创建多人房间
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_createMultiRoom(UZModuleContext moduleContext) {
		Log.v("test", "--------jsmethod_createMultiRoom---------"+ moduleContext.toString());
		if (Util.isNetworkAvailable(mContext)) {
			int relationId = moduleContext.optInt("relationId");
			relationId = 6666;
			if (relationId != 0) {
				Log.v("test", "--------jsmethod_createMultiRoom----11111-----");
				AvSdkAppDelegate.getQavsdkControlMulti().enterRoom(relationId);
				handler.sendEmptyMessageDelayed(MSG_CREATEROOM_TIMEOUT, MAX_TIMEOUT);				
			}	
		} else {
			jscallback(mJsbstartContext,"{msg:network_not_find}",false);
		}
	}

	/**
	 * 退出双人房间
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_exitMultiRoom(UZModuleContext moduleContext) {
		if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
			int ret = AvSdkAppDelegate.getQavsdkControlMulti().exitRoom();
		}
	}

	/**
	 * 打开关闭 mic麦克风
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_enableMicPair(UZModuleContext moduleContext) {
		boolean enable = moduleContext.optBoolean("enable");
		if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
			if (AvSdkAppDelegate.getQavsdkControlMulti().getAVContext() != null) {
				AvSdkAppDelegate.getQavsdkControlMulti().getAVContext()
						.getAudioCtrl().enableMic(enable);
			}
		}
	}

	/**
	 * 打开关闭 Speak
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_enableSpeakerPair(UZModuleContext moduleContext) {
		boolean enable = moduleContext.optBoolean("enable");
		if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
			if (AvSdkAppDelegate.getQavsdkControlMulti().getAVContext() != null) {
				AvSdkAppDelegate.getQavsdkControlMulti().getAVContext()
						.getAudioCtrl().enableSpeaker(enable);
			}
		}
	}

	/**
	 * 打开双人对话视频页面
	 */
	@UzJavascriptMethod
	public void jsmethod_openMultiVideoView(UZModuleContext moduleContext) {
		
		int relationId = moduleContext.optInt("relationId");
		mSelfIdentifier = moduleContext.optString("selfIdentifier");
		mRecvIdentifier = moduleContext.optString("mRecvIdentifier");
		int netType = Util.getNetWorkType(mContext);
		AvSdkAppDelegate.getQavsdkControlMulti().setNetType(netType);	
		
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
		
		if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
			if (AvSdkAppDelegate.getQavsdkControlMulti().getAVContext() != null) {
				
				if(AvSdkAppDelegate.getQavsdkControlMulti().getAVContext().getAudioCtrl() != null){
					if (mContext instanceof Activity) {
						videoPair = (View) LayoutInflater
								.from(mContext)
								.inflate(UZResourcesIDFinder.getResLayoutID("av_activity"),null);
						insertViewToCurWindow(videoPair, rlp);
						AvSdkAppDelegate.getQavsdkControlMulti().onCreate(mContext,
								videoPair);
						String retstr = "{action:success}";
						jscallback(moduleContext,retstr,true);
					}
				}
				
			}
		}
	}

	/**
	 * 打开关闭照相机
	 */
	@UzJavascriptMethod
	public void jsmethod_toggleEnableCameraPair(UZModuleContext moduleContext) {
		if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
			if (AvSdkAppDelegate.getQavsdkControlMulti().getAVContext() != null) {
				int mOnOffCameraErrorCode = AvSdkAppDelegate
						.getQavsdkControlMulti().toggleEnableCamera();
				if (mOnOffCameraErrorCode != AVError.AV_OK) {
					Log.v("test", "--------mOnOffCameraErrorCode----11111-----"+mOnOffCameraErrorCode);
					AvSdkAppDelegate.getQavsdkControlMulti().setIsInOnOffCamera(
							false);
				}
			}
		}
	}

	/**
	 * 查看照相机是否打开
	 */
	public void jsmethod_IsEnableCameraPair(UZModuleContext moduleContext) {
		if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
			if (AvSdkAppDelegate.getQavsdkControlMulti().getAVContext() != null) {
				boolean isenable = AvSdkAppDelegate.getQavsdkControlMulti()
						.getIsEnableCamera();
				Log.v("test", "-----jsmethod_IsEnableCameraPair---------" + isenable);
				try {
					JSONObject jobj = new JSONObject();
					jobj.put("isenable", isenable);
					moduleContext.success(jobj, true);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 设置是否免提
	 */
	@UzJavascriptMethod
	public void jsmethod_setHandfreePair(UZModuleContext moduleContext) {
		if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
			if (AvSdkAppDelegate.getQavsdkControlMulti().getAVContext() != null) {
				int mOnOffCameraErrorCode = AvSdkAppDelegate
						.getQavsdkControlMulti().toggleEnableCamera();
				if (mOnOffCameraErrorCode != AVError.AV_OK) {
					AvSdkAppDelegate
							.getQavsdkControlMulti()
							.getAVContext()
							.getAudioCtrl()
							.setAudioOutputMode(
									AvSdkAppDelegate.getQavsdkControlMulti()
											.getHandfreeChecked() ? AVAudioCtrl.OUTPUT_MODE_SPEAKER
											: AVAudioCtrl.OUTPUT_MODE_HEADSET);
				}
			}
		}
	}

	/**
	 * 查看照相机是否免提
	 */
	public void jsmethod_IsHandfreePair(UZModuleContext moduleContext) {
		if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
			if (AvSdkAppDelegate.getQavsdkControlMulti().getAVContext() != null) {
				boolean isenable = AvSdkAppDelegate.getQavsdkControlMulti()
						.getHandfreeChecked();
				try {
					JSONObject jobj = new JSONObject();
					jobj.put("checked", isenable);
					moduleContext.success(jobj, true);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 切换前后摄像头
	 */
	public void jsmethod_toggleSwitchCamera(UZModuleContext moduleContext) {
		if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
			if (AvSdkAppDelegate.getQavsdkControlMulti().getAVContext() != null) {
				int ret = AvSdkAppDelegate.getQavsdkControlMulti()
						.toggleSwitchCamera();
			}
		}
	}
	

	/**
	 * 挂断
	 */
	@UzJavascriptMethod
	public void jsmethod_hangupPair(UZModuleContext moduleContext) {
		if (videoPair != null) {
			removeViewFromCurWindow(videoPair);
		}
		if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
			AvSdkAppDelegate.getQavsdkControlMulti().onDestroy();
		}
	}
	
	/**
	 * 取得自己用户id
	 */
	@UzJavascriptMethod
	public void jsmethod_getSelfIdentifier(UZModuleContext moduleContext) {
		if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
			String selfIdentifier = AvSdkAppDelegate.getQavsdkControlMulti().getSelfIdentifier();
			String retstr = "{action:getSelfIdentifier,data:"+selfIdentifier+"}";
			jscallback(mJsbstartContext,retstr,false);
		}
	}
	
	/**
	 * 取得成员列表
	 */
	@UzJavascriptMethod
	public void jsmethod_getMemberList(UZModuleContext moduleContext) {
		if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
			ArrayList<MemberInfo> list = AvSdkAppDelegate.getQavsdkControlMulti().getMemberList();
			JSONArray jsonarray = new JSONArray(list);
			String retstr = "{action:getMemberList,data:"+jsonarray.toString()+"}";
			jscallback(mJsbstartContext,retstr,false);
		}
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
