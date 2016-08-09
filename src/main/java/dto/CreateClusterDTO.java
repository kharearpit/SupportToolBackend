package dto;

public class CreateClusterDTO {

	private String cluster_name;
	private String os_type;
	private String ambari_version;
	private String nodes_count;
	private String domain_name;
	private String default_password;
	private String cluster_version;
	private String host_names;
	
	public String getCluster_name() {
		return cluster_name;
	}
	public void setCluster_name(String cluster_name) {
		this.cluster_name = cluster_name;
	}
	public String getOs_type() {
		return os_type;
	}
	public void setOs_type(String os_type) {
		this.os_type = os_type;
	}
	public String getAmbari_version() {
		return ambari_version;
	}
	public void setAmbari_version(String ambari_version) {
		this.ambari_version = ambari_version;
	}
	public String getNodes_count() {
		return nodes_count;
	}
	public void setNodes_count(String nodes_count) {
		System.out.println("nodes_count DTO: "+nodes_count);
		this.nodes_count = nodes_count;
	}
	public String getDomain_name() {
		return domain_name;
	}
	public void setDomain_name(String domain_name) {
		this.domain_name = domain_name;
	}
	public String getDefault_password() {
		return default_password;
	}
	public void setDefault_password(String default_password) {
		this.default_password = default_password;
	}
	public String getCluster_version() {
		return cluster_version;
	}
	public void setCluster_version(String cluster_version) {
		this.cluster_version = cluster_version;
	}
	public String getHost_names() {
		return host_names;
	}
	public void setHost_names(String host_names) {
		this.host_names = host_names;
	}

	
}
