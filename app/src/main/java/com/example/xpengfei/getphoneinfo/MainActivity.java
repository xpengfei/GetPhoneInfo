package com.example.xpengfei.getphoneinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    WifiManager.WifiLock mWifiLock;                 // 定义一个WifiLock
    private MediaPlayer mediaPlayer;
    private WifiManager mWifiManager;                // 定义WifiManager（WiFi管理类）对象
    private WifiInfo mWifiInfo;                      //  当前连接的WiFi对象
    private List<ScanResult> mWifiList;              // 扫描出的网络连接  列表
    private List<WifiConfiguration> mWifiConfiguration; // wifi网络配置信息 列表
    private Boolean isAlarming = false;         // 手机是否处于响铃状态的标识
    private Button buttonAlarm;
    private Button buttonAdd;
    private Button buttonReduce;
    private Button stopAlarm;
    private Button getCellInfoBtn;
    private Button openWifi;
    private Button closeWifi;
    private Button getWifiList;
    private Button getWifiInfo;
    private Button getNetBtn;
    private ConnectivityManager connectivityManager;        //网络连接管理器
    private NetworkInfo networkInfo;            //网络连接状态对象
    private TextView textView;
    //获取电池信息的广播接收器
    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                //得到电池状态
                int status = intent.getIntExtra("status", 0);
                //得到电池健康状态
                int health = intent.getIntExtra("health", 0);
                //得到电池剩余容量
                int level = intent.getIntExtra("level", 0);
                //得到电池最大值，通常为100
                int scale = intent.getIntExtra("scale", 0);
                //得到图标ID
                int icon_small = intent.getIntExtra("icon-small", 0);
                //充电方式
                int plugged = intent.getIntExtra("plugged", 0);
                //得到电池电压
                int voltage = intent.getIntExtra("voltage", 0);
                //得到电池的温度，0.1度单位
                int temperature = intent.getIntExtra("temperature", 0);
                //得到电池的类型
                String technology = intent.getStringExtra("technology");
                // 得到电池状态
                String statusString = "";
                // 根据状态id，得到状态字符串
                switch (status) {
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        statusString = "unknown";
                        break;
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        statusString = "charging";
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        statusString = "discharging";
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        statusString = "not charging";
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        statusString = "full";
                        break;
                }
                //得到电池的寿命状态
                String healthString = "";
                //根据状态id，得到电池寿命
                switch (health) {
                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                        healthString = "unknown";
                        break;
                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        healthString = "good";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        healthString = "overheat";
                        break;
                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        healthString = "dead";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        healthString = "voltage";
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                        healthString = "unspecified failure";
                        break;
                }
                //得到充电模式
                String acString = "";
                //根据充电状态id，得到充电模式
                switch (plugged) {
                    case BatteryManager.BATTERY_PLUGGED_AC:
                        acString = "plugged ac";
                        break;
                    case BatteryManager.BATTERY_PLUGGED_USB:
                        acString = "plugged usb";
                        break;
                }

                textView.setText("电池状态：" + statusString + "\n健康值：" + healthString + "\n电池电量："
                        + String.valueOf(((float) level / scale) * 100 + "%") + "\n充电方式：" + acString + "\n电池电压："
                        + voltage + "\n电池温度：" + (float) temperature * 0.1 + "\n电池类型：" + technology);
            }
        }
    };


    /**
     * 广播接收，监听网络
     */
    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            // wifi已成功扫描到可用wifi。
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                Log.d("广播", "接收到WiFi广播");
                mWifiManager.startScan();
                mWifiList = mWifiManager.getScanResults();
                Log.d("扫描到的WiFi列表大小", "mScanResults.size()===" + mWifiList.size());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 取得WifiManager对象
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiManager.startScan();
        //得到扫描结果
        List<ScanResult> results = mWifiManager.getScanResults();
        buttonAlarm = (Button) findViewById(R.id.start_ring);
        stopAlarm = (Button) findViewById(R.id.stop_voice);
        buttonAdd = (Button) findViewById(R.id.add_voice);
        buttonReduce = (Button) findViewById(R.id.reduce_voice);
        getCellInfoBtn = (Button) findViewById(R.id.get_cellInfo);
        openWifi = (Button) findViewById(R.id.open_wifi);
        closeWifi = (Button) findViewById(R.id.close_WiFi);
        getWifiList = (Button) findViewById(R.id.get_wifiList);
        getWifiInfo = (Button) findViewById(R.id.get_wifiInfo);
        textView = (TextView) findViewById(R.id.text_info);
        getNetBtn = (Button) findViewById(R.id.get_netInfo);
        getNetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取网络连接服务
                connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                //获取代表互联网连接状态的NetWorkInfo对象
                networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    String netInfo = "";
                    //当前网络状态是否可用
                    if (networkInfo.isAvailable()) {
                        netInfo += "当前网络状态可用\n";
                    } else {
                        netInfo += "当前网络状态不可用\n";
                    }
                    //获取GPRS网络模式连接的描述
                    NetworkInfo.State state = connectivityManager.
                            getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
                    if (state == NetworkInfo.State.CONNECTED) {
                        netInfo += "当前使用数据流量...\n";
                    }
                    //获取WIFI网络模式连接的描述
                    state = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                            .getState();
                    if (state == NetworkInfo.State.CONNECTED) {
                        netInfo += "当前网络连接为WIFI...\n";
                    }
                    textView.setText(netInfo);
                } else {
                    textView.setText("当前无网络连接...");
                }
            }
        });


        buttonAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAlarming == false) {
                    MediaPlayerHelper.startAlarm(MainActivity.this);
                    isAlarming = true;
                }

            }
        });
        stopAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAlarming == true) {
                    MediaPlayerHelper.stopAlarm();
                    isAlarming = false;
                }
            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaPlayerHelper.addVoice(MainActivity.this);
            }
        });
        buttonReduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaPlayerHelper.reduceVoice(MainActivity.this);
            }
        });
        getCellInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //注册广播接收器
                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_BATTERY_CHANGED);
                registerReceiver(broadcastReceiver, filter);
            }
        });
        openWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWifi(MainActivity.this);
                checkState(MainActivity.this);
            }
        });
        closeWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeWifi(MainActivity.this);
                checkState(MainActivity.this);
            }
        });
        //获取WiFi列表
        getWifiList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //扫描---先判断WiFi是否开启
                openWifi(MainActivity.this);
                //注册WiFi广播
                IntentFilter filter = new IntentFilter();
                filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                registerReceiver(wifiReceiver, filter);

            }
        });
        //获取wifi信息
        getWifiInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 取得WifiInfo对象
                mWifiInfo = mWifiManager.getConnectionInfo();
                textView.setText(mWifiInfo.getSSID() + "\t" + mWifiInfo.getMacAddress());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 解除注册监听
        try {
            unregisterReceiver(broadcastReceiver);
            unregisterReceiver(wifiReceiver);
        } catch (Exception e) {

        }
    }


    // 打开WIFI
    public void openWifi(Context context) {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        } else if (mWifiManager.getWifiState() == 2) {
            Toast.makeText(context, "Wifi正在开启...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Wifi已经开启...", Toast.LENGTH_SHORT).show();
        }
    }

    // 关闭WIFI
    public void closeWifi(Context context) {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        } else if (mWifiManager.getWifiState() == 1) {
            Toast.makeText(context, "Wifi已经关闭...", Toast.LENGTH_SHORT).show();
        } else if (mWifiManager.getWifiState() == 0) {
            Toast.makeText(context, "Wifi正在关闭...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "请重新关闭...", Toast.LENGTH_SHORT).show();
        }
    }

    // 检查当前WIFI状态
    public void checkState(Context context) {
        if (mWifiManager.getWifiState() == 0) {
            Toast.makeText(context, "WiFi正在关闭...", Toast.LENGTH_SHORT).show();
        } else if (mWifiManager.getWifiState() == 1) {
            Toast.makeText(context, "WiFi已经关闭...", Toast.LENGTH_SHORT).show();
        } else if (mWifiManager.getWifiState() == 2) {
            Toast.makeText(context, "WiFi正在开启...", Toast.LENGTH_SHORT).show();
        } else if (mWifiManager.getWifiState() == 3) {
            Toast.makeText(context, "WiFi已经开启...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "没有获取到WiFi状态...", Toast.LENGTH_SHORT).show();
        }
    }

    // 锁定WifiLock
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    // 解锁WifiLock
    public void releaseWifiLock() {
        // 判断时候锁定
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    // 创建一个WifiLock
    public void creatWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("Test");
    }

    // 得到配置好的网络
    public List<WifiConfiguration> getConfiguration() {
        return mWifiConfiguration;
    }

    // 指定配置好的网络进行连接
    public void connectConfiguration(int index) {
        // 索引大于配置好的网络索引返回
        if (index > mWifiConfiguration.size()) {
            return;
        }
        // 连接配置好的指定ID的网络
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,
                true);
    }

    //开始扫描
    public void startScan(Context context) {
        mWifiManager.startScan();
        //得到扫描结果
        List<ScanResult> results = mWifiManager.getScanResults();
        Log.d("扫描到的WiFiList大小为：", String.valueOf(results.size()));
        // 得到配置好的网络连接
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();
        if (results == null) {
            if (mWifiManager.getWifiState() == 3) {
                Toast.makeText(context, "当前区域没有无线网络...", Toast.LENGTH_SHORT).show();
            } else if (mWifiManager.getWifiState() == 2) {
                Toast.makeText(context, "wifi正在开启，请稍后扫描...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "WiFi未开启...", Toast.LENGTH_SHORT).show();
            }
        } else {
            mWifiList = new ArrayList();
            for (ScanResult result : results) {
                if (result.SSID == null || result.SSID.length() == 0 || result.capabilities.contains("[IBSS]")) {
                    continue;
                }
                boolean found = false;
                for (ScanResult item : mWifiList) {
                    if (item.SSID.equals(result.SSID) && item.capabilities.equals(result.capabilities)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    mWifiList.add(result);
                }
            }
        }
    }


    // 得到MAC地址
    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    // 得到接入点的BSSID
    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    // 得到IP地址
    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    // 得到连接的ID
    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    // 得到WifiInfo的所有信息包
    public String getWifiInfo() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }

    // 添加一个网络并连接
    public void addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        boolean b = mWifiManager.enableNetwork(wcgID, true);
        System.out.println("a--" + wcgID);
        System.out.println("b--" + b);
    }

    // 断开指定ID的网络
    public void disconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    public void removeWifi(int netId) {
        disconnectWifi(netId);
        mWifiManager.removeNetwork(netId);
    }

    //然后是一个实际应用方法，只验证过没有密码的情况：
    public WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = this.IsExsits(SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        if (Type == 1) //WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 2) //WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) //WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    private WifiConfiguration IsExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }


}
