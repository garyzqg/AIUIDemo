package payfun.lib.net.bean;

import okhttp3.MediaType;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/5/27 18:04
 * desc   : <下载结果bean类>
 */
public class DownloadEntity {

    /**
     * 文件存储路径
     */
    private String filePath;
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件大小
     */
    private long fileLength;
    /**
     * 文件类型
     */
    private MediaType mediaType;
    /**
     * 文件下载结果信息
     */
    private String msg;
    /**
     * 文件下载结果
     */
    private int code;

    public DownloadEntity(String filePath, String fileName) {
        this.filePath = filePath;
        this.fileName = fileName;
    }


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return code == 200;
    }
}
