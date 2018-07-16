package com.dadpat.goofypapa.dadpat;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class UIWebViewActivity extends AppCompatActivity {

    private WebView m_vm_main;

    private WebChromeClient mWebChromeClient;

    public static String s_animalId;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);


        m_vm_main = findViewById(R.id.wv_main);

        WebSettings ws = m_vm_main.getSettings();
        /**
         *
         * setAllowFileAccess 启用或禁止WebView访问文件数据 setBlockNetworkImage 是否显示网络图像
         * setBuiltInZoomControls 设置是否支持缩放 setCacheMode 设置缓冲的模式
         * setDefaultFontSize 设置默认的字体大小 setDefaultTextEncodingName 设置在解码时使用的默认编码
         * setFixedFontFamily 设置固定使用的字体 setJavaSciptEnabled 设置是否支持Javascript
         * setLayoutAlgorithm 设置布局方式 setLightTouchEnabled 设置用鼠标激活被选项
         * setSupportZoom 设置是否支持变焦
         * */
        ws.setBuiltInZoomControls(true);// 隐藏缩放按钮
        ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);// 排版适应屏幕
        ws.setUseWideViewPort(true);// 可任意比例缩放
        ws.setLoadWithOverviewMode(true);// setUseWideViewPort方法设置webview推荐使用的窗口。setLoadWithOverviewMode方法是设置webview加载的页面的模式。
//        ws.setSavePassword(true);
        ws.setSaveFormData(true);// 保存表单数据
        ws.setJavaScriptEnabled(true);
        ws.setGeolocationEnabled(true);// 启用地理定位
        ws.setGeolocationDatabasePath("/data/data/org.itri.html5webview/databases/");// 设置定位的数据库路径
        ws.setDomStorageEnabled(true);


        ws.setNeedInitialFocus(false);

        ws.setSupportZoom(true);

        ws.setLoadsImagesAutomatically(true);//自动加载图片


        m_vm_main.loadUrl("http://www.dadpat.com/dist/dadpat01/details.html?resourceId=" + s_animalId);

        m_vm_main.addJavascriptInterface( this, "goofyPapa" );

        mWebChromeClient = new WebChromeClient();
        m_vm_main.setWebChromeClient(mWebChromeClient);

        m_vm_main.setWebViewClient(new WebViewClient() {
            //设置加载前的函数
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            }

            //设置结束加载函数
            @Override
            public void onPageFinished(WebView view, String url) {
            }
        });

    }

    //销毁Webview
    @Override
    protected void onDestroy() {
        if (m_vm_main != null) {
            m_vm_main.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            m_vm_main.clearHistory();

            ((ViewGroup) m_vm_main.getParent()).removeView(m_vm_main);
            m_vm_main.destroy();
            m_vm_main = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if( !m_vm_main.canGoBack() )
        {
            super.onBackPressed();
        }else{
            back();
        }
    }

    @JavascriptInterface
    public void back()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if( m_vm_main.canGoBack() ){
                    m_vm_main.goBack();
                }else {
                    finish();
                }

//                finish();
            }
        });
    }

}
