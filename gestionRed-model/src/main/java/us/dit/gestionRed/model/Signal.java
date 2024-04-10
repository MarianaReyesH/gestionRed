package us.dit.gestionRed.model;


/**
 * Clase para definir los mensajes que se incluyen en las señales
 */
public class Signal {
	private String signalName;
	private SyslogMsj msj_logstash;

	public String getSignalName() {
		return signalName;
	}

	public void setSignalName(String signalName) {
		this.signalName = signalName;
	}

	public SyslogMsj getMsj_logstash() {
		return msj_logstash;
	}

	public void setMsj_logstash(SyslogMsj msj_logstash) {
		this.msj_logstash = msj_logstash;
	}

}