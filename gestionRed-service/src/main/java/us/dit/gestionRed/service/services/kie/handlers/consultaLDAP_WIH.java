package us.dit.gestionRed.service.services.kie.handlers;


import java.util.Map;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.springframework.stereotype.Component;

@Component("consultaLDAP")
public class consultaLDAP_WIH implements WorkItemHandler {
	private static final Logger logger = LogManager.getLogger();

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		logger.info("Se está ejecutando consultaLDAP_WIH con los detalles de workItem " + workItem);
		
		Map<String,Object> parametros = workItem.getParameters();
		String dirIP = (String) parametros.get("dirIP");
		
		logger.info("Direccion IP a comprobar: " + dirIP);
		
		// Realizar consula al servidor de LDAP
		
		
		Map<String, Object> resultados = new HashMap<String, Object>();
		resultados.put("clave", "valor");
		manager.completeWorkItem(workItem.getId(), resultados);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

	}

}

