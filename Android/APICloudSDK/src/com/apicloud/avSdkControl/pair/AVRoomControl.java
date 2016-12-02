package com.apicloud.avSdkControl.pair;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.apicloud.avSdkApp.AvSdkAppDelegate;
import com.apicloud.avSdkControl.Util;
import com.tencent.av.sdk.AVContext;
import com.tencent.av.sdk.AVEndpoint;
import com.tencent.av.sdk.AVError;
import com.tencent.av.sdk.AVRoom;
import com.tencent.av.sdk.AVRoomPair;

class AVRoomControl {
	private static final String TAG = "AVRoomControl";
	private boolean mIsInCreateRoom = false;
	private boolean mIsInCloseRoom = false;
	private boolean mIsInJoinRoom = false;
	private boolean mIsVideo = false;
	private long mRoomId = 0;
	private Context mContext = null;
	private boolean mIsCreateRoom = true;
	
	private boolean mPeerHasAudio = false;
	private boolean mPeerHasVideo = false;
	private static final int TYPE_MEMBER_CHANGE_IN = 1;
	private static final int TYPE_MEMBER_CHANGE_OUT = TYPE_MEMBER_CHANGE_IN + 1;
	private static final int TYPE_MEMBER_CHANGE_UPDATE = TYPE_MEMBER_CHANGE_OUT + 1;

	private AVRoomPair.Delegate mRoomDelegate = new AVRoomPair.Delegate() {
		// 创建房间成功回调
		protected void onEnterRoomComplete(int result) {
			Log.e(TAG, "WL_DEBUG mRoomDelegate.onEnterRoomComplete result = "
					+ result);
			mIsInCreateRoom = false;
			mIsInJoinRoom = false;
			
			if(mIsCreateRoom)
			{
				QavsdkControlPair qavsdkControl = AvSdkAppDelegate.getQavsdkControlPair();
				AVRoomPair roomPair = (AVRoomPair) qavsdkControl.getRoom();
				if (roomPair != null && result == AVError.AV_OK) {
					mRoomId = roomPair.getRoomId();
					qavsdkControl.inviteIntenal();
					Log.d(TAG, "onEnterRoomComplete. roomId = " + mRoomId);
				} else {
					mRoomId = 0;
					Log.e(TAG, "onEnterRoomComplete. mRoomPair == null");
				}
				mContext.sendBroadcast(new Intent(Util.ACTION_ROOM_CREATE_COMPLETE)
						.putExtra(Util.EXTRA_ROOM_ID, mRoomId).putExtra(
								Util.EXTRA_AV_ERROR_RESULT, result));
			}
			else
			{
				Log.d(TAG, "OnRoomJoinComplete. result = " + result);
				mContext.sendBroadcast(new Intent(Util.ACTION_ROOM_JOIN_COMPLETE)
						.putExtra(Util.EXTRA_AV_ERROR_RESULT, result));
			}
		}

		// 离开房间成功回调
		protected void onExitRoomComplete(int result) {
			Log.d(TAG, "onExitRoomComplete. result = " + result);
			mIsInCloseRoom = false;
			mContext.sendBroadcast(new Intent(Util.ACTION_CLOSE_ROOM_COMPLETE));	
		}
		
/*		protected void onEndpointsEnterRoom(int endpointCount, AVEndpoint endpointList[]) {
			Log.d(TAG, "WL_DEBUG onEndpointsEnterRoom. endpointCount = " + endpointCount);
			//nothing to do
			//mContext.sendBroadcast(new Intent(Util.ACTION_PEER_ENTER));
		}

		protected void onEndpointsExitRoom(int endpointCount, AVEndpoint endpointList[]) {
			Log.d(TAG, "WL_DEBUG onEndpointsExitRoom. endpointCount = " + endpointCount);
			mContext.sendBroadcast(new Intent(Util.ACTION_PEER_LEAVE));
		}*/
		
		@SuppressWarnings("deprecation")
		protected void onEndpointsUpdateInfo(int eventid, String[] updateList) {
			Log.d(TAG, "onEndpointsUpdateInfo. eventid = "
					+ eventid);
			if (TYPE_MEMBER_CHANGE_IN == eventid) 
			{
				//nothing to do
				//mContext.sendBroadcast(new Intent(Util.ACTION_PEER_ENTER));
			}
			else if(TYPE_MEMBER_CHANGE_OUT == eventid)
			{
				mContext.sendBroadcast(new Intent(Util.ACTION_PEER_LEAVE));
			}
			else// if(TYPE_MEMBER_CHANGE_UPDATE == eventid)
			{
				QavsdkControlPair qavsdkControl = AvSdkAppDelegate.getQavsdkControlPair();
				
				String peerIdentifier = qavsdkControl.getPeerIdentifier();
				boolean peerHasAudioOld = mPeerHasAudio;
				boolean peerHasVideoOld = mPeerHasVideo;
				
				int i = 0;
				for(; i < updateList.length; i++)
				{				
					if(peerIdentifier.equals(updateList[i]))
					{
						AVRoomPair avRoomPair = ((AVRoomPair) qavsdkControl.getRoom());
						AVEndpoint endpoint = avRoomPair.getEndpointById(updateList[i]);
						
						if(endpoint != null)
						{
							mPeerHasAudio = endpoint.hasAudio();
							mPeerHasVideo = endpoint.hasVideo();
						}
						
						break;
					}
				}
				
				//if(i < updateList.length)
				{
					//--------------lbbniu----------------onEndpointsUpdateInfo. mPeerHasAudio = false, peerHasAudioOld = false, mPeerHasVideo = true, peerHasVideoOld = true
					Log.d(TAG, "--------------lbbniu----------------onEndpointsUpdateInfo. mPeerHasAudio = " + mPeerHasAudio + ", peerHasAudioOld = " + peerHasAudioOld
							+ ", mPeerHasVideo = " + mPeerHasVideo+ ", peerHasVideoOld = " + peerHasVideoOld);
					mContext.sendStickyBroadcast(new Intent(mPeerHasAudio ? Util.ACTION_PEER_MIC_OPEN : Util.ACTION_PEER_MIC_CLOSE));
					//mContext.sendBroadcast(new Intent(mPeerHasAudio ? Util.ACTION_PEER_MIC_OPEN : Util.ACTION_PEER_MIC_CLOSE));
					
					mContext.sendStickyBroadcast(new Intent(mPeerHasVideo ? Util.ACTION_PEER_CAMERA_OPEN : Util.ACTION_PEER_CAMERA_CLOSE));
					//mContext.sendBroadcast(new Intent(mPeerHasVideo ? Util.ACTION_PEER_CAMERA_OPEN : Util.ACTION_PEER_CAMERA_CLOSE));
				}	
			}
		}
		
		protected void OnPrivilegeDiffNotify(int privilege) {
			Log.d(TAG, "OnPrivilegeDiffNotify. privilege = " + privilege);
		}
		/*
		protected void OnCameraStart() 
		{
			Log.d(TAG, "OnCameraStart"  );	
			mContext.sendBroadcast(new Intent(Util.ACTION_PEER_CAMERA_OPEN));			
		}
		protected void OnCameraClose() 
		{
			Log.d(TAG, "OnCameraClose"  );	
			mContext.sendBroadcast(new Intent(Util.ACTION_PEER_CAMERA_CLOSE));	
		}		
		protected void OnMicStart()
		{
			mContext.sendBroadcast(new Intent(Util.ACTION_PEER_MIC_OPEN));				
			Log.d(TAG, "OnMicStart"  );	
		}
		protected void OnMicClose()
		{
			mContext.sendBroadcast(new Intent(Util.ACTION_PEER_MIC_CLOSE));				
			Log.d(TAG, "OnMicClose"  );	
//			mContext.sendBroadcast(new Intent(Util.ACTION_PEER_LEAVE));
		}	
		*/	
	
	};


