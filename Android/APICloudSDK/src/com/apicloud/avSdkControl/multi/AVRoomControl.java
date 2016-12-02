package com.apicloud.avSdkControl.multi;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.apicloud.avSdkApp.AvSdkAppDelegate;
import com.apicloud.avSdkControl.MemberInfo;
import com.apicloud.avSdkControl.Util;
import com.tencent.av.sdk.AVConstants;
import com.tencent.av.sdk.AVContext;
import com.tencent.av.sdk.AVEndpoint;
import com.tencent.av.sdk.AVRoom;
import com.tencent.av.sdk.AVRoomMulti;


class AVRoomControl {
	private static final int TYPE_MEMBER_CHANGE_IN = 3;
	private static final int TYPE_MEMBER_CHANGE_OUT = TYPE_MEMBER_CHANGE_IN + 1;
	private static final int TYPE_MEMBER_CHANGE_UPDATE = TYPE_MEMBER_CHANGE_OUT + 1;
	private static final String TAG = "AVRoomControl";
	private boolean mIsInCreateRoom = false;
	private boolean mIsInCloseRoom = false;
	private Context mContext;
	private ArrayList<MemberInfo> mMemberList = new ArrayList<MemberInfo>();
	
	private int audioCat = 0;
	public void setAudioCat(int audioCat) {
		this.audioCat = audioCat;
	}

	/*private AVRoomMulti.Delegate mRoomDelegate = new AVRoomMulti.Delegate() {
		// 创建房间成功回调
		protected void onEnterRoomComplete(int result) {
			Log.d(TAG, "WL_DEBUG mRoomDelegate.onEnterRoomComplete result = " + result);
			mIsInCreateRoom = false;
			mContext.sendBroadcast(new Intent(Util.ACTION_ROOM_CREATE_COMPLETE).putExtra(Util.EXTRA_AV_ERROR_RESULT, result));
		}
		
		// 离开房间成功回调
		protected void onExitRoomComplete(int result) {
			Log.d(TAG, "WL_DEBUG mRoomDelegate.onExitRoomComplete result = " + result);
			mIsInCloseRoom = false;
			mMemberList.clear();
			mContext.sendBroadcast(new Intent(Util.ACTION_CLOSE_ROOM_COMPLETE));			
		}

		protected void onEndpointsEnterRoom(int endpointCount, AVEndpoint endpointList[]) {
			Log.d(TAG, "WL_DEBUG onEndpointsEnterRoom. endpointCount = " + endpointCount);
			onMemberChange(TYPE_MEMBER_CHANGE_IN, endpointList, endpointCount);
		}

		protected void onEndpointsExitRoom(int endpointCount, AVEndpoint endpointList[]) {
			Log.d(TAG, "WL_DEBUG onEndpointsExitRoom. endpointCount = " + endpointCount);
			onMemberChange(TYPE_MEMBER_CHANGE_OUT, endpointList, endpointCount);
		}

		protected void onEndpointsUpdateInfo(int endpointCount, AVEndpoint endpointList[]) {
			Log.d(TAG, "WL_DEBUG onEndpointsUpdateInfo. endpointCount = " + endpointCount);
			onMemberChange(TYPE_MEMBER_CHANGE_UPDATE, endpointList, endpointCount);
		}
				
		protected void OnPrivilegeDiffNotify(int privilege) {
			Log.d(TAG, "OnPrivilegeDiffNotify. privilege = " + privilege);
		}
	};*/
	
	private AVRoomMulti.Delegate mRoomDelegate = new AVRoomMulti.Delegate() {
		// 创建房间成功回调
		protected void onEnterRoomComplete(int result) {
			Log.d(TAG, "WL_DEBUG mRoomDelegate.onEnterRoomComplete result = " + result);
			mIsInCreateRoom = false;
			mContext.sendBroadcast(new Intent(Util.ACTION_MULTI_ROOM_CREATE_COMPLETE).putExtra(Util.EXTRA_AV_ERROR_RESULT, result));
		}
		
		// 离开房间成功回调
		protected void onExitRoomComplete(int result) {
			Log.d(TAG, "WL_DEBUG mRoomDelegate.onExitRoomComplete result = " + result);
			mIsInCloseRoom = false;
			mMemberList.clear();
			mContext.sendBroadcast(new Intent(Util.ACTION_MULTI_CLOSE_ROOM_COMPLETE));			
		}
/*
		protected void onEndpointsEnterRoom(int endpointCount, AVEndpoint endpointList[]) {
			Log.d(TAG, "WL_DEBUG onEndpointsEnterRoom. endpointCount = " + endpointCount);
			onMemberChange(TYPE_MEMBER_CHANGE_IN, endpointList, endpointCount);
		}

		protected void onEndpointsExitRoom(int endpointCount, AVEndpoint endpointList[]) {
			Log.d(TAG, "WL_DEBUG onEndpointsExitRoom. endpointCount = " + endpointCount);
			onMemberChange(TYPE_MEMBER_CHANGE_OUT, endpointList, endpointCount);
		}
*/
		protected void onEndpointsUpdateInfo(int eventid, String[] updateList) {
			Log.d(TAG, "WL_DEBUG onEndpointsUpdateInfo. eventid = " + eventid);
			onMemberChange(eventid, updateList);
		}
				
		protected void OnPrivilegeDiffNotify(int privilege) {
			Log.d(TAG, "OnPrivilegeDiffNotify. privilege = " + privilege);
		}
	};


