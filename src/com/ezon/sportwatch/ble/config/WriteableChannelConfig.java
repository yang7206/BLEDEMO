package com.ezon.sportwatch.ble.config;

import java.util.ArrayList;
import java.util.List;

/**
 * ���ö�д��������
 * 
 * @author yxy
 * 
 */
public class WriteableChannelConfig {

	private static List<String> attributes = new ArrayList<String>();
	static {
		// Sample Services.
		attributes.add("0000ffb0");
		attributes.add("0000ffb2");
	}

	/**
	 * EZON��дͨ��
	 * 
	 * @param uuid
	 * @return
	 */
	public static boolean isEzonChannel(String uuid) {
		for (String prefix : attributes) {
			if (uuid.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}
}
