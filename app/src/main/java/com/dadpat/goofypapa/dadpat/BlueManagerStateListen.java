package com.dadpat.goofypapa.dadpat;

/**
 * Created by Lenovo on 2018/3/22.
 */

public interface BlueManagerStateListen {
    public void onStateChange( int p_state );
    public void onScan( String p_code );
    public void onLog( String p_str );
}
