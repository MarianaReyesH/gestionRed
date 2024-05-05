package us.dit.gestionRed.service.services.kie.handlers;


import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.springframework.stereotype.Component;

@Component("ping")
public class pingWIH implements WorkItemHandler {
	private static final Logger logger = LogManager.getLogger();

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		logger.info("Se está ejecutando pingWIH con los detalles de workItem " + workItem);
		
		Map<String,Object> parametros = workItem.getParameters();
		logger.info("Direccion IP a comprobar: "+(String)parametros.get("dirIP"));
		Map<String,Object> resultados = Map.of("accesoMaquina", true);
		logger.info("Resultado del ping: "+ resultados.get("accesoMaquina"));
		manager.completeWorkItem(workItem.getId(), resultados);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

	}

}

