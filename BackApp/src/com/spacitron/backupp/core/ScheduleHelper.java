package com.spacitron.backupp.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

class ScheduleHelper {
	
	private ScheduleHelper(){
	}
	
	
	
	protected static ArrayList<Document> groupDocsForBackup(ArrayList<HashMap<String, String>> dataMaps, Set<String> masterDocPaths){
		ArrayList<Document> storedDocuments = makeStoredDocuments(dataMaps);
		ArrayList<MasterDocument> masterDocuments = getMasterDocuments(masterDocPaths);
		return groupDocuments(masterDocuments, storedDocuments);
	}
	
	
	
	/**
	 * @param paths Absolute paths of files and folders than need backing up.
	 * @return Returns a list of documents from the sources assigned to backup. This method will automatically clear out the sources that are 
	 * no longer available.
	 */
	private static ArrayList<MasterDocument> getMasterDocuments(Set<String> paths){
		ArrayList<MasterDocument> masterDocuments = new ArrayList<>();
		ArrayList<String> invalidPaths = new ArrayList<>();
		for(String path: paths){
			File file = new File(path);
			if(file.isFile()){
				masterDocuments.add(new MasterDocument(file));
			}else if(file.isDirectory()){
				Set <String> dirPath = new TreeSet<>();
				for(File f:file.listFiles()){
					dirPath.add(f.getAbsolutePath());
				}
				masterDocuments.addAll(getMasterDocuments(dirPath));
			}else if(!file.exists()){
				invalidPaths.add(path);
			}
		}
		paths.removeAll(invalidPaths);
		return masterDocuments;
	}
	
	
	 private static ArrayList<Document> makeStoredDocuments(ArrayList<HashMap<String, String>> dataMaps) {
			ArrayList<Document> docs = new ArrayList<Document>();
			for (HashMap<String, String> map : dataMaps) {
				String originalLocation = map.get(Document.ORIGINALLOCATION);
				String itemName = map.get(Document.ITEMNAME);
				int version = Integer.valueOf(map.get(Document.VERSION));
				Document doc = new Document(originalLocation, itemName, version); 
				docs.add(doc);
			}
			return docs;
		}
		
		
		private static ArrayList<Document> groupDocuments(ArrayList<MasterDocument> masterDocs, ArrayList<Document> storedDocs) {
			ArrayList<Document> docList = new ArrayList<>();
			for (Document masterDoc : masterDocs) {
				boolean needsBackup = true;
				ArrayList<Document>	tempList = new ArrayList<>();
				tempList.add(masterDoc);
				
				for(Document storedDoc:storedDocs){
					if(storedDoc.isBackupOf(masterDoc)){
						tempList.add(storedDoc);
					}
					if(storedDoc.equals(masterDoc)){
						needsBackup = false;
					}
				}
				if(needsBackup){
					docList.addAll(tempList);
				}
			}
			return docList;
		}

	

}
