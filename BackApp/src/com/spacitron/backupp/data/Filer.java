package com.spacitron.backupp.data;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author paolo
 * 
 * Defines the interface to be implemented by data storage plug-ins.
 *
 */
public interface Filer {
	
	public String getDestination();
	public boolean store(ArrayList<DataStorable> dataStorables);
	public boolean store(DataStorable dataStorable);
	public boolean delete(DataStorable dataStorable);
	public boolean delete(ArrayList<DataStorable> dataStorable);
	public HashMap<String, String> getDataMap(String type, String itemName);
	public ArrayList<HashMap<String, String>> getDataMaps(String type);
	public boolean update(DataStorable dataStorable);
	public boolean update(ArrayList<DataStorable> dataStorables);
}