	AVRoomControl(Context context) {
		mContext = context;
	}

	/**
	 * 成员列表变化
	 * 
	 * @param type
	 *            类型
	 * @param endpointList
	 *            成员列表
	 * @param endpointCount
	 *            成员总数
	 */
	private void onMemberChange(int eventid, String[] updateList) {
		Log.d(TAG, "WL_DEBUG onMemberChange type = " + eventid);
		Log.d(TAG, "WL_DEBUG onMemberChange endpointCount = " + updateList.length);
		int endpointCount = updateList.length;
		QavsdkControlMulti qavsdk = AvSdkAppDelegate.getQavsdkControlMulti();
		AVRoomMulti avRoomMulti = ((AVRoomMulti) qavsdk.getRoom());
		/*
		for (int i = 0; i < endpointCount; i++) {
			Log.d(TAG, "WL_DEBUG onMemberChange endpointList[" + i + "].getInfo().openId = " + endpointList[i].getInfo().openId);
			Log.d(TAG, "WL_DEBUG onMemberChange endpointList[" + i + "].hasVideo() = " + endpointList[i].hasVideo());
		}
		*/
		//String selfIdentifier = ((QavsdkApplication) mContext).getQavsdkControl().getSelfIdentifier();
		if (TYPE_MEMBER_CHANGE_IN == eventid) {/*1.3测试进度风险，先和老版本保持一直，不展示非语音视频成员信息*/
			//*1.3测试进度风险，先和老版本保持一直，不展示非语音视频成员信息
			for (int i = 0; i < endpointCount; i++) {
				AVEndpoint endpoint = avRoomMulti.getEndpointById(updateList[i]);
				AVEndpoint.Info userInfo = endpoint.getInfo();		
				String identifier = userInfo.openId;	
				boolean bExist = false;
				for(int j = 0; j < mMemberList.size(); j++) {
					if (mMemberList.get(j).identifier.equals(identifier)) {
						bExist = true;
						break;
					}	
				}

				if (!bExist) {
					MemberInfo info = new MemberInfo();
					info.identifier = userInfo.openId;
					info.name = userInfo.openId;
					info.isVideoIn = endpoint.hasVideo();
					info.isSpeaking = endpoint.hasAudio();
					mMemberList.add(info);	
				}
			}
			//*/
			//lbbniu start
			if(mMemberList != null && mMemberList.size() >=2){
				int count = mMemberList.size() - 1;
				String[] identifierArray = new String[mMemberList.size()];
				AVEndpoint.View[] endpoidViewArray = new AVEndpoint.View[mMemberList.size()];
				int index = 0;
				for( int j = 0; j < mMemberList.size(); j++) {
					final String identer = mMemberList.get(j).identifier;
					if (!identer.equals(AvSdkAppDelegate.getQavsdkControlMulti().selfindentify))
					{
						identifierArray[index] = identer;
						AVEndpoint.View  viewAVEndpoint= new AVEndpoint.View();
						viewAVEndpoint.videoSrcType = AVEndpoint.View.VIDEO_SRC_TYPE_CAMERA;//SDK1.2版本只支持摄像头视频源，所以当前只能设置为VIDEO_SRC_TYPE_CAMERA。
						viewAVEndpoint.viewSizeType = AVEndpoint.View.VIEW_SIZE_TYPE_SMALL;
						endpoidViewArray[index]   = viewAVEndpoint;
						index ++;
					}
				}
				
				AVEndpoint.requestViewList(identifierArray, endpoidViewArray, count, new AVEndpoint.RequestViewListCompleteCallback () {
					protected void OnComplete(java.lang.String[] identifierList, int count, int result) {
						// TODO
						Log.d(TAG, "RequestViewCompleteCallback.OnComplete");
						for (int i = 0; i < identifierList.length; i++) {
							AvSdkAppDelegate.getQavsdkControlMulti().setRemoteHasVideo(identifierList[i], AVConstants.VIDEO_SRC_CAMERA, true);
						}
						
					}
				});
			}
			//lbbniu end
		} else if (TYPE_MEMBER_CHANGE_OUT == eventid) {
			for (int i = 0; i < endpointCount; i++) {
				for (int j = 0; j < mMemberList.size(); j++) {
					if (mMemberList.get(j).identifier.equals(updateList[i])) {
						mMemberList.remove(j);
						break;
					}
				}
			}
		} else{
			for (int i = 0; i < endpointCount; i++) {
				AVEndpoint endpoint = avRoomMulti.getEndpointById(updateList[i]);
				if(endpoint == null)//endpoint is not exist at all
				{
					for (int k=0; k<mMemberList.size(); k++) {
						if (mMemberList.get(k).identifier.equals(updateList[i])){
							mMemberList.remove(k);
							break;
						}
					}
					continue;
				}	
				
				AVEndpoint.Info userInfo = endpoint.getInfo();
				String identifier = userInfo.openId;
				boolean identifierExist = false;
				for (int j = 0; j < mMemberList.size(); j++) {
					if (mMemberList.get(j).identifier.equals(identifier)) {
						mMemberList.remove(j);
						MemberInfo info = new MemberInfo();
						info.identifier = userInfo.openId;
						info.name = userInfo.openId;
						info.isVideoIn = endpoint.hasVideo();
						info.isSpeaking = endpoint.hasAudio();						
						mMemberList.add(j, info);
						identifierExist = true;
						break;
					}
				}
				
				if (!identifierExist) {
					MemberInfo info = new MemberInfo();
					info.identifier = userInfo.openId;
					info.name = userInfo.openId;
					info.isVideoIn = endpoint.hasVideo();
					info.isSpeaking = endpoint.hasAudio();					
					mMemberList.add(info);	
				}
			}
			
			for (int i=0; i<mMemberList.size(); i++) {
				MemberInfo info = mMemberList.get(i);
				if (info != null) {
					if (!info.isSpeaking && !info.isVideoIn) {
						mMemberList.remove(i);
					}
				}	
			}
		}

		for (int i = 0; i < mMemberList.size(); i++) {
			Log.d(TAG, "WL_DEBUG onMemberChange mMemberList.get(" + i + ") = " + mMemberList.get(i));
		}

		mContext.sendBroadcast(new Intent(Util.ACTION_MEMBER_CHANGE));
	}

