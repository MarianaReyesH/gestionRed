package us.dit.gestionRed.service.services.kie.handlers;

import java.util.Map;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.springframework.stereotype.Component;

import us.dit.gestionRed.service.conf.SmtpConfig;

import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.*;
import javax.mail.internet.*;

@Component("verificaServicioMail")
public class VerificaServicioMail_WIH implements WorkItemHandler {
	private static final Logger logger = LogManager.getLogger();
	
	@Autowired
    private SmtpConfig emailConfig;

	/***
	 * Se envía un correo electrónico para comprobar el correcto funcionamiento del servidor SMTP
	 */
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		logger.info("Ejecutando VerificaServicioMail_WIH con los detalles de workItem " + workItem);

		Map<String, Object> parametros = workItem.getParameters();
		String dirIP = (String) parametros.get("dirIP");
		int servicePort = (int) parametros.get("servicePort");

		logger.info("Configuramos las propiedades relativas al servidor SMTP");
		
		// Configuración de las propiedades
        Properties props = new Properties();
        
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", dirIP);
        props.put("mail.smtp.port", String.valueOf(servicePort));
        props.put("mail.smtp.mail.sender", emailConfig.getFrom());
        props.put("mail.smtp.user", emailConfig.getUsername());
        
        logger.info("Propiedades configuradas");

        // Autenticación
        //final String username = emailConfig.getUsername();
        //final String password = emailConfig.getPassword();
        
        logger.info("Establecemos la sesion con el servidor smpt");
        
        Session session = Session.getDefaultInstance(props);
        
//        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(username, password);
//            }
//        });
//        
        logger.info("Sesion establecida");

        try {
            // Crear un objeto MimeMessage
        	MimeMessage message = new MimeMessage(session);
        	message.setFrom(new InternetAddress((String)props.get("mail.smtp.mail.sender")));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailConfig.getTo()));
			message.setSubject("Prueba");
			message.setText("Texto");
			
			logger.info("Msj MIME creado, enviamos el correo...");
			
			Transport t = session.getTransport("smtp");
			t.connect((String)props.get("mail.smtp.user"), emailConfig.getPassword());
			t.sendMessage(message, message.getAllRecipients());
			Thread.sleep(1000);
			t.close();
        	
			
        	
        	
        	
//        	MimeMessage message = new MimeMessage(session);
//        	message.addHeader("Content-type", "text/HTML; charset=UTF-8");
//        	message.addHeader("format", "flowed");
//        	message.addHeader("Content-Transfer-Encoding", "8bit");
//        	
//            message.setFrom(new InternetAddress(emailConfig.getFrom()));
//            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailConfig.getTo()));
//            message.setSubject("Test SMPT server", "UTF-8");
//            message.setText("Correo enviado a través del servidor smpt!", "UTF-8");
//
//            logger.info("Msj MIME creado, enviamos el correo...");
//            
//            // Enviar el correo
//            Transport.send(message);
            
            logger.info("llega???");

        } catch (Exception  e) {
        	Map<String,Object> resultados = Map.of("estadoServicio", false);
        	System.out.println("Error al enviar el correo: " + e.getMessage());
    		manager.completeWorkItem(workItem.getId(), resultados);
    		
    		e.printStackTrace();
            //throw new RuntimeException(e);
        }

		Map<String,Object> resultados = Map.of("estadoServicio", true);
		logger.info("Correo enviado exitosamente!");
		manager.completeWorkItem(workItem.getId(), resultados);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

	}

}
