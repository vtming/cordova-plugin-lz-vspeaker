package com.longzheng.speaker;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeakerVerifier;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechListener;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VerifierListener;
import com.iflytek.cloud.VerifierResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wyfei on 2017/7/3.
 *
 */

public class IsvUtil {

    private String TAG = "IsvUtil";
    /*
     当前声纹密码类型，1、2、3分别为文本、自由说和数字密码
      */
    //安全不高,并且只有"芝麻开门"一组密码,暂时不使用
    //private static final int PWD_TYPE_TEXT = 1;
    // 自由说.由于效果问题，讯飞暂不开放
    //private static final int PWD_TYPE_FREE = 2;
    private static final int PWD_TYPE_NUM = 3;
    private int mPwdType = PWD_TYPE_NUM;

    // 声纹AuthId，用户在云平台的身份标识，也是声纹模型的标识
    // 请使用英文字母或者字母和数字的组合，勿使用中文字符
    private String mAuthId = "";
    private String[] mNumPwdSegs;//获取到的数字密码

    private SpeakerVerifier mVerifier;
    private OnReceived mOnReceived;

    public void verify(Context context, String appid, String authId, OnReceived receivedLis) {
        mAuthId = authId;
        mOnReceived = receivedLis;
        SpeechUtility.createUtility(context, "appid=" + appid);

        goVerify(context);
    }

