package us.dit.gestionRed.model;


/**
 * Clase para definir los mensajes que se incluyen en las señales
 */
public class Signal{
	private String signalName;
	private Object message;	


	public Object getMessage() {
		return this.message;
	}
	public void setMessage(Object message) {
		this.message = message;
	}

	public String getName() {
		return signalName;
	}
	public void setName(String signalName) {
		this.signalName = signalName;
	}	

}