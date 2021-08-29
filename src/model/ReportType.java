package model;

/**
 * The reportType model class.
 *
 * @author Christian Lopez
 * Software II - C195
 */
public class ReportType {
    private String day;
    private String month;
    private String type;
    private int total;

    /**
     * Instantiates a new Report type.
     *
     * @param month the month
     * @param type  the type
     * @param total the total
     */
    public ReportType(String month, String type, int total) {
        this.month = month;
        this.type = type;
        this.total = total;
    }

    /**
     * Instantiates a new Report type.
     *
     * @param day   the day
     * @param total the total
     */
    public ReportType(String day, int total) {
        this.day = day;
        this.total = total;
    }
    /**
     * Gets day.
     *
     * @return the day
     */
    public String getDay() {
        return day;
    }

    /**
     * Sets day.
     *
     * @param day the day
     */
    public void setDay(String day) {
        this.day = day;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets month.
     *
     * @return the month
     */
    public String getMonth() {
        return month;
    }

    /**
     * Sets month.
     *
     * @param month the month
     */
    public void setMonth(String month) {
        this.month = month;
    }

    /**
     * Gets total.
     *
     * @return the total
     */
    public int getTotal() {
        return total;
    }

    /**
     * Sets total.
     *
     * @param total the total
     */
    public void setTotal(int total) {
        this.total = total;
    }
}