    private void goVerify(Context context) {
        mVerifier = getSpeakerVerifier(context);
        // 清空参数
        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH,
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/verify.pcm");
        mVerifier = SpeakerVerifier.getVerifier();
        // 设置业务类型为验证
        mVerifier.setParameter(SpeechConstant.ISV_SST, "verify");
        // 对于某些麦克风非常灵敏的机器，如nexus、samsung i9300等，建议加上以下设置对录音进行消噪处理
        //mVerify.setParameter(SpeechConstant.AUDIO_SOURCE, "" + MediaRecorder.AudioSource.VOICE_RECOGNITION);

        // 数字密码注册需要传入密码
        String verifyPwd = mVerifier.generatePassword(8);
        sendMsg(1300, "请读出："+ verifyPwd, verifyPwd);//发送密码

        mVerifier.setParameter(SpeechConstant.ISV_PWD, verifyPwd);
        // 设置auth_id，不能设置为空
        mVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthId);
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
        // 开始验证
        mVerifier.startListening(mVerifyListener);
    }


    private VerifierListener mVerifyListener = new VerifierListener() {
        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            sendMsg(1200, "当前正在说话，音量大小：" + volume, volume);
            Log.d(TAG, "返回音频数据：" + data.length);
        }
        @Override
        public void onResult(VerifierResult result) {
            if (result.ret == 0) {
                sendMsg(1102, "验证通过！");// 验证通过
            } else {// 验证不通过
                switch (result.err) {
                    case VerifierResult.MSS_ERROR_IVP_GENERAL:
                        sendMsg(result.err, "内核异常");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
                        sendMsg(result.err, "出现截幅");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
                        sendMsg(result.err, "太多噪音");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
                        sendMsg(result.err, "录音太短");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
                        sendMsg(result.err, "您所读的文本不一致");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
                        sendMsg(result.err, "音量太低");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
                        sendMsg(result.err, "音频长达不到自由说的要求");
                        break;
                    default:
                        sendMsg(1103, "验证不通过");
                        break;
                }
            }
        }

        // 保留方法，暂不用
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle arg3) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }

        @Override
        public void onError(SpeechError error) {
            if (ErrorCode.MSP_ERROR_NOT_FOUND == error.getErrorCode()) {
                sendMsg(1110, "模型不存在，请先注册");
            } else {
                sendMsg(1500, error.getErrorCode() + "：" + error.getPlainDescription(true));
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            sendMsg(1401, "结束说话");
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            sendMsg(1400, "开始说话");
        }
    };


    public void register(Context context,String appid,String authId, OnReceived receivedLis){
        mAuthId = authId;
        mOnReceived = receivedLis;
        SpeechUtility.createUtility(context, "appid=" + appid);
        getPasswordList(context);
    }

    private void getPasswordList(Context context) {
        mVerifier = getSpeakerVerifier(context);
        // 获取密码之前先终止之前的注册或验证过程
        mVerifier.cancel();

        // 清空参数  当参数1为SpeechConstant.PARAMS, 参数2为null 时, 会清空SpeakerVerifier所有参数数据
        //不知道哪个2货写的这种SDK
        mVerifier.setParameter(SpeechConstant.PARAMS, null);

        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + PWD_TYPE_NUM);//数字密码
        mVerifier.getPasswordList(mPwdListenter);
    }

    private SpeakerVerifier getSpeakerVerifier(Context context){
        SpeakerVerifier mVerifier = SpeakerVerifier.createVerifier(context, new InitListener() {
            @Override
            public void onInit(int errorCode) {
                //不知道哪个2货写的烂代码,这个监听根本没有使用
//                if (ErrorCode.SUCCESS == errorCode) {
//                    Log.d(TAG, "引擎初始化成功");
//                } else {
//                    Log.d(TAG, "引擎初始化失败，错误码");
//                }
            }
        });

        return mVerifier;
    }

    private SpeechListener mPwdListenter = new SpeechListener() {
        @Override
        public void onEvent(int eventType, Bundle params) {}

        @Override
        public void onBufferReceived(byte[] buffer) {
            String result = new String(buffer);
            StringBuffer numberString = new StringBuffer();
            try {
                JSONObject object = new JSONObject(result);
                if (!object.has("num_pwd")) {
                    return;
                }

                JSONArray pwdArray = object.optJSONArray("num_pwd");
                numberString.append(pwdArray.get(0));
                for (int i = 1; i < pwdArray.length(); i++) {
                    numberString.append("-" + pwdArray.get(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String mNumPwd = numberString.toString();
            mNumPwdSegs = mNumPwd.split("-");
            goRegister(mNumPwd);
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (null != error && ErrorCode.SUCCESS != error.getErrorCode()) {
                sendMsg(1500, error.getErrorCode()+" "+error.getPlainDescription(true));
            }
        }
    };

    private void goRegister(String mNumPwd){
        // 清空参数
        mVerifier.setParameter(SpeechConstant.PARAMS, null);
        mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH,
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/test.pcm");
        // 对于某些麦克风非常灵敏的机器，如nexus、samsung i9300等，建议加上以下设置对录音进行消噪处理
        //mVerify.setParameter(SpeechConstant.AUDIO_SOURCE, "" + MediaRecorder.AudioSource.VOICE_RECOGNITION);

        // 数字密码注册需要传入密码
        if (TextUtils.isEmpty(mNumPwd)) {
            //showTip("数字密码为空");
            return;
        }

        //密码
        mVerifier.setParameter(SpeechConstant.ISV_PWD, mNumPwd);
        // 设置auth_id，不能设置为空
        mVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthId);
        // 设置业务类型为注册
        mVerifier.setParameter(SpeechConstant.ISV_SST, "train");
        // 设置声纹密码类型
        mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
        // 训练次数
         mVerifier.setParameter( SpeechConstant.ISV_RGN, "2" );//TODO

        // 开始注册
        mVerifier.startListening(mRegisterListener);
    }


    private VerifierListener mRegisterListener = new VerifierListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            sendMsg(1200, "当前正在说话，音量大小：" + volume, volume);
            Log.d(TAG, "返回音频数据："+data.length);
        }
        @Override
        public void onResult(VerifierResult result) {

            if (result.ret == ErrorCode.SUCCESS) {
                switch (result.err) {
                    case VerifierResult.MSS_ERROR_IVP_GENERAL:
                        sendMsg(result.err, "内核异常");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_EXTRA_RGN_SOPPORT:
                        sendMsg(result.err, "训练达到最大次数");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
                        sendMsg(result.err, "出现截幅");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
                        sendMsg(result.err, "太多噪音");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
                        sendMsg(result.err, "录音太短");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
                        sendMsg(result.err, "训练失败，您所读的文本不一致");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
                        sendMsg(result.err, "音量太低");
                        break;
                    case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
                        sendMsg(result.err, "音频长达不到自由说的要求");
                    default:
//                        mShowRegFbkTextView.setText("");
                        break;
                }

                if (result.suc == result.rgn) {
                    sendMsg(1100, "注册成功！");

                } else {
                    int nowTimes = result.suc + 1;
                    int leftTimes = result.rgn - nowTimes;

                    String msg = "训练 第" + nowTimes + "遍，剩余" + leftTimes + "遍。\n" +
                            "请读出：" + mNumPwdSegs[nowTimes - 1] ;
                    sendMsg(1300, msg, mNumPwdSegs[nowTimes - 1]);
                }
            }else {
                sendMsg(1101, "注册失败，请重新开始！");
            }
        }
        // 保留方法，暂不用
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle arg3) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }

        @Override
        public void onError(SpeechError error) {
//            if (error.getErrorCode() == ErrorCode.MSP_ERROR_ALREADY_EXIST) {
//                sendMsg(ErrorCode.MSP_ERROR_ALREADY_EXIST, "模型已存在，如需重新注册，请先删除！");
//            } else {
//                sendMsg(1500, error.getErrorCode()+"："+error.getPlainDescription(true));
//            }
            sendMsg(1500, error.getErrorCode()+"："+error.getPlainDescription(true));
        }

        @Override
        public void onBeginOfSpeech() {
            sendMsg(1400, "开始说话");
        }
        @Override
        public void onEndOfSpeech() {
            sendMsg(1401, "结束说话");
        }
    };


    private void sendMsg(int code, String msg) {
        VMessage vMessage = new VMessage();
        vMessage.code = code;
        vMessage.msg = msg;

        send(vMessage.toJson());
    }
    private void sendMsg(int code, String msg, String pwd) {
        VMessage vMessage = new VMessage();
        vMessage.code = code;
        vMessage.msg = msg;
        vMessage.pwd = pwd;

        send(vMessage.toJson());
    }
    private void sendMsg(int code, String msg, int volume) {
        VMessage vMessage = new VMessage();
        vMessage.code = code;
        vMessage.msg = msg;
        vMessage.volume = volume;

        send(vMessage.toJson());
    }

    private void send(String msg){
        Log.i(TAG, msg);
        mOnReceived.onReceived(msg);
    }

    interface OnReceived{
        void onReceived(String json);
    }

    private static class VMessage{
        int code = -1;
        String msg = "";
        int volume = -1;
        String pwd = "";

        public String toJson() {
            return "{\"code\":"+
                    code +
                    ",\"msg\":\"" +
                    msg +
                    "\",\"volume\":" +
                    volume +
                    ",\"pwd\":\"" +
                    pwd +
                    "\"}";
        }

        @Override
        public String toString() {
            return "VMessage{" +
                    "code=" + code +
                    ", msg='" + msg + '\'' +
                    ", volume=" + volume +
                    ", pwd='" + pwd + '\'' +
                    '}';
        }
    }

}
