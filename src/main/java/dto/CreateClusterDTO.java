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
	private String project_name;
	private String utils_version;
	private String flavor_name;
	private String keypair_name;
	private String pvt_keyfile;
	private String okta_id;
	
	
	public String getOkta_id() {
		return okta_id;
	}
	public void setOkta_id(String okta_id) {
		this.okta_id = okta_id;
	}
	public String getUtils_version() {
		return utils_version;
	}
	public void setUtils_version(String utils_version) {
		this.utils_version = utils_version;
	}
	
	public String getFlavor_name() {
		return flavor_name;
	}
	public void setFlavor_name(String flavor_name) {
		this.flavor_name = flavor_name;
	}
	public String getKeypair_name() {
		return keypair_name;
	}
	public void setKeypair_name(String keypair_name) {
		this.keypair_name = keypair_name;
	}
	public String getPvt_keyfile() {
		return pvt_keyfile;
	}
	public void setPvt_keyfile(String pvt_keyfile) {
		this.pvt_keyfile = pvt_keyfile;
	}
	public String getProject_name() {
		return project_name;
	}
	public void setProject_name(String project_name) {
		this.project_name = project_name;
	}
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
