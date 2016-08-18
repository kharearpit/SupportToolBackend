package versioncheck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class CheckVersion {
	
	public ArrayList<HashMap<String,String>> checkForComponentVersion(String components,String jira_id) throws InterruptedException{
		
System.out.println("hi: "+components+"   "+jira_id);	
		
		String folderName=components.toLowerCase().trim();
		//String totalOutput="";
		BufferedReader reader;
		String param=components+"-"+jira_id;
		// if component selected is HDFS then point to Hadoop repo only
		if(folderName.equalsIgnoreCase("hdfs"))
			folderName="hadoop";

		//sending result to the UI in the key-value pair format: storing it in version variable
		ArrayList<HashMap<String, String>> version = new ArrayList<HashMap<String, String>>();
		try {
			//executing shell script
			Process process = Runtime.getRuntime().exec("/root/support-tools/backend/version-check/"+folderName+"/find_hdp_commit.sh"+" "+param);
			//reading the result of console line by line
			System.out.println("process.waitFor(): "+process.waitFor());
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			//used to fetch per line of output
			String line = "";     
			// reading line if it is not empty
			while ((line = reader.readLine())!= null) {
				System.out.println("[DEBUG] output line "+line);
				//if the line contains some result--comments
				if(!line.isEmpty() && (!line.endsWith("tag") || !line.endsWith("maint")) && line.contains(param) && !line.contains("Greping") && (line.contains("-tag") || line.contains("-maint")))
				{ 	  // splitting the result in 2 parts: tag and comments
					String splitter="";
					if(line.contains("-tag"))
						splitter="-tag";
					if(line.contains("-maint"))
						splitter="-maint";
					
					String[] result_split = line.split(splitter);
					String tag = result_split[0].trim()+splitter;
					System.out.println("tag: "+tag);
					
					String comments = result_split[1].trim()+splitter;
					System.out.println("comments: "+comments);
					// to store the result in key value pair
					HashMap<String, String> hm = new HashMap<String, String>();
					// storing default values if empty
					if(tag.isEmpty() && comments.isEmpty()){
						hm.put("tag", "No Tag/Branches found");
						hm.put("comments", "No Comments found");
					}
					// storing the result
					else{
						hm.put("tag",tag);
						hm.put("comments",comments);
					}
					//adding the result to the array list o make json format
					version.add(hm);
				}
			}

		} catch (IOException e) {	
			e.printStackTrace();
		}
		catch (ArrayIndexOutOfBoundsException e) {

			e.printStackTrace();
		}
		// if version is empty, returing the default json	
		if(!version.isEmpty())
			return version;

		else{
			ArrayList<HashMap<String, String>> version_empty = new ArrayList<HashMap<String, String>>();
			HashMap<String, String> hm_empty = new HashMap<String, String>();
			hm_empty.put("tag", "No Tag/Branches found");
			hm_empty.put("comments", "No Comments found");
			version_empty.add(hm_empty);
			return version_empty;
		}
		
	}

}
