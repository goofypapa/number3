package com.dadpat.goofypapa.dadpat;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.webkit.DownloadListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class Control {

    private BlueManager m_blueManager;
    private ControlListen m_controlListen;
    private Context m_context;
    private Control m_this;

    private Handler m_handler;

    private Db_Animals m_db_animals;

    private ArrayList<Animal> m_downloadQueue;

    private static String sm_serviceHost = "http://www.dadpat.com";

    private String m_audioPath;

    private int m_prevCardId;
    private int m_audioIndex;

    private ArrayList<MediaPlayer> m_mediaPlayerList;


    public Control(final Context p_context, ControlListen p_controlListen)
    {
        m_this = this;
        m_controlListen = p_controlListen;
        m_context = p_context;
        m_handler = new Handler(){
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                String t_cmd = (String)msg.obj;
                if( t_cmd == "download" )
                {
                    downloadFiles();
                }else{
                    m_controlListen.onLog((String)msg.obj);
                }
            }
        };
        m_blueManager = new BlueManager( p_context, new BlueManagerStateListen(){
            @Override
            public void onStateChange( int p_state )
            {
                m_controlListen.connectStateChanged(p_state);
            }

            @Override
            public void onScan( String p_code )
            {
                int t_id = Integer.parseInt(p_code);
                m_this.onScan( t_id );
            }
            @Override
            public void onLog( String p_str )
            {
                m_controlListen.onLog( p_str );
            }
        } );

        m_db_animals = new Db_Animals(m_context, new LogListen() {
            @Override
            public void onLog(String p_log) {
                m_controlListen.onLog( p_log );
            }
        });

        m_downloadQueue = new ArrayList<Animal>();

        m_audioPath = m_context.getExternalFilesDir(null).getPath() + "/audios/";

        m_mediaPlayerList = new ArrayList<MediaPlayer>(10 );

    }

    public void connectBlue()
    {
        m_blueManager.connect();
    }

    public void desConnectBlue()
    {
        m_blueManager.desconnect();
    }

    public boolean isDeviceConnected()
    {
        return m_blueManager.isConnectDevice();
    }

    public boolean needDownload()
    {
        return m_downloadQueue.size() > 0;
    }

    public void downloadFiles()
    {
        if( !needDownload() )
        {
            return;
        }

        int t_fileCount = 0;
        for( int i = 0; i < m_downloadQueue.size(); ++i )
        {
            Animal t_animal = m_downloadQueue.get(i);

            String t_dir = m_audioPath + t_animal.m_serviceId;
            File t_file = new File(t_dir);
            if( !t_file.exists() )
            {
                if( !t_file.mkdirs() )
                {
                    //创建文件夹失败
                    return;
                }
            }

            t_animal.m_downloadSize = 0;

            for( int k = 0; k < t_animal.m_musicPaths.size(); ++k )
            {
                t_fileCount ++;
                String t_path = t_animal.m_musicPaths.get(k);
                String[] t_list = t_path.split("/");
                String t_fileName = t_list[t_list.length - 1];

                new DownLoadFile(m_context, new DownLoadFileListen() {
                    @Override
                    public void onLog(String p_log) {

                    }

                    @Override
                    public void onStartDownLoad() {
                        m_controlListen.onLog("download start");
                    }

                    @Override
                    public void onEndDownload(boolean p_state, Object p_obj) {
                        m_controlListen.onLog("download end: " + p_state);
                        Animal t_a = (Animal)p_obj;
                        if( p_state ) {
                            t_a.m_downloadSize++;

                            if( t_a.m_downloadSize == t_a.m_musicPaths.size() )
                            {
                                m_db_animals.insert( t_a );
                            }
                        }
                    }

                    @Override
                    public void onUpdateDownloadRate(float p_rate) {
                        m_controlListen.onLog("download rate:" + p_rate );
                    }
                }, sm_serviceHost + t_path, t_dir + "/" + t_fileName,t_animal ).start();
            }
        }
    }

    public void clearData()
    {
        m_db_animals.clearAnimals();
        deleteDir( m_audioPath );
        m_controlListen.onLog("清空本地数据");
    }

    private void onScan(final int p_id )
    {

        Animal t_animal = null;

        //判断本地是否有当前卡片信息
        t_animal = m_db_animals.getAnimalByCardId( p_id );
        if( t_animal != null )
        {

            if( m_prevCardId == p_id )
            {
                m_audioIndex = m_audioIndex + 1 >= t_animal.m_musicPaths.size() ? 0 : m_audioIndex + 1;
            }else{
                m_audioIndex = 0;
                m_prevCardId = p_id;
            }

            String t_dir = m_audioPath + t_animal.m_serviceId;
            String t_path = t_animal.m_musicPaths.get(m_audioIndex);

            Message t_message = new Message();
            t_message.obj = "path: " + t_path;
            m_handler.sendMessage(t_message);

            if( t_animal.m_musicPaths.size() <= m_audioIndex )
            {
                //没有音频
                return;
            }

            while( m_mediaPlayerList.size() <= m_audioIndex )
            {
                m_mediaPlayerList.add(null);
            }

            MediaPlayer t_mediaPlayer = m_mediaPlayerList.get(m_audioIndex);

            String[] t_list = t_animal.m_musicPaths.get(m_audioIndex).split("/");
            String t_fileName = t_list[t_list.length - 1];

            if( t_mediaPlayer == null ) {
                t_mediaPlayer = new MediaPlayer();
                m_mediaPlayerList.set( m_audioIndex, t_mediaPlayer );
            }else {
                t_mediaPlayer.reset();
            }

            try{
                t_mediaPlayer.setDataSource( t_dir + "/" + t_fileName );
                t_mediaPlayer.prepare();
            }catch (Exception e)
            {
                t_message = new Message();
                t_message.obj = "play error: " + e.toString();
                m_handler.sendMessage(t_message);
            }

//            for( int i = 0; i < m_mediaPlayerList.size(); ++i )
//            {
//                MediaPlayer tt_mediaPlayer = m_mediaPlayerList.get(i);
//                if( tt_mediaPlayer != null && tt_mediaPlayer.isPlaying() )
//                {
//                    tt_mediaPlayer.stop();
//                }
//            }

            if( t_mediaPlayer != null )
            {
                t_mediaPlayer.start();
            }

            return;
        }

        //网络查询卡号
        new Http( m_context, sm_serviceHost + "/card/listOfSameBatch.do?cardId=" + p_id, new HttpListen() {
            @Override
            public void callBack(String p_str) {

                try
                {
                    JSONObject t_jsonObject = new JSONObject(p_str);

                    boolean t_success = t_jsonObject.getBoolean("success");

                    if( t_success )
                    {
                        JSONArray t_data = t_jsonObject.getJSONArray("data");
                        if( t_data.length() <= 0 )
                        {
                            //不认识的卡片
                            return;
                        }

                        for( int i = 0; i < t_data.length(); ++i ){
                            JSONObject t_animal = t_data.getJSONObject(i);
                            int t_cardId = t_animal.getInt("cardId");

                            //判断数据库中是否存在
                            boolean t_isExist = m_db_animals.getAnimalByCardId( t_cardId ) != null;

                            if( t_isExist )
                            {
                                continue;
                            }

                            String t_resourceId = t_animal.getString("resourceId");
                            int t_groupId = t_animal.getInt("batchNo");
                            ArrayList<String> t_musicList = new ArrayList<String>();

                            JSONArray t_musics = t_animal.getJSONArray("audios");
                            for( int k = 0; k < t_musics.length(); ++k ){
                                t_musicList.add( t_musics.getString(k) );
                            }

                            Message t_message = new Message();
                            t_message.obj = "need download: " + t_resourceId;
                            m_handler.sendMessage(t_message);

                            m_downloadQueue.add( new Animal( t_cardId, t_resourceId, t_groupId, t_musicList ));

                        }

                        if( m_downloadQueue.size() > 0 )
                        {
                            Message t_message = new Message();
                            t_message.obj = "download";
                            m_handler.sendMessage(t_message);
                        }
                    }

                }catch (Exception e)
                {
                    Message t_message = new Message();
                    t_message.obj = "parse json error: " + e.toString();
                    m_handler.sendMessage(t_message);
                }
            }
        }).get();
    }


    private void play()
    {

    }


    //删除文件夹和文件夹里面的文件
    public static void deleteDir(final String pPath) {
        File dir = new File(pPath);
        deleteDirWihtFile(dir);
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }
}
