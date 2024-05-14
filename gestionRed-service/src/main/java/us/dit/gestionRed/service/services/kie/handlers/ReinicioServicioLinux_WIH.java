package us.dit.gestionRed.service.services.kie.handlers;


import java.util.Map;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.springframework.stereotype.Component;

@Component("reinicioServicioLinux")
public class ReinicioServicioLinux_WIH implements WorkItemHandler {
	private static final Logger logger = LogManager.getLogger();

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		logger.info("Ejecutando ReinicioServicioLinux_WIH con los detalles de workItem " + workItem);
		
		Map<String,Object> parametros = workItem.getParameters();
		String dirIP = (String) parametros.get("dirIP");
		String process_service = (String) parametros.get("process_service");
		
		logger.info("Nos conectamos por ssh a: " + dirIP);
		logger.info("Reiniciamos el servicio: " + process_service);
		
		/***
		 * Si el servicio está apagado -> se levanta
		 * Si está encendido -> se apaga y se vuelve a levantar
		 */
		
		
		
		manager.completeWorkItem(workItem.getId(), null);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

	}

}

