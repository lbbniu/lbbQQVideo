package com.apicloud.avSdkApp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.apicloud.avSdkControl.multi.QavsdkControlMulti;
import com.apicloud.avSdkControl.pair.QavsdkControlPair;
import com.tencent.av.sdk.AVContext;
import com.uzmap.pkg.uzcore.uzmodule.ApplicationDelegate;

public class AvSdkAppDelegate extends ApplicationDelegate {
	
	private static QavsdkControlPair mQavsdkControlPair = null;
	private static QavsdkControlMulti mQavsdkControlMulti = null;
	private static String mReceiveIdentifier = "";
	private static String mSelfIdentifier = "";
	public static AVContext mAVContext = null;
	public static int index=0;
	public AvSdkAppDelegate() {
		//应用运行期间，该对象只会初始化一个出来
	}
	public static void setSelfId(String selfId){
		mSelfIdentifier = selfId;
	}
	public static String getSelfId(){
		return mSelfIdentifier;
	}
	
	public static void setReceiveId(String receiveId){
		mSelfIdentifier = receiveId;
	}
	public static String getReceiveId(){
		return mReceiveIdentifier;
	}
	@Override
	public void onApplicationCreate(Context context) {
		//请在这个函数中初始化模块中需要在ApplicationCreate中初始化的东西
		mQavsdkControlPair = new QavsdkControlPair(context);
		mQavsdkControlMulti = new QavsdkControlMulti(context);
	}

	@Override
	public void onActivityResume(Activity activity) {
		if(mQavsdkControlPair != null && mQavsdkControlPair.getAVContext() != null){
			mQavsdkControlPair.onResume();
		}
		if(mQavsdkControlMulti != null && mQavsdkControlMulti.getAVContext() != null){
			mQavsdkControlMulti.onResume();
		}
	}

	@Override
	public void onActivityPause(Activity activity) {
		if(mQavsdkControlPair != null && mQavsdkControlPair.getAVContext() != null){
			mQavsdkControlPair.onPause();
		}
		if(mQavsdkControlMulti != null && mQavsdkControlMulti.getAVContext() != null){
			mQavsdkControlMulti.onPause();
		}
	}

	@Override
	public void onActivityFinish(Activity activity) {
		if(mQavsdkControlPair != null && mQavsdkControlPair.getAVContext() != null){
			mQavsdkControlPair.stopContext();
			mQavsdkControlPair.onDestroy();
		}
		if(mQavsdkControlMulti != null && mQavsdkControlMulti.getAVContext() != null){
			mQavsdkControlMulti.stopContext();
			mQavsdkControlMulti.onDestroy();
		}
	}
	
	public static QavsdkControlMulti getQavsdkControlMulti() {
		return mQavsdkControlMulti;
	}
	
	public static QavsdkControlPair getQavsdkControlPair() {
		return mQavsdkControlPair;
	}
}
