
package com.ibrahim.alharaka;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.util.Log;

public class MainActivity extends Activity {
    private WebView web;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        web.addJavascriptInterface(new PrintBridge(this), "Android");

        web.setWebViewClient(new WebViewClient());

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage message) {
                Log.e("APP_JS_ERROR",
                    message.message() + " Line: " + message.lineNumber());
                return true;
            }
        });

        web.loadUrl("file:///android_asset/index.html");
    }

    public class PrintBridge {
        private final Context context;

        PrintBridge(Context c) {
            context = c;
        }

        @JavascriptInterface
        public void printPage() {
            runOnUiThread(() -> {
                PrintManager pm =
                    (PrintManager) getSystemService(PRINT_SERVICE);

                if (pm != null) {
                    pm.print(
                        "كشف الحركة اليومية",
                        web.createPrintDocumentAdapter("كشف الحركة اليومية"),
                        new PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                            .build()
                    );
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
