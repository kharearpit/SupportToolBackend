package controller;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.catalina.Cluster;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.net.HttpHeaders;
import dto.CreateClusterDTO;

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
			String filePathToBeServed ="/Users/akhare/Desktop/Archive.zip"; //complete file name with path;
			//making file object
			File fileToDownload = new File(filePathToBeServed);
			InputStream inputStream = new FileInputStream(fileToDownload);
			response.setContentType("application/force-download");
			response.setHeader("Content-Disposition", "attachment; filename="+fileName+".zip"); 
			IOUtils.copy(inputStream, response.getOutputStream());
			//sending the file to the client for download
			response.flushBuffer();
			//closing the session
			inputStream.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	//create cluster
	@CrossOrigin(origins = "*") //enabling cross browser request
	@RequestMapping(value = "/createCluster", method = RequestMethod.POST, headers="Content-Type=application/json")

    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ArrayList<HashMap<String,String>> downloadClusterFiles
            (@Valid @RequestBody  CreateClusterDTO createClusterDTO,HttpSession session,HttpServletResponse res) throws InterruptedException{
		ArrayList<HashMap<String, String>> downloadArrayList = new ArrayList<HashMap<String, String>>(); //for converting result into json , key value pair
		HashMap<String, String> hm = new HashMap<String, String>();// storing result in keys
		
        System.out.println("Ambari version: "+createClusterDTO.getAmbari_version());
        System.out.println(createClusterDTO.getCluster_name());
        System.out.println("HDP Version: "+createClusterDTO.getCluster_version());
        System.out.println(createClusterDTO.getDefault_password());
        System.out.println(createClusterDTO.getDomain_name());
        System.out.println(createClusterDTO.getHost_names());
        System.out.println("Node count: "+createClusterDTO.getNodes_count());
        System.out.println("OS type: "+createClusterDTO.getOs_type());
        
        int hostCount = Integer.parseInt(createClusterDTO.getNodes_count());
        System.out.println("hostCount: "+hostCount);
        
        try {
			Process process = Runtime.getRuntime().exec("/root/createCluster/gitFetch.sh");
			//check if git fetch was success or not
			if(process.waitFor()==0){
				
				//creating prop file
				String file="'"+createClusterDTO.getCluster_name()+"'yyyyMMddhhmmss";
				String fileName= new SimpleDateFormat(file).format(new Date());
				System.out.println("fileName: "+fileName);
				//creating variables equivalent to the lines in .props file
				String clusterName="CLUSTERNAME="+createClusterDTO.getCluster_name();
				String OS="OS="+createClusterDTO.getOs_type();
				String ambariVersion="AMBARIVERSION="+createClusterDTO.getAmbari_version();
				String node_count="NUM_OF_NODES="+createClusterDTO.getNodes_count();
				String domainName="DOMAIN_NAME="+createClusterDTO.getDomain_name();
				String password="DEFAULT_PASSWORD="+createClusterDTO.getDefault_password();
				String clusterVersion="CLUSTER_VERSION="+createClusterDTO.getCluster_version();
				//get hostnames
				String hostArray[]=createClusterDTO.getHost_names().split(",");
				/*for (int i = 0; i < hostArray.length; i++) {
					System.out.println(i+" :"+hostArray[i]);
				}*/
				
				//write to the file		
				PrintWriter writer = new PrintWriter("/root/createCluster/props_files/"+fileName+".props", "UTF-8");
				writer.println(clusterName);
				writer.println(OS);
				writer.println(ambariVersion);
				writer.println(node_count);
				writer.println(domainName);
				writer.println(password);
				writer.println(clusterVersion);
				
				for (int i = 0; i < hostArray.length; i++) {
					
					writer.println("HOST"+(i+1)+"="+hostArray[i]);
				}
				
				writer.println("HOST1_SERVICES=\"NAMENODE,NODEMANAGER,DATANODE,ZOOKEEPER_CLIENT,HDFS_CLIENT,YARN_CLIENT,MAPREDUCE2_CLIENT,ZOOKEEPER_SERVER\"");
				writer.println("HOST2_SERVICES=\"SECONDARY_NAMENODE,NODEMANAGER,DATANODE,ZOOKEEPER_CLIENT,ZOOKEEPER_SERVER,HDFS_CLIENT,YARN_CLIENT,MAPREDUCE2_CLIENT\"");
				writer.println("HOST3_SERVICES=\"RESOURCEMANAGER,APP_TIMELINE_SERVER,HISTORYSERVER,NODEMANAGER,DATANODE,ZOOKEEPER_CLIENT,ZOOKEEPER_SERVER,HDFS_CLIENT,YARN_CLIENT,MAPREDUCE2_CLIENT\"");
				
				if(hostCount>3){
					
					for (int i = 0; i < hostCount-3; i++) {
						int count=4+i;
						writer.println("HOST"+count+"_SERVICES=\"NODEMANAGER,DATANODE,ZOOKEEPER_CLIENT,HDFS_CLIENT,YARN_CLIENT\"");
					}	
					
				}
				//get the BASE_URL's and UTILITY_URL's and append to the file
				BufferedReader reader;
				Process process2 = Runtime.getRuntime().exec("grep HDP /root/createCluster/useful-scripts/hdp-automated-setup/templates/multi-node/cluster.props");
				if(process2.waitFor()==0)
				{
					reader = new BufferedReader(new InputStreamReader(process2.getInputStream()));
					//used to fetch per line of output
					String line = "";     
					// reading line if it is not empty
					while ((line = reader.readLine())!= null) {
					writer.println(line);
					}
				}
				else{
					writer.println("HDP_2.4_BASE_URL=\"http://172.26.64.249/hdp/centos7/HDP-2.4.2.0/\"");
					writer.println("HDP_2.4_BASE_URL=\"http://172.26.64.249/hdp/centos7/HDP-2.4.0.0/\"");
					writer.println("HDP_2.3_BASE_URL=\"http://172.26.64.249/hdp/centos7/HDP-2.3.4.0/\"");
					writer.println("HDP_2.3_BASE_URL=\"http://172.26.64.249/hdp/centos7/HDP-2.3.2.0/\"");
					writer.println("HDP_2.3_BASE_URL=\"http://172.26.64.249/hdp/centos7/HDP-2.3.0.0/\"");
					writer.println("HDP_2.4_BASE_URL=\"http://172.26.64.249/hdp/centos6/HDP-2.4.2.0/\"");
					writer.println("HDP_2.4_BASE_URL=\"http://172.26.64.249/hdp/centos6/HDP-2.4.0.0/\"");
					writer.println("HDP_2.3_BASE_URL=\"http://172.26.64.249/hdp/centos6/HDP-2.3.6.0/\"");
					writer.println("HDP_2.3_BASE_URL=\"http://172.26.64.249/hdp/centos6/HDP-2.3.4.7/\"");
					writer.println("HDP_2.3_BASE_URL=\"http://172.26.64.249/hdp/centos6/HDP-2.3.4.0/\"");
					writer.println("HDP_2.3_BASE_URL=\"http://172.26.64.249/hdp/centos6/HDP-2.3.2.0/\"");
					writer.println("HDP_2.3_BASE_URL=\"http://172.26.64.249/hdp/centos6/HDP-2.3.0.0/\"");
					writer.println("HDP_2.2_BASE_URL=\"http://172.26.64.249/hdp/centos6/HDP-2.2.9.0/\"");
					writer.println("HDP_2.2_BASE_URL=\"http://172.26.64.249/hdp/centos6/HDP-2.2.8.0/\"");
					writer.println("HDP_2.2_BASE_URL=\"http://172.26.64.249/hdp/centos6/HDP-2.2.6.0/\"");
					writer.println("HDP_2.2_BASE_URL=\"http://172.26.64.249/hdp/centos6/HDP-2.2.4.8/\"");
					writer.println("HDP_2.2_BASE_URL=\"http://172.26.64.249/hdp/centos6/HDP-2.2.4.2/\"");
					writer.println("HDP_2.2_BASE_URL=\"http://172.26.64.249/hdp/centos6/HDP-2.2.0.0/\"");
					writer.println("HDP_UTILS_URL=\"http://172.26.64.249/hdp/centos7/HDP-UTILS-1.1.0.20/\"");
					writer.println("HDP_UTILS_URL=\"http://172.26.64.249/hdp/centos6/HDP-UTILS-1.1.0.20/\"");
				}
				
				writer.close();
				
				//String propPath="/root/createCluster/props_files/"+fileName+".props";
				//String shellpath="/root/createCluster/useful-scripts/hdp-automated-setup/setup_cluster.sh";
				//String outputPath="/root/createCluster/props_files/"+"arpit.gz";
				//zip the file starts
				
				String zipFile = "/root/createCluster/props_files/Hwx-"+fileName+".zip";

		        String[] srcFiles = { "/root/createCluster/useful-scripts/hdp-automated-setup/setup_cluster.sh", "/root/createCluster/props_files/"+fileName+".props"};

		        try {

		            // create byte buffer
		            byte[] buffer = new byte[1024];

		            FileOutputStream fos = new FileOutputStream(zipFile);

		            ZipOutputStream zos = new ZipOutputStream(fos);

		            for (int i=0; i < srcFiles.length; i++) {

		                File srcFile = new File(srcFiles[i]);

		                FileInputStream fis = new FileInputStream(srcFile);

		                // begin writing a new ZIP entry, positions the stream to the start of the entry data
		                zos.putNextEntry(new ZipEntry(srcFile.getName()));

		                int length;

		                while ((length = fis.read(buffer)) > 0) {
		                    zos.write(buffer, 0, length);
		                }

		                zos.closeEntry();

		                // close the InputStream
		                fis.close();

		            }

		            // close the ZipOutputStream
		            zos.close();
		            System.out.println("DOne");
		            


		        }
		        catch (IOException ioe) {
		            System.out.println("Error creating zip file: " + ioe);
		        }

				
				//zip the file ends
		        
		        try {
					String zippedFileName="Arpit";
					//fetching file from a location
					String filePathToBeServed =zipFile; //complete file name with path;
					//making file object
					File fileToDownload = new File(filePathToBeServed);
					InputStream inputStream = new FileInputStream(fileToDownload);
					res.setContentType("application/force-download");
					res.setHeader("Content-Disposition", "attachment; filename="+zippedFileName+".zip"); 
					IOUtils.copy(inputStream, res.getOutputStream());
					//sending the file to the client for download
					res.flushBuffer();
					//closing the session
					inputStream.close();
				} catch (Exception e){
					e.printStackTrace();
				}
				
				
			}
			else{
				hm.put("result","failed");
			}
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        downloadArrayList.add(hm);
        return downloadArrayList;
    }
	
	
	
	//hdfs audit log
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


	/* Apache Jira version check */
	@CrossOrigin(origins = "*") //enabling cross browser request
	@RequestMapping(value = "/checkForVersion", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody ArrayList<HashMap<String, String>> checkForVersion(

			@Valid @RequestParam(value="components") String components, // fetching the component value
			@Valid @RequestParam(value="jira_id") String jira_id) throws InterruptedException{ // fetching jira id

		System.out.println("hi: "+components+"   "+jira_id);	
		String folderName=components.toLowerCase().trim();
		//String totalOutput="";
		BufferedReader reader;
		String param=components+"-"+jira_id;

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
