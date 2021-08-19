package red.man10.shigenassist.logic;

public class SANotice extends SASounder {

    public static final String PERMISSION = "ShigenAssist.assist.notice";

    private final int percentage;
    private String format;

    public SANotice(int percentage) {
        this.percentage = percentage;
        this.key = null;
        this.sound = null;
        this.format = null;
    }

    public int getPercentage() {
        return percentage;
    }
    public void setFormat(String format) {
        this.format = format;
    }
    public String getFormat() {
        return format;
    }
}