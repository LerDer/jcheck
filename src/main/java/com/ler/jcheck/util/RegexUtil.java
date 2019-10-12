package com.ler.jcheck.util;

/**
 * @author lww
 * 正则表达式字符串
 */
public interface RegexUtil {

	/**
	 * 用户名正则表达式
	 */
	String USER_NAME_REGEX = "^[\\u4e00-\\u9fa5|A-Za-z0-9]{4,20}$";

	/**
	 * 手机号正则表达式
	 */
	String PHONE_REGEX = "^1[0-9]{10}$";

	/**
	 * 邮箱正则表达式
	 */
	String MAIL_REGEX = "^([a-zA-Z0-9_-|\\.])+@([a-zA-Z0-9_-])+((\\.[a-zA-Z0-9_-]{2,}){1,})$";

	/**
	 * 密码正则表达式
	 */
	String PASSWORD_REGEX = "^[A-Za-z0-9]{6,15}$";

	/**
	 * 字母数字正则表达式
	 */
	String WORD_NUMBER_REGEX = "^[A-Za-z0-9]+$";

	/**
	 * 银行卡正则表达式
	 */
	String BANK_CARD_REGEX = "^[0-9]{13,}$";

	/**
	 * 日期校验 yyyy-MM-dd
	 */
	String DATE_REGEX = "^[1-9][0-9]{3}-((0)[1-9]|(1)[0-2])-((0)[1-9]|[1,2][0-9]|(3)[0,1])$";

	/**
	 * 时间校验 yyyy-MM-dd HH:mm:ss
	 */
	String DATE_TIME_REGEX = "^[1-9][0-9]{3}-((0)[1-9]|(1)[0-2])-((0)[1-9]|[1,2][0-9]|(3)[0,1])(\\s)((0)[0-9]|(1)[0-9]|(2)[0-3]):([0-5][0-9]):([0-5][0-9])$";
}
