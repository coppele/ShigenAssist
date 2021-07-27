package red.man10.shigenassist.data;

public class SARank {

    private final String display;
    private final int conditionsMining;

    public SARank(String display, int ConditionsMining) {
        this.display = display;
        this.conditionsMining = ConditionsMining;
    }

    public String getDisplay() {
        return this.display;
    }
    public int getConditionsMining() {
        return this.conditionsMining;
    }
}