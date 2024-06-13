/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.dit.gestionRed.service.services.kie.handlers;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.email.Connection;
import org.jbpm.process.workitem.email.Email;
import org.jbpm.process.workitem.email.SendHtml;
import org.jbpm.process.workitem.email.TemplateManager;
import org.jbpm.process.workitem.email.Message;
import org.jbpm.process.workitem.email.Recipient;
import org.jbpm.process.workitem.email.Recipients;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dit.gestionRed.service.conf.SmtpConfig;

/**
 * WorkItemHandler for sending email.
 * 
 */
@Component("verificaServicioMail")
public class VerificaServicioMail_WIH implements WorkItemHandler {
	private static final Logger logger = LogManager.getLogger();
	
	@Autowired
    private SmtpConfig emailConfig;
	
	private Connection connection;
	private TemplateManager templateManager = TemplateManager.get();
	

	public void setConnection(String host, String port, String userName, String password) {
		connection = new Connection();
		connection.setHost(host);
		connection.setPort(port);
		connection.setUserName(userName);
		connection.setPassword(password);
	}

	public Connection getConnection() {
		return connection;
	}

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		logger.info("Ejecutando VerificaServicioMail_WIH con los detalles de workItem " + workItem);
		
		Boolean resultado = false;

		Map<String, Object> parametros = workItem.getParameters();
		String dirIP = (String) parametros.get("dirIP");
		int servicePort = (int) parametros.get("servicePort");
		
		setConnection(dirIP, Integer.toString(servicePort), emailConfig.getUsername(), emailConfig.getPassword());
		
		try {
    		Email email = createEmail(workItem, connection);
    		logger.info("Se envia el correo...");
    		SendHtml.sendHtml(email, getDebugFlag(workItem));
    		logger.info("Correo enviado correctamente!");
    		resultado = true;
    	    
		} catch (Exception e) {
			System.out.println("Error al enviar el correo. Msj: " + e.getMessage() + ". Causa: " + e.getCause());
		}
		
		if (manager != null) {
	    	Map<String,Object> resultados = Map.of("estadoServicio", resultado);
	        logger.info("Resultado de sendHtml: " + resultado);
			manager.completeWorkItem(workItem.getId(), resultados);
	    }
	}

	protected Email createEmail(WorkItem workItem, Connection connection) {
	    Email email = new Email();
        Message message = new Message();
        message.setFrom((String) emailConfig.getFrom());

        // Set recipients
        Recipients recipients = new Recipients();
        String to = (String) emailConfig.getTo();
        if ( to == null || to.trim().length() == 0 ) {
            throw new RuntimeException( "Email must have one or more to adresses" );
        }
        for (String s: to.split(";")) {
            if (s != null && !"".equals(s)) {
                Recipient recipient = new Recipient();
                recipient.setEmail(s);
                recipient.setType( "To" );
                recipients.addRecipient(recipient);
            }
        }

        // Fill message
        String body = "Correo de verificación del servidor smtp";
        String template = (String) workItem.getParameter("Template");
        if (template != null) {            
            body = templateManager.render(template, workItem.getParameters());
        }
        
        message.setRecipients(recipients);
        message.setSubject("Prueba de verificación");
        message.setBody(body);

        // setup email
        email.setMessage(message);
        email.setConnection(connection);

        return email;
	}

	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {
	}

	protected boolean getDebugFlag(WorkItem workItem) {
	    Object debugParam  = workItem.getParameter("Debug");
	    if (debugParam == null) {
	        return false;
	    }
	    
	    return Boolean.parseBoolean(debugParam.toString());
	}

}