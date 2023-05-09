package com.inspur.mspeech.audio;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * @author : zhangqinggong
 * date    : 2023/3/24 9:51
 * desc    : 录音工具类
 */
public class AudioRecordOperator {
    private static final String TAG = "AudioRecordOperator";
    private AudioRecord audioRecord;
    private int recordBufsize = 0;
    private int sampleRateInHz = 16000;
    private Thread recordingThread;
    private boolean isRecording = false;
    private RecordListener mRecordListener;
    private static final String FILE_NAME = "/sdcard/iflytek/ivw/test.pcm";

    @SuppressLint("MissingPermission")
    public void createAudioRecord(RecordListener recordListener){
        mRecordListener = recordListener;
        recordBufsize = AudioRecord.getMinBufferSize(sampleRateInHz,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
        /**
         * audioSource 表示数据来源 一般为麦克风 MediaRecorder.AudioSource.MIC
         * sampleRateInHz 表示采样率
         * channelConfig 表示声道 一般设置为 AudioFormat.CHANNEL_IN_MONO
         * audioFormat 数据编码方式 这里使用 AudioFormat.ENCODING_PCM_16BIT
         * bufferSizeInBytes 数据大小 这里使用AudioRecord.getMinBufferSize 获取
         */
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRateInHz,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                recordBufsize);

    }


    public void startRecord() {
        if (isRecording) {
            return;
        }
        isRecording = true;
        audioRecord.startRecording();

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte data[] = new byte[recordBufsize];
                while (isRecording) {
                    audioRecord.read(data, 0, recordBufsize);
                    if (mRecordListener != null){
                        mRecordListener.data(data);
                    }
                }
            }
        });

        recordingThread.start();

//        recordingThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                byte data[] = new byte[recordBufsize];
//                File file = new File(FILE_NAME);
//                FileOutputStream os = null;
//                try {
//                    if (!file.exists()) {
//                        file.createNewFile();
//                    }
//                    os = new FileOutputStream(file);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                int read;
//                if (os != null) {
//                    while (isRecording) {
//                        read = audioRecord.read(data, 0, recordBufsize);
//                        if (mRecordListener != null){
//                            mRecordListener.data(data);
//                        }
//                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
//                            try {
//                                os.write(data);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }
//                try {
//                    os.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    public void stopRecord() {
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
//            audioRecord.release();
//            audioRecord = null;
            recordingThread = null;
        }
    }

    public interface RecordListener{
        void data(byte data[]);
    }
}
