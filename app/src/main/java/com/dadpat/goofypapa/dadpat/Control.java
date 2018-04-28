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

    private Db_Animals m_db_animals;

    private ArrayList<AnimalAudio> m_downloadQueue;
    private ArrayList<ImageInfo> m_ImageDownloadQueue;

    ArrayList<BatchInfo> m_batches = null;

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
                    UpdateActivity.m_instance.download();
                }

                else if( t_cmd == "tryUpdateAnimal" )
                {
                    tryUpdateAnimal();
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
            public void onScan( String p_code )
            {
                try {
                    int t_id = Integer.parseInt(p_code);
                    m_this.onScan(t_id);
                }catch (Exception e){

                }
            }
        } );

        m_db_animals = new Db_Animals( m_context );

//        m_db_animals.clearAnimals();

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
        ArrayList<String> t_list = m_db_animals.getAnimalGroupList();
        for( int i = 0; i < t_list.size(); ++i )
        {
            String t_url = sm_serviceHost + "/resource/card/list.do?batchId=" + t_list.get(i);
            UpdateActivity.m_instance.m_listenQueue.add(t_url);
            new Http(m_context, t_url, new HttpListen() {
                @Override
                public void callBack(String p_url, String p_str) {

                    ArrayList<Animal> t_localAnimals = m_db_animals.getListInfo();

                    int t_deleteAnimalCount = 0, t_deleteAudioCount = 0;

                    try {
                        JSONObject t_jsonObject = new JSONObject(p_str);

                        boolean t_success = t_jsonObject.getBoolean("success");
                        if(t_success)
                        {
                            JSONArray t_data = t_jsonObject.getJSONArray("data");

                            for( int i = 0; i < t_data.length(); ++i )
                            {
                                JSONObject t_animalObject = t_data.getJSONObject(i);

                                int t_rfid = t_animalObject.getInt("rfId");
                                String t_resourceId = t_animalObject.getString("resourceId");
                                String t_ownerId = t_animalObject.getString("ownerId");


                                ArrayList<AnimalAudio> t_animalAudios = new ArrayList<>();

                                JSONArray t_musics = t_animalObject.getJSONArray("audios");
                                for( int k = 0; k < t_musics.length(); ++k ){
                                    String t_url = t_musics.getJSONObject(k).getString("attUrl");
                                    String t_md5 = t_musics.getJSONObject(k).getString("md5");

                                    t_animalAudios.add( new AnimalAudio( t_rfid, t_resourceId, t_url, t_md5, 1 ) );
                                }

                                if( t_animalObject.getString("pronAudio") != "null" ) {

                                    JSONObject t_pronAudios = t_animalObject.getJSONObject("pronAudio");
                                    if (t_pronAudios != null && t_pronAudios.getString("attUrl") != "") {
                                        String t_url = t_pronAudios.getString("attUrl");
                                        String t_md5 = t_pronAudios.getString("md5");
                                        t_animalAudios.add(new AnimalAudio(t_rfid, t_resourceId, t_url, t_md5, 2));
                                    }
                                }

                                if( t_animalObject.getString("descAudio") != "null" ) {
                                    JSONObject t_descAudios = t_animalObject.getJSONObject("descAudio");
                                    if( t_descAudios != null && t_descAudios.getString("attUrl") != "" )
                                    {
                                        String t_url = t_descAudios.getString("attUrl");
                                        String t_md5 = t_descAudios.getString("md5");
                                        t_animalAudios.add(new AnimalAudio(t_rfid, t_resourceId, t_url, t_md5, 2));
                                    }
                                }

                                Animal t_animal = new Animal( t_rfid, t_resourceId, t_ownerId, "", t_animalAudios );

                                Animal t_oldAnimal = null;
                                for( int k = 0; k < t_localAnimals.size(); ++k )
                                {
                                    if( t_localAnimals.get(k).m_cardNumber == t_rfid )
                                    {
                                        t_oldAnimal = t_localAnimals.get(k);
                                        break;
                                    }
                                }

                                if( t_oldAnimal != null )
                                {
                                    t_localAnimals.remove(t_oldAnimal);

                                    if( !t_oldAnimal.m_serviceId.equals(t_resourceId) )
                                    {
                                        deleteDir(m_audioPath + t_oldAnimal.m_serviceId );
                                        m_downloadQueue.addAll(t_animalAudios);
                                    }

                                    if( t_animal.m_group.equals(t_oldAnimal.m_group) || t_animal.m_serviceId.equals(t_oldAnimal.m_serviceId) || !t_animal.m_coverImage.equals(t_oldAnimal.m_coverImage) )
                                    {
                                        m_db_animals.update(t_animal);
                                    }

                                    for( int k = 0; k < t_animalAudios.size(); ++k )
                                    {
                                        AnimalAudio t_animalAudio = t_animalAudios.get(k);

                                        AnimalAudio t_oldAnimalAudio = null;
                                        for(int n = 0; n < t_oldAnimal.m_musicPaths.size(); ++n)
                                        {
                                            if( t_oldAnimal.m_musicPaths.get(n).url.equals( t_animalAudio.url ) )
                                            {
                                                t_oldAnimalAudio = t_oldAnimal.m_musicPaths.get(n);
                                            }
                                        }

                                        if( t_oldAnimalAudio == null )
                                        {
                                            m_downloadQueue.add(t_animalAudio);

                                            Message t_message = new Message();
                                            t_message.obj = "null-download: " + t_animalAudio.url;
                                            m_handler.sendMessage(t_message);
                                        }else
                                        {

                                            if( !t_oldAnimalAudio.md5.equals( t_animalAudio.md5 ) )
                                            {
                                                m_downloadQueue.add(t_animalAudio);

                                                Message t_message = new Message();
                                                t_message.obj = "md5-download: " + t_animalAudio.url + ", old: " + t_oldAnimalAudio.md5 + ", new: " + t_animalAudio.md5 ;
                                                m_handler.sendMessage(t_message);
                                            }

                                            if( !t_oldAnimalAudio.md5.equals( t_animalAudio.md5 ) || t_oldAnimalAudio.audioType != t_oldAnimalAudio.audioType )
                                            {
                                                t_animalAudio.id = t_oldAnimalAudio.id;
                                                m_db_animals.update(t_animalAudio);
                                            }

                                            t_oldAnimal.m_musicPaths.remove(t_oldAnimalAudio);
                                        }
                                    }


                                    //删除本地多余的音频文件
                                    for(int k = 0; k < t_oldAnimal.m_musicPaths.size(); ++k)
                                    {
                                        AnimalAudio t_oldAnimalAudio = t_oldAnimal.m_musicPaths.get(k);
                                        m_db_animals.delete(t_oldAnimalAudio);

                                        //删除文件

                                        String t_dir = m_audioPath + t_oldAnimalAudio.dir;
                                        String[] t_list = t_oldAnimalAudio.url.split("/");
                                        String t_fileName = t_list[t_list.length - 1];

                                        File t_file = new File(t_dir + "/" + t_fileName );

                                        if( t_file.exists() )
                                        {
                                            t_file.delete();
                                        }
                                    }

                                }else{
                                    m_db_animals.insert( t_animal );
                                    m_downloadQueue.addAll(t_animalAudios);
                                }
                            }

                            //删除多余卡片信息

                            for (int i = 0; i < t_localAnimals.size(); ++i) {
                                Animal t_oldAnimal = t_localAnimals.get(i);

                                Message t_message = new Message();
                                t_message.obj = "delete :" + t_oldAnimal.m_serviceId;
                                m_handler.sendMessage(t_message);

                                for (int k = 0; k < t_oldAnimal.m_musicPaths.size(); ++k) {
                                    AnimalAudio t_oldAnimalAudio = t_oldAnimal.m_musicPaths.get(k);
                                    m_db_animals.delete(t_oldAnimalAudio);

                                    //删除文件
                                    String t_dir = m_audioPath + t_oldAnimalAudio.dir;
                                    String[] t_list = t_oldAnimalAudio.url.split("/");
                                    String t_fileName = t_list[t_list.length - 1];

                                    t_message = new Message();
                                    t_message.obj = "delete :" + t_dir + "/" + t_fileName;
                                    m_handler.sendMessage(t_message);

                                    File t_file = new File(t_dir + "/" + t_fileName);

                                    if (t_file.exists()) {
                                        t_file.delete();
                                        t_deleteAudioCount++;
                                    }
                                }

                                m_db_animals.delete(t_oldAnimal);
                                t_deleteAnimalCount++;

                            }

                            ArrayList<Animal> t_list = m_db_animals.getListInfo();
                            Message t_message = new Message();
                            t_message.obj = "size :" + t_list.size();
                            m_handler.sendMessage(t_message);


                            t_message = new Message();
                            t_message.obj = "delete animal: " + t_deleteAnimalCount + ", delete audio: " + t_deleteAudioCount;
                            m_handler.sendMessage(t_message);

                        }else{

                        }

                    }catch (Exception e) {
                        Log.e("ERROR", e.toString());
                    }

                    UpdateActivity.m_instance.m_listenQueue.remove(p_url);

                    Message t_message = new Message();
                    t_message.obj = "listen size changed";
                    UpdateActivity.m_instance.m_handler.sendMessage(t_message);
                }
            }).get();
        }
    }

    public void tryUpdateAnimal()
    {
        Log.d("DEBUG", "tryUpdateAnimal" );

        ArrayList<BatchInfo> t_batchList = m_db_animals.getBatchList();

        for( int i = 0; i < t_batchList.size(); ++i )
        {
            BatchInfo t_batch = t_batchList.get(i);

            String t_url = Control.sm_serviceHost + "/resource/card/list.do?batchId=" + t_batch.m_id;

            UpdateActivity.m_instance.m_listenQueue.add(t_url);

            new Http(m_context, t_url, new HttpListen() {
                @Override
                public void callBack(String p_url, String p_str) {

                    try{
                        JSONObject t_json = new JSONObject(p_str);

                        Boolean t_success = t_json.getBoolean("success");

                        //读取数据失败
                        if(!t_success)
                        {
                            return;
                        }

                        JSONArray t_data = t_json.getJSONArray("data");

                        ArrayList<Animal> t_animalList = new ArrayList<>();

                        for( int i = 0; i < t_data.length(); ++i )
                        {
                            JSONObject t_animalObject = t_data.getJSONObject(i);

                            int t_rfId = t_animalObject.getInt("rfId");
                            String t_resourceId = t_animalObject.getString("resourceId");
                            String t_ownerId = t_animalObject.getString("ownerId");

                            if( i == 0 )
                            {
                                t_animalList = m_db_animals.getAnimalsImage( t_ownerId );
                            }

                            String t_coverImageUrl = "";
                            String t_md5 = "";

                            if( t_animalObject.getString("coverImage") != "null" )
                            {
                                JSONObject t_coverImage = t_animalObject.getJSONObject("coverImage");
                                t_coverImageUrl = t_coverImage.getString("attUrl");
                                ImageInfo t_image = m_db_animals.getImage(t_coverImageUrl);
                                t_md5 = t_coverImage.getString("md5");
                                if( t_image == null )
                                {
                                    m_ImageDownloadQueue.add( new ImageInfo( t_coverImageUrl, t_md5 ) );
                                }else{

                                    String[] t_list = t_image.m_url.split("/");
                                    String t_fileName = t_list[t_list.length - 1];
                                    if( !getMD5Three(m_imagePath + t_fileName).equals(t_md5) )
                                    {
                                        m_ImageDownloadQueue.add( new ImageInfo( t_coverImageUrl, t_md5 ) );
                                    }
                                }
                            }

                            Animal t_animal = new Animal( t_rfId, t_resourceId, t_ownerId, t_coverImageUrl, new ArrayList<AnimalAudio>() );

                            Animal t_oldAnimal = null;

                            for(int k = 0; k < t_animalList.size(); ++k)
                            {
                                t_oldAnimal = t_animalList.get(k);

                                if( t_oldAnimal.m_cardNumber == t_animal.m_cardNumber )
                                {
                                    break;
                                }
                                t_oldAnimal = null;
                            }

                            if( t_oldAnimal == null )
                            {
                                m_db_animals.insertAnimalImage(t_animal);
                            }else {
                                t_animalList.remove(t_oldAnimal);
                                if (
                                        !t_oldAnimal.m_serviceId.equals(t_animal.m_serviceId) ||
                                                !t_oldAnimal.m_group.equals(t_animal.m_group) ||
                                                !t_oldAnimal.m_coverImage.equals(t_animal.m_coverImage)) {
                                    m_db_animals.updateAnimalImage(t_animal);
                                }
                            }

                            ImageInfo t_image = m_db_animals.getImage( t_animal.m_coverImage );

                            if( t_image == null || !t_image.m_md5.equals(t_md5) )
                            {
                                m_ImageDownloadQueue.add( new ImageInfo( t_animal.m_coverImage, t_md5 ) );
                            }
                        }

                        //删除多余卡片信息
                        for(int i = 0; i < t_animalList.size(); ++i)
                        {
                            Animal t_animal = t_animalList.get(i);

                            ImageInfo t_image = m_db_animals.getImage(t_animal.m_coverImage);

                            if( t_image != null )
                            {

                                String[] t_list = t_image.m_url.split("/");
                                String t_fileName = t_list[t_list.length - 1];
                                File t_file = new File(m_imagePath + t_fileName);
                                if( t_file.exists() )
                                {
                                    t_file.delete();
                                }

                                m_db_animals.delete(t_image);
                            }

                            m_db_animals.deleteAnimalImage(t_animal);
                        }


                    }catch (Exception e)
                    {
                        Log.e("ERROR", e.toString());
                    }

                    UpdateActivity.m_instance.m_listenQueue.remove(p_url);
                    Message t_message = new Message();
                    t_message.obj = "listen size changed";
                    UpdateActivity.m_instance.m_handler.sendMessage(t_message);
                }
            }).get();
        }
    }

    public void tryUpdateBatch()
    {

        m_batches = m_db_animals.getBatchList();

        String t_url = Control.sm_serviceHost + "/resource/batch/list/summary.do";
        UpdateActivity.m_instance.m_listenQueue.add(t_url);
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
                        BatchInfo t_batch = null;
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

                            m_db_animals.insert(new BatchInfo(t_batchId, t_batchName, t_batchDesc, t_coverImage));
                            m_ImageDownloadQueue.add( new ImageInfo( t_coverImage, t_md5) );
                            continue;
                        }

                        ImageInfo t_image = m_db_animals.getImage(t_coverImage);
                        if( t_image == null )
                        {
                            m_ImageDownloadQueue.add( new ImageInfo( t_coverImage, t_md5) );
                            continue;
                        }

                        if( !t_batch.m_cover.equals(t_coverImage) )
                        {
                            ImageInfo t_localImg = m_db_animals.getImage(t_coverImage);
                            if( t_localImg != null )
                            {
                                m_db_animals.delete(t_localImg);
                            }

                            String[] t_list = t_coverImage.split("/");
                            String t_fileName = t_list[t_list.length - 1];

                            File t_file = new File(m_imagePath + t_fileName );
                            if( t_file.exists() )
                            {
                                t_file.delete();
                            }

                            m_ImageDownloadQueue.add( new ImageInfo( t_coverImage, t_md5) );
                        }else {

                            String[] t_list = t_coverImage.split("/");
                            String t_fileName = t_list[t_list.length - 1];

                            File t_file = new File(m_imagePath + t_fileName);
                            if ( ( !t_image.m_md5.equals(t_md5) || !t_file.exists() ) && !t_coverImage.equals("null") && !t_md5.equals("null") ) {
                                m_ImageDownloadQueue.add( new ImageInfo( t_coverImage, t_md5) );
                            }
                        }

                        if( ( !t_batch.m_cover.equals(t_coverImage) || !t_batch.m_batchName.equals(t_batchName) || !t_batch.m_explain.equals(t_batchDesc) ) && !t_coverImage.equals("null") && !t_md5.equals("null") )
                        {
                            t_batch.m_explain = t_batchDesc;
                            t_batch.m_batchName = t_batchName;
                            t_batch.m_cover = t_coverImage;

                            Log.d("DEBUG", "batch update");
                            m_db_animals.update(t_batch);
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
                    BatchInfo t_batch = m_batches.get(i);

                    ImageInfo t_image = m_db_animals.getImage(t_batch.m_cover);

                    if( t_image != null )
                    {
                        String[] t_list = t_image.m_url.split("/");
                        String t_fileName = t_list[t_list.length - 1];
                        File t_file = new File(m_imagePath + t_fileName);
                        if( t_file.exists() )
                        {
                            t_file.delete();
                        }

                        m_db_animals.delete(t_image);
                    }
                    m_db_animals.delete(t_batch);
                }

                Message t_message = new Message();
                t_message.obj = "tryUpdateAnimal";
                m_handler.sendMessage(t_message);

                UpdateActivity.m_instance.m_listenQueue.remove(p_url);

                t_message = new Message();
                t_message.obj = "listen size changed";
                UpdateActivity.m_instance.m_handler.sendMessage(t_message);
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
        return m_downloadQueue.size() > 0;
    }

    public void downloadFiles()
    {

        if( !needDownload() )
        {
            return;
        }

        Log.d("DEBUG", "download file start");

        while( m_downloadQueue.size() > 0 )
        {
            AnimalAudio t_animalAudio = m_downloadQueue.get(0);
            m_downloadQueue.remove(t_animalAudio);

            String t_dir = m_audioPath + t_animalAudio.dir;
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

            String t_path = t_animalAudio.url;
            String[] t_list = t_path.split("/");
            String t_fileName = t_list[t_list.length - 1];

            Log.d("DEBUG", "download file: " + sm_serviceHost + t_path );

            UpdateActivity.m_instance.m_downloadQueue.add( new DownLoadFile( new DownLoadFileListen() {

                @Override
                public void onStartDownLoad() {
                    Log.d("DEBUG","download start");
                }

                @Override
                public void onEndDownload(boolean p_state, Object p_obj) {
                    Log.d("DEBUG", "download end: " + p_state);
                    if( p_state ) {

                        UpdateActivity.m_instance.downloadEnd();
                        //校验文件md5

                        AnimalAudio t_animalAudio = (AnimalAudio)p_obj;

                        String t_dir = m_audioPath + t_animalAudio.dir;
                        String[] t_list = t_animalAudio.url.split("/");
                        String t_fileName = t_list[t_list.length - 1];

                        File t_file = new File( t_dir + "/" + t_fileName );

                        if( !t_file.exists() )
                        {
                            Log.d("DEBUG","---file not exists: " + t_dir + "/" + t_fileName + ", " + p_state );
                            return;
                        }

                        String t_md5 = getMD5Three( t_dir + "/" + t_fileName );

                        Log.d( "DEBUG","service md5: " + t_animalAudio.md5 + ", local md5: " + t_md5 );

                        if( ! t_md5.equals(t_animalAudio.md5) )
                        {

                            if( t_file.exists() )
                            {
                                t_file.delete();
                            }

                            return;
                        }

                        m_db_animals.insert(t_animalAudio);
                        //添加到音频表

                    }
                }

                @Override
                public void onUpdateDownloadRate(float p_rate) {
                }
            }, sm_serviceHost + "/" + t_path, t_dir + "/" + t_fileName, t_animalAudio ) );

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

            ImageInfo t_image = m_ImageDownloadQueue.get(0);
            m_ImageDownloadQueue.remove(t_image);

            String[] t_list = t_image.m_url.split("/");
            String t_fileName = t_list[t_list.length - 1];

            Log.d("DEBUG", sm_serviceHost + "/" + t_image.m_url);
            Log.d("DEBUG", m_imagePath + t_fileName);

            UpdateActivity.m_instance.m_downloadQueue.add( new DownLoadFile(new DownLoadFileListen() {
                @Override
                public void onStartDownLoad() {

                }

                @Override
                public void onEndDownload(boolean p_state, Object p_obj) {
                    UpdateActivity.m_instance.downloadEnd();

                    ImageInfo t_image = (ImageInfo) p_obj;

                    String[] t_list = t_image.m_url.split("/");
                    String t_fileName = t_list[t_list.length - 1];

                    String t_imagePath = m_imagePath + t_fileName;

                    File t_file = new File( t_imagePath );
                    if( !t_file.exists() )
                    {
                        Log.d("DEBUG", "---file not exists: " + t_imagePath + ", " + p_state );
                        return;
                    }

                    String t_md5 = getMD5Three( t_imagePath );

                    Log.d( "DEBUG","local md5: " + t_md5 );

                    if(!t_md5.equals(t_image.m_md5))
                    {
                        return;
                    }

                    ImageInfo t_img = m_db_animals.getImage(t_image.m_url);
                    if( t_img == null ) {
                        m_db_animals.insert(new ImageInfo(t_image.m_url, t_md5));
                        return;
                    }

                    if( t_img.m_md5 != t_md5 )
                    {
                        m_db_animals.update(new ImageInfo(t_image.m_url, t_md5));
                    }
                }

                @Override
                public void onUpdateDownloadRate(float p_rate) {

                }
            }, sm_serviceHost + "/" + t_image.m_url,m_imagePath + t_fileName, t_image ) );
        }
    }

    public void clearData()
    {
        m_db_animals.clearAnimals();
        deleteDir( m_audioPath );
        Log.d("DEBUG", "清空本地数据");
    }

    private void onScan(final int p_id )
    {

        Animal t_animal = null;

        //判断本地是否有当前卡片信息
        t_animal = m_db_animals.getAnimalByCardId( p_id );

        if( t_animal != null && t_animal.m_musicPaths.size() > 0 )
        {

            boolean m_existNormalAudio = false;

            for( int i = 0; i < t_animal.m_musicPaths.size() ; ++i )
            {
                if( t_animal.m_musicPaths.get(i).audioType != 2 )
                {
                    m_existNormalAudio = true;
                    break;
                }
            }


            //随机播放音频
            do {
                m_audioIndex = (int) Math.floor(Math.random() * t_animal.m_musicPaths.size());
            }while( m_audioIndex == t_animal.m_musicPaths.size() );

            AnimalAudio t_animalAudio = t_animal.m_musicPaths.get(m_audioIndex);

            if(t_animalAudio == null)
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

            if(t_animalAudio.audioType == 2)
            {
                if( m_soloMediaPlayer != null && m_soloMediaPlayer.isPlaying() && m_existNormalAudio )
                {
                    //随机播放音频
                    do {
                        do {
                            m_audioIndex = (int) Math.floor(Math.random() * t_animal.m_musicPaths.size());
                        } while (m_audioIndex == t_animal.m_musicPaths.size());

                        t_animalAudio = t_animal.m_musicPaths.get(m_audioIndex);
                    }while(t_animalAudio.audioType == 2);
                }else {

                    for (int i = 0; i < m_mediaPlayerList.size(); ++i) {
                        MediaPlayer t_mediaPlayer = m_mediaPlayerList.get(i);

                        if (t_mediaPlayer != null && t_mediaPlayer.isPlaying()) {
                            t_mediaPlayer.stop();
                        }
                    }
                }
            }


            String t_dir = m_audioPath + t_animal.m_serviceId;
            String t_path = t_animal.m_musicPaths.get(m_audioIndex).url;

            Message t_message = new Message();
            t_message.obj = "path: " + t_path;
            m_handler.sendMessage(t_message);


            MediaPlayer t_mediaPlayer = null;

            if( t_animalAudio.audioType == 2 )
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

            if(t_animalAudio == null)
            {
                return;
            }


            String[] t_list = t_animal.m_musicPaths.get(m_audioIndex).url.split("/");
            String t_fileName = t_list[t_list.length - 1];

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

                        for( int i = 0; i < t_data.length(); ++i ){
                            JSONObject t_animal = t_data.getJSONObject(i);
                            int t_cardId = t_animal.getInt("rfId");

                            //判断数据库中是否存在
                            boolean t_isExist = m_db_animals.getAnimalByCardId( t_cardId ) != null;

                            if( t_isExist )
                            {
                                continue;
                            }

                            Log.d("Debug", "---------");

                            String t_resourceId = t_animal.getString("resourceId");
                            String t_groupId = t_animal.getString("ownerId");


                            JSONArray t_musics = t_animal.getJSONArray("audios");
                            for( int k = 0; k < t_musics.length(); ++k ){
                                String t_url = t_musics.getJSONObject(k).getString("attUrl");
                                String t_md5 = t_musics.getJSONObject(k).getString("md5");

                                m_downloadQueue.add( new AnimalAudio( t_cardId, t_resourceId, t_url, t_md5, 1 ) );
                            }

                            Log.d("DEBUG", t_animal.getString("pronAudio") );

                            if( !t_animal.getString("pronAudio").equals("null") ) {

                                JSONObject t_pronAudios = t_animal.getJSONObject("pronAudio");
                                if (t_pronAudios != null && t_pronAudios.getString("attUrl") != "") {
                                    String t_url = t_pronAudios.getString("attUrl");
                                    String t_md5 = t_pronAudios.getString("md5");
                                    m_downloadQueue.add(new AnimalAudio(t_cardId, t_resourceId, t_url, t_md5, 2));
                                }
                            }

                            if( !t_animal.getString("descAudio").equals("null") ) {
                                JSONObject t_descAudios = t_animal.getJSONObject("descAudio");
                                if( t_descAudios != null && t_descAudios.getString("attUrl") != "" )
                                {
                                    String t_url = t_descAudios.getString("attUrl");
                                    String t_md5 = t_descAudios.getString("md5");
                                    m_downloadQueue.add(new AnimalAudio(t_cardId, t_resourceId, t_url, t_md5, 2));
                                }
                            }

//                            String t_coverImageUrl = "";
//
//                            if( !t_animal.getString("coverImage").equals("null") )
//                            {
//                                JSONObject t_coverImage = t_animal.getJSONObject("coverImage");
//                                t_coverImageUrl = t_coverImage.getString("attUrl");
//                                ImageInfo t_image = m_db_animals.getImage(t_coverImageUrl);
//                                if( t_image == null )
//                                {
//                                    m_ImageDownloadQueue.add( t_coverImageUrl );
//                                }
//                            }


                            Log.d("DEBUG", "need download: " + t_resourceId);

                            m_db_animals.insert( new Animal( t_cardId, t_resourceId, t_groupId, "", new ArrayList<AnimalAudio>() ) );

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
