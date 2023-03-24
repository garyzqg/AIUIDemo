package com.inspur.mspeech.utils;

import android.app.Activity;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

import payfun.lib.basis.utils.LogUtil;

/**
 * @author : zhangqinggong
 * date    : 2023/3/24 14:14
 * desc    : 动态权限申请工具类
 */
public class PermissionUtil {
    public static void getPermission(Activity context, PermissonListener permissonListener) {
        XXPermissions.with(context)
                // 申请多个权限
                .permission(Permission.ACCESS_COARSE_LOCATION, Permission.MANAGE_EXTERNAL_STORAGE, Permission.RECORD_AUDIO)
                // 申请单个权限
//                .permission(Permission.Group.CALENDAR)
                // 设置权限请求拦截器（局部设置）
                //.interceptor(new PermissionInterceptor())
                // 设置不触发错误检测机制（局部设置）
                //.unchecked()
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> permissions, boolean all) {
                        if (all) {
                            LogUtil.i("权限获取成功");

                            if (permissonListener != null){
                                permissonListener.onGranted();
                            }
                        } else {
                            LogUtil.i("部分权限获取成功");

                            XXPermissions.startPermissionActivity(context, permissions);
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if (quick) {
                            LogUtil.e("拒绝权限,请手动授予");
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(context, denied);
                        } else {
                            LogUtil.e("权限获取失败");
                        }
                    }
                });

    }

    public interface PermissonListener {
        void onGranted();
    }
}
