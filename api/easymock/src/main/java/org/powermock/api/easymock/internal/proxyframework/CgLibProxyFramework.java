package org.powermock.api.easymock.internal.proxyframework;

import net.sf.cglib.proxy.Enhancer;

import org.powermock.reflect.proxyframework.RegisterProxyFramework;
import org.powermock.reflect.spi.ProxyFramework;

/**
 * CGLib proxy framework setup.
 */
public class CgLibProxyFramework implements ProxyFramework {

	/**
	 * Registers a new instance of the proxy framework.
	 */
	public static void registerProxyFramework() {
		RegisterProxyFramework.registerProxyFramework(new CgLibProxyFramework());
	}

	/**
	 * {@inheritDoc}
	 */
	public Class<?> getUnproxiedType(Class<?> type) {
		Class<?> currentType = type;
		while (isProxy(currentType)) {
			currentType = currentType.getSuperclass();
		}
		return currentType;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isProxy(Class<?> type) {
		return Enhancer.isEnhanced(type);
	}
}
