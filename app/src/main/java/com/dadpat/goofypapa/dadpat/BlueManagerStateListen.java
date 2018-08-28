package com.dadpat.goofypapa.dadpat;

/**
 * Created by Lenovo on 2018/3/22.
 */

public interface BlueManagerStateListen {
    void onStateChange( int p_state );
    void onScan( int p_code );
}
