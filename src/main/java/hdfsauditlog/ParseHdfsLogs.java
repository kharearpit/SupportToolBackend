package hdfsauditlog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.springframework.web.multipart.MultipartFile;

public class ParseHdfsLogs {

	public ArrayList<HashMap<String, String>> parseHdfsAuditLogs(String table_name, MultipartFile file){
		
		 // fetching the file

		//timestamping the file so that for every request a new file is added
		String fileName = new SimpleDateFormat("'hdfs-'yyyy-MM-dd-hh-mm-ss'.log'").format(new Date());
		System.out.println("fileName:"+fileName);

		//timestamping the table_name
		String table="'"+table_name+"'yyyyMMddhhmmss";
		//System.out.println("converted table name to be saved: "+table);
		String tableName = new SimpleDateFormat(table).format(new Date());
		System.out.println(tableName);

		String response=""; // Storing final output in this variable
		ArrayList<HashMap<String, String>> upload_result = new ArrayList<HashMap<String, String>>(); //for converting result into json , key value pair
		HashMap<String, String> hm = new HashMap<String, String>();// storing result in keys

		if (!file.isEmpty()) {
			System.out.println("name:"+fileName);
			try {
				//fetching file
				byte[] bytes = file.getBytes();
				//storing file at the given location
				System.out.println("Uploading the file...");
				BufferedOutputStream stream =
						new BufferedOutputStream(new FileOutputStream(new File("/home/hive/zeppelin_tool/logs/"+fileName)));
				stream.write(bytes);
				stream.close();
				
				System.out.println("File uploaded. Cheching the uploaded file..");
				File uploadedFile = new File("/home/hive/zeppelin_tool/logs/"+fileName);
				if(uploadedFile.length() >0){
					System.out.println("Uploaded file is not empty");
					String param = "/home/hive/zeppelin_tool/logs/"+fileName+ " "+tableName;
					try {
						System.out.println("Running the shell script");
						Process process = Runtime.getRuntime().exec("/home/hive/zeppelin_tool/wrapper.sh"+" "+param);
						if(process.waitFor()==0){
							System.out.println("Shell successfully executed");
							System.out.println("process completed.Preparing response");
							String query="Select *from "+tableName;
							String html_tags="<html><body><b>Logs analyzed successfully!</b><br>"
									+ "<b>Table Name(timestamped):</b>"+tableName+"<br>"
									+ "<b>Zeppelin URL:</b><a href='http://172.26.64.103:9995'>Zeppelin</a><br>"
									+ "<b><u>Instructions</u></b><br>"
									+ "1. Go to the Zeppelin URL and create a new note<br>"
									+ "2. Run the query: <b>"+query+"<b><br></body></html>"; 
							response=html_tags;
							
						}
						else
						{
							System.out.println("Error in analyzing the logs");
							response="<html><body><b><font color='red'>Error in analyzing the logs. Zeppelin URL cannot be generated. SQL statement not fetched</font></b></body></html";
							//response="Error in analyzing the logs. Zepplin URL cannot be generated. SQL statement not fetched";
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					hm.put("result", response);
					upload_result.add(hm);
					System.out.println("returning response");
					return upload_result;

				}
				else{ 
				response="<html><body><b><font color='red'>File uploaded with size 0KB. So further processing was not done.</font></b></body></html>";
				//response="File uploaded with size 0KB. So further processing was not done.";
				hm.put("result", response);
				upload_result.add(hm);
				System.out.println("returning response");
				return upload_result;
				}

			} catch (Exception e) {
				response = "<html><body><b><font color='red'>File upload failed:" + e.getMessage()+"</font></b></body></html>";
				//response="File upload FAILED:" + e.getMessage();
				hm.put("result", response);
				upload_result.add(hm);
				return upload_result;
			}

		} 
		else {
			response="<html><body>Failed to upload because the file was <font color='red'><b>EMPTY</b></font></body></html>";
			//response="Failed to upload because the file was EMPTY";
			hm.put("result", response);
			upload_result.add(hm);
			return upload_result;
		}

	
	}
}
