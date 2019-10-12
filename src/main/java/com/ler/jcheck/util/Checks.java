package com.ler.jcheck.util;

import com.ler.jcheck.config.ErrorCode;
import com.ler.jcheck.config.UserFriendlyException;

/**
 * @author lww
 * @date 2019-09-04 16:27
 */
public class Checks {

	public static void isTrue(Boolean ex, String msg) {
		if (!ex) {
			throw new UserFriendlyException(msg);
		}
	}

	public static void isTrue(Boolean ex, String msg, ErrorCode errorCode) {
		if (!ex) {
			throw new UserFriendlyException(msg, errorCode);
		}
	}

}
