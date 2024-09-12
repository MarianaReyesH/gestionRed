package us.dit.gestionRed.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Clase para mapear los json que llegan desde logstash
 * @author Mariana Reyes Henriquez
 */
public class SyslogMsj implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Date timestamp;
	private String hostname_client;
	private String process_client;
	private String pid;
	private String service;
	private String hostname_service;
	private String msj_error;


	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getHostname_client() {
		return hostname_client;
	}

	public void setHostname_client(String hostname_client) {
		this.hostname_client = hostname_client;
	}

	public String getProcess() {
		return process_client;
	}

	public void setProcess(String process_client) {
		this.process_client = process_client;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getHostname_service() {
		return hostname_service;
	}

	public void setHostname_service(String hostname_service) {
		this.hostname_service = hostname_service;
	}

	public String getMsj_error() {
		return msj_error;
	}

	public void setMsj_error(String msj_error) {
		this.msj_error = msj_error;
	}

	

}