	AVRoomControl(Context context) {
		mContext = context;
	}

	/**
	 * 创建房间
	 * 
	 * @param isVideo
	 *            是否有视�
	 */
	void enterRoom(long roomId, String peerIdentifier, boolean isVideo) {	
		mIsCreateRoom = (roomId == 0 ? true : false);
		QavsdkControlPair qavsdkControl = AvSdkAppDelegate.getQavsdkControlPair();
		if ((qavsdkControl != null) && (qavsdkControl.getAVContext() != null)) {			
			Log.e(TAG, "WL_DEBUG enterRoom peerIdentifier = " + peerIdentifier + ", roomId = " + roomId + ", isVideo = " + isVideo);
		
			AVRoom.EnterRoomParam enterRoomParam = new AVRoomPair.EnterRoomParam(roomId, isVideo ? AVRoom.AV_MODE_VIDEO : AVRoom.AV_MODE_AUDIO, peerIdentifier);
			int audioCategory = AVRoom.AUDIO_CATEGORY_VOICECHAT;//音频场景策略；有三种取值：实时通信场景，直播场景中的主播人员，直播场景中的听众；TODO：请业务侧根据自己的情况填这个值�
			 
			// create room
			qavsdkControl.getAVContext().enterRoom(AVRoom.AV_ROOM_PAIR, mRoomDelegate, enterRoomParam);
			if(mIsCreateRoom) mIsInCreateRoom = true;
			else mIsInJoinRoom = true;
			mIsVideo = isVideo;		
		} else {
			Log.e(TAG, "WL_DEBUG enterRoom qavsdkControl = " + (qavsdkControl==null));			
			mIsInCreateRoom = false;
			mIsInJoinRoom = false;
			mIsVideo = false;				
		}
	}



	/** 关闭房间 */
	int exitRoom() {
		Log.e(TAG, "WL_DEBUG exitRoom");
		QavsdkControlPair qavsdk = AvSdkAppDelegate.getQavsdkControlPair();
		if ((qavsdk != null) && (qavsdk.getAVContext() != null)) {
			AVContext avContext = qavsdk.getAVContext();
			int result = avContext.exitRoom();
			qavsdk.setPeerIdentifier(""); //lbbniu
			mIsInCloseRoom = true;	
			return result;	
		} else {
			Log.e(TAG, "WL_DEBUG exitRoom qavsdkControl = " + (qavsdk==null));					
			mIsInCloseRoom = false;	
			return -1;		
		}

	}

	boolean getIsInCreateRoom() {
		return mIsInCreateRoom;
	}

	boolean getIsInCloseRoom() {
		return mIsInCloseRoom;
	}

	boolean getIsInJoinRoom() {
		return mIsInJoinRoom;
	}

	boolean getIsVideo() {
		return mIsVideo;
	}

	long getRoomId() {
		return mRoomId;
	}

	void setRoomId(long roomId) {
		mRoomId = roomId;
	}
	
	public void setNetType(int netType) {
		QavsdkControlPair qavsdk = AvSdkAppDelegate.getQavsdkControlPair();
		AVContext avContext = qavsdk.getAVContext();
		AVRoomPair room = (AVRoomPair)avContext.getRoom();
	}
}