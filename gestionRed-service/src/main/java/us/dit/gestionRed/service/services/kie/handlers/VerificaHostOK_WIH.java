package us.dit.gestionRed.service.services.kie.handlers;


import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.springframework.stereotype.Component;

/***
 * WIH para confirmar que se tiene acceso a una dirección IP
 * 
 * ~ ping
 */

@Component("verificaHostOK")
public class VerificaHostOK_WIH implements WorkItemHandler {
	private static final Logger logger = LogManager.getLogger();

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		logger.info("Ejecutando VerificaHostOK_WIH con los detalles de workItem " + workItem);
		
		Map<String,Object> parametros = workItem.getParameters();
		String dirIP = (String) parametros.get("dirIP");
		Boolean resultado = false;
        int timeout = 3000; // Tiempo de espera en milisegundos
		logger.info("Direccion IP a comprobar: " + dirIP);
		
		// Comprobar el acceso a la máquina
		try {
            InetAddress address = InetAddress.getByName(dirIP);
            resultado = address.isReachable(timeout);
        } catch (IOException e) {
        	logger.info("Ha habido algún error:" + e);
        }
		
		Map<String,Object> resultados = Map.of("accesoMaquina", resultado);
		logger.info("Resultado: "+ resultados.get("accesoMaquina"));
		manager.completeWorkItem(workItem.getId(), resultados);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

	}

}

