package dtri.com.tw.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Fm_Time {

	private static SimpleDateFormat format_yyyyMMdd_HHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat format_yyyyMMdd_HHmm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static SimpleDateFormat format_yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	/** 格式:yyyy-MM-dd HH:mm **/
	public static String to_yMd_Hm(Date date) {
		return format_yyyyMMdd_HHmm.format(date);
	}
	/** 格式:yyyy-MM-dd HH:mm:ss **/
	public static String to_yMd_Hms(Date date) {
		return format_yyyyMMdd_HHmmss.format(date);
	}
	/** 格式:yyyy-MM-dd **/
	public static String to_y_M_d(Date date) {
		return sdf.format(date);
	}

	/** 格式:yyyyMMdd **/
	public static String to_yMd(Date date) {
		return format_yyyyMMdd.format(date);
	}

	/** 計算:加/減 天數 **/
	public static Date to_count(Integer n, Date dt) {
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		c.add(Calendar.DATE, n);
		dt = c.getTime();
		return dt;
	}

	/** yyyy-MM-dd to Date **/
	public static Date toDate(String dt) {

		try {
			return sdf.parse(dt);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;

	}

	/** yyyy-MM-dd HH:mm:ss to Date **/
	public static Date toDateTime(String dt) {

		try {
			return format_yyyyMMdd_HHmmss.parse(dt);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;

	}
}
