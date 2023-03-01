package payfun.lib.dialog.base;


public class InputInfo {

    /**
     * 最大长度,-1不生效
     */
    private int MAX_LENGTH = -1;
    /**
     * 类型详见 android.text.InputType
     */
    private int inputType;
    /**
     * 默认字体样式
     */
    private TextInfo textInfo;
    /**
     * 支持多行
     */
    private boolean multipleLines;
    /**
     * 默认选中所有文字（便于修改）
     */
    private boolean selectAllText;

    public int getMAX_LENGTH() {
        return MAX_LENGTH;
    }

    public InputInfo setMAX_LENGTH(int MAX_LENGTH) {
        this.MAX_LENGTH = MAX_LENGTH;
        return this;
    }

    public int getInputType() {
        return inputType;
    }

    public InputInfo setInputType(int inputType) {
        this.inputType = inputType;
        return this;
    }

    public TextInfo getTextInfo() {
        return textInfo;
    }

    public InputInfo setTextInfo(TextInfo textInfo) {
        this.textInfo = textInfo;
        return this;
    }

    public boolean isMultipleLines() {
        return multipleLines;
    }

    public InputInfo setMultipleLines(boolean multipleLines) {
        this.multipleLines = multipleLines;
        return this;
    }

    public boolean isSelectAllText() {
        return selectAllText;
    }

    public InputInfo setSelectAllText(boolean selectAllText) {
        this.selectAllText = selectAllText;
        return this;
    }
}
