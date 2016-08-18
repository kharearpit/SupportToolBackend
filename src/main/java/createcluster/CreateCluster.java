package createcluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.util.http.fileupload.IOUtils;

import dto.CreateClusterDTO;

public class CreateCluster {

	public ArrayList<HashMap<String,String>> createCluster(CreateClusterDTO createClusterDTO,HttpSession session,HttpServletResponse res) throws InterruptedException{
		

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
        System.out.println("utils_version: "+createClusterDTO.getUtils_version());
        System.out.println("flavor_name: "+createClusterDTO.getFlavor_name());
        System.out.println("keypair_name: "+createClusterDTO.getKeypair_name());
        System.out.println("pvt_keyfile: "+createClusterDTO.getPvt_keyfile());
        System.out.println("project_name: "+createClusterDTO.getProject_name());
        System.out.println("okta_id: "+createClusterDTO.getOkta_id());
        
        int hostCount = Integer.parseInt(createClusterDTO.getNodes_count());
        System.out.println("hostCount: "+hostCount);
        String time="";
        
        try {
			Process process = Runtime.getRuntime().exec("/root/support-tools/backend/create-cluster/gitPull.sh");
			//check if git pull was success or not
			if(process.waitFor()==0){

				Date currentTime = new Date();

		        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
		         time = dateFormat.format(currentTime);
		        System.out.println(time);
		        
		        String clusterPropFileName="cluster"+time+".props";
		        String openstackCliSupport="openstack_cli_support"+time+".sh";
		        
		        System.out.println("fileName: "+clusterPropFileName);
		        
				//creating variables equivalent to the lines in .props file
				String clusterName="CLUSTERNAME="+createClusterDTO.getCluster_name();
				String OS="OS="+createClusterDTO.getOs_type();
				String ambariVersion="AMBARIVERSION="+createClusterDTO.getAmbari_version();
				String node_count="NUM_OF_NODES="+createClusterDTO.getNodes_count();
				String domainName="DOMAIN_NAME="+createClusterDTO.getDomain_name();
				String password="DEFAULT_PASSWORD="+createClusterDTO.getDefault_password();
				String clusterVersion="CLUSTER_VERSION="+createClusterDTO.getCluster_version();
				String utilVersion="UTILS_VERSION="+createClusterDTO.getUtils_version();
				String flavorName="FLAVOR_NAME="+createClusterDTO.getFlavor_name();
				String keyPairName="KEYPAIR_NAME="+createClusterDTO.getKeypair_name();
				String pvtFile="PVT_KEYFILE="+createClusterDTO.getPvt_keyfile();
				String repoVersionIP="172.26.64.249";
				String repoVersion="REPO_SERVER="+repoVersionIP;
				//get hostnames
				String hostArray[]=createClusterDTO.getHost_names().split(",");
				
				//write to the file		
				PrintWriter writer = new PrintWriter("/root/support-tools/backend/create-cluster/props_files/"+clusterPropFileName, "UTF-8");
				writer.println(clusterName);
				writer.println(OS);
				writer.println(ambariVersion);
				writer.println(node_count);
				writer.println(domainName);
				writer.println(password);
				writer.println(clusterVersion);
				writer.println(utilVersion);
				writer.println(flavorName);
				writer.println(keyPairName);
				writer.println(pvtFile);
				
				for (int i = 0; i < hostArray.length; i++) {
					
					writer.println("HOST"+(i+1)+"="+hostArray[i]);
				}
				writer.println(repoVersion);
				
				writer.println("HOST1_SERVICES=\"NAMENODE,NODEMANAGER,DATANODE,ZOOKEEPER_CLIENT,HDFS_CLIENT,YARN_CLIENT,MAPREDUCE2_CLIENT,ZOOKEEPER_SERVER\"");
				writer.println("HOST2_SERVICES=\"SECONDARY_NAMENODE,NODEMANAGER,DATANODE,ZOOKEEPER_CLIENT,ZOOKEEPER_SERVER,HDFS_CLIENT,YARN_CLIENT,MAPREDUCE2_CLIENT\"");
				writer.println("HOST3_SERVICES=\"RESOURCEMANAGER,APP_TIMELINE_SERVER,HISTORYSERVER,NODEMANAGER,DATANODE,ZOOKEEPER_CLIENT,ZOOKEEPER_SERVER,HDFS_CLIENT,YARN_CLIENT,MAPREDUCE2_CLIENT\"");
				
				if(hostCount>3){
					
					for (int i = 0; i < hostCount-3; i++) {
						int count=4+i;
						writer.println("HOST"+count+"_SERVICES=\"NODEMANAGER,DATANODE,ZOOKEEPER_CLIENT,HDFS_CLIENT,YARN_CLIENT\"");
					}	
				}

				writer.close();
				
				//create file: openstack_cli_support.sh
				PrintWriter writerOpenStack = new PrintWriter("/root/support-tools/backend/create-cluster/props_files/"+openstackCliSupport, "UTF-8");
				BufferedReader br = null;
		        String strLine = "";
		        try {
		            br = new BufferedReader( new FileReader("/root/support-tools/backend/create-cluster/useful-scripts/hdp-automated-setup/openstack_cli_support.sh"));
		            while( (strLine = br.readLine()) != null){
		                if(strLine.contains("export OS_TENANT_NAME")){
		                	String tenant_name="export OS_TENANT_NAME="+"\""+createClusterDTO.getProject_name()+"\"";
		                	writerOpenStack.println(tenant_name);
		                }
		                else if(strLine.contains("export OS_PROJECT_NAME")){
		                	String project_name="export OS_PROJECT_NAME="+"\""+createClusterDTO.getProject_name()+"\"";
		                	writerOpenStack.println(project_name);
		                }
		                else if(strLine.contains("export OS_USERNAME")){
		                	String os_name="export OS_USERNAME="+"\""+createClusterDTO.getOkta_id()+"\"";
		                	writerOpenStack.println(os_name);
		                }
		                else
		                	writerOpenStack.println(strLine);
		                
		            }
		        } catch (FileNotFoundException e) {
		            System.err.println("Unable to find the file: fileName");
		        } catch (IOException e) {
		            System.err.println("Unable to read the file: fileName");
		        }
		        finally {
					writerOpenStack.close();
				}
				
				String zipFile = "/root/support-tools/backend/create-cluster/props_files/Hwx-"+time+".zip";
				//files to zip
		        String[] srcFiles = { "/root/support-tools/backend/create-cluster/useful-scripts/hdp-automated-setup/generate_json.sh",
		        		              "/root/support-tools/backend/create-cluster/useful-scripts/hdp-automated-setup/create_cluster.sh",
		        		              "/root/support-tools/backend/create-cluster/useful-scripts/hdp-automated-setup/setup_cluster.sh", 
		        		              "/root/support-tools/backend/create-cluster/props_files/"+clusterPropFileName,
		        		              "/root/support-tools/backend/create-cluster/props_files/"+openstackCliSupport };

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
					String zippedFileName="Hwx.zip";
					//fetching file from a location
					String filePathToBeServed =zipFile; //complete file name with path;
					//making file object
					File fileToDownload = new File(filePathToBeServed);
					InputStream inputStream = new FileInputStream(fileToDownload);
					res.setContentType("application/zip");
					res.setHeader("Content-Disposition", "attachment; filename="+zippedFileName); 
					IOUtils.copy(inputStream, res.getOutputStream());
					//sending the file to the client for download
					res.flushBuffer();
					//closing the session
					inputStream.close();
					hm.put("result", "command");
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
        
        try {System.out.println(time);
        String param = "*"+time+"*";
        System.out.println("param: "+param);
     		Process process = Runtime.getRuntime().exec("/root/support-tools/backend/create-cluster/removeFilesAfterDownload.sh"+" "+param);
     		if(process.waitFor()==0){System.out.println("success");}
     		else{
     			System.out.println("failed");
     			
     		}
     	} catch (IOException e) {
     		// TODO Auto-generated catch block
     		e.printStackTrace();
     	}
        downloadArrayList.add(hm);
        return downloadArrayList;
     
    
	}
}
