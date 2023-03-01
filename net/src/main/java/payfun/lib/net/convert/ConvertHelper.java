package payfun.lib.net.convert;

import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import payfun.lib.basis.utils.CloseUtil;
import payfun.lib.net.bean.DownloadEntity;
import payfun.lib.net.helper.GsonHelper;

/**
 * @author : zhangqg
 * date   : 2021/5/27 15:58
 * desc   : <参数转换类>
 */
public final class ConvertHelper {

    private ConvertHelper() {
        throw new UnsupportedOperationException("do not instantiation me" + "（ConvertHelper）...");
    }


    public static RequestBody convertToRequestBody(Object request, String mediaType) {
        String bodyStr = GsonHelper.GSON.toJson(request);
        if (TextUtils.isEmpty(mediaType)) {
            mediaType = "application/json; charset=utf-8";
        }
        RequestBody requestBody = RequestBody.create(bodyStr, MediaType.parse(mediaType));
        return requestBody;
    }

    public static DownloadEntity convertStreamFile(ResponseBody responseBody, DownloadEntity target) {
        File tempFile;
//        if (targetFile.isDirectory()) {
//            tempFile = targetFile.
//        }
//        if (!targetFile.exists()) {
//            targetFile.isDirectory()
//        }
        tempFile = new File(target.getFilePath(), target.getFileName());

        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        try {
            target.setFileLength(responseBody.contentLength());
            target.setMediaType(responseBody.contentType());

            inputStream = responseBody.byteStream();
            fileOutputStream = new FileOutputStream(tempFile, false);
            byte[] buffer = new byte[2048];
            boolean var10 = false;
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, len);
            }
            fileOutputStream.flush();

            target.setCode(200);
            target.setMsg("Success");
        } catch (Exception e) {
            target.setCode(100);
            target.setMsg(e.getMessage());
        } finally {
            CloseUtil.closeIO(fileOutputStream, inputStream);
        }
        return target;
    }

}
