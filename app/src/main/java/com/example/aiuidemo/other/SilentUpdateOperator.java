package com.example.aiuidemo.other;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.aiuidemo.utils.LogUtil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * @author : zhangqinggong
 * date    : 2023/1/11 9:19
 * desc    : 静默升级工具类
 */
public class SilentUpdateOperator {

    private static final String TAG = "SilentUpdateOperator";

    /**
     * 条件:ROOT + 系统签名
     * @param path
     * @return
     */
    public static String installSilently(String path) {
        Log.d(TAG, "path" + path);
        // 通过命令行来安装APK
        String[] args = {"pm", "install", "-r", path};
        String result = "";
        // 创建一个操作系统进程并执行命令行操作
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
            Log.d(TAG, "result" + result);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        LogUtil.iTag(TAG,"silent: "+result);
        return result;
    }


    public static boolean installSilently2(String path) {
        // 进行资源的转移 将assets下的文件转移到可读写文件目录下
        File file = new File(path);
        boolean result = false;
        Process process = null;
        OutputStream out = null;
        Log.i(TAG, "file.getPath()：" + file.getPath());
        if (file.exists()) {
            System.out.println(file.getPath() + "==");
            try {
                process = Runtime.getRuntime().exec("su");
                out = process.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(out);
                // 获取文件所有权限
                dataOutputStream.writeBytes("chmod 777 " + file.getPath()
                        + "\n");
                // 进行静默安装命令
                dataOutputStream
                        .writeBytes("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r "
                                + file.getPath());
                dataOutputStream.flush();
                // 关闭流操作
                dataOutputStream.close();
                out.close();
                int value = process.waitFor();
                // 代表成功
                if (value == 0) {
                    Log.i(TAG, "安装成功！");
                    result = true;
                    // 失败
                } else if (value == 1) {
                    Log.i(TAG, "安装失败！");
                    result = false;
                    // 未知情况
                } else {
                    Log.i(TAG, "未知情况！");
                    result = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!result) {
                Log.i(TAG, "root权限获取失败，将进行普通安装");
//                normal(path);
                result = true;
            }
        }
        return result;
    }

//    public static final int INSTALL_REPLACE_EXISTING = 0x00000002; //如果已经存在的包使用这个flag,例如升级

//    public static final int INSTALL_ALL_USERS = 0x00000040; //如果系统中没有安装过使用这个flag，例如安装其他的APP
//    public void installSilently3(String path) {
//        File file = new File(path);
//        PackageManager pm = MyApplication.getApplication().getPackageManager();
//        Class<?>[] types = new Class[]{Uri.class, IPackageInstallObserver.class, int.class,   String.class};
//        try {
//            Method method = pm.getClass().getMethod("installPackage", types);
//            // method.invoke(pm,Uri.fromFile(file), new PackageInstallObserver(), INSTALL_ALL_USERS, null);
//            method.invoke(pm, Uri.fromFile(file), new PackageInstallObserver(), INSTALL_REPLACE_EXISTING, null);
//        } catch (Exception e) {
//            Log.d("安装失败", "");
//            e.printStackTrace();
//        }
//    }


    public static void excutesucmd(Context context,String currenttempfilepath) {
        Process process = null;
        OutputStream out = null;
        InputStream in = null;
        try {
            // 请求root
            process = Runtime.getRuntime().exec("sh");
            out = process.getOutputStream();
            // 调用安装
            out.write(("pm install -r " + currenttempfilepath + "\n").getBytes());
            in = process.getInputStream();
            int len = 0;
            byte[] bs = new byte[256];
            while (-1 != (len = in.read(bs))) {
                String state = new String(bs, 0, len);
                if (state.equals("success\n")) {
                    //静态注册自启动广播
                    Intent intent = new Intent();
                    //与清单文件的receiver的anction对应
                    intent.setAction("android.intent.action.PACKAGE_REPLACED");
                    // 发送广播
                    context.sendBroadcast(intent);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void installPackage(String path) {

        try {
            new ProcessBuilder()
                    .command("pm", "install", "-i", "com.example.aiuidemo", path)
                    .start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    private static final String COMMAND_SU = "su";
    private static final String COMMAND_SH = "sh";
    private static final String COMMAND_EXIT = "exit\n";
    private static final String COMMAND_LINE_END = "\n";

    /**
     * 执行shell命令
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot,
                                            boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        DataOutputStream os = null;
        StringBuilder cmd = new StringBuilder();
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            cmd.append(isRoot ? COMMAND_SU : COMMAND_SH);
            cmd.append(COMMAND_LINE_END);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }
                os.writeBytes(command);
                os.writeBytes(COMMAND_LINE_END);
                cmd.append(command);
                cmd.append(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            cmd.append(COMMAND_EXIT);
            os.flush();
            result = process.waitFor();
            // get command result
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(
                        process.getErrorStream()));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        /*Log.i(TAG, "execCommand: " + cmd.toString());
        Log.i(TAG, "execCommand: result = " + result +
                ", successMsg = " + (successMsg == null ? null : successMsg.toString()) +
                ", errorMsg = " + (errorMsg == null ? null : errorMsg.toString()));*/
        return new CommandResult(result,
                successMsg == null ? null : successMsg.toString(),
                errorMsg == null ? null : errorMsg.toString());
    }

    /**
     * 运行结果
     */
    public static class CommandResult {
        //运行结果
        private int result;
        //运行成功结果
        private String successMsg;
        //运行失败结果
        private String errorMsg;

        public CommandResult(int result) {
            this.result = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }

        public String getSuccessMsg() {
            return successMsg;
        }

        public void setSuccessMsg(String successMsg) {
            this.successMsg = successMsg;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }
    }


    /**
     * 判断是否root
     * @return
     */
    public static boolean isRoot(){
        boolean bool = false;

        try{
            if ((!new File("/system/bin/su").exists()) && (!new File("/system/xbin/su").exists())){
                bool = false;
            } else {
                bool = true;
            }
        } catch (Exception e) {

        }
        return bool;
    }

}
