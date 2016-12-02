package com.apicloud.avSdkControl.multi;

import android.content.Context;
import android.content.Intent;

import com.apicloud.avSdkApp.AvSdkAppDelegate;
import com.apicloud.avSdkControl.Util;
import com.tencent.av.sdk.AVAudioCtrl;
import com.tencent.av.sdk.AVAudioCtrl.Delegate;

public class AVAudioControl {
	private Context mContext = null;
	
	private Delegate mDelegate = new Delegate() {
		@Override
		protected void onOutputModeChange(int outputMode) {
            super.onOutputModeChange(outputMode);
            mContext.sendBroadcast(new Intent(Util.ACTION_MULTI_OUTPUT_MODE_CHANGE));
        }
	};
	
	AVAudioControl(Context context) {
		mContext = context;
	}
	
	void initAVAudio() {
		QavsdkControlMulti qavsdk = AvSdkAppDelegate.getQavsdkControlMulti();
		qavsdk.getAVContext().getAudioCtrl().setDelegate(mDelegate);
	}
	
	boolean getHandfreeChecked() {
		QavsdkControlMulti qavsdk = AvSdkAppDelegate.getQavsdkControlMulti();
		return qavsdk.getAVContext().getAudioCtrl().getAudioOutputMode() == AVAudioCtrl.OUTPUT_MODE_HEADSET;
	}
	
	String getQualityTips() {
		QavsdkControlMulti qavsdk = AvSdkAppDelegate.getQavsdkControlMulti();
		AVAudioCtrl avAudioCtrl;
		if (qavsdk != null) {
			avAudioCtrl = qavsdk.getAVContext().getAudioCtrl();
			return avAudioCtrl.GetQualityTips();
		}
		
		return "";
	}	
}