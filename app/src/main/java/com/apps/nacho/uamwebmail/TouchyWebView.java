package com.apps.nacho.uamwebmail;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * Created by nacho on 13/11/16.
 */
public class TouchyWebView extends WebView {

    public  TouchyWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchyWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        System.out.println("asdfasdfdafasdf");
        //requestDisallowInterceptTouchEvent(true);
        return super.onTouchEvent(event);
    }
}
