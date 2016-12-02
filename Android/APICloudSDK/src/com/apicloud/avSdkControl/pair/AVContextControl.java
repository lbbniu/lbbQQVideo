package com.apicloud.avSdkControl.pair;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.apicloud.avSdkApp.AvSdkAppDelegate;
import com.apicloud.avSdkControl.Util;
import com.tencent.TIMCallBack;
import com.tencent.TIMManager;
import com.tencent.TIMUser;
import com.tencent.av.sdk.AVContext;
import com.tencent.av.sdk.AVError;
import com.tencent.openqq.IMSdkInt;

class AVContextControl {
  private static final String TAG = "AvContextControl";
  private static int DEMO_SDK_APP_ID = 1400003324;
  private static String DEMO_ACCOUNT_TYPE = "1806";
  private boolean mIsInStartContext = false;
  private boolean mIsInStopContext = false;
  private Context mContext;
//  private AVContext mAVContext = null;
  private String mSelfIdentifier = "";
  private String mPeerIdentifier = "";
  private AVContext.Config mConfig = null;
  private String mUserSig = "";
  /**
   * 启动SDK系统的回调函数
   */
  private AVContext.StartContextCompleteCallback mStartContextCompleteCallback = new AVContext.StartContextCompleteCallback() {
    public void OnComplete(int result) {
      mIsInStartContext = false;
      
      if (result == AVError.AV_OK) {
        QavsdkControlPair qavsdkControl = AvSdkAppDelegate.getQavsdkControlPair();
        if(null != qavsdkControl){
        	qavsdkControl.initAVInvitation();
        }
      }

      Log.d(TAG, "WL_DEBUG mStartContextCompleteCallback.OnComplete result = "
          + result);
      mContext.sendBroadcast(new Intent(Util.ACTION_START_CONTEXT_COMPLETE)
          .putExtra(Util.EXTRA_AV_ERROR_RESULT, result));

      if (result != AVError.AV_OK) {
//        mAVContext = null;
        AvSdkAppDelegate.mAVContext = null;
        Log.d(TAG, "WL_DEBUG mStartContextCompleteCallback mAVContext is null");
      }
    }
  };

  /**
   * 关闭SDK系统的回调函数
   */
  private AVContext.StopContextCompleteCallback mStopContextCompleteCallback = new AVContext.StopContextCompleteCallback() {
    public void OnComplete() {
      mIsInStopContext = false;

      logout();
    }
  };

  AVContextControl(Context context) {
    mContext = context;
  }

  /**
   * 启动SDK系统
   * 
   * @param identifier
   *          用户身份的唯一标识
   * @param usersig
   *          用户身份的校验信息
   */
  int startContext(String identifier, String usersig) {
    int result = AVError.AV_OK;

    if (!hasAVContext()) {
      Log.d(TAG, "WL_DEBUG startContext identifier = " + identifier);
      Log.d(TAG, "WL_DEBUG startContext usersig = " + usersig);

      if (!TextUtils.isEmpty(Util.modifyAppid)) {
        DEMO_SDK_APP_ID = Integer.parseInt(Util.modifyAppid);
      }
      if (!TextUtils.isEmpty(Util.modifyUid)) {
        DEMO_ACCOUNT_TYPE = Util.modifyUid;
      }
      mConfig = new AVContext.Config();
      mConfig.sdk_app_id = DEMO_SDK_APP_ID;
      mConfig.account_type = DEMO_ACCOUNT_TYPE;
      mConfig.app_id_at3rd = Integer.toString(DEMO_SDK_APP_ID);
      mConfig.identifier = identifier;

      mUserSig = usersig;
      login();
    }

    return result;
  }
  
  
  public void resetAppid(){
	  if (!TextUtils.isEmpty(Util.modifyAppid)) {
        DEMO_SDK_APP_ID = Integer.parseInt(Util.modifyAppid);
      }
      if (!TextUtils.isEmpty(Util.modifyUid)) {
        DEMO_ACCOUNT_TYPE = Util.modifyUid;
      }
  }

  /**
   * 关闭SDK系统
   */
  void stopContext() {
    if (hasAVContext()) {
      Log.d(TAG, "WL_DEBUG stopContext");
      QavsdkControlPair qavsdkControl = AvSdkAppDelegate.getQavsdkControlPair();
      if(null != qavsdkControl){
    	  qavsdkControl.uninitAVInvitation();
      }
      mIsInStopContext = true;
//      mAVContext.stopContext(mStopContextCompleteCallback);
      AvSdkAppDelegate.mAVContext.stopContext(mStopContextCompleteCallback);
    }
  }

