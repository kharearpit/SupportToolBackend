package controller;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import createcluster.CreateCluster;
import dto.CreateClusterDTO;
import hdfsauditlog.ParseHdfsLogs;
import versioncheck.CheckVersion;

@RestController
@EnableAutoConfiguration
@ComponentScan
@RequestMapping("/hortonworks/support-tool/v1/*")
public class AppController {

	/*--------------------------------------test web service------------------------------------------*/
	//REST API to test web service to check if server is reachable */
	@CrossOrigin(origins = "*") //enabling cross browser request
	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public @ResponseBody String cancelReservation(){
		System.out.println("hi");
		return "<html><h1>Support Tool RESTful web service.</h1><p><font color='blue'>Hello Team! This is a sample RESTful API which would download the logs at backend "
		+ "and would respond with the recommendations</font></p><p>- akhare@hortonworks.com</p>";
	}

	/*----------------------download the log file from server to client- NOT USED AS IF NOW-----------*/
	//REST API to download the log file from server to client- NOT USED AS IF NOW */
	@CrossOrigin(origins = "*") //enabling cross browser request
	@RequestMapping(value="/downloadLogFile",method=RequestMethod.GET	)
	public void getLogFile(HttpSession session,HttpServletResponse response) throws Exception {
		try {
			String fileName="Hortonworks";
			//fetching file from a location
			String filePathToBeServed ="/Users/akhare/Desktop/HWX.zip"; //complete file name with path;
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
	
	
	/*-------------------------------------------create cluster----------------------------------------*/
	//REST API to create cluster
	@CrossOrigin(origins = "*") //enabling cross browser request
	@RequestMapping(value = "/createCluster", method = RequestMethod.POST, headers="Content-Type=application/json")
	
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ArrayList<HashMap<String,String>> downloadClusterFiles
            (@Valid @RequestBody  CreateClusterDTO createClusterDTO,HttpSession session,HttpServletResponse res) throws InterruptedException{
		
		ArrayList<HashMap<String, String>> clusterList = new ArrayList<HashMap<String, String>>();
		CreateCluster cc = new CreateCluster();
		clusterList=cc.createCluster(createClusterDTO, session, res);
		return clusterList;
	}
	
	/*-------------------------------------------hdfs audit log----------------------------------------*/
	//REST API to upload the log file to the server for making it available to the perl script */
	@CrossOrigin(origins = "*") //enabling cross browser request
	@RequestMapping(value="/upload", method=RequestMethod.POST, produces = "application/json") //consumes = {"multipart/form-data"},
	public @ResponseBody ArrayList<HashMap<String, String>> handleFileUpload(
			@RequestParam("table_name") String table_name, //table name to store the results in this table in hive
			@RequestParam("file") MultipartFile file){
		
		ArrayList<HashMap<String, String>> parseLogsList = new ArrayList<HashMap<String, String>>();
		ParseHdfsLogs phl=new ParseHdfsLogs();
		parseLogsList=phl.parseHdfsAuditLogs(table_name, file);
		return parseLogsList;
	}

	/*-------------------------------------------Apache version check----------------------------------------*/
	//REST API to Apache Jira version check */
	@CrossOrigin(origins = "*") //enabling cross browser request
	@RequestMapping(value = "/checkForVersion", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody ArrayList<HashMap<String, String>> checkForVersion(

			@Valid @RequestParam(value="components") String components, // fetching the component value
			@Valid @RequestParam(value="jira_id") String jira_id) throws InterruptedException{ // fetching jira id
		
		ArrayList<HashMap<String, String>> version = new ArrayList<HashMap<String, String>>();
		CheckVersion cv = new CheckVersion();
		version=cv.checkForComponentVersion(components, jira_id);
		return version;
	}

}
