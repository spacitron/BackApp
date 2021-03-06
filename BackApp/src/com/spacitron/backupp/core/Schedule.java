package com.spacitron.backupp.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import com.spacitron.backupp.data.DataStorable;
import com.spacitron.backupp.data.Filer;


public class Schedule implements DataStorable{
	
	protected static final String ITEMNAME= "ITEMNAME";
	public static final String VERSIONLIMIT = "VERSIONLIMIT";
	public static final String INTERVAL = "INTERVAL";
	public static final String DATECREATED = "DATECREATED";
	public static final String TYPE = "SCHEDULE";
	public static final String DESTINATION = "DESTINATION";
	
	private HashMap<String, String> itemData;
	private Set<String> masterDocPaths;
	private boolean started;
	private Filer filer;
	private int threadCount;
	ArrayList<Integer> workerCount;
	

	/**
	 * Creates a backup schedule
	 * 
	 * @param name Unique name to be used to identify the backup schedule.
	 * @param interval Interval between backups in milliseconds.
	 * @param versionLimit Maximum number of copies of each file to be tracked by this schedule.
	 * @param filer Filer object that will be responsible for storing data associated with this schedule.
	 */
	protected Schedule(String name, Long interval, int versionLimit, Filer filer) {
		started = false;
		threadCount = Runtime.getRuntime().availableProcessors();
		masterDocPaths = new TreeSet<>();
		
		//All data that needs to be stored for this DataStorable object need to go in the itemData map
		itemData = new HashMap<String, String>();
		itemData.put(ITEMNAME, name);
		itemData.put(INTERVAL, String.valueOf(interval));
		itemData.put(VERSIONLIMIT, String.valueOf(versionLimit));
		itemData.put(DATECREATED, String.valueOf(System.currentTimeMillis()));
		itemData.put(DESTINATION, filer.getDestination());

		//Saves this schedule
		this.filer = filer;
		filer.store(this);
		
		//Repopulates backup file list
		ArrayList<HashMap<String, String>> maps = filer.getDataMaps(Document.TYPE);
		for(HashMap<String, String> map:maps){
			addMaster(map.get(Document.ORIGINALLOCATION));
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
	
	protected void setVerionLimit(int newVerLimit){
		itemData.remove(VERSIONLIMIT);
		itemData.put(VERSIONLIMIT, String.valueOf(newVerLimit));
	}
	
	protected void setInterval(long newInterval){
		itemData.remove(INTERVAL);
		itemData.put(INTERVAL, String.valueOf(newInterval));
	}
	
	protected Set<String> getMasterDocuments(){
		return masterDocPaths;
	}

	protected ArrayList<HashMap<String, String>> getBackupMaps(){
		return filer.getDataMaps(Document.TYPE);
	}
	
	
	protected void addMaster(String filePath) {
		masterDocPaths.add(filePath);
	}
	
	protected void removeMasterDocument(String filePath) {
		masterDocPaths.remove(filePath);
	}
	
	/**
	 * Adds a file to the backup schedule.
	 * 
	 * @param filePath Absolute path of file or folders to be added to this backup schedule. In case of a folder
	 * the schedule will backup all the files contained within the folder itself and all its subfolders.
	 * 
	 */
	protected void deleteStoredDocument(String name){
		HashMap<String, String> map = filer.getDataMap(Document.TYPE, name);
		Document doc = new Document(map.get(Document.ORIGINALLOCATION), name, Integer.valueOf(map.get(Document.VERSION)));
		filer.delete(doc);
	}
	
	/**
	 * Deletes all data, files and directories associated with this schedule.
	 * 
	 * @return True on successful deletion.
	 */
	protected boolean delete(){
		return 	filer.delete(this);
	}

	protected boolean start() {
		started = true;
		if(masterDocPaths.size()==0){
			started = false;
			return false;
		}
		new Thread(new Runnable() {
			
			@Override
			//
			public void run() {
				while (started) {
					int versionLim = Integer.valueOf(itemData.get(VERSIONLIMIT));
					ArrayList<Document> docList = ScheduleHelper.groupDocsForBackup(filer.getDataMaps(Document.TYPE), masterDocPaths);
					Thread[] workerThreads = setThreads(docList, filer, versionLim);
					for (Thread workerThread : workerThreads) {
						workerThread.start();
					}
					try {
						Thread.sleep(Long.valueOf(itemData.get(INTERVAL)));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
		return true;
	}
	
	protected void stop() {
		started = false;
	}
	
	protected boolean isStarted(){
		return started;
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
			this.versionLimit = Integer.valueOf(itemData.get(VERSIONLIMIT));
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














