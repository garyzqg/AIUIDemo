package com.inspur.mspeech.wekws;

public class Spot {

    static {
        System.loadLibrary("wekws");
        System.loadLibrary("onnxruntime");
        System.loadLibrary("onnxruntime4j_jni");
    }

    public static native void init(String modelDir);
    public static native void reset();
    public static native void acceptWaveform(short[] waveform);
    public static native void setInputFinished();
    public static native void startSpot();
    public static native String getResult();
}
