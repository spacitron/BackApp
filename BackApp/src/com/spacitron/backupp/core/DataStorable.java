package com.spacitron.backupp.core;

import java.util.HashMap;

public interface DataStorable {
	
	public String getType();
	
	public String getItemName();
	
	public HashMap<String, String> getData();
}
