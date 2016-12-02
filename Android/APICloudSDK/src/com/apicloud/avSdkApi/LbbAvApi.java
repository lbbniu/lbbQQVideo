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
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.apicloud.avSdkApp.AvSdkAppDelegate;
import com.apicloud.avSdkControl.MemberInfo;
import com.apicloud.avSdkControl.Util;
import com.tencent.av.sdk.AVAudioCtrl;
import com.tencent.av.sdk.AVConstants;
import com.tencent.av.sdk.AVError;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.annotation.UzJavascriptMethod;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
//UZUtility.dipToPix：css-》设备像素，UZCoreUtil.pixToDip：设备像素-》css，
public class LbbAvApi  extends UZModule {
	private UZModuleContext mJsbstartContext;
	private Context mContext;
	private String mReceiveIdentifier = "";
	private String mSelfIdentifier = "";
	private String mRecvIdentifier = "";
	
	private boolean mIsVideo = false;
	private boolean isSender = false;
	private boolean isReceiver = false;
	private boolean mIsPaused = false;
	private boolean peerCameraOpend = false;
	private boolean peerMicOpend = false;
	
	private boolean isDuo = false; // 是否多人通过 默认是双人通话
	
	private int mCreateRoomErrorCode = AVError.AV_OK;
	private int mCloseRoomErrorCode = AVError.AV_OK;
	private int mAcceptErrorCode = AVError.AV_OK;
	private int mInviteErrorCode = AVError.AV_OK;
	private int mRefuseErrorCode = AVError.AV_OK;
	private int mJoinRoomErrorCode = AVError.AV_OK;
	private int mOnOffCameraErrorCode = AVError.AV_OK;
	private int mSwitchCameraErrorCode = AVError.AV_OK;
	
