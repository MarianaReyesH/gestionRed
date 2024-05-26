package us.dit.gestionRed.model;

/**
 * Clase para mapear el json que llega de consultaLDAP_WIH
 */
public class LdapMsj {
	private String process_service;
	private String dirIP;
	private String os;
	private String hostname_service;
	private int sshPort;
	private String sshPass;


	public String getProcess_service() {
		return process_service;
	}

	public void setProcess_service(String process_service) {
		this.process_service = process_service;
	}

	public String getDirIP() {
		return dirIP;
	}

	public void setDirIP(String dirIP) {
		this.dirIP = dirIP;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getHostname_service() {
		return hostname_service;
	}

	public void setHostname_service(String hostname_service) {
		this.hostname_service = hostname_service;
	}
	
	public int getSshPort() {
		return sshPort;
	}

	public void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}
	
	public String getSshPass() {
		return sshPass;
	}

	public void setSshPass(String sshPass) {
		this.sshPass = sshPass;
	}
}