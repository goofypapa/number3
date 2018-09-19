package com.dadpat.goofypapa.dadpat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public class Http {

    Context m_context;
    String m_url;
    String m_result;
    boolean m_requesting;
    HttpListen m_httpListen;


    public Http( Context p_context, String p_url, HttpListen p_httpListen )
    {
        m_context = p_context;
        m_url = p_url;
        m_requesting = false;
        m_result = null;
        m_httpListen = p_httpListen;
    }

    public void get( )
    {
        if( m_requesting ) return;
        m_result = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                m_requesting = true;
                m_result = "";
                try {
                    URL t_url = new URL(m_url);
                    URLConnection t_conn = t_url.openConnection();
                    InputStream t_is = t_conn.getInputStream();

                    BufferedReader t_reader = new BufferedReader(new InputStreamReader(t_is, Charset.forName("UTF-8")));

                    String t_line = null;

                    while( (t_line = t_reader.readLine()) != null )
                    {
                        if( m_result == null )
                        {
                            m_result = t_line;
                            continue;
                        }
                        m_result += t_line;
                    }

                    m_httpListen.callBack( m_url, m_result );

                }catch (Exception e){

                }
                m_requesting = false;
            }
        }).start();
    }

//    public void uploadFile( File file, String RequestURL )
//    {
//        /**
//         * android上传文件到服务器
//         * @param file  需要上传的文件
//         * @param RequestURL  请求的rul
//         * @return  返回响应的内容
//         */
//        public static String uploadFile(File file, String RequestURL){
//        String result = null;
//        String  BOUNDARY =  UUID.randomUUID().toString();  //边界标识   随机生成
//        String PREFIX = "--" , LINE_END = "\r\n";
//        String CONTENT_TYPE = "multipart/form-data";   //内容类型
//
//        try {
//            URL url = new URL(RequestURL);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setReadTimeout(TIME_OUT);
//            conn.setConnectTimeout(TIME_OUT);
//            conn.setDoInput(true);  //允许输入流
//            conn.setDoOutput(true); //允许输出流
//            conn.setUseCaches(false);  //不允许使用缓存
//            conn.setRequestMethod("POST");  //请求方式
//            conn.setRequestProperty("Charset", CHARSET);  //设置编码
//            conn.setRequestProperty("connection", "keep-alive");
//            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
//            conn.setRequestProperty("action", "upload");
//            conn.connect();
//
//            if(file!=null){
//                /**
//                 * 当文件不为空，把文件包装并且上传
//                 */
//                DataOutputStream dos = new DataOutputStream( conn.getOutputStream());
//                StringBuffer sb = new StringBuffer();
//                sb.append(PREFIX);
//                sb.append(BOUNDARY);
//                sb.append(LINE_END);
//                /**
//                 * 这里重点注意：
//                 * name里面的值为服务器端需要key   只有这个key 才可以得到对应的文件
//                 * filename是文件的名字，包含后缀名的   比如:abc.png
//                 */
//
//                sb.append("Content-Disposition: form-data; name=\"img\"; filename=\""+file.getName()+"\""+LINE_END);
//                sb.append("Content-Type: application/octet-stream; charset="+CHARSET+LINE_END);
//                sb.append(LINE_END);
//                dos.write(sb.toString().getBytes());
//                InputStream is = new FileInputStream(file);
//                byte[] bytes = new byte[1024];
//                int len = 0;
//                while((len=is.read(bytes))!=-1){
//                    dos.write(bytes, 0, len);
//                }
//                is.close();
//                dos.write(LINE_END.getBytes());
//                byte[] end_data = (PREFIX+BOUNDARY+PREFIX+LINE_END).getBytes();
//                dos.write(end_data);
//                dos.flush();
//                /**
//                 * 获取响应码  200=成功
//                 * 当响应成功，获取响应的流
//                 */
//                int res = conn.getResponseCode();
//                if(res==200){
//                    InputStream input =  conn.getInputStream();
//                    StringBuffer sb1= new StringBuffer();
//                    int ss ;
//                    while((ss=input.read())!=-1){
//                        sb1.append((char)ss);
//                    }
//                    result = sb1.toString();
//                    System.out.println(result);
//                }
//            }
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return result;
//    }
//    }

//    public static void getBitmap(final String p_url, ImageView p_imageView, GetBitmapListen p_getBitmapListen)
//    {
//        final Bitmap[] t_result = {null};
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try{
//
//                    URL conn = new URL(p_url);
//                    InputStream in = conn.openConnection().getInputStream();
//                    t_result[0] = BitmapFactory.decodeStream(in);
//                    in.close();
//
//                }catch (Exception e){
//
//                }
//            }
//        }).start();
//
//        p_imageView.setImageBitmap(t_result[0]);
//
//    }
}
