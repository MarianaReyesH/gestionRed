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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/***
 * WIH para confirmar el correcto funcionamiento del Servidor Web NGINX
 * 
 * Se envía una petición HTTP y se espera una respuesta 200 OK
 */

@Component("verificaServicioNginx")
public class VerificaServicioNginx_WIH implements WorkItemHandler {
	private static final Logger logger = LogManager.getLogger();

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		logger.info("Ejecutando VerificaServicioNginx_WIH con los detalles de workItem " + workItem);
		
		Map<String,Object> parametros = workItem.getParameters();
		String dirIP = (String) parametros.get("dirIP");
		int servicePort = (int) parametros.get("servicePort");
		
		Boolean resultado = false;
		
		try {
            // URL del servidor Nginx en el contenedor
            URL url = new URL("http://" + dirIP + ":" + servicePort);
            logger.info("Nos conectamos al servidor. URL: "+ url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Método de petición GET
            connection.setRequestMethod("GET");
            
            // Leer la respuesta
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            
            // Cerrar conexiones
            in.close();
            connection.disconnect();
            
            // Imprimir la respuesta
            System.out.println(content.toString());
            resultado = true;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		Map<String,Object> resultados = Map.of("estadoServicio", resultado);
		logger.info("Resultado: "+ resultados.get("estadoServicio"));
		manager.completeWorkItem(workItem.getId(), resultados);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

	}

}

