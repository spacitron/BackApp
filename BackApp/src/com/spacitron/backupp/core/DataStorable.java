package com.spacitron.backupp.core;

import java.util.HashMap;

/**
 * @author paolo
 * 
 * Defines the interface to be implemented by the system's entities that need to be stored through Filer objects. 
 *
 */
public interface DataStorable {
	
	public String getType();
	
	public String getItemName();
	
	public HashMap<String, String> getData();
}
