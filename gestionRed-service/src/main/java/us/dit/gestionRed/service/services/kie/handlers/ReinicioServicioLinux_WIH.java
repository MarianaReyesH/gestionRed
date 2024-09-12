package us.dit.gestionRed.service.services.kie.handlers;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;

/**
* 	WIH para reiniciar un servicio bajo el OS Linux
* 	Si el servicio está apagado -> se levanta 
* 	Si está encendido -> se apaga y se vuelve a levantar
*	@author Mariana Reyes Henriquez
*/
@Component("reinicioServicioLinux")
public class ReinicioServicioLinux_WIH implements WorkItemHandler {
	private static final Logger logger = LogManager.getLogger();

	/**
	 * 
	 * @param ip			Dirección de la máquina a gestionar
	 * @param port 			Puerto donde se está ejecutando el servicio ssh
	 * @param password		Contraseña para conectarse por ssh
	 * @param command		Comando a ejecutar en la máquina una vez nos hemos conectado por ssh
	 * @return				TRUE: si todo ha ido bien; FALSE: si la conexión ssh falla
	 */
	public static Boolean executeSshCommand(String ip, int port, String password, String command) {
		JSch jsch = new JSch();
		Session session = null;
		ChannelExec channel = null;
		Boolean resultado = false;

		try {
			// Configura la sesión SSH
			session = jsch.getSession("root", ip, port);
			session.setPassword(password);
			session.setConfig("StrictHostKeyChecking", "no");

			logger.info("Conectando a la sesión SSH...");
			session.connect(60000); // Tiempo de espera de 30 segundos
			logger.info("Conexión SSH establecida.");

			// Configura el canal para ejecutar comandos
			channel = (ChannelExec) session.openChannel("exec");

			logger.info("Ejecución del comando \"service <service> start/stop\"...");
			channel.setCommand(command);

			// Obtiene la salida del comando
			InputStream in = channel.getInputStream();
			channel.connect();

			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					System.out.print(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					if (in.available() > 0)
						continue;
					// Si la conexión ssh ha sido exitosa -> resultado = true
					if (channel.getExitStatus() == 0) {
						resultado = true;
					}
					logger.info("Exit-status: " + channel.getExitStatus());
					break;
				}
				Thread.sleep(1000);
			}

		} catch (Exception e) {
			// Si se produce cualquier error -> resultado = false
			logger.info("No se ha podido reiniciar el servicio");

		} finally {
			if (channel != null) {
				channel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
		}

		return resultado;
	}
	
	/**
	 * 
	 * @param ip				Dirección de la máquina a gestionar
	 * @param port 				Puerto donde se está ejecutando el servicio ssh
	 * @param password			Contraseña para conectarse por ssh
	 * @param process_service	Servicio a reiniciar
	 * @param start				TRUE: para encender; FALSE: para apagar
	 * @return					TRUE: si todo ha ido bien; FALSE: si la conexión ssh falla
	 */
	public static Boolean manageSmtpService(String ip, int port, String password, String process_service,
			boolean start) {
		String command = start ? "service " + process_service + " start" : "service " + process_service + " stop";
		Boolean resultado = false;
		resultado = executeSshCommand(ip, port, password, command);

		return resultado;
	}

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		logger.info("Ejecutando ReinicioServicioLinux_WIH con los detalles de workItem " + workItem);

		Map<String, Object> parametros = workItem.getParameters();
		String dirIP = (String) parametros.get("dirIP");
		String process_service = (String) parametros.get("process_service");
		int sshPort = (int) parametros.get("sshPort");
		String sshPass = (String) parametros.get("sshPass");

		Boolean resultadoApagar = false;
		Boolean resultadoEncender = false;

		logger.info("Nos conectamos por ssh a: " + dirIP + " por el puerto: " + sshPort);
		logger.info("Apagamos el servicio: " + process_service);
		resultadoApagar = manageSmtpService(dirIP, sshPort, sshPass, process_service, false);
		logger.info("Encendemos el servicio: " + process_service);
		resultadoEncender = manageSmtpService(dirIP, sshPort, sshPass, process_service, true);

		if (resultadoApagar == true && resultadoEncender == true) {
			Map<String, Object> resultados = Map.of("reinicioSSH", true);
			logger.info("Se ha podido establecer la conexión ssh y se ha reiniciado el servicio");
			manager.completeWorkItem(workItem.getId(), resultados);
		} else {
			Map<String, Object> resultados = Map.of("reinicioSSH", false);
			manager.completeWorkItem(workItem.getId(), resultados);
		}
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

	}

}
