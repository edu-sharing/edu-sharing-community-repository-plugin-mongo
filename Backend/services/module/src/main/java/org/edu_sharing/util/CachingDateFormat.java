package org.edu_sharing.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

public class CachingDateFormat extends SimpleDateFormat {

    public static final String FORMAT_DATE_GENERIC = "yyyy-MM-dd";


    static ThreadLocal<SimpleDateFormat> S_LOCAL_DATEONLY_FORMAT = ThreadLocal.withInitial(() -> newDateFormat(FORMAT_DATE_GENERIC));


    /**
     * @return Returns a thread-safe formatter for the generic date format
     * @see #FORMAT_DATE_GENERIC
     */
    public static SimpleDateFormat getDateOnlyFormat() {
        return S_LOCAL_DATEONLY_FORMAT.get();
    }


    /**
     * Creates a new non-lenient {@link CachingDateFormat} instance.
     *
     * @param pattern the date / datetime pattern.
     * @return new non-lenient {@link CachingDateFormat} instance.
     */
    private static CachingDateFormat newDateFormat(String pattern) {
        CachingDateFormat formatter = new CachingDateFormat(pattern);
        formatter.setLenient(false);
        return formatter;
    }

    private Map<String, Date> cacheDates = new WeakHashMap<>(89);

    private CachingDateFormat(String pattern, Locale locale) {
        super(pattern, locale);
    }

    private CachingDateFormat(String pattern) {
        super(pattern);
    }

    /**
     * Parses and caches date strings.
     *
     * @see java.text.DateFormat#parse(java.lang.String,
     *      java.text.ParsePosition)
     */
    public Date parse(String text, ParsePosition pos)
    {
        Date cached = cacheDates.get(text);
        if (cached == null)
        {
            Date date = super.parse(text, pos);
            if ((date != null) && (pos.getIndex() == text.length()))
            {
                cacheDates.put(text, date);
                return (Date) date.clone();
            }
            else
            {
                return date;
            }
        }
        else
        {
            pos.setIndex(text.length());
            return (Date) cached.clone();
        }
    }

    @Override
    public String toString() {
        return this.toPattern();
    }

}
