package com.dadpat.goofypapa.dadpat;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Lenovo on 2018/3/22.
 */


public class BlueManager extends Object {
    static String sm_blueName = "REMAX-01";
//    static String sm_blueName = "goofyPapa";

    private int m_blueState;

    BluetoothAdapter m_blueToolThAdapter;

    BluetoothDevice m_bluetoothDevice;

    BroadcastReceiver m_broadcastReceiver;

    public ArrayList<BlueManagerStateListen> blueManagerStateListens;

    Context m_appContext;

    Handler m_handler;

    private static final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothA2dp m_bluetoothA2dp;
    private BluetoothSocket m_socket;

    private InputStream m_inStream;

    private void setBlueState( int p_state )
    {
        m_blueState = p_state;

        for ( BlueManagerStateListen item : blueManagerStateListens)
        {
            item.onStateChange(m_blueState);
        }

        Log.d("BlueState", "" + m_blueState);

        if( p_state != 0 ) connect();
    }

    public BlueManager( Context p_context )
    {

        //初始化成员
        m_bluetoothDevice = null;

        m_socket = null;
        m_inStream = null;

        blueManagerStateListens = new ArrayList<>();

        m_appContext = p_context;

        m_handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String t_str = (String)msg.obj;
                int t_cardId = 0;

                if( t_str.contains("卡号：") )
                {

                    Log.d( "DEBUG", t_str);

                    String[] t_sp = t_str.split("：");

                    int[] t_data = new int[t_sp[1].length() / 2];

                    for( int i = 0; i * 2 < t_sp[1].length(); ++i )
                    {
                        String t_bit = t_sp[1].substring( i * 2, i * 2 + 2 );
                        t_data[i] = Integer.valueOf( t_bit, 16 );
                    }

                    if( t_data[0] == 0xAB && t_data[1] == 0x03 )
                    {
                        t_cardId = t_data[3];

                        for(BlueManagerStateListen item : blueManagerStateListens)
                        {
                            item.onScan(t_cardId);
                        }
                    }


                }
                
                Log.d("DEBUG", t_str);
            }
        };

        m_broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String t_action = intent.getAction();

                //开始扫描设备
                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(t_action)) {
                    Log.d("BroadcastReceiver", "ACTION_DISCOVERY_STARTED");
                    Log.d("DEBUG", "开始扫描设备");
                }

                //找到设备
                if (BluetoothDevice.ACTION_FOUND.equals(t_action)) {
                    Log.d("BroadcastReceiver", "ACTION_FOUND");
                    BluetoothDevice t_device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // 已经配对的则跳过

                    if( t_device == null ) return;

                    String t_name = t_device.getName();

                    if( t_name == null ) return;

                    try {
                        Log.d("DEBUG", "--：" + t_device.getName() + "--" + t_device.getAddress());
                        if ( t_device.getBondState() != BluetoothDevice.BOND_BONDED && t_device.getName().equals(sm_blueName)) {
                            m_blueToolThAdapter.cancelDiscovery();
                            Log.d("DEBUG", "找到未配对蓝牙：" + t_device.getName() + "--" + t_device.getAddress());
                            m_bluetoothDevice = t_device;
                            setBlueState(2);
                        }
                    }catch (Exception e){
                        Log.d("DEBUG", "检测到异常" + e.toString());
                    }

                }


                //配对状态
                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(t_action)) {
                    Log.d("BroadcastReceiver", "ACTION_BOND_STATE_CHANGED");

                    BluetoothDevice t_device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    switch (t_device.getBondState()) {
                        case BluetoothDevice.BOND_BONDING://正在配对
                            Log.d("DEBUG",  "正在配对......" );
                            break;
                        case BluetoothDevice.BOND_BONDED://配对结束
                            setBlueState(3);
                            Log.d("DEBUG",  "完成配对" );

                            break;
                        case BluetoothDevice.BOND_NONE://取消配对/未配对
                            Log.d("DEBUG", "取消配对" );
                        default:
                            break;
                    }
                }

                //扫描结束
                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(t_action)) {
                    Log.d("BroadcastReceiver", "ACTION_DISCOVERY_FINISHED");
                    Log.d("DEBUG", "扫描设备结束");

                    if( m_bluetoothDevice == null ){
                        Log.d("DEBUG", "没有找到设备");
                    }
                }

                //蓝牙状态
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(t_action)) {
                    Log.d("BroadcastReceiver", "ACTION_STATE_CHANGED");
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    switch (state) {
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Log.d("DEBUG", "正在打开蓝牙");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            setBlueState(1);
                            Log.d("DEBUG", "蓝牙已打开");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            setBlueState(0);
                            Log.d("DEBUG", "正在关闭蓝牙");
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            setBlueState(0);
                            Log.d("DEBUG", "蓝牙已关闭");
                            break;
                    }
                }
            }
        };

        //开启蓝牙服务线程
        IntentFilter t_intent = new IntentFilter();
        t_intent.addAction(BluetoothDevice.ACTION_FOUND);
        t_intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
        t_intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//行动扫描模式改变了
        t_intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//动作状态发生了变化

        t_intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//扫描开始
        t_intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//扫描结束

        p_context.registerReceiver(m_broadcastReceiver, t_intent);


        m_blueToolThAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean isConnectDevice()
    {
        return m_blueState == 100;
    }


    public void connect()
    {
        switch (m_blueState)
        {
            case 0:
                openDevice();
                break;
            case 1:
                scan();
                break;
            case 2:
                pair();
                break;
            case 3:
                listen();
                break;
            case 4:
                connectA2DP();
                break;
        }
    }

    public void desconnect()
    {
        cancelListen();
        closeA2DP();
        cancelPair();
        closeDevice();
    }

    private void openDevice()
    {
        if(m_blueToolThAdapter.isEnabled())
        {
            setBlueState(1);
            return;
        }
        m_blueToolThAdapter.enable();
    }

    private void closeDevice()
    {
        if(m_blueToolThAdapter.isEnabled()){
            boolean ret = m_blueToolThAdapter.disable();
        }
    }

    private void scan()
    {

        if( m_bluetoothDevice != null )
        {
            m_bluetoothDevice = null;
        }

        Set<BluetoothDevice> pairedDevices = m_blueToolThAdapter.getBondedDevices();
        //
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(sm_blueName)) {
                    m_bluetoothDevice = device;
                    Log.d("DEBUG", "已配对设备:" + m_bluetoothDevice.getName() + ", " + m_bluetoothDevice.getAddress() );
                    setBlueState(3);
                    return;
                }
            }
        }

        if( m_blueToolThAdapter.isDiscovering() )
        {
            m_blueToolThAdapter.cancelDiscovery();
        }
        m_blueToolThAdapter.startDiscovery();
    }

    private void pair()
    {
        if( m_bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDING )
        {
            return;
        }
        m_bluetoothDevice.createBond();
    }

    private void cancelPair()
    {
        if( m_bluetoothDevice == null ) return;
        try{
            Method removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
            removeBondMethod.invoke(m_bluetoothDevice);
        }catch (Exception e){

        }
    }

    private void listen()
    {
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    m_socket = m_bluetoothDevice.createRfcommSocketToServiceRecord(SPP_UUID);
                }catch (IOException e){

                    Message message = new Message();
                    message.obj = "创建Socket失败";
                    m_handler.sendMessage(message);
                }

                try{
                    m_socket.connect();
                }catch (IOException e){
                    Message message = new Message();
                    message.obj = "连接Socket失败";
                    m_handler.sendMessage(message);
                }
                try {
                    m_inStream = m_socket.getInputStream();
                }catch (IOException e){
                    Message message = new Message();
                    message.obj = "获取流异常";
                    m_handler.sendMessage(message);
                }

                Message message = new Message();
                message.obj = "监听成功";
                m_handler.sendMessage(message);

                //connectA2DP
                setBlueState(4);

                byte[] t_buffer = new byte[256];
                boolean t_socketConnected = true;
                while( m_socket != null )
                {
                    try {
                        int t_size = m_inStream.read(t_buffer);
                        message = new Message();
                        message.obj = "卡号：" + byteArrayToHexStr( t_buffer, t_size );
                        m_handler.sendMessage(message);
                    }catch(IOException e){
                        t_socketConnected = false;

                        message = new Message();
                        message.obj = "读取信息失败" + e.toString() + "---" + m_socket.isConnected();
                        m_handler.sendMessage(message);
                    }

                    if(t_socketConnected)
                    {
                        continue;
                    }

                    try {
                        m_socket.close();
                        m_socket = null;
                    }catch (IOException e){
                    }
                }

                setBlueState(0);
                message = new Message();
                message.obj = "断开连接";
                m_handler.sendMessage(message);
                closeA2DP();

            }
        }).start();
    }

    private void connectA2DP() {

        if( checkConnected() )
        {
            setBlueState(100);
            return;
        }

        if(m_blueToolThAdapter.getProfileConnectionState(BluetoothProfile.A2DP)!=BluetoothProfile.STATE_CONNECTED){
            //在listener中完成A2DP服务的调用
            m_blueToolThAdapter.getProfileProxy( m_appContext, new BluetoothProfile.ServiceListener() {
                @Override
                public void onServiceDisconnected(int profile) {
                    if(profile == BluetoothProfile.A2DP){
                        m_bluetoothA2dp = null;
                    }
                }
                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    if(profile == BluetoothProfile.A2DP){
                        m_bluetoothA2dp = (BluetoothA2dp) proxy; //转换
                        _connectA2dp( );
                    }
                }
            }, BluetoothProfile.A2DP);
        }
    }

    private void _connectA2dp( ){
        setPriority(m_bluetoothDevice, 100); //设置priority
        try {
            //通过反射获取BluetoothA2dp中connect方法（hide的），进行连接。
            Method connectMethod =BluetoothA2dp.class.getMethod("connect",
                    BluetoothDevice.class);
            connectMethod.invoke(m_bluetoothA2dp, m_bluetoothDevice);
            Log.d("DEBUG", "连接a2dp成功");

            //连接成功
            if( m_blueState != 0 ) {
                setBlueState(100);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("DEBUG", "连接a2dp失败" + e.toString());
            setBlueState(0);
        }
    }

    public void setPriority(BluetoothDevice device, int priority) {
        if (m_bluetoothA2dp == null) return;
        try {//通过反射获取BluetoothA2dp中setPriority方法（hide的），设置优先级
            Method connectMethod =BluetoothA2dp.class.getMethod("setPriority",
                    BluetoothDevice.class,int.class);
            connectMethod.invoke(m_bluetoothA2dp, device, priority);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("DEBUG", "设置优先级失败" + e.toString());
        }
    }

    private void cancelListen()
    {
        if( m_socket == null ) return;
        try {
            m_socket.close();
        } catch (IOException e){

            Log.d("DEBUG", "----cancel listen fiald----");
        }
    }

    private void closeA2DP()
    {
        if( m_bluetoothA2dp == null ) return;
        //关闭ProfileProxy，也就是断开service连接
        m_blueToolThAdapter.closeProfileProxy(BluetoothProfile.A2DP,m_bluetoothA2dp);
    }

    public static String byteArrayToHexStr(byte[] byteArray, int length) {
        if (byteArray == null){
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[length * 2];
        for (int j = 0; j < length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private boolean checkConnected()
    {
        try {//得到连接状态的方法
            Method method = m_blueToolThAdapter.getClass().getDeclaredMethod("getConnectionState", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            int state = (int) method.invoke(m_blueToolThAdapter, (Object[]) null);

            if(state == BluetoothAdapter.STATE_CONNECTED){
                Set<BluetoothDevice> devices = m_blueToolThAdapter.getBondedDevices();

                for(BluetoothDevice device : devices){
                    Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    method.setAccessible(true);
                    boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                    if(isConnected && device.getName().equals(sm_blueName)){
                        Log.d("DEBUG", "connected:"+device.getName());
                        return true;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}
