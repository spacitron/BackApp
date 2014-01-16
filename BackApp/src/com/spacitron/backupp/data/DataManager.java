package com.spacitron.backupp.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;



public class DataManager {

	// Final String DataStrings
	public static final String TABLE = "TABLE";
	public static final String ITEMNAME = "ITEMNAME";
	private static final String SLASH = "\\";
	private static final String DOTS = "....";
	private String destination;
	
	
	protected DataManager(String destination){
		this.destination = destination;
	}

	protected  synchronized HashMap<String, String> getRow(String tableName, String itemName) {
		
		HashMap<String, String> rowMap = new HashMap<String, String>();
		try {
			Connection conn = getDataSource();
			Statement stat = conn.createStatement();
			String statement = "SELECT * FROM " + tableName + " WHERE " + ITEMNAME+ " = '" + itemName + "'";
			ResultSet dataNames = stat.executeQuery(escapeProcess(statement,SLASH, DOTS));
			ResultSetMetaData fieldNames = dataNames.getMetaData();
			int columnCount = fieldNames.getColumnCount();

			rowMap.put(TABLE, tableName);
			for (int i = 1; i <= columnCount; i++) {
				Object value = reprocess(dataNames.getObject(i), DOTS, SLASH);
				String dataName = fieldNames.getColumnName(i);

				rowMap.put(dataName, (String) value);
			}
			stat.close();
			conn.close();
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return rowMap;
	}

	protected  synchronized ArrayList<HashMap<String,String>> getTable(String tableName) {
		
		ArrayList<HashMap<String, String>> tableMatrix = new ArrayList<HashMap<String,String>>();
		try {
			Connection conn = getDataSource();
			Statement stat = conn.createStatement();
			ResultSet data = stat.executeQuery("SELECT * FROM " + tableName);
			ResultSetMetaData meta = data.getMetaData();
			while (data.next()) {
				HashMap<String, String> g = new HashMap<String, String>();
				g.put(TABLE, tableName);
				for (int i = 1; i <= meta.getColumnCount(); i++) {
					String dataName = meta.getColumnName(i);
					Object value = reprocess(data.getObject(i), DOTS, SLASH);
					g.put(dataName, (String) value);
				}
				tableMatrix.add(g);
			}
			
			stat.close();
			conn.close();
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return tableMatrix;
	}

	
	protected  synchronized void insert(String table, HashMap<String, String> dataMap) {
		ArrayList<HashMap<String, String>> map = new ArrayList<HashMap<String,String>>();
		map.add(dataMap);
		insert(table, map);
	}
	
	/**
	 * @param insertValues
	 *            Inserts values to database. The input map needs to have the
	 *            table name under the "TABLE" key and a unique entry name under
	 *            the "NAME" key. All other values need to be under their
	 *            corresponding keys depending on the data type. Will not
	 *            perform the insert operation if an entry of the same name
	 *            already exists.
	 */
	protected void insert(String table,ArrayList<HashMap<String, String>> dataMaps) {
		Statement stat;
		Connection conn;
		try {
			conn = getDataSource();
			for (HashMap<String, String> dataMap : dataMaps) {
				stat = conn.createStatement();
				String itemName = dataMap.get(ITEMNAME);
				dataMap.remove(ITEMNAME);
				Set<String> keySet = dataMap.keySet();
				String fields = "(";
				String values = "(";
				for(String key: keySet){
					fields += key + ", ";
					values +="'"+dataMap.get(key)+ "',";
				}

				fields += ITEMNAME + ")";
				values += "'" + itemName + "')";
				String statement = "INSERT OR IGNORE INTO " + table + fields + " VALUES " + values;
				statement = escapeProcess(statement, SLASH, DOTS);
				statement = escapeProcess(statement, ",$", "");
				stat.execute(statement);
				stat.close();
			}
			conn.close();
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected  synchronized void update(String table,String itemName, HashMap<String, String> dataMap){
		ArrayList<HashMap<String,String>> dataMaps = new ArrayList<HashMap<String,String>>();
		ArrayList<String> itemNames = new ArrayList<String>();
		dataMaps.add(dataMap);
		update(table, itemNames, dataMaps);
	}
	
	protected  synchronized void update(String table, ArrayList<String> itemNames, ArrayList<HashMap<String, String>> dataMaps) {
		deleteRows(table, itemNames);
		insert(table, dataMaps);
	}
	
	protected  synchronized void deleteRow(String table, String itemName){
		ArrayList<String> itemNames = new ArrayList<String>();
		itemNames.add(itemName);
		deleteRows(table, itemNames);
	}
	
	
	protected  synchronized void deleteRows(String table, ArrayList<String> itemNames) {
		try {
			Connection conn = getDataSource();
			for (String itemName : itemNames) {
				Statement stat = conn.createStatement();
				String sql = escapeProcess("DELETE FROM " + table + " WHERE ITEMNAME = '" + itemName + "'", SLASH, DOTS);
				stat.execute(sql);
				stat.close();
			}
			conn.close();
		} catch (SQLException | ClassNotFoundException e) {
		}
	}
	
	protected  synchronized void makeTable(String tableName, String... columns) {
		String sql = "('ITEMNAME' TEXT PRIMARY KEY,";
		for (String g : columns) {
			sql += "'" + g + "' TEXT,";
		}
		sql += "$)";
		sql = "CREATE TABLE IF NOT EXISTS " + tableName	+ escapeProcess(sql, ",$", "");
		Statement stat;
		try {
			Connection conn = getDataSource();
			stat = conn.createStatement();
			stat.execute(sql);
			stat.close();
			conn.close();
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	protected  synchronized void deleteTable(String tablename) {
		try {
			Connection conn = getDataSource();
			Statement stat = conn.createStatement();
			stat.execute("DROP TABLE IF EXISTS " + tablename);
			stat.close();
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Connection getDataSource() throws SQLException, ClassNotFoundException {
			Class.forName("org.sqlite.JDBC");
			return DriverManager.getConnection("jdbc:sqlite:"+destination+".db");
	}
	
	private String escapeProcess(String SQLStatement, String bad,
			String good) {
		return SQLStatement.replace(bad, good);
	}

	private Object reprocess(Object stringObj, String bad, String good) {
		if (stringObj instanceof String) {
			return ((String) stringObj).replace(bad, good);
		}
		return stringObj;
	}
	
// 	This is here for testing purposes	
//	
//	public void printData(String table){
//		ArrayList<HashMap<String, String>> rows = getTable(table);
//		for(HashMap<String, String> row:rows){
//			Iterator <Entry<String, String>> it = row.entrySet().iterator();
//			while(it.hasNext()){
//				Map.Entry<String, String> p = it.next();
//				System.out.print(" | "+p.getKey()+" = "+p.getValue()+" | ");	
//			}
//			System.out.println();
//		}
//	}
	
	

}
