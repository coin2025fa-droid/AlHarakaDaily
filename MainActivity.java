
package com.ibrahim.alharaka;

import android.app.Activity;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.util.Log;
import org.json.JSONObject;

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
        s.setMixedContentMode(
            WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        );

        web.addJavascriptInterface(new PrintBridge(), "Android");

        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String check =
                    "setTimeout(function(){" +
                    "var e=document.getElementById('loginMsg');" +
                    "if(e){" +
                    "if(!window.supabase){" +
                    "e.textContent='خطأ: مكتبة Supabase لم يتم تحميلها';" +
                    "}else if(typeof window.login!=='function'){" +
                    "e.textContent='خطأ: دالة تسجيل الدخول غير موجودة';" +
                    "}else{" +
                    "e.textContent='تم تحميل الصفحة ومكتبة الاتصال بنجاح';" +
                    "}" +
                    "}" +
                    "},1000);";

                view.evaluateJavascript(check, null);
            }

            @Override
            public void onReceivedError(
                WebView view,
                WebResourceRequest request,
                WebResourceError error
            ) {
                if (request.isForMainFrame()) {
                    showMessage("خطأ تحميل الصفحة: " +
                        error.getDescription());
                }
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage message) {
                String error = message.message() +
                    " - السطر: " + message.lineNumber();

                Log.e("APP_JS_ERROR", error);
                showMessage("خطأ برمجي: " + error);
                return true;
            }
        });

        web.loadUrl("file:///android_asset/index.html");
    }

    private void showMessage(String message) {
        if (web == null) return;

        web.post(() -> {
            String safe = JSONObject.quote(message);
            web.evaluateJavascript(
                "if(document.getElementById('loginMsg')){" +
                "document.getElementById('loginMsg').textContent=" +
                safe + ";}",
                null
            );
        });
    }

    public class PrintBridge {
        @JavascriptInterface
        public void printPage() {
            runOnUiThread(() -> {
                PrintManager pm =
                    (PrintManager) getSystemService(PRINT_SERVICE);

                if (pm != null) {
                    pm.print(
                        "كشف الحركة اليومية",
                        web.createPrintDocumentAdapter(
                            "كشف الحركة اليومية"
                        ),
                        new PrintAttributes.Builder()
                            .setMediaSize(
                                PrintAttributes.MediaSize.ISO_A4
                            )
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
