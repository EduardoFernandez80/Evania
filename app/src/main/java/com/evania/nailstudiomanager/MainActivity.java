
package com.evania.nailstudiomanager;

import android.webkit.ConsoleMessage;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import java.io.OutputStream;
import java.io.IOException;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {
    private Uri[] parse(int rc, Intent d) { return ImagenUtil.preparar(this, WebChromeClient.FileChooserParams.parseResult(rc, d)); }

    private WebView webView;
    private ValueCallback<Uri[]> fileChooserCallback;

    private static final int FILE_CHOOSER_REQUEST = 1207;
    private static final int BACKUP_SAVE_REQUEST = 1208;

    private byte[] pendingBackup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);

        CookieManager.getInstance().setAcceptCookie(true);

        webView.addJavascriptInterface(new Object() {

            @JavascriptInterface
            public void saveBackup(String filename, String base64) {
                try {
                    pendingBackup = Base64.decode(
                        base64,
                        Base64.DEFAULT
                    );
                } catch (IllegalArgumentException ex) {
                    runOnUiThread(() ->
                        Toast.makeText(
                            MainActivity.this,
                            "El respaldo no se pudo preparar.",
                            Toast.LENGTH_LONG
                        ).show()
                    );
                    return;
                }

                runOnUiThread(() -> {
                    Intent i = new Intent(
                        Intent.ACTION_CREATE_DOCUMENT
                    );

                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("application/json");
                    i.putExtra(Intent.EXTRA_TITLE, filename);

                    try {
                        startActivityForResult(
                            i,
                            BACKUP_SAVE_REQUEST
                        );
                    } catch (ActivityNotFoundException ex) {
                        pendingBackup = null;
                        Toast.makeText(
                            MainActivity.this,
                            "No hay selector de archivos disponible.",
                            Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }

        }, "Android");

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest req) {

                Uri u = req.getUrl();

                String scheme = u.getScheme() == null
                    ? ""
                    : u.getScheme();

                String host = u.getHost() == null
                    ? ""
                    : u.getHost().toLowerCase();

                if ("tel".equals(scheme)
                        || "mailto".equals(scheme)
                        || ("https".equals(scheme)
                        && (host.equals("wa.me")
                        || host.equals("whatsapp.com")
                        || host.endsWith(".whatsapp.com")))) {

                    openExternal(u);
                    return true;
                }

                return !(
                    "file".equals(scheme)
                    || "https".equals(scheme)
                    || "http".equals(scheme)
                );
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onConsoleMessage(ConsoleMessage m) {
                if (m.message() != null
                        && m.message().startsWith("ADDP")) {
                    runOnUiThread(() ->
                        Toast.makeText(
                            MainActivity.this,
                            m.message(),
                            Toast.LENGTH_LONG
                        ).show()
                    );
                }
                return true;
            }

            @Override
            public boolean onShowFileChooser(
                    WebView w,
                    ValueCallback<Uri[]> cb,
                    FileChooserParams params) {

                if (fileChooserCallback != null) {
                    fileChooserCallback.onReceiveValue(null);
                }

                fileChooserCallback = cb;

                try {
                    startActivityForResult(
                        params.createIntent(),
                        FILE_CHOOSER_REQUEST
                    );
                } catch (ActivityNotFoundException ex) {
                    fileChooserCallback = null;

                    Toast.makeText(
                        MainActivity.this,
                        "No hay aplicación para elegir archivos.",
                        Toast.LENGTH_LONG
                    ).show();

                    return false;
                }

                return true;
            }
        });

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(
                "file:///android_asset/index.html"
            );
        }
    }

    private void openExternal(Uri uri) {
        try {
            startActivity(
                new Intent(Intent.ACTION_VIEW, uri)
            );
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(
                this,
                "No se encontró una aplicación para abrir este enlace.",
                Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        );

        if (requestCode == FILE_CHOOSER_REQUEST
                && fileChooserCallback != null) {

            fileChooserCallback.onReceiveValue(
                parse(
                    resultCode,
                    data
                )
            );

            fileChooserCallback = null;

        } else if (requestCode == BACKUP_SAVE_REQUEST) {

            if (resultCode == RESULT_OK
                    && data != null
                    && data.getData() != null
                    && pendingBackup != null) {

                try (OutputStream out =
                        getContentResolver().openOutputStream(
                            data.getData()
                        )) {

                    if (out == null) {
                        throw new IOException(
                            "No se pudo abrir el archivo"
                        );
                    }

                    out.write(pendingBackup);

                    Toast.makeText(
                        this,
                        "Copia de seguridad guardada.",
                        Toast.LENGTH_LONG
                    ).show();

                } catch (IOException ex) {
                    Toast.makeText(
                        this,
                        "Error al guardar la copia.",
                        Toast.LENGTH_LONG
                    ).show();
                }
            }

            pendingBackup = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        if (webView != null) {
            webView.saveState(out);
        }

        super.onSaveInstanceState(out);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }

        super.onDestroy();
    }
}

