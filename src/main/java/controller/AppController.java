package controller;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.net.HttpHeaders;

@RestController
@EnableAutoConfiguration
@ComponentScan
@RequestMapping("/hortonworks/support-tool/v1/*")
public class AppController {

	/*test web service to check if server is reachable */
	@CrossOrigin(origins = "*") //enabling cross browser request
	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public @ResponseBody String cancelReservation(){
		System.out.println("hi");
		return "<html><h1>Support Tool RESTful web service.</h1><p><font color='blue'>Hello Team! This is a sample RESTful API which would download the logs at backend "
		+ "and would respond with the recommendations</font></p><p>- akhare@hortonworks.com</p>";
	}


	/* download the log file from server to client- NOT USED AS IF NOW */
	@CrossOrigin(origins = "*") //enabling cross browser request
	@RequestMapping(value="/downloadLogFile",method=RequestMethod.GET	)
	public void getLogFile(HttpSession session,HttpServletResponse response) throws Exception {
		try {
			String fileName="Arpit";
			//fetching file from a location
			String filePathToBeServed ="/Users/akhare/Desktop/text.rtf"; //complete file name with path;
			//making file object
			File fileToDownload = new File(filePathToBeServed);
			InputStream inputStream = new FileInputStream(fileToDownload);
			response.setContentType("application/force-download");
			response.setHeader("Content-Disposition", "attachment; filename="+fileName+".txt"); 
			IOUtils.copy(inputStream, response.getOutputStream());
			//sending the file to the client for download
			response.flushBuffer();
			//closing the session
			inputStream.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	@CrossOrigin(origins = "*")	//enabling cross browser request
	@RequestMapping(value="/upload", method=RequestMethod.GET)
	public @ResponseBody String provideUploadInfo() {
		return "You can upload a file by posting to this same URL.";
	}

	/* to upload the log file to the server for making it available to the perl script */
	@CrossOrigin(origins = "*") //enabling cross browser request
	@RequestMapping(value="/upload", method=RequestMethod.POST, produces = "application/json") //consumes = {"multipart/form-data"},
	public @ResponseBody ArrayList<HashMap<String, String>> handleFileUpload(
			@RequestParam("table_name") String table_name, //table name to store the results in this table in hive
			@RequestParam("file") MultipartFile file){ // fetching the file

		//timestamping the file so that for every request a new file is added
		String fileName = new SimpleDateFormat("'hdfs-'yyyy-MM-dd-hh-mm-ss'.log'").format(new Date());
		System.out.println("fileName:"+fileName);

		//timestamping the table_name
		String table="'"+table_name+"'yyyyMMddhhmmss";
		//System.out.println("converted table name to be saved: "+table);
		String tableName = new SimpleDateFormat(table).format(new Date());
		System.out.println(tableName);

		String response=""; // toring final output in this variable
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
						new BufferedOutputStream(new FileOutputStream(new File("/home/hive/zepplin_tool/logs/"+fileName)));
				stream.write(bytes);
				stream.close();
				
				System.out.println("File uploaded. Cheching the uploaded file..");
				File uploadedFile = new File("/home/hive/zepplin_tool/logs/"+fileName);
				if(uploadedFile.length() >0){
					System.out.println("Uploaded file is not empty");
					String param = "/home/hive/zepplin_tool/logs/"+fileName+ " "+tableName;
					try {
						System.out.println("Running the shell script");
						Process process = Runtime.getRuntime().exec("/home/hive/zepplin_tool/wrapper.sh"+" "+param);
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
							response="<html><body><b><font color='red'>Error in analyzing the logs. Zepplin URL cannot be generated. SQL statement not fetched</font></b></body></html";
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


	/* Apache Jira version check */
	@CrossOrigin(origins = "*") //enabling cross browser request
	@RequestMapping(value = "/checkForVersion", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody ArrayList<HashMap<String, String>> checkForVersion(

			@Valid @RequestParam(value="components") String components, // fetching the component value
			@Valid @RequestParam(value="jira_id") String jira_id) throws InterruptedException{ // fetching jira id

		System.out.println("hi: "+components+"   "+jira_id);			
		//String totalOutput="";
		BufferedReader reader;
		String param=components+"-"+jira_id;

		//String locationOfScript="/root/hadoop/find_hdp_commit.sh";
		//String exportToFile=" >> /tmp/ARPIT.txt";
		//String parameter = param+exportToFile;

		//sending result to the UI in the key-value pair format: storing it in version variable
		ArrayList<HashMap<String, String>> version = new ArrayList<HashMap<String, String>>();
		try {
			//executing shell script
			Process process = Runtime.getRuntime().exec("/root/hadoop/find_hdp_commit.sh"+" "+param);
			//Process process = Runtime.getRuntime().exec("/Users/akhare/Desktop/find_hdp_commit.sh"+" "+param);
			//System.out.println("[DEBUG] .exec() ");
			//reading the result of console line by line
			System.out.println("process.waitFor(): "+process.waitFor());
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			//used to fetch per line of output
			String line = "";     
			// reading line if it is not empty
			while ((line = reader.readLine())!= null) {
				System.out.println("[DEBUG] output line "+line);
				//if the line contains some result--comments
				if(!line.isEmpty() && !line.endsWith("tag") &&line.contains(param) && !line.contains("Greping"))
				{ 	  // splitting the result in 2 parts: tag and comments
					String[] result_split = line.split(param+".");
					String tag = result_split[0].trim();
					String comments = result_split[1].trim();
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
