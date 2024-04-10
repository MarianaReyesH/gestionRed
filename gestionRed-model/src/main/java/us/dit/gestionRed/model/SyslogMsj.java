package us.dit.gestionRed.model;

import java.util.Date;

/**
 * Clase para mapear los json que llegan desde logstash
 */
public class SyslogMsj {
	private Date timestamp;
	private String hostname;
	private String process;
	private String pid;
	private String msj;


	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getMessage() {
		return this.msj;
	}

	public void setMessage(String msj) {
		this.msj = msj;
	}

}