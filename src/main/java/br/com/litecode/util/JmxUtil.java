package br.com.litecode.util;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class JmxUtil {
	public static void registerMBean(Object mBean, String simpleName) {
		try {
			String mbeanName = "br.com.litecode:type=" + simpleName;
			if (ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName(mbeanName))) {
				ManagementFactory.getPlatformMBeanServer().registerMBean(mBean, new ObjectName(mbeanName));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