	/**
	 * 创建房间
	 * 
	 * @param relationId
	 *            讨论组号
	 */
	void enterRoom(int relationId) {
		Log.d(TAG, "WL_DEBUG enterRoom relationId = " + relationId);
		QavsdkControlMulti qavsdk = AvSdkAppDelegate.getQavsdkControlMulti();
		AVContext avContext = qavsdk.getAVContext();
		long authBits = AVRoom.AUTH_BITS_DEFUALT;//权限位；默认值是拥有所有权限。TODO：请业务侧填根据自己的情况填上权限位�
		byte[] authBuffer = null;//权限位加密串；TODO：请业务侧填上自己的加密串�
		
		AVRoom.EnterRoomParam enterRoomParam = new AVRoomMulti.EnterRoomParam(relationId, authBits, authBuffer, "", audioCat);
		// create room
		avContext.enterRoom(AVRoom.AV_ROOM_MULTI, mRoomDelegate, enterRoomParam);
		mIsInCreateRoom = true;
	}

	/** 关闭房间 */
	int exitRoom() {
		Log.d(TAG, "WL_DEBUG exitRoom");
		QavsdkControlMulti qavsdk = AvSdkAppDelegate.getQavsdkControlMulti();
		AVContext avContext = qavsdk.getAVContext();
		int result = avContext.exitRoom();
		mIsInCloseRoom = true;

		return result;
	}

	/**
	 * 获取成员列表
	 * 
	 * @return 成员列表
	 */
	ArrayList<MemberInfo> getMemberList() {
		return mMemberList;
	}

	boolean getIsInEnterRoom() {
		return mIsInCreateRoom;
	}

	boolean getIsInCloseRoom() {
		return mIsInCloseRoom;
	}
	
	public void setCreateRoomStatus(boolean status) {
		mIsInCreateRoom = status;
	}
	public void setCloseRoomStatus(boolean status) {
		mIsInCloseRoom = status;
	}
	
	public void setNetType(int netType) {
		QavsdkControlMulti qavsdk = AvSdkAppDelegate.getQavsdkControlMulti();
		AVContext avContext = qavsdk.getAVContext();
		AVRoomMulti room = (AVRoomMulti)avContext.getRoom();
	}
}