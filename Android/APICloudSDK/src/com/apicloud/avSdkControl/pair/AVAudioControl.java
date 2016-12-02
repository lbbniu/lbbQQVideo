package com.apicloud.avSdkControl.pair;

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
			mContext.sendBroadcast(new Intent(Util.ACTION_OUTPUT_MODE_CHANGE));
		}
	};

	AVAudioControl(Context context) {
		mContext = context;
	}

	void initAVAudio() {
		QavsdkControlPair qavsdk = AvSdkAppDelegate.getQavsdkControlPair();
		qavsdk.getAVContext().getAudioCtrl().setDelegate(mDelegate);
	}

	boolean getHandfreeChecked() {
		QavsdkControlPair qavsdk = AvSdkAppDelegate.getQavsdkControlPair();
		return qavsdk.getAVContext().getAudioCtrl().getAudioOutputMode() == AVAudioCtrl.OUTPUT_MODE_HEADSET;
	}
}