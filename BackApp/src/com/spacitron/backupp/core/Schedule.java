package com.spacitron.backupp.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class Schedule implements DataStorable{
	
	protected static final String ITEMNAME= "ITEMNAME";
	public static final String VERSIONLIMIT = "VERSIONLIMIT";
	public static final String INTERVAL = "INTERVAL";
	public static final String DATECREATED = "DATECREATED";
	public static final String TYPE = "SCHEDULE";
	
	private HashMap<String, String> itemData;
	private ArrayList<String> masterPaths;
	private boolean stopped;
	private Filer filer;
	private int threadCount;
	ArrayList<Integer> workerCount;
	

	protected Schedule(String name, Long interval, int versionLimit, Filer filer) {
		stopped = false;
		threadCount = Runtime.getRuntime().availableProcessors();
		masterPaths = new ArrayList<String>();
		
		//All data that needs to be stored for this DataStorable object need to go in the itemData map
		itemData = new HashMap<String, String>();
		itemData.put(ITEMNAME, name);
		itemData.put(INTERVAL, String.valueOf(interval));
		itemData.put(VERSIONLIMIT, String.valueOf(versionLimit));
		itemData.put(DATECREATED, String.valueOf(System.currentTimeMillis()));
		this.filer = filer;
		
		//Saves this schedule
		filer.store(this);
		
		//Repopulates backup file list
		ArrayList<HashMap<String, String>> maps = filer.getDataMaps(Document.TYPE);
		for(HashMap<String, String> map:maps){
			addMasterDocument(map.get(Document.ORIGINALLOCATION));
		}
	}
	
	@Override
	public String getType(){
		return TYPE;
	}

	@Override
	public String getItemName() {
		return itemData.get(ITEMNAME);
	}

	@Override
	public HashMap<String, String> getData(){
		return itemData;
	}

	protected void stop() {
		stopped = true;
	}
	
	protected void addMasterDocument(String filePath) {
		masterPaths.add(filePath);
	}

	protected void removeMasterDocument(String filePath) {
		for(int i=0; i<masterPaths.size(); i++){
			if(masterPaths.get(i).equals(filePath)){
				masterPaths.remove(i);
			}
		}
	}
	
	protected void deleteStoredDocument(Document doc){
		filer.delete(doc);
	}
	
	protected boolean delete(){
			return 	filer.delete(this);
	}
	
	
	protected int getVersionLimit(){
		int v = Integer.valueOf(itemData.get(VERSIONLIMIT));
		return v;
	}
	protected long getInterval(){
		long i = Long.valueOf(itemData.get(INTERVAL));
		return i;
	}
	
	protected String getDateCreated(){
		return itemData.get(DATECREATED);
	}
	
	protected boolean start() {
		if(masterPaths.size()==0){
			return false;
		}
		new Thread(new Runnable() {
			
			@Override
			//
			public void run() {
				while (stopped == false) {
					int versionLim = getVersionLimit();
					ArrayList<HashMap<String, String>> dataMaps = filer.getDataMaps(Document.TYPE);
					ArrayList<Document> storedDocuments = makeStoredDocuments(dataMaps);
					ArrayList<MasterDocument> masterDocuments = getMasterDocuments(masterPaths);
					ArrayList<Document> docList = groupDocuments(masterDocuments, storedDocuments);
					Thread[] workerThreads = setThreads(docList, filer, versionLim);
					for (Thread workerThread : workerThreads) {
						workerThread.start();
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
		return true;
	}
	
	private ArrayList<MasterDocument> getMasterDocuments(ArrayList<String> masterPaths){
		ArrayList<MasterDocument> masterDocuments = new ArrayList<>();
		ArrayList<String> invalidPaths = new ArrayList<>();
		for(String path: masterPaths){
			File file = new File(path);
			if(file.exists()){
				masterDocuments.add(new MasterDocument(getItemName(), file));
			}else{
				invalidPaths.add(path);
			}
		}
		masterPaths.removeAll(invalidPaths);
		return masterDocuments;
	}
	
	
				
	 private ArrayList<Document> makeStoredDocuments(ArrayList<HashMap<String, String>> dataMaps) {
			ArrayList<Document> docs = new ArrayList<Document>();
			for (HashMap<String, String> map : dataMaps) {
				String parent = getItemName();
				String originalLocation = map.get(Document.ORIGINALLOCATION);
				String itemName = map.get(ITEMNAME);
				int version = Integer.valueOf(map.get(Document.VERSION));
				Document doc = new Document(parent, originalLocation, itemName, version); 
				docs.add(doc);
			}
			return docs;
		}
		
		
		private ArrayList<Document> groupDocuments(ArrayList<MasterDocument> masterDocs, ArrayList<Document> storedDocs) {
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

		private Thread[] setThreads(ArrayList<Document> docList, Filer filer, int versionLimit) {
			threadCount = Runtime.getRuntime().availableProcessors();
			//Checks that there are enough documents for each thread so that no threads are created if they have
			//no work to do.
			if(threadCount>docList.size()){
				threadCount= docList.size();
			}
			
			Thread[] threads = new Thread[threadCount];
			CopyWorker[] copyWorkers = new CopyWorker[threadCount];
			
			for (int i = 0; i < threadCount; i++) {
				copyWorkers[i] = new CopyWorker(versionLimit);
				threads[i] = new Thread(copyWorkers[i]);
			}
			for (int i = 0; i < docList.size(); i++) {
				copyWorkers[i % threadCount].add(docList.get(i));
			}
			return threads;
		}

	
	class CopyWorker implements Runnable {

		private ArrayList<Document> docList;
		private int versionLimit;

		protected CopyWorker(int versionLimit) {
			this.versionLimit = getVersionLimit();
			docList = new ArrayList<Document>();
		}
		
		protected void add(Document doc) {
			docList.add(doc);
		}

		
		//Sends items to the file. Groups together store documents to update and
		//master documents to store. The filer will check if there is a File that it needs to save
		//in the DataStorable object.
		@Override
		public void run() {
			ArrayList<DataStorable> docsToStore = new ArrayList<>();
			ArrayList<DataStorable> docsToUpdate = new ArrayList<>();
			ArrayList<DataStorable> toDelete = new ArrayList<>();
		
				for (Document doc : docList) {
					if (doc.getVersion() >= versionLimit) {
						toDelete.add(doc);
					}
					if(doc instanceof MasterDocument){
						doc.updateVersion(+1);
						docsToStore.add(doc);
					}else{
						doc.updateVersion(+1);
						docsToUpdate.add(doc);
					}
				}
				filer.delete(toDelete);
				filer.store(docsToStore);
				filer.update(docsToUpdate);
			}
		
	}
	

	
}














