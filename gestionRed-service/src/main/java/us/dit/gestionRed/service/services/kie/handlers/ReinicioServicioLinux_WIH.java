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

@Component("reinicioServicioLinux")
public class ReinicioServicioLinux_WIH implements WorkItemHandler {
	private static final Logger logger = LogManager.getLogger();

	/***
	 * Si el servicio está apagado -> se levanta Si está encendido -> se apaga y se
	 * vuelve a levantar
	 */
	public static void executeSshCommand(String ip, int port, String password, String command) {
		JSch jsch = new JSch();
		Session session = null;
		ChannelExec channel = null;

		try {
			// Configura la sesión SSH
			session = jsch.getSession("root", ip, port);
			session.setPassword(password);
			session.setConfig("StrictHostKeyChecking", "no");
			
			System.out.println("Conectando a la sesión SSH...");
            session.connect(60000); // Tiempo de espera de 30 segundos
            System.out.println("Conexión SSH establecida.");

			// Configura el canal para ejecutar comandos
			channel = (ChannelExec) session.openChannel("exec");

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
					System.out.println("Exit-status: " + channel.getExitStatus());
					break;
				}
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
		}

	}
	
	public static void manageSmtpService(String ip, int port, String password, boolean start) {
		String command = start ? "service postfix start" : "service postfix stop";
		executeSshCommand(ip, port, password, command);
	}

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		logger.info("Ejecutando ReinicioServicioLinux_WIH con los detalles de workItem " + workItem);

		Map<String, Object> parametros = workItem.getParameters();
		String dirIP = (String) parametros.get("dirIP");
		String process_service = (String) parametros.get("process_service");
		int sshPort = (int) parametros.get("sshPort");
		String sshPass = (String) parametros.get("sshPass");

		logger.info("Nos conectamos por ssh a: " + dirIP + " por el puerto: " + sshPort);
		logger.info("Apagamos el servicio: " + process_service);
		manageSmtpService(dirIP, sshPort, sshPass, false);
		logger.info("Encendemos el servicio: " + process_service);
		manageSmtpService(dirIP, sshPort, sshPass, true);

		manager.completeWorkItem(workItem.getId(), null);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

	}

}