  boolean getIsInStartContext() {
    return mIsInStartContext;
  }

  boolean getIsInStopContext() {
    return mIsInStopContext;
  }

  boolean hasAVContext() {
//    return mAVContext != null;
    return AvSdkAppDelegate.mAVContext != null;
  }

  AVContext getAVContext() {
//    return mAVContext;
    return AvSdkAppDelegate.mAVContext;
  }

  String getSelfIdentifier() {
    return mSelfIdentifier;
  }

  String getPeerIdentifier() {
    return mPeerIdentifier;
  }

  void setPeerIdentifier(String peerIdentifier) {
    mPeerIdentifier = peerIdentifier;
  }

  boolean isDefaultAppid() {
    if (DEMO_SDK_APP_ID == Util.DEFAULT_SDK_APP_ID)
      return true;
    else
      return false;
  }

  boolean isDefaultUid() {
    return DEMO_ACCOUNT_TYPE.equals(Util.DEFAULT_ACCOUNT_TYPE);
  }

  private void login() {
    // 请确保TIMManager.getInstance().init()一定执行在主线程
    TIMManager.getInstance().init(mContext);

    TIMUser userId = new TIMUser();
    userId.setAccountType(DEMO_ACCOUNT_TYPE);
    userId.setAppIdAt3rd(mConfig.app_id_at3rd);
    userId.setIdentifier(mConfig.identifier);
    
    /**
     * 登陆所需信息:
     * 1.sdk_app_id ： 创建应用时页面上分配的 sdkappid; 
     * 2.account_type 创建应用账号集成配置页面上分配的;
     * 3.app_id_at3rd ： 第三方开放平台账号 appid，如果是自有的账号，那么直接填 sdkappid的字符串形式;
     * 4.identifier ：用户标示符，也就是我们常说的用户 id 5.user_sig ：使用 tls 后台 api tls_gen_signature_ex 或者工具生成的 user_sig
     */
    TIMManager.getInstance().login(
    		mConfig.sdk_app_id , 
    		userId, mUserSig, // 以前的accessToken
        new TIMCallBack() {// 回调接口，以前的listener
          @Override
          public void onSuccess() {
            Log.i(TAG, "init successfully. tiny id = "
                + IMSdkInt.get().getTinyId());
            onLogin(true, IMSdkInt.get().getTinyId(), 0);
          }

          @Override
          public void onError(int code, String desc) {
            Log.e(TAG, "init failed, imsdk error code  = " + code + ", desc = "
                + desc);
            onLogin(false, 0, code);
          }
        });
  }

  private void onLogin(boolean result, long tinyId, int errorCode) {
    if (result) {
//      mAVContext = AVContext.createContext(mConfig);
    	AvSdkAppDelegate.mAVContext =  AVContext.createContext(mConfig);
//      Log.d(TAG, "WL_DEBUG startContext mAVContext is null? "
//          + (mAVContext == null));
      Log.d(TAG, "WL_DEBUG startContext mAVContext is null? "+ (AvSdkAppDelegate.mAVContext == null));
//      if (mAVContext==null) return;
      if (AvSdkAppDelegate.mAVContext==null) return;
      mSelfIdentifier = mConfig.identifier;

//      int ret = mAVContext.startContext(mContext, mStartContextCompleteCallback);
      int ret = AvSdkAppDelegate.mAVContext.startContext(mContext, mStartContextCompleteCallback);

      mIsInStartContext = true;
    } else {
      mStartContextCompleteCallback.OnComplete(errorCode);
    }
  }

  private void logout() {
    TIMManager.getInstance().logout();
    onLogout(true);
  };

  private void onLogout(boolean result) {
    Log.d(TAG, "WL_DEBUG mStopContextCompleteCallback.OnComplete");
//    mAVContext.onDestroy();
//    mAVContext = null;
    AvSdkAppDelegate.mAVContext.onDestroy();
    AvSdkAppDelegate.mAVContext = null;
    Log.d(TAG, "WL_DEBUG mStopContextCompleteCallback mAVContext is null");
    mIsInStopContext = false;
    mContext.sendBroadcast(new Intent(Util.ACTION_CLOSE_CONTEXT_COMPLETE));
  }

}