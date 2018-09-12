package com.dadpat.goofypapa.dadpat;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
public class Control {

    private BlueManager m_blueManager;
    private Context m_context;
    private Control m_this;

    private Handler m_handler;

    private DataBase m_dataBase;

    private ArrayList<DBAudioInfo> m_fristDownloadQueue;
    private ArrayList<DBAudioInfo> m_downloadQueue;
    private ArrayList<DBImageInfo> m_ImageDownloadQueue;

    ArrayList<DBBatchInfo> m_batches = null;

    public String m_audioPath;
    public String m_imagePath;

    private int m_prevCardId;
    private int m_audioIndex;

    private ArrayList<MediaPlayer> m_mediaPlayerList;
    private int m_mediaPlayerIndex;

    private MediaPlayer m_soloMediaPlayer;

    public static String sm_serviceHost = "http://www.dadpat.com";

    private static Control m_instance = null ;

    public static void createInstance( final Context p_context )
    {
        m_instance = new Control(p_context);
    }

    public static Control instance()
    {
        return m_instance;
    }

    public Control(final Context p_context)
    {
        m_this = this;
        m_context = p_context;
        m_handler = new Handler(){
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                String t_cmd = (String)msg.obj;

                if( t_cmd == "download" )
                {
                    downloadFiles();
                    UIUpdateActivity.m_instance.download();
                }

                else if( t_cmd == "tryUpdate" )
                {
                    tryUpdate();
                }

                Log.d("DEBUG", (String)msg.obj);

            }
        };
        m_blueManager = new BlueManager( p_context);

        addBlueStateListen( new BlueManagerStateListen(){
            @Override
            public void onStateChange( int p_state )
            {

            }

            @Override
            public void onScan( int p_code )
            {
                try {
                    m_this.onScan( p_code );
                }catch (Exception e){

                }
            }
        } );

        m_dataBase = new DataBase( m_context );

//        m_dataBase.clearDataBase();

        m_fristDownloadQueue = new ArrayList<>();
        m_downloadQueue = new ArrayList<>();
        m_ImageDownloadQueue = new ArrayList<>();

        m_audioPath = m_context.getExternalFilesDir(null).getPath() + "/audios/";
        m_imagePath = m_context.getExternalFilesDir(null).getPath() + "/image/";

        m_mediaPlayerList = new ArrayList<>( );
        m_mediaPlayerIndex = 0;

        for( int i = 0; i < 10; ++i )
        {
            m_mediaPlayerList.add( null );
        }

        m_soloMediaPlayer = null;
    }

    public void tryUpdate()
    {
        ArrayList<DBBatchInfo> t_list = m_dataBase.getBatchList();
        for( int i = 0; i < t_list.size(); ++i )
        {
            String t_url = sm_serviceHost + "/resource/card/list.do?batchId=" + t_list.get(i).m_id;
            UIUpdateActivity.m_instance.m_listenQueue.add(t_url);
            new Http(m_context, t_url, new HttpListen() {
                @Override
                public void callBack(String p_url, String p_str) {

                    ArrayList<DBCardInfo> t_localDBCardInfos = null;

                    try {
                        JSONObject t_jsonObject = new JSONObject(p_str);

                        boolean t_success = t_jsonObject.getBoolean("success");
                        if(t_success)
                        {
                            JSONArray t_data = t_jsonObject.getJSONArray("data");

                            for( int i = 0; i < t_data.length(); ++i )
                            {
                                JSONObject t_cardObject = t_data.getJSONObject(i);

                                int t_rfid = t_cardObject.getInt("rfId");
                                String t_resourceId = t_cardObject.getString("resourceId");
                                String t_ownerId = t_cardObject.getString("ownerId");

                                if( t_localDBCardInfos == null ){
                                    t_localDBCardInfos = m_dataBase.getCardListByBatche( t_ownerId );
                                }


                                ArrayList<DBAudioInfo> t_DBAudioInfos = new ArrayList<>();

                                if( t_cardObject.getString("audios") != "null" ) {
                                    JSONArray t_musics = t_cardObject.getJSONArray("audios");
                                    for (int k = 0; k < t_musics.length(); ++k) {
                                        String t_url = t_musics.getJSONObject(k).getString("attUrl");
                                        String t_md5 = t_musics.getJSONObject(k).getString("md5");

                                        t_DBAudioInfos.add(new DBAudioInfo(t_md5, t_rfid, t_url, 1));
                                    }
                                }

                                if( t_cardObject.getString("pronAudio") != "null" ) {

                                    JSONObject t_pronAudios = t_cardObject.getJSONObject("pronAudio");
                                    if (t_pronAudios != null && t_pronAudios.getString("attUrl") != "") {
                                        String t_url = t_pronAudios.getString("attUrl");
                                        String t_md5 = t_pronAudios.getString("md5");
                                        t_DBAudioInfos.add(new DBAudioInfo( t_md5, t_rfid, t_url, 2));
                                    }
                                }

                                if( t_cardObject.getString("descAudio") != "null" ) {
                                    JSONObject t_descAudios = t_cardObject.getJSONObject("descAudio");
                                    if( t_descAudios != null && t_descAudios.getString("attUrl") != "" )
                                    {
                                        String t_url = t_descAudios.getString("attUrl");
                                        String t_md5 = t_descAudios.getString("md5");
                                        t_DBAudioInfos.add(new DBAudioInfo( t_md5, t_rfid, t_url, 2 ));
                                    }
                                }

                                String t_coverImageMD5 = "";
                                String t_coverImageUrl = "";

                                String t_lineDrawingMD5 = "";
                                String t_lineDrawingUrl = "";

                                if( t_cardObject.getString("coverImage") != "null" ){
                                    JSONObject t_coverImage = t_cardObject.getJSONObject("coverImage");
                                    t_coverImageMD5 = t_coverImage.getString("md5");
                                    t_coverImageUrl = t_coverImage.getString("attUrl");
                                }

                                if( t_cardObject.getString("handDrawImage") != "null" ){
                                    JSONObject t_lineDrawing = t_cardObject.getJSONObject("handDrawImage");
                                    t_lineDrawingMD5 = t_lineDrawing.getString("md5");
                                    t_lineDrawingUrl = t_lineDrawing.getString("attUrl");
                                }



                                DBCardInfo t_DB_card = new DBCardInfo( t_rfid, t_resourceId, t_ownerId, t_coverImageMD5, t_lineDrawingMD5, false, t_DBAudioInfos);

                                DBCardInfo t_oldDBCardInfo = null;
                                for(int k = 0; k < t_localDBCardInfos.size(); ++k )
                                {
                                    if( t_localDBCardInfos.get(k).m_cardNumber == t_rfid )
                                    {
                                        t_oldDBCardInfo = t_localDBCardInfos.get(k);
                                        break;
                                    }
                                }

                                if( t_oldDBCardInfo != null )
                                {
                                    t_localDBCardInfos.remove(t_oldDBCardInfo);


                                    if( t_DB_card.m_group.equals(t_oldDBCardInfo.m_group) || t_DB_card.m_serviceId.equals(t_oldDBCardInfo.m_serviceId) || !t_DB_card.m_coverImage.equals(t_oldDBCardInfo.m_coverImage) || !t_DB_card.m_lineDrawing.equals(t_oldDBCardInfo.m_lineDrawing) )
                                    {
                                        t_DB_card.m_activation = t_oldDBCardInfo.m_activation;
                                        m_dataBase.update(t_DB_card);
                                    }

                                    if( !t_DB_card.m_coverImage.equals(t_oldDBCardInfo.m_coverImage) )
                                    {
                                        DBImageInfo t_image = m_dataBase.getImage(t_oldDBCardInfo.m_coverImage);

                                        if( t_image != null )
                                        {
                                            File t_file = new File(t_image.m_path );
                                            if( t_file.exists() )
                                            {
                                                t_file.delete();
                                            }

                                            m_dataBase.delete(t_image);
                                        }
                                    }

                                    if( !t_DB_card.m_lineDrawing.equals(t_oldDBCardInfo.m_lineDrawing) )
                                    {
                                        DBImageInfo t_image = m_dataBase.getImage(t_oldDBCardInfo.m_lineDrawing);

                                        if( t_image != null )
                                        {
                                            File t_file = new File(t_image.m_path );
                                            if( t_file.exists() )
                                            {
                                                t_file.delete();
                                            }

                                            m_dataBase.delete(t_image);
                                        }
                                    }


                                    //如果没有激活 不更新音频文件
                                    if( t_oldDBCardInfo.m_activation ) {

                                        for (int k = 0; k < t_DBAudioInfos.size(); ++k) {
                                            DBAudioInfo t_DBAudioInfo = t_DBAudioInfos.get(k);

                                            DBAudioInfo t_oldDBAudioInfo = null;
                                            for (int n = 0; n < t_oldDBCardInfo.m_musicPaths.size(); ++n) {
                                                if (t_oldDBCardInfo.m_musicPaths.get(n).md5.equals(t_DBAudioInfo.md5)) {
                                                    t_oldDBAudioInfo = t_oldDBCardInfo.m_musicPaths.get(n);
                                                }
                                            }

                                            if (t_oldDBAudioInfo == null) {

                                                if( m_dataBase.getAudio( t_DBAudioInfo.md5 ) == null ) {

                                                    m_downloadQueue.add(t_DBAudioInfo);

                                                    Message t_message = new Message();
                                                    t_message.obj = "null-download: " + t_DBAudioInfo.path;
                                                    m_handler.sendMessage(t_message);
                                                }

                                            } else {
                                                if (t_oldDBAudioInfo.audioType != t_oldDBAudioInfo.audioType) {
                                                    m_dataBase.update(t_DBAudioInfo);
                                                }

                                                t_oldDBCardInfo.m_musicPaths.remove(t_oldDBAudioInfo);
                                            }
                                        }


                                        //删除本地多余的音频文件
                                        for (int k = 0; k < t_oldDBCardInfo.m_musicPaths.size(); ++k) {
                                            DBAudioInfo t_oldDBAudioInfo = t_oldDBCardInfo.m_musicPaths.get(k);
                                            m_dataBase.delete(t_oldDBAudioInfo);

                                            //删除文件

                                            File t_file = new File(t_oldDBAudioInfo.path);

                                            if (t_file.exists()) {
                                                t_file.delete();
                                            }
                                        }
                                    }

                                }else{
                                    m_dataBase.insert(t_DB_card);
                                }

                                //下载新的封面
                                if( t_coverImageMD5 != "" && t_coverImageUrl != "" && m_dataBase.getImage(t_coverImageMD5) == null )
                                {
                                    m_ImageDownloadQueue.add( new DBImageInfo( t_coverImageMD5, t_coverImageUrl ) );
                                }

                                //下载新的简笔画
                                if( t_lineDrawingMD5 != "" && t_lineDrawingUrl != "" && m_dataBase.getImage(t_lineDrawingMD5) == null )
                                {
                                    m_ImageDownloadQueue.add( new DBImageInfo( t_lineDrawingMD5, t_lineDrawingUrl ) );
                                }
                            }

                            //删除多余卡片信息

                            for (int i = 0; i < t_localDBCardInfos.size(); ++i) {
                                DBCardInfo t_oldDBCardInfo = t_localDBCardInfos.get(i);

                                DBImageInfo t_image = m_dataBase.getImage(t_oldDBCardInfo.m_coverImage);

                                if( t_image != null ){
                                    File t_file = new File( t_image.m_path );

                                    if (t_file.exists()) {
                                        t_file.delete();
                                    }
                                }

                                t_image = m_dataBase.getImage(t_oldDBCardInfo.m_lineDrawing);

                                if( t_image != null ){
                                    File t_file = new File( t_image.m_path );

                                    if (t_file.exists()) {
                                        t_file.delete();
                                    }
                                }


                                for (int k = 0; k < t_oldDBCardInfo.m_musicPaths.size(); ++k) {
                                    DBAudioInfo t_oldDBAudioInfo = t_oldDBCardInfo.m_musicPaths.get(k);
                                    m_dataBase.delete(t_oldDBAudioInfo);

                                    //删除文件

                                    File t_file = new File(t_oldDBAudioInfo.path);

                                    if (t_file.exists()) {
                                        t_file.delete();
                                    }
                                }

                                m_dataBase.delete(t_oldDBCardInfo);
                            }

                            ArrayList<DBCardInfo> t_list = m_dataBase.getActivationCardList();
                            Message t_message = new Message();
                            t_message.obj = "size :" + t_list.size();
                            m_handler.sendMessage(t_message);

                        }else{

                        }

                    }catch (Exception e) {
                        Log.e("ERROR", e.toString());
                    }

                    UIUpdateActivity.m_instance.m_listenQueue.remove(p_url);

                    Message t_message = new Message();
                    t_message.obj = "listen size changed";
                    UIUpdateActivity.m_instance.m_handler.sendMessage(t_message);
                }
            }).get();
        }
    }

    public void tryUpdateBatch()
    {

        m_batches = m_dataBase.getBatchList();

        String t_url = Control.sm_serviceHost + "/resource/batch/list/summary.do";
        UIUpdateActivity.m_instance.m_listenQueue.add(t_url);
        new Http(m_context, t_url, new HttpListen() {
            @Override
            public void callBack(String p_url, String p_str){

                try {

                    JSONObject t_json = new JSONObject(p_str);

                    Boolean t_success = t_json.getBoolean("success");


                    //读取数据失败
                    if(!t_success)
                    {
                        return;
                    }

                    JSONArray t_data = t_json.getJSONArray("data");

                    for( int i = 0; i < t_data.length(); ++i )
                    {
                        Log.d("DEBUG", "" + i );

                        JSONObject t_batchInfo = t_data.getJSONObject(i);

                        String t_batchId = t_batchInfo.getString("batchId");
                        String t_batchName = t_batchInfo.getString("batchSource");
                        String t_batchDesc = t_batchInfo.getString("batchDesc");
                        String t_coverImage = t_batchInfo.getString("coverImage");
                        String t_md5 = t_batchInfo.getString("coverMd5");

                        boolean t_isExist = false;
                        DBBatchInfo t_batch = null;
                        for( int k = 0; k < m_batches.size(); ++k )
                        {
                            t_batch = m_batches.get(k);

                            if( t_batch.m_id.equals( t_batchId ) )
                            {
                                m_batches.remove(m_batches.get(k));
                                t_isExist = true;
                                break;
                            }
                        }

                        if(!t_isExist)
                        {
                            m_dataBase.insert(new DBBatchInfo(t_batchId, t_batchName, t_batchDesc, t_md5, false, "animal"));
                            DBImageInfo t_image = m_dataBase.getImage(t_md5);
                            if( t_image == null )
                            {
                                m_ImageDownloadQueue.add( new DBImageInfo( t_md5, t_coverImage) );
                            }
                            continue;
                        }


                        if( !t_batch.m_cover.equals( t_md5 ) )
                        {
                            DBImageInfo t_localImg = m_dataBase.getImage(t_md5);
                            if( t_localImg != null )
                            {
                                m_dataBase.delete( t_localImg );
                            }

                            File t_file = new File( t_localImg.m_path );
                            if( t_file.exists() )
                            {
                                t_file.delete();
                            }
                        }

                        if( m_dataBase.getImage(t_md5) == null ){
                            m_ImageDownloadQueue.add( new DBImageInfo( t_md5, t_coverImage) );
                        }

                        if( ( !t_batch.m_cover.equals(t_coverImage) || !t_batch.m_batchName.equals(t_batchName) || !t_batch.m_explain.equals(t_batchDesc) ) || !t_batch.m_cover.equals( t_md5 ) )
                        {
                            t_batch.m_explain = t_batchDesc;
                            t_batch.m_batchName = t_batchName;
                            t_batch.m_cover = t_md5;

                            Log.d("DEBUG", "batch update");
                            m_dataBase.update(t_batch);
                        }
                    }


                }catch (Exception e)
                {
                    Log.d("ERROR", e.toString());
                    //解析数据出错
                    return;
                }

                for( int i = 0; i < m_batches.size(); ++i )
                {
                    //删除多余分组 (包括图片文件)
                    DBBatchInfo t_batch = m_batches.get(i);

                    //删除卡片

                    DBImageInfo t_image = m_dataBase.getImage(t_batch.m_cover);

                    if( t_image != null )
                    {
                        File t_file = new File( t_image.m_path );
                        if( t_file.exists() )
                        {
                            t_file.delete();
                        }

                        m_dataBase.delete(t_image);
                    }
                    m_dataBase.delete(t_batch);
                }

                Message t_message = new Message();
                t_message.obj = "tryUpdate";
                m_handler.sendMessage(t_message);

                UIUpdateActivity.m_instance.m_listenQueue.remove(p_url);

                t_message = new Message();
                t_message.obj = "listen size changed";
                UIUpdateActivity.m_instance.m_handler.sendMessage(t_message);
            }
        }).get();
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
        return m_fristDownloadQueue.size() > 0 || m_downloadQueue.size() > 0;
    }

    public void downloadFiles()
    {

        if( !needDownload() )
        {
            return;
        }

        Log.d("DEBUG", "download file start");

        while( m_fristDownloadQueue.size() > 0 )
        {
            m_downloadQueue.add( 0, m_fristDownloadQueue.get(0) );
            m_fristDownloadQueue.remove(0);
        }

        while( m_downloadQueue.size() > 0 )
        {
            DBAudioInfo t_DBAudioInfo = m_downloadQueue.get(0);
            m_downloadQueue.remove(0);

            String t_dir = m_audioPath + t_DBAudioInfo.cardId;
            File t_file = new File(t_dir);
            if( !t_file.exists() )
            {
                if( !t_file.mkdirs() )
                {
                    Log.d("DEBUG", "create dirs filed");
                    //创建文件夹失败
                    return;
                }
            }

            String t_path = t_DBAudioInfo.path;
            String[] t_list = t_path.split("/");
            String t_fileName = t_list[t_list.length - 1];

            t_DBAudioInfo.path = t_dir + "/" + t_fileName;

            boolean t_needDownload = true;

            for( DownLoadFile item : UIUpdateActivity.m_instance.m_downloadQueue )
            {
                if( item.compare( sm_serviceHost + "/" + t_path, t_DBAudioInfo.path ) )
                {
                    t_needDownload = false;
                    break;
                }
            }

            if( !t_needDownload )
            {
                continue;
            }

            Log.d("DEBUG", "download file: " + sm_serviceHost + t_path );

            UIUpdateActivity.m_instance.m_downloadQueue.add( new DownLoadFile(new DownLoadFileListen() {

                @Override
                public void onStartDownLoad() {
                    Log.d("DEBUG","download start");
                }

                @Override
                public void onEndDownload(boolean p_state, Object p_obj) {
                    Log.d("DEBUG", "download end: " + p_state);
                    if( p_state ) {

                        UIUpdateActivity.m_instance.downloadEnd();
                        //校验文件md5

                        DBAudioInfo t_DBAudioInfo = (DBAudioInfo)p_obj;

                        File t_file = new File( t_DBAudioInfo.path );

                        if( !t_file.exists() )
                        {
                            Log.d("DEBUG","---file not exists: " + t_DBAudioInfo.path + ", " + p_state );
                            return;
                        }

                        String t_md5 = getMD5Three( t_DBAudioInfo.path  );

                        Log.d( "DEBUG","service md5: " + t_DBAudioInfo.md5 + ", local md5: " + t_md5 );

                        if( ! t_md5.equals(t_DBAudioInfo.md5) )
                        {

                            if( t_file.exists() )
                            {
                                t_file.delete();
                            }

                            return;
                        }

                        m_dataBase.insert(t_DBAudioInfo);
                        //添加到音频表

                    }
                }

                @Override
                public void onUpdateDownloadRate(float p_rate) {
                }
            }, sm_serviceHost + "/" + t_path, t_DBAudioInfo.path, t_DBAudioInfo) );

        }
    }

    public void downloadImages()
    {

        while( m_ImageDownloadQueue.size() > 0 )
        {

            Log.d("DEBUG", "download images:" + m_ImageDownloadQueue.size() );

            File t_file = new File(m_imagePath);
            if( !t_file.exists() )
            {
                if( !t_file.mkdirs() )
                {
                    //创建文件夹失败
                    return;
                }
            }

            DBImageInfo t_image = m_ImageDownloadQueue.get(0);
            m_ImageDownloadQueue.remove(t_image);

            String[] t_list = t_image.m_path.split("/");
            String t_fileName = t_list[t_list.length - 1];

//
            Log.d("DEBUG", sm_serviceHost + "/" + t_image.m_path);
            Log.d("DEBUG", m_imagePath + t_fileName);

            String t_url = sm_serviceHost + "/" + t_image.m_path;

            t_image.m_path = m_imagePath + t_fileName;

            UIUpdateActivity.m_instance.m_downloadQueue.add( new DownLoadFile(new DownLoadFileListen() {
                @Override
                public void onStartDownLoad() {

                }

                @Override
                public void onEndDownload(boolean p_state, Object p_obj) {
                    UIUpdateActivity.m_instance.downloadEnd();

                    DBImageInfo t_image = (DBImageInfo) p_obj;

                    File t_file = new File( t_image.m_path );
                    if( !t_file.exists() )
                    {
                        Log.d("DEBUG", "---file not exists: " + t_image.m_path + ", " + p_state );
                        return;
                    }

                    String t_md5 = getMD5Three( t_image.m_path );

                    Log.d( "DEBUG","local md5: " + t_md5 );

                    if(!t_md5.equals(t_image.m_md5))
                    {
                        return;
                    }

                    DBImageInfo t_img = m_dataBase.getImage(t_image.m_md5);
                    if( t_img == null ) {
                        m_dataBase.insert(t_image);
                        return;
                    }

                    if( t_img.m_path != t_image.m_path )
                    {
                        m_dataBase.update(t_image);
                    }
                }

                @Override
                public void onUpdateDownloadRate(float p_rate) {

                }
            }, t_url, t_image.m_path, t_image ) );
        }
    }

    public void clearData()
    {
        m_dataBase.clearDataBase();
        deleteDir( m_audioPath );
        Log.d("DEBUG", "清空本地数据");
    }

    private void onScan(final int p_id )
    {
        DBCardInfo t_DB_card = null;

        //判断本地是否有当前卡片信息
        t_DB_card = m_dataBase.getCardById( p_id );

        if( t_DB_card != null && t_DB_card.m_musicPaths.size() > 0 )
        {

            Log.d("debug", "play");

            boolean m_existNormalAudio = false;

            for(int i = 0; i < t_DB_card.m_musicPaths.size() ; ++i )
            {
                if( t_DB_card.m_musicPaths.get(i).audioType != 2 )
                {
                    m_existNormalAudio = true;
                    break;
                }
            }


            //随机播放音频
            do {
                m_audioIndex = (int) Math.floor(Math.random() * t_DB_card.m_musicPaths.size());
            }while( m_audioIndex == t_DB_card.m_musicPaths.size() );

            DBAudioInfo t_DBAudioInfo = t_DB_card.m_musicPaths.get(m_audioIndex);

            if(t_DBAudioInfo == null)
            {
                return;
            }


            if( m_prevCardId != p_id )
            {
                if( m_soloMediaPlayer != null && m_soloMediaPlayer.isPlaying() )
                {
                    m_soloMediaPlayer.stop();
                }

                m_prevCardId = p_id;
            }

            if(t_DBAudioInfo.audioType == 2)
            {
                if( m_soloMediaPlayer != null && m_soloMediaPlayer.isPlaying() && m_existNormalAudio )
                {
                    //随机播放音频
                    do {
                        do {
                            m_audioIndex = (int) Math.floor(Math.random() * t_DB_card.m_musicPaths.size());
                        } while (m_audioIndex == t_DB_card.m_musicPaths.size());

                        t_DBAudioInfo = t_DB_card.m_musicPaths.get(m_audioIndex);
                    }while(t_DBAudioInfo.audioType == 2);
                }else {

                    for (int i = 0; i < m_mediaPlayerList.size(); ++i) {
                        MediaPlayer t_mediaPlayer = m_mediaPlayerList.get(i);

                        if (t_mediaPlayer != null && t_mediaPlayer.isPlaying()) {
                            t_mediaPlayer.stop();
                        }
                    }
                }
            }


            String t_path = t_DB_card.m_musicPaths.get(m_audioIndex).path;

            Message t_message = new Message();
            t_message.obj = "path: " + t_path;
            m_handler.sendMessage(t_message);


            MediaPlayer t_mediaPlayer = null;

            if( t_DBAudioInfo.audioType == 2 )
            {
                if( m_soloMediaPlayer == null )
                {
                    m_soloMediaPlayer = new MediaPlayer();
                }

                if(m_soloMediaPlayer.isPlaying())
                {
                    m_soloMediaPlayer.stop();
                }

                m_soloMediaPlayer.reset();

                t_mediaPlayer = m_soloMediaPlayer;

            }else {

                if( m_soloMediaPlayer != null && m_soloMediaPlayer.isPlaying())
                {
                    m_soloMediaPlayer.stop();
                }

                //选择播放器
                t_mediaPlayer = m_mediaPlayerList.get(m_mediaPlayerIndex);
                if( t_mediaPlayer == null )
                {
                    t_mediaPlayer = new MediaPlayer();
                    m_mediaPlayerList.set(m_mediaPlayerIndex, t_mediaPlayer );
                }

                m_mediaPlayerIndex = m_mediaPlayerIndex + 1 < m_mediaPlayerList.size() ? m_mediaPlayerIndex + 1 : 0;

                t_mediaPlayer.reset();
            }

            if(t_DBAudioInfo == null)
            {
                return;
            }

            try{
                t_mediaPlayer.setDataSource( t_DB_card.m_musicPaths.get(m_audioIndex).path );
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
        new Http( m_context, sm_serviceHost + "/resource/card/listOfSameBatch.do?RFID=" + p_id, new HttpListen() {
            @Override
            public void callBack(String p_url, String p_str) {

                try
                {
                    Log.d("DEBUG", "----" + p_str);

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

                        Log.d("Debug", "t_data.length()" + t_data.length());

                        for( int i = 0; i < t_data.length(); ++i ){
                            JSONObject t_animal = t_data.getJSONObject(i);
                            int t_cardId = t_animal.getInt("rfId");

                            //判断数据库中是否存在
                            DBCardInfo t_cardInfo = m_dataBase.getCardById( t_cardId );

                            Log.d( "Debug", "" + t_cardId + ": " + t_cardInfo.m_activation );

//                            if( t_cardInfo != null && t_cardInfo.m_activation )
//                            {
//                                continue;
//                            }

                            String t_resourceId = t_animal.getString("resourceId");
                            String t_groupId = t_animal.getString("ownerId");


                            if( !t_animal.getString("audios").equals("null") ) {
                                JSONArray t_musics = t_animal.getJSONArray("audios");
                                for (int k = 0; k < t_musics.length(); ++k) {
                                    String t_url = t_musics.getJSONObject(k).getString("attUrl");
                                    String t_md5 = t_musics.getJSONObject(k).getString("md5");

                                    DBAudioInfo t_audioInfo = new DBAudioInfo(t_md5, t_cardId, t_url, 1);

                                    if( t_cardId == p_id && k == 0 )
                                    {
                                        m_fristDownloadQueue.add( t_audioInfo );
                                    }else {
                                        m_downloadQueue.add( t_audioInfo );
                                    }
                                }
                            }

                            Log.d("DEBUG", t_animal.getString("pronAudio") );

                            if( !t_animal.getString("pronAudio").equals("null") ) {

                                JSONObject t_pronAudios = t_animal.getJSONObject("pronAudio");
                                if (t_pronAudios != null && t_pronAudios.getString("attUrl") != "") {
                                    String t_url = t_pronAudios.getString("attUrl");
                                    String t_md5 = t_pronAudios.getString("md5");

                                    DBAudioInfo t_audioInfo = new DBAudioInfo(t_md5, t_cardId, t_url, 2);

                                    if( t_cardId == p_id )
                                    {
                                        m_fristDownloadQueue.add( t_audioInfo );
                                    }else {
                                        m_downloadQueue.add(t_audioInfo);
                                    }
                                }
                            }

                            if( !t_animal.getString("descAudio").equals("null") ) {
                                JSONObject t_descAudios = t_animal.getJSONObject("descAudio");
                                if( t_descAudios != null && t_descAudios.getString("attUrl") != "" )
                                {
                                    String t_url = t_descAudios.getString("attUrl");
                                    String t_md5 = t_descAudios.getString("md5");

                                    DBAudioInfo t_audioInfo = new DBAudioInfo(t_md5, t_cardId, t_url, 2);

                                    if( t_cardId == p_id )
                                    {
                                        m_fristDownloadQueue.add( t_audioInfo );
                                    }else {
                                        m_downloadQueue.add(t_audioInfo);
                                    }
                                }
                            }

                            t_cardInfo.m_activation = true;
                            m_dataBase.update( t_cardInfo );

                            DBBatchInfo t_batch = m_dataBase.getBatchInfo( t_cardInfo.m_group );
                            if( !t_batch.m_activation )
                            {
                                t_batch.m_activation = true;
                                m_dataBase.update( t_batch );
                            }


                            Log.d("DEBUG", "need download: " + t_resourceId);
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

    public void addBlueStateListen( BlueManagerStateListen p_blueStateListen )
    {
        if(!m_blueManager.blueManagerStateListens.contains(p_blueStateListen))
        {
            m_blueManager.blueManagerStateListens.add(p_blueStateListen);
        }
    }

    public void deleteBlueStateListen(BlueManagerStateListen p_blueStateListen)
    {

        if(m_blueManager.blueManagerStateListens.contains(p_blueStateListen))
        {
            m_blueManager.blueManagerStateListens.remove(p_blueStateListen);
        }
    }


    public boolean isNetworkConnected()
    {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) m_context
                       .getSystemService(Context.CONNECTIVITY_SERVICE);
       NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
       if (mNetworkInfo != null) {
           return mNetworkInfo.isAvailable();
       }

       return false;
    }

    public boolean isBlueOpen()
    {
        return m_blueManager.deviceIsOpen();
    }

    //删除文件夹和文件夹里面的文件
    public static void deleteDir(final String pPath) {
        File dir = new File(pPath);
        deleteDirWithFile(dir);
    }

    public static void deleteDirWithFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWithFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }


    public static String getMD5Three(String path)
    {
        String result = "";
        BigInteger bi = null;
        try {
            byte[] buffer = new byte[8192];
            int len = 0;
            MessageDigest md = MessageDigest.getInstance("MD5");
            File f = new File(path);
            FileInputStream fis = new FileInputStream(f);
            while ((len = fis.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            fis.close();
            byte[] b = md.digest();
            bi = new BigInteger(1, b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(bi != null)
        {
            result = bi.toString(16);

            while (result.length() < 32) {
                result = "0" + result;
            }
        }

        return result;
    }

}