	private View videoPair;
	private View videoPairM;
	//多人代码移植
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
	
	
	public BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.v("test", "-----------------" + action);
			if (action.equals(Util.ACTION_START_CONTEXT_COMPLETE)) {
				int mLoginErrorCode = intent.getIntExtra(
						Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);
				if (mLoginErrorCode == AVError.AV_OK) {
					int netType = Util.getNetWorkType(mContext);
					AvSdkAppDelegate.getQavsdkControlPair().setNetType(netType);
				}
				String retstr = "{action:"+Util.ACTION_START_CONTEXT_COMPLETE+",retcode:"+mLoginErrorCode+"}";
				jscallback(mJsbstartContext,retstr,false);
			}else if (action.equals(Util.ACTION_CLOSE_CONTEXT_COMPLETE)) {//关闭sdk回调
				AvSdkAppDelegate.getQavsdkControlMulti().setIsInStopContext(false);
				String retstr = "{action:"+Util.ACTION_CLOSE_CONTEXT_COMPLETE+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);
			}else if (action.equals(Util.ACTION_ACCEPT_COMPLETE)) {
				String identifier = intent
						.getStringExtra(Util.EXTRA_IDENTIFIER);
				mSelfIdentifier = intent
						.getStringExtra(Util.EXTRA_SELF_IDENTIFIER);
				mReceiveIdentifier = identifier;
				AvSdkAppDelegate.setSelfId(mSelfIdentifier);
				AvSdkAppDelegate.setReceiveId(mReceiveIdentifier);
				long roomId = intent.getLongExtra(Util.EXTRA_ROOM_ID, -1);
				mAcceptErrorCode = intent.getIntExtra(
						Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);
				
				String retstr = "{action:"+Util.ACTION_ACCEPT_COMPLETE+",retcode:"+mAcceptErrorCode+"}";
				jscallback(mJsbstartContext,retstr,false);
				if (mAcceptErrorCode == AVError.AV_OK) {
					AvSdkAppDelegate.getQavsdkControlPair().enterRoom(roomId,
							identifier, mIsVideo);
				} else {
					
				}
			} else if (action.equals(Util.ACTION_INVITE_ACCEPTED)) {
				String retstr = "{action:"+Util.ACTION_INVITE_ACCEPTED+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);
			} else if (action.equals(Util.ACTION_INVITE_CANCELED)) {
				String retstr = "{action:"+Util.ACTION_INVITE_CANCELED+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);
			} else if (action.equals(Util.ACTION_INVITE_COMPLETE)) {
				mInviteErrorCode = intent.getIntExtra(
						Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);
				
				String retstr = "{action:"+Util.ACTION_INVITE_COMPLETE+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);

				if (mInviteErrorCode == AVError.AV_OK) {

				} else {
					if (AvSdkAppDelegate.getQavsdkControlPair() != null) {
						mInviteErrorCode = AvSdkAppDelegate.getQavsdkControlPair().exitRoom();
					}
				}
			} else if (action.equals(Util.ACTION_INVITE_REFUSED)) {
				String retstr = "{action:"+Util.ACTION_INVITE_REFUSED+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);
			} else if (action.equals(Util.ACTION_RECV_INVITE)) {
				String retstr = "{action:"+Util.ACTION_RECV_INVITE+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);
				isReceiver = true;
				mIsVideo = intent.getBooleanExtra(Util.EXTRA_IS_VIDEO, false);
			} else if (action.equals(Util.ACTION_REFUSE_COMPLETE)) {
				mRefuseErrorCode = intent.getIntExtra(
						Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);
				if (mRefuseErrorCode != AVError.AV_OK) {

				}
				String retstr = "{action:"+Util.ACTION_REFUSE_COMPLETE+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);
			} else if (action.equals(Util.ACTION_ROOM_CREATE_COMPLETE)) {
				String retstr = "{action:"+Util.ACTION_ROOM_CREATE_COMPLETE+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);
				mCreateRoomErrorCode = intent.getIntExtra(
						Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);
				if (mCreateRoomErrorCode != AVError.AV_OK) {

				}
			} else if (action.equals(Util.ACTION_ROOM_JOIN_COMPLETE)) {
				mJoinRoomErrorCode = intent.getIntExtra(
						Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);
				if (mJoinRoomErrorCode != AVError.AV_OK) {
					String retstr = "{action:"+Util.ACTION_ROOM_JOIN_COMPLETE+",retcode:"+1+"}";
					jscallback(mJsbstartContext,retstr,false);
				} else {
					// start avactivity
					String retstr = "{action:"+Util.ACTION_ROOM_JOIN_COMPLETE+",retcode:"+0+"}";
					jscallback(mJsbstartContext,retstr,false);
				}
			} else if (action.equals(Util.ACTION_CLOSE_ROOM_COMPLETE)) {
				String retstr = "{action:"+Util.ACTION_CLOSE_ROOM_COMPLETE+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);
			} else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				if (AvSdkAppDelegate.getQavsdkControlPair() != null
						&& AvSdkAppDelegate.getQavsdkControlPair()
								.getAVContext() != null) {
					int netType = Util.getNetWorkType(mContext);
					AvSdkAppDelegate.getQavsdkControlPair().setNetType(netType);
				}
			} else if (action.equals(Util.ACTION_SURFACE_CREATED)) {
				if (AvSdkAppDelegate.getQavsdkControlPair().isVideo()) {
					boolean isEnable = AvSdkAppDelegate.getQavsdkControlPair().getIsEnableCamera();
					mOnOffCameraErrorCode = AvSdkAppDelegate.getQavsdkControlPair().toggleEnableCamera();
					if (mOnOffCameraErrorCode != AVError.AV_OK) {
						AvSdkAppDelegate.getQavsdkControlPair().setIsInOnOffCamera(false);
					}					
				}	
				String retstr = "{action:"+Util.ACTION_SURFACE_CREATED+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);			
			} else if (action.equals(Util.ACTION_ENABLE_CAMERA_COMPLETE)) {
				mOnOffCameraErrorCode = intent.getIntExtra(Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);
				boolean isEnable = intent.getBooleanExtra(Util.EXTRA_IS_ENABLE, false);
				if (mOnOffCameraErrorCode == AVError.AV_OK) {
					if (!mIsPaused) {
						AvSdkAppDelegate.getQavsdkControlPair().setSelfId(mSelfIdentifier);				
						AvSdkAppDelegate.getQavsdkControlPair().setLocalHasVideo(isEnable, mSelfIdentifier);
					}
				} else {
				}
				String retstr = "{action:"+Util.ACTION_ENABLE_CAMERA_COMPLETE+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);
			} else if (action.equals(Util.ACTION_SWITCH_CAMERA_COMPLETE)) {
				mSwitchCameraErrorCode = intent.getIntExtra(Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);
				boolean isFront = intent.getBooleanExtra(Util.EXTRA_IS_FRONT, false);
				if (mSwitchCameraErrorCode != AVError.AV_OK) {
				}
				String retstr = "{action:"+Util.ACTION_SWITCH_CAMERA_COMPLETE+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);
			} else if (action.equals(Util.ACTION_OUTPUT_MODE_CHANGE)) {
				String retstr = "{action:"+Util.ACTION_OUTPUT_MODE_CHANGE+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);
			} else if (action.equals(Util.ACTION_PEER_LEAVE)) {
				if (videoPair != null) {
					removeViewFromCurWindow(videoPair);
				}
				if (AvSdkAppDelegate.getQavsdkControlPair() != null) {
					AvSdkAppDelegate.getQavsdkControlPair().onDestroy();
				}
				String retstr = "{action:"+Util.ACTION_PEER_LEAVE+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);
			} else if (action.equals(Util.ACTION_PEER_CAMERA_OPEN)) {//对方开启摄像头
				AvSdkAppDelegate.getQavsdkControlPair().setSelfId(mSelfIdentifier);							
				AvSdkAppDelegate.getQavsdkControlPair().setRemoteHasVideo(true, mReceiveIdentifier);	
				Log.v("test", "--------------mReceiveIdentifier-------lbbniu---" + mReceiveIdentifier);
				if (AvSdkAppDelegate.getQavsdkControlPair().isVideo()) {							
					if (!peerCameraOpend) {
						
					}
					peerCameraOpend = true;			
				}
				String retstr = "{action:"+Util.ACTION_PEER_CAMERA_OPEN+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);
				
				mContext.removeStickyBroadcast(intent);
			} else if (action.equals(Util.ACTION_PEER_CAMERA_CLOSE)) {						
				AvSdkAppDelegate.getQavsdkControlPair().setRemoteHasVideo(false, mReceiveIdentifier);				
				if (AvSdkAppDelegate.getQavsdkControlPair().isVideo()) {
					if (peerCameraOpend) {
						
					}
					peerCameraOpend = false;			
				}
				String retstr = "{action:"+Util.ACTION_PEER_CAMERA_CLOSE+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);
				mContext.removeStickyBroadcast(intent);				
			} else if (action.equals(Util.ACTION_PEER_MIC_OPEN)) {
				if (!peerMicOpend) {
				
				}
				peerMicOpend = true;
				String retstr = "{action:"+Util.ACTION_PEER_MIC_OPEN+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);
				mContext.removeStickyBroadcast(intent);				
			} else if (action.equals(Util.ACTION_PEER_MIC_CLOSE)) {
				if (peerMicOpend) {
				
				}
				peerMicOpend = false;
				String retstr = "{action:"+Util.ACTION_PEER_MIC_CLOSE+",retcode:"+0+"}";
				jscallback(mJsbstartContext,retstr,false);
				mContext.removeStickyBroadcast(intent);				
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
			}else if (action.equals(Util.ACTION_VIDEO_SHOW)) {
				String identifier = intent.getStringExtra(Util.EXTRA_IDENTIFIER);
				mRecvIdentifier = identifier;
				//单路视频
				//AvSdkAppDelegate.getQavsdkControlMulti().setRemoteHasVideo(true, mRecvIdentifier);
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
				jscallback(mJsbstartContext,retstr,false);
			} else if (action.equals(Util.ACTION_MULTI_OUTPUT_MODE_CHANGE)) {
				
			} else if(action.equals(Util.VIDEO_CLICK)){
				int index = intent.getIntExtra("VIDEO_INDEX", 0);
				int x = intent.getIntExtra("x", 0);
				int y = intent.getIntExtra("y", 0);
				int w = intent.getIntExtra("w", 0);
				int h = intent.getIntExtra("h", 0);
				String retstr = "{action:videoview_clicked,index:"+index+",x:"+x+",y:"+y+",w:"+w+",h:"+h+"}";
				jscallback(mJsbstartContext,retstr,false);
			}
		}
	};
	public LbbAvApi(UZWebView webView) {
		super(webView);
		// TODO Auto-generated constructor stub
		mContext = getContext();
	}

	
	/**
	 * 启动sdk avcontext
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_startAvContextPair(final UZModuleContext moduleContext) {
		mJsbstartContext = moduleContext;
		
		final IntentFilter intentFilter = new IntentFilter();
		// 启动关闭sdk
		intentFilter.addAction(Util.ACTION_START_CONTEXT_COMPLETE);
		intentFilter.addAction(Util.ACTION_CLOSE_CONTEXT_COMPLETE);
		// 双人对话创建房间
		intentFilter.addAction(Util.ACTION_ACCEPT_COMPLETE);
		intentFilter.addAction(Util.ACTION_CLOSE_ROOM_COMPLETE);
		intentFilter.addAction(Util.ACTION_INVITE_ACCEPTED);
		intentFilter.addAction(Util.ACTION_INVITE_CANCELED);
		intentFilter.addAction(Util.ACTION_INVITE_COMPLETE);
		intentFilter.addAction(Util.ACTION_INVITE_REFUSED);
		intentFilter.addAction(Util.ACTION_RECV_INVITE);
		intentFilter.addAction(Util.ACTION_REFUSE_COMPLETE);
		intentFilter.addAction(Util.ACTION_ROOM_CREATE_COMPLETE);
		intentFilter.addAction(Util.ACTION_ROOM_JOIN_COMPLETE);

		// 双人对话视频房间
		intentFilter.addAction(Util.ACTION_SURFACE_CREATED);
		intentFilter.addAction(Util.ACTION_ENABLE_CAMERA_COMPLETE);
		intentFilter.addAction(Util.ACTION_SWITCH_CAMERA_COMPLETE);
		intentFilter.addAction(Util.ACTION_OUTPUT_MODE_CHANGE);
		intentFilter.addAction(Util.ACTION_PEER_LEAVE);
		intentFilter.addAction(Util.ACTION_PEER_CAMERA_OPEN);
		intentFilter.addAction(Util.ACTION_PEER_CAMERA_CLOSE);
		intentFilter.addAction(Util.ACTION_PEER_MIC_OPEN);
		intentFilter.addAction(Util.ACTION_PEER_MIC_CLOSE);
		
		
		//多人专有的广播事件		
		//lbbniu start 多人对话创建房间
		intentFilter.addAction(Util.ACTION_MULTI_ROOM_CREATE_COMPLETE);  //xuyao
		intentFilter.addAction(Util.ACTION_MULTI_CLOSE_ROOM_COMPLETE);   //xuyao
		// 多人对话视频view
		intentFilter.addAction(Util.ACTION_MULTI_SURFACE_CREATED);       //xuyao
		intentFilter.addAction(Util.ACTION_VIDEO_SHOW);            //=====
		intentFilter.addAction(Util.ACTION_VIDEO_CLOSE);		   //=====
		intentFilter.addAction(Util.ACTION_MULTI_ENABLE_CAMERA_COMPLETE); //xuyao
		intentFilter.addAction(Util.ACTION_ENABLE_EXTERNAL_CAPTURE_COMPLETE);//======
		intentFilter.addAction(Util.ACTION_MULTI_SWITCH_CAMERA_COMPLETE);//xuyao
		intentFilter.addAction(Util.ACTION_MEMBER_CHANGE);//====
		intentFilter.addAction(Util.ACTION_MULTI_OUTPUT_MODE_CHANGE);//xuyao
		//lbbniu end 多人对话创建房间
		
		intentFilter.addAction(Util.VIDEO_CLICK);//视频被点击
		
		


		// 网络切换
//		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mContext.registerReceiver(mBroadcastReceiver, intentFilter);
		

		final String identifier = moduleContext.optString("identifier");
		final String usersig = moduleContext.optString("usersig");
		mSelfIdentifier = identifier;
		AvSdkAppDelegate.getQavsdkControlMulti().selfindentify = identifier;//多人
		
		if (AvSdkAppDelegate.getQavsdkControlPair().getAVContext() != null) {
			String retstr = "{action:"+Util.ACTION_START_CONTEXT_COMPLETE+",retcode:0}";
			jscallback(mJsbstartContext,retstr,false);
		} else if (AvSdkAppDelegate.getQavsdkControlPair() != null) {
			AvSdkAppDelegate.setSelfId(mSelfIdentifier);
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub					
					 AvSdkAppDelegate.getQavsdkControlPair().startContext(identifier,usersig);
				}
			});
		}
	}

	/**
	 * 关闭avsdk
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_stopContext(UZModuleContext moduleContext) {
		if (null != AvSdkAppDelegate.getQavsdkControlPair()) {
			AvSdkAppDelegate.getQavsdkControlPair().stopContext();
		}
	}

	/**
	 * 邀请加入双人房间
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_invitePair(UZModuleContext moduleContext) {
		isDuo = false;
		// 邀请人id
		final String peerIdentifier = moduleContext.optString("peerIdentifier");
		mReceiveIdentifier = peerIdentifier;
		AvSdkAppDelegate.setReceiveId(mReceiveIdentifier);
		// 是否开启视频
		final boolean isVideo = moduleContext.optBoolean("isVideo");
		mIsVideo = isVideo;
		if (AvSdkAppDelegate.getQavsdkControlPair() != null) {
			AvSdkAppDelegate.getQavsdkControlPair().invite(peerIdentifier,
					isVideo);
		}
	}

	/**
	 * 接受双人房间邀请
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_pairInviteAccept(UZModuleContext moduleContext) {
		isDuo = false;
		if (AvSdkAppDelegate.getQavsdkControlPair() != null) {
			AvSdkAppDelegate.getQavsdkControlPair().accept();
		}
	}

	/**
	 * 拒绝双人房间邀请
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_pairInviteRefuse(UZModuleContext moduleContext) {
		isDuo = false;
		if (AvSdkAppDelegate.getQavsdkControlPair() != null) {
			AvSdkAppDelegate.getQavsdkControlPair().refuse();
		}
	}

	/**
	 * 进入双人房间
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_enterPairRoom(UZModuleContext moduleContext) {
		isDuo = false;
		long roomId = moduleContext.optLong("roomId");
		String identifier = moduleContext.optString("identifier");
		boolean mIsVideo = moduleContext.optBoolean("isVideo");
		if (AvSdkAppDelegate.getQavsdkControlPair() != null) {
			AvSdkAppDelegate.getQavsdkControlPair().enterRoom(roomId,
					identifier, mIsVideo);
		}
	}

	/**
	 * 退出双人房间
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_exitPairRoom(UZModuleContext moduleContext) {
		isDuo = false;
		if (AvSdkAppDelegate.getQavsdkControlPair() != null) {
			int ret = AvSdkAppDelegate.getQavsdkControlPair().exitRoom();
		}
	}

	/**
	 * 打开关闭 mic麦克风  是否禁用话筒
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_enableMicPair(UZModuleContext moduleContext) {
		boolean enable = moduleContext.optBoolean("enable");
		if(isDuo){
			if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
				if (AvSdkAppDelegate.getQavsdkControlMulti().getAVContext() != null) {
					AvSdkAppDelegate.getQavsdkControlMulti().getAVContext()
							.getAudioCtrl().enableMic(enable);
				}
			}
		}else{
			if (AvSdkAppDelegate.getQavsdkControlPair() != null) {
				if (AvSdkAppDelegate.getQavsdkControlPair().getAVContext() != null) {
					AvSdkAppDelegate.getQavsdkControlPair().getAVContext()
							.getAudioCtrl().enableMic(enable);
				}
			}
		}
	}

	/**
	 * 打开关闭 Speak  是否静音
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_enableSpeakerPair(UZModuleContext moduleContext) {
		boolean enable = moduleContext.optBoolean("enable");
		if(isDuo){
			if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
				if (AvSdkAppDelegate.getQavsdkControlMulti().getAVContext() != null) {
					AvSdkAppDelegate.getQavsdkControlMulti().getAVContext()
							.getAudioCtrl().enableSpeaker(enable);
				}
			}
		}else{
			if (AvSdkAppDelegate.getQavsdkControlPair() != null) {
				if (AvSdkAppDelegate.getQavsdkControlPair().getAVContext() != null) {
					AvSdkAppDelegate.getQavsdkControlPair().getAVContext()
							.getAudioCtrl().enableSpeaker(enable);
				}
			}
		}
	}

	/**
	 * 打开双人对话视频页面
	 */
	@UzJavascriptMethod
	public void jsmethod_openPairVideoView(UZModuleContext moduleContext) {
		isDuo = false;
		mSelfIdentifier = AvSdkAppDelegate.getSelfId();
		mReceiveIdentifier = AvSdkAppDelegate.getReceiveId();
		Log.v("testlbbniu","-----------------"+mSelfIdentifier+"-----------------------------"+mReceiveIdentifier+"---------------------");
		
		//jscallback(moduleContext,"{mSelfIdentifier:"+mSelfIdentifier+",mReceiveIdentifier:"+mReceiveIdentifier+"}",true);
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
		
		if (AvSdkAppDelegate.getQavsdkControlPair() != null) {
			if (AvSdkAppDelegate.getQavsdkControlPair().getAVContext() != null) {
				if(AvSdkAppDelegate.getQavsdkControlPair().getAVContext().getAudioCtrl() != null){
					AvSdkAppDelegate.getQavsdkControlPair().getAVContext().getAudioCtrl().startTRAEService();
					if (mContext instanceof Activity) {
						videoPair = (View) LayoutInflater
								.from(mContext)
								.inflate(UZResourcesIDFinder.getResLayoutID("qav_video_layer_ui"),null);
						videoPair.setClickable(true);
						videoPair.setFocusable(true);
						videoPair.setOnClickListener(new OnClickListener() {						
							@Override
							public void onClick(View v) {
								Log.v("----------","---------------------lbbniu-----------------------------"+mReceiveIdentifier+"---------------------");
								// TODO Auto-generated method stub
								String retstr = "{action:videoview_clicked}";
								jscallback(mJsbstartContext,retstr,false);
							}
						});
						insertViewToCurWindow(videoPair, rlp);
						AvSdkAppDelegate.getQavsdkControlPair().onCreate(mContext,
								videoPair);
						String retstr = "{action:success}";
						jscallback(moduleContext,retstr,true);
						videoPair.invalidate();
					}
				}
				
			}
		}
	}
	
	/**
	 * 重复利用现有的画面
	 */
	@UzJavascriptMethod
	public void jsmethod_reuserPairVideoView(UZModuleContext moduleContext) {
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
		
		if(null != videoPair){
			insertViewToCurWindow(videoPair, rlp);
		}
	}
	
	/**
	 * 刷新界面
	 */
	public void jsmethod_invalidate(UZModuleContext moduleContext) {
		isDuo = false;
		if(null != videoPair){
			videoPair.invalidate();
			Log.v("test", "--------------mReceiveIdentifier-------jsmethod_invalidate---" + mReceiveIdentifier);
		}
	}
	

	/**
	 * 打开关闭照相机
	 */
	@UzJavascriptMethod
	public void jsmethod_toggleEnableCameraPair(UZModuleContext moduleContext) {
		if(isDuo){
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
		}else{
			if (AvSdkAppDelegate.getQavsdkControlPair() != null) {
				if (AvSdkAppDelegate.getQavsdkControlPair().getAVContext() != null) {
					int mOnOffCameraErrorCode = AvSdkAppDelegate
							.getQavsdkControlPair().toggleEnableCamera();
					if (mOnOffCameraErrorCode != AVError.AV_OK) {
						AvSdkAppDelegate.getQavsdkControlPair().setIsInOnOffCamera(
								false);
					}
				}
			}
		}
	}

	/**
	 * 查看照相机是否打开
	 */
	public void jsmethod_IsEnableCameraPair(UZModuleContext moduleContext) {
		if(isDuo){
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
		}else{
			if (AvSdkAppDelegate.getQavsdkControlPair() != null) {
				if (AvSdkAppDelegate.getQavsdkControlPair().getAVContext() != null) {
					boolean isenable = AvSdkAppDelegate.getQavsdkControlPair()
							.getIsEnableCamera();
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
	}

	/**
	 * 设置是否免提
	 */
	@UzJavascriptMethod
	public void jsmethod_setHandfreePair(UZModuleContext moduleContext) {
		if(isDuo){
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
		}else{
			if (AvSdkAppDelegate.getQavsdkControlPair() != null) {
				if (AvSdkAppDelegate.getQavsdkControlPair().getAVContext() != null) {
					int mOnOffCameraErrorCode = AvSdkAppDelegate
							.getQavsdkControlPair().toggleEnableCamera();
					if (mOnOffCameraErrorCode != AVError.AV_OK) {
						AvSdkAppDelegate
								.getQavsdkControlPair()
								.getAVContext()
								.getAudioCtrl()
								.setAudioOutputMode(
										AvSdkAppDelegate.getQavsdkControlPair()
												.getHandfreeChecked() ? AVAudioCtrl.OUTPUT_MODE_SPEAKER
												: AVAudioCtrl.OUTPUT_MODE_HEADSET);
					}
				}
			}
		}
	}

	/**
	 * 查看照相机是否免提
	 */
	public void jsmethod_IsHandfreePair(UZModuleContext moduleContext) {
		if(isDuo){
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
		}else{
			if (AvSdkAppDelegate.getQavsdkControlPair() != null) {
				if (AvSdkAppDelegate.getQavsdkControlPair().getAVContext() != null) {
					boolean isenable = AvSdkAppDelegate.getQavsdkControlPair()
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
	}
	
	/**
	 * 切换前后摄像头
	 */
	public void jsmethod_toggleSwitchCamera(UZModuleContext moduleContext) {
		if(isDuo){
			if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
				if (AvSdkAppDelegate.getQavsdkControlMulti().getAVContext() != null) {
					int ret = AvSdkAppDelegate.getQavsdkControlMulti()
							.toggleSwitchCamera();
				}
			}
		}else{
			if (AvSdkAppDelegate.getQavsdkControlPair() != null) {
				if (AvSdkAppDelegate.getQavsdkControlPair().getAVContext() != null) {
					int ret = AvSdkAppDelegate.getQavsdkControlPair()
							.toggleSwitchCamera();
				}
			}
		}
	}
	/**
	 * 挂断
	 */
	@UzJavascriptMethod
	public void jsmethod_hangupPair(UZModuleContext moduleContext) {
		if(isDuo){//多人
			if (videoPairM != null) {
				removeViewFromCurWindow(videoPairM);
			}
			if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
				AvSdkAppDelegate.getQavsdkControlMulti().onDestroy();
			}
		}else{
			if (videoPair != null) {
				removeViewFromCurWindow(videoPair);
			}
			if (AvSdkAppDelegate.getQavsdkControlPair() != null) {
				AvSdkAppDelegate.getQavsdkControlPair().onDestroy();
			}
		}
		
	}
	//==========================================================================================================================
	/**
	 * 创建多人房间
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_createMultiRoom(UZModuleContext moduleContext) {
		isDuo = true;
		Log.v("test", "--------jsmethod_createMultiRoom---------"+ moduleContext.toString());
		if (Util.isNetworkAvailable(mContext)) {
			int relationId = moduleContext.optInt("relationId");
			relationId = 6666;
			if (relationId != 0) {
				AvSdkAppDelegate.getQavsdkControlMulti().enterRoom(relationId);
				handler.sendEmptyMessageDelayed(MSG_CREATEROOM_TIMEOUT, MAX_TIMEOUT);				
			}	
		} else {
			jscallback(mJsbstartContext,"{msg:network_not_find}",false);
		}
	}
	/**
	 * 退出多人房间
	 * 
	 * @param moduleContext
	 */
	@UzJavascriptMethod
	public void jsmethod_exitMultiRoom(UZModuleContext moduleContext) {
		isDuo = true;
		if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
			int ret = AvSdkAppDelegate.getQavsdkControlMulti().exitRoom();
		}
	}
	/**
	 * 打开多人对话视频页面
	 */
	@UzJavascriptMethod
	public void jsmethod_openMultiVideoView(UZModuleContext moduleContext) {
		isDuo = true;
		//int relationId = moduleContext.optInt("relationId");
		//mSelfIdentifier = moduleContext.optString("selfIdentifier");
		//mRecvIdentifier = moduleContext.optString("mRecvIdentifier");
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
						videoPairM = (View) LayoutInflater
								.from(mContext)
								.inflate(UZResourcesIDFinder.getResLayoutID("av_activity"),null);
						insertViewToCurWindow(videoPairM, rlp);
						AvSdkAppDelegate.getQavsdkControlMulti().onCreate(mContext,
								videoPairM);
						String retstr = "{action:success}";
						jscallback(moduleContext,retstr,true);
						Log.v("test", "--------jsmethod_createMultiRoom----11111-----");
					}
				}
				
			}
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
	
	/**
	 * 课件与视频交换
	 */
	@UzJavascriptMethod
	public void jsmethod_switchVideo(UZModuleContext moduleContext) {
		if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
			int index = moduleContext.optInt("index");
			AvSdkAppDelegate.getQavsdkControlMulti().switchVideo(0, index);
			//String retstr = "{action:getMemberList,data:"+jsonarray.toString()+"}";
			//jscallback(mJsbstartContext,retstr,false);
		}
	}
	
	/**
	 * 课件与视频交换
	 */
	@UzJavascriptMethod
	public void jsmethod_setIndex(UZModuleContext moduleContext) {
		if (AvSdkAppDelegate.getQavsdkControlMulti() != null) {
			AvSdkAppDelegate.index = moduleContext.optInt("index");
			//String retstr = "{action:getMemberList,data:"+jsonarray.toString()+"}";
			//jscallback(mJsbstartContext,retstr,false);
		}
	}
	
	public void jscallback(UZModuleContext moduleContext,String json,boolean del){
		if(moduleContext != null){
			try {
				Log.v("--------", "---------"+json+"------");
				JSONObject obj = new JSONObject(json);
				moduleContext.success(obj, del);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	
}
