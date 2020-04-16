package kr.co.mtnw.m;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private String myUrl = "http://m.mtnw.co.kr/";
    //private String myUrl = "http://mdev.sincontool.co.kr/a.php";
    private String packageName = "kr.co.futurewiz.gen5mediaplayer";
    private boolean mFlag = false;
    private final Handler handler = new Handler();
    public static FrameLayout mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (WebView) findViewById(R.id.webView);
        String userAgent = mWebView.getSettings().getUserAgentString();
        mWebView.getSettings().setUserAgentString(userAgent+" inApp");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setSupportMultipleWindows(true);
        mWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        mWebView.loadUrl(myUrl);
        mWebView.setWebChromeClient(new WebChromeClientClass());
        mWebView.setWebViewClient(new WebViewClientClass());

        // 현재 앱 버전 확인
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int versionCode = pInfo.versionCode;
        String versionName = pInfo.versionName; // build.gradle 에 기록된 VersionName
        //Toast.makeText(this, " Ver "+versionName, Toast.LENGTH_SHORT).show();
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void resultAuthSuccess() {
            Log.e(String.valueOf("resultAuthSuccess"),"resultAuthSuccess");
            mWebView.loadUrl("javascript:window.close();");
        }

        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean FinishToast() {
        if(!mFlag) {
            Toast.makeText(getApplicationContext(), "'뒤로'버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
            mFlag = true;
        } else {
            mFlag = false;
            finish();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(mWebView.canGoBack()){
                if(mWebView.getUrl().indexOf("/mobile/live") > -1 || String.valueOf(mWebView.getUrl()).equals(myUrl)){
                    return this.FinishToast();
                }else{
                    mWebView.goBack();
                    mFlag = false;
                    return true;
                }
            }else{
                return this.FinishToast();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url != null && url.startsWith("intent://")) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                    if (existPackage != null) {
                        startActivity(intent);
                    } else {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                        marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                        startActivity(marketIntent);
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (url != null && url.startsWith("market://")) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    if (intent != null) {
                        startActivity(intent);
                    }
                    return true;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } else if (url != null && url.indexOf("/player/mobile") > -1) {
                if(!isInstallApp(packageName)){
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                    marketIntent.setData(Uri.parse("market://details?id="+packageName));
                    startActivity(marketIntent);
                }
            } else if (url != null && url.startsWith("gen5mediaplayer://")) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    if (intent != null) {
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            intent.setData(Uri.parse("market://details?id="+packageName));
                            Intent webIntent = new Intent(Intent.ACTION_VIEW);
                            webIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id="+packageName));
                            if (webIntent.resolveActivity(getPackageManager()) != null) {
                                startActivity(webIntent);
                            }
                        }
                    }
                    return true;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            view.loadUrl(url);
            return false;
        }
    }

    private class WebChromeClientClass extends WebChromeClient {
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {

            //mContainer = (FrameLayout) findViewById(R.id.webview_frame);
            WebView mWebViewPop = new WebView(view.getContext());
            mWebViewPop.getSettings().setJavaScriptEnabled(true);
            mWebViewPop.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            mWebViewPop.getSettings().setSupportMultipleWindows(true);
            mWebViewPop.getSettings().setDomStorageEnabled(true);
            mWebViewPop.setWebChromeClient(new WebChromeClient(){
                @Override
                public void onCloseWindow(WebView window) {
                    Log.e(String.valueOf("resultMsg"),"onCloseWindow");
                   // mContainer.removeView(window);
                    window.destroy();
                }
            });
            //mWebViewPop.addJavascriptInterface(new WebAppInterface(view.getContext()), "Android");
            //mContainer.addView(mWebViewPop);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mWebViewPop);
            resultMsg.sendToTarget();
            return true;
        }
    }

    private boolean isInstallApp(String pakageName) {
        Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(pakageName);
        if (intent == null) {
            return false;
        } else {
            return true;
        }
    }
}
