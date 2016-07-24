package website.julianrosser.birthdays;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class Birthday {

    // JSON keys
    private static final String JSON_NAME = "name";
    private static final String JSON_DATE = "date";
    private static final String JSON_YEAR = "year";
    private static final String JSON_REMIND = "remind";
    private static final String JSON_SHOW_YEAR = "show_year";

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
            "dd.MM.yyyy", Locale.getDefault());

    public static final int DAY_IN_MILLIS = 86400000;

    // References to data
    private String name;
    private Date date;
    private boolean remind;
    private int yearOfBirth;
    private boolean showYear;

    Context mAppContext;

    /**
     * Constructor for creating new birthday.
     */
    public Birthday(String name, Date dateOfBirthday, boolean notifyUserOfBirthday, boolean includeYear, Context c) {

        this.name = name;
        this.remind = notifyUserOfBirthday;
        this.date = dateOfBirthday;
        this.mAppContext = c;
        this.yearOfBirth = dateOfBirthday.getYear();
        this.showYear = includeYear;
    }

    /**
     * For updating Birthday information without creating new
     */
    public void edit(String editName, Date editDate, boolean editRemind, boolean includeYear, Context c) {

        this.name = editName;
        this.date = editDate;
        this.mAppContext = c;
        this.yearOfBirth = editDate.getYear();
        this.showYear = includeYear;
    }

    /**
     * Constructor for parsing JSON data to Birthday objects. For each Birthday variable, check JSON data and update if found.
     */
    public Birthday(JSONObject json) throws JSONException {

        // Find String name of person attached to this birthday.
        if (json.has(JSON_NAME)) {
            name = json.getString(JSON_NAME);
        }

        // Check whether user wants to be reminded for this birthday.
        remind = !json.has(JSON_REMIND) || json.getBoolean(JSON_REMIND);
        // Default to true if not found, log message.

        // Date of birthday in millis.
        if (json.has(JSON_DATE)) {
            date = new Date();
            date.setTime(json.getLong(JSON_DATE));
        }
        // year
        if (json.has(JSON_YEAR)) {
            yearOfBirth = json.getInt(JSON_YEAR);
        } else {
            yearOfBirth = 0;
        }
        // Should use age
        showYear = json.has(JSON_SHOW_YEAR) && json.getBoolean(JSON_SHOW_YEAR);
    }

    /**
     * Convert current Birthday to JSON format and return.
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_NAME, this.getName());
        json.put(JSON_DATE, this.getDate().getTime());
        json.put(JSON_YEAR, this.getYear());
        json.put(JSON_REMIND, this.getRemind());
        json.put(JSON_SHOW_YEAR, this.shouldIncludeYear());
        return json;
    }

    /**
     * Getters & setters for variables
     */
    public boolean shouldIncludeYear() {
        return showYear;
    }

    public int getYear() {
        return yearOfBirth;
    }

    public Date getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public void setYearOfDate(int year) {
        this.date.setYear(year);
    }

    public boolean getRemind() {
        return remind;
    }

    public String getReminderString() {
        if (remind) {
            return MainActivity.getAppContext().getString(R.string.reminder_set);
        } else {
            return MainActivity.getAppContext().getString(R.string.reminder_canceled);
        }
    }

    public Drawable getRemindAlarmDrawable() {
        if (remind) {
            return MainActivity.getAppContext().getResources().getDrawable(R.drawable.ic_alarm_on_white_24dp);
        } else {
            return MainActivity.getAppContext().getResources().getDrawable(R.drawable.ic_alarm_off_white_24dp);
        }

    }

    public boolean toggleReminder() {
        remind = !remind;
        return remind;
    }

    public String getBirthMonth() {
        return (String) DateFormat.format("MMM", date);
    }

    public String getBirthDay() {
        return "" + date.getDate() + getDateSuffix();
    }

    public String getFormattedDaysRemainingString() {
        int i = getDaysBetween();

        if (i == 0) {
            return WordUtils.capitalize(MainActivity.getAppContext().getString(R.string.date_today) + "!");
        } else if (i == 1) {
            return WordUtils.capitalize(MainActivity.getAppContext().getString(R.string.date_tomorrow) + "!");
        } else if (i == -1) {
            return MainActivity.getAppContext().getString(R.string.date_yesterday);
        } else if (i > 1 && i <= 6) {
            Date newDate = new Date();
            newDate.setTime(getDate().getTime() - DAY_IN_MILLIS);
            return (String) DateFormat.format("EEEE", newDate);
        } else if (i == 7) {
            return WordUtils.capitalize(MainActivity.getAppContext().getString(R.string.date_week));
        } else if (i < 9) {
            return " " + String.valueOf(i) + " " + MainActivity.getAppContext().getString(R.string.date_days);
        } else if (i > 99) {
            return "  " + String.valueOf(i) + " " + MainActivity.getAppContext().getString(R.string.date_days);
        } else {
            return "" + i + " " + MainActivity.getAppContext().getString(R.string.date_days);
        }
    }

    // Return a formatted int of exact amount of days until the next birthday
    public int getDaysBetween() {

        Date dateBirthday = getDate();
        String birthday = String.valueOf(dateBirthday.getDate()) + "."
                + String.valueOf(dateBirthday.getMonth() + 1) + "."
                + String.valueOf(getYearOfNextBirthday(dateBirthday));

        Date dateNow = new Date();
        String today = String.valueOf((dateNow.getDate()) + "."
                + String.valueOf(dateNow.getMonth() + 1) + "."
                + String.valueOf(dateNow.getYear() + 1900));

        // use below method to calculate days until next birthday occurance
        int daysBetween = (int) getDayCount(today, birthday);

        // If exactly a year until next, we know the birthday is today
        if (daysBetween == 366) {
            daysBetween = 0;
        }
        return daysBetween;
    }

    /**
     * Helper method, convert dates to millis and use to calculate full days between them
     */
    public static long getDayCount(String start, String end) {

        long dayCount = 0;
        try {
            Date dateStart = simpleDateFormat.parse(start);
            Date dateEnd = simpleDateFormat.parse(end);
            dayCount = Math.round((dateEnd.getTime() - dateStart.getTime())
                    / (double) DAY_IN_MILLIS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dayCount;
    }

    /**
     * Helper method which returns the year of next birthday occurrence of passed date
     */
    public static int getYearOfNextBirthday(Date date) {

        int year = 2014;
        date.setYear(year);

        boolean nowAhead = dateInFuture(date);

        // While date instance is in the past, increase by a year and check again
        while (nowAhead) {

            year += 1;
            date.setYear(year);

            nowAhead = dateInFuture(date);
        }
        return year;
    }

    /**
     * This method returns false when the passed date is in the future or already passed.
     */
    private static boolean dateInFuture(Date queryDate) {
        // Use calender reference to get correct date
        Calendar nowCal = Calendar.getInstance();

        // Set date to desired time
        Date now = new Date();
        now.setYear(nowCal.get(Calendar.YEAR));
        now.setMonth(nowCal.get(Calendar.MONTH));
        now.setDate(nowCal.get(Calendar.DATE));

        // Get dates in form of milliseconds
        long millisNow = now.getTime();
        long millisBDAY = queryDate.getTime();

        // use this to ensure a birthday
        return millisNow > millisBDAY + DAY_IN_MILLIS;
    }

    private String getDateSuffix() {
        // d stands for date of birthday
        int d = this.getDate().getDate();

        if (d == 11 || d == 12 || d == 13) {
            return "th";
        } else if (d % 10 == 1) {
            return "st";
        } else if (d % 10 == 2) {
            return "nd";
        } else if (d % 10 == 3) {
            return "rd";
        } else {
            return "th";
        }
    }

    /**
     * Returns a formatted day string built for notification display.
     */
    public static String getFormattedStringDay(Birthday b, Context c) {

        String dayFormatted = "";

        int daysFromNotiUntilDay = getDaysBeforeReminderPref(c);

        if (daysFromNotiUntilDay == 0) {
            dayFormatted += c.getResources().getString(R.string.date_today);
        } else if (daysFromNotiUntilDay == 1) {
            dayFormatted += c.getResources().getString(R.string.date_tomorrow);
        } else if (daysFromNotiUntilDay == 7) {
            dayFormatted += c.getResources().getString(R.string.date_week);
        } else if (daysFromNotiUntilDay == 14) {
            dayFormatted += c.getResources().getString(R.string.date_2_week);
        } else {
            Date newDate = new Date();
            newDate.setTime(b.getDate().getTime() - DAY_IN_MILLIS);
            return c.getResources().getString(R.string.date_this) + " " + DateFormat.format("EEEE", newDate);
        }
        dayFormatted += "!";

        return dayFormatted;
    }

    public static int getDaysBeforeReminderPref(Context c) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        return Integer.valueOf(sharedPref.getString(c.getString(R.string.pref_days_before_key), "1"));
    }


    public String getAge() {
        int currentYear = new Date().getYear() + 1900;
        int age = currentYear - yearOfBirth;
        // Age at next birthday modifier
        age  += 1;
        if (age < 0) {
            return "N/A";
        } else {
            return String.valueOf(age);
        }
    }
}