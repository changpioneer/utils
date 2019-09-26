package com.pioneer.network.library.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.pioneer.network.library.type.NetType;

public class NetworkUtils {

    /**
     * 当前网络是否可用,旧方法
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        NetworkInfo[] networkInfos = manager.getAllNetworkInfo();
        if (networkInfos != null) {
            for (NetworkInfo info : networkInfos) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public static NetType getNetType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return NetType.NONE;
        }
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return NetType.NONE;
        }
        int type = networkInfo.getType();
        if (type == ConnectivityManager.TYPE_MOBILE) {
            if (networkInfo.getExtraInfo().toLowerCase().equals("cmnet")) {
                return NetType.CMNET;
            } else {
                return NetType.CMWAP;
            }
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            return NetType.WIFI;
        } else {
            return NetType.NONE;
        }
    }

    /*----------------------------------------------------------------------------------------*/

    /**
     * 获取所有的NetworkInfo并打印，用于测试
     * [type: WIFI[], state: CONNECTED/CONNECTED, reason: (unspecified), extra: "ChinaNet-bnXt-5G", failover: false, available: true, roaming: false, metered: false]
     * [type: MOBILE[CDMA - 1xRTT], state: CONNECTED/CONNECTED, reason: connected, extra: ctnet, failover: false, available: true, roaming: false, metered: true]
     */
    public static void getAllNetworkInfo(Context context) {
        if (context != null) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network[] allNetworks = manager.getAllNetworks();
            for (Network network : allNetworks) {
                NetworkInfo networkInfo = manager.getNetworkInfo(network);
                String string = networkInfo.toString();
                Log.d(Constants.LOG_TAG, string);
            }
        }
    }

    /**
     * 当前网络是否可用
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = manager.getActiveNetworkInfo();
        if (net != null && net.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * 检查wifi是否可用
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (info != null) {
                return info.isConnected();
            }
        }
        return false;
    }

    /**
     * 检查移动网络是否可用
     */
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (info != null) {
                return info.isConnected();
            }
        }
        return false;
    }

    /**
     * 检查当前网络类型
     */
    public static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                return info.getType();
            }
        }
        return -1;
    }

    /**
     * 打开网络设置界面
     */
    public static void openSetting(Activity activity) {
        Intent intent = new Intent("/");
        ComponentName cm = new ComponentName("com.android.settings",
                "com.android.settings.WirelessSettings");
        intent.setComponent(cm);
        intent.setAction("android.intent.action.VIEW");
        activity.startActivityForResult(intent, 0);
    }

    /**
     * 获取移动网络运营商名称，如中国联通、中国移动、中国电信
     */
    public static String getNetworkOperatorName(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getNetworkOperatorName();
    }

    /**
     * 返回移动终端类型
     * PHONE_TYPE_NONE :0 手机制式未知
     * PHONE_TYPE_GSM :1 手机制式为GSM，移动和联通
     * PHONE_TYPE_CDMA :2 手机制式为CDMA，电信
     */
    public static int getPhoneType(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getPhoneType();
    }

    /**
     * 判断当前手机的网络类型(WIFI还是2,3,4G)，需要用到上面的方法
     */
    public static NetType getNetWorkStatus(Context context) {
        NetType netWorkType = NetType.NONE;
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            int type = networkInfo.getType();
            if (type == ConnectivityManager.TYPE_WIFI) {
                netWorkType = NetType.WIFI;
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
//                netWorkType = getNetWorkClass(context);
            }
        }
        return netWorkType;
    }

    /**
     * 替代BroadcaastReceiver实现网络监听
     */
    public static void registerNetworkCallback(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            NetworkRequest request = builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .build();
            connManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    Log.d(Constants.LOG_TAG, "onAvailable-->" + network.toString());
                }

                @Override
                public void onLosing(Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);
                    Log.d(Constants.LOG_TAG, "onLosing-->" + network.toString() + " \nmaxMsToLive=" + maxMsToLive);
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    Log.d(Constants.LOG_TAG, "onLost-->" + network.toString());
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    Log.d(Constants.LOG_TAG, "onUnavailable-->");
                }

                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    Log.d(Constants.LOG_TAG, "onCapabilitiesChanged-->" + network.toString()
                            + " \nnetworkCapabilities="+networkCapabilities.toString());
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            Log.d(Constants.LOG_TAG, "onCapabilitiesChanged-->连接WiFi");
                        } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            Log.d(Constants.LOG_TAG, "onCapabilitiesChanged-->连接蜂窝数据");
                        } else {
                            Log.d(Constants.LOG_TAG, "onCapabilitiesChanged-->其他网络");
                        }
                    }
                }

                @Override
                public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                    super.onLinkPropertiesChanged(network, linkProperties);
                    Log.d(Constants.LOG_TAG, "onCapabilitiesChanged-->" + network.toString()
                            + " \nlinkProperties="+linkProperties.toString());
                }
            });
        }
    }

}
