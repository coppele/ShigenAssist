package red.man10.shigenassist.data.logic;

public class SANotice extends SASounder {

    private int percentage;
    private String format;

    public SANotice(int percentage) {
        this.percentage = percentage;
        this.key = null;
        this.sound = null;
        this.format = null;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
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