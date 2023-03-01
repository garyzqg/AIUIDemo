package payfun.lib.dialog.custom;

import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import java.lang.reflect.Method;

import payfun.lib.dialog.R;
import payfun.lib.dialog.listener.OnAutoDismissListener;
import payfun.lib.dialog.widget.ProgressView;

/**
 * @author : zhangqg
 * date   : 2023/3/1 15:30
 * desc   : <隐私协议对话框构建器>
 */
public class PrivacyPolicyDialogBuilder extends UiWithBtnDialogBuilder<PrivacyPolicyDialogBuilder> {

    private String webUrl = "http://oems.t.dev.pay.fun/uei.html?app_id=A00275E0&app_oem_secret=938AD97D54E8A8F15F8AECBE672CA054";

    private OnPrivacySelectListener onPrivacySelectListener;


    @Override
    public void onDoBeforeViewPerform(DialogFragment dialog, View view) {
        setLeftMsg(dialog.getResources().getString(R.string.privacy_policy_next));
        setRightMsg("");
    }

    @Override
    public int getChildLayoutId() {
        return R.layout.dialog_pm_privacy_policy;
    }


    @Override
    public void onInitChildView(DialogFragment dialog, View view, Bundle savedInstanceState) {
        CheckBox cbPrivacySelect = view.findViewById(R.id.cb_privacy_select);
        ProgressView progressPrivacy = view.findViewById(R.id.progress_privacy);
        TextView leftBtn = dialog.getView().findViewById(R.id.btn_dialog_left);

        setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (onPrivacySelectListener != null) {
                    onPrivacySelectListener.onConfirm(cbPrivacySelect.isChecked());
                }
            }
        });
        setOnAutoDismissListener(new OnAutoDismissListener() {
            @Override
            public void onAutoDismiss(DialogFragment baseDialog, View v) {
                if (onPrivacySelectListener != null) {
                    onPrivacySelectListener.onAutoDismiss(cbPrivacySelect.isChecked());
                }
            }
        });

        cbPrivacySelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (onPrivacySelectListener != null) {
                    onPrivacySelectListener.onStatusChanged(isChecked);
                }
            }
        });

        WebView webView = view.findViewById(R.id.wv_privacy);
        initWebView(webView, progressPrivacy, leftBtn);
    }


    void initWebView(WebView webView, ProgressView progressView, TextView btn) {
        progressView.setVisibility(View.VISIBLE);
        btn.setEnabled(false);
        btn.setClickable(false);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
//        webSettings.setUseWideViewPort(true);
        // 支持HTML5中的一些控件标签
        webSettings.setDomStorageEnabled(true);
        //处理http和https混合的问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        } else {
            webSettings.setMixedContentMode(WebSettings.LOAD_NORMAL);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 允许javascript出错
            try {
                Method method = Class.forName("android.webkit.WebView").
                        getMethod("setWebContentsDebuggingEnabled", Boolean.TYPE);
                if (method != null) {
                    method.setAccessible(true);
                    method.invoke(null, true);
                }
            } catch (Exception e) {
                // do nothing
            }
        }
        webSettings.setSupportZoom(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (progressView != null) {
                    progressView.setVisibility(View.GONE);
                }
                if (btn != null) {
                    btn.setEnabled(true);
                    btn.setClickable(true);
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //super.onReceivedSslError(view, handler, error);
                handler.proceed();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }
        });

        webView.loadUrl(webUrl);
    }


    //region set方法

    public PrivacyPolicyDialogBuilder setWebUrl(String webUrl) {
        this.webUrl = webUrl;
        return this;
    }

    public PrivacyPolicyDialogBuilder setOnPrivacySelectListener(OnPrivacySelectListener onPrivacySelectListener) {
        this.onPrivacySelectListener = onPrivacySelectListener;
        return this;
    }


    //endregion set方法

    //region get方法

    public String getWebUrl() {
        return webUrl;
    }

    public OnPrivacySelectListener getOnPrivacySelectListener() {
        return onPrivacySelectListener;
    }


    //endregion get方法


    public static abstract class OnPrivacySelectListener {

        /**
         * 隐私协议同意与否的状态变化
         *
         * @param isSelected true=当前同意协议；false=当前不同意协议
         */
        public void onStatusChanged(boolean isSelected) {
        }

        /**
         * 对话框结束时，隐私协议的状态
         *
         * @param isConfirm true=当前同意协议；false=当前不同意协议
         */
        public abstract void onConfirm(boolean isConfirm);

        public abstract void onAutoDismiss(boolean isConfirm);

    }
}
