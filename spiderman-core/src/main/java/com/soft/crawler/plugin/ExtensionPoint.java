package com.soft.crawler.plugin;

import java.util.Collection;

public interface ExtensionPoint<T> {

	public Collection<T> getExtensions();
	
}
