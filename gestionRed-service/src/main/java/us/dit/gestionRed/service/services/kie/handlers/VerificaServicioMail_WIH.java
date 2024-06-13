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
	
	private final static  Properties props = new Properties();
	private static  Session session;

	/***
	 * Se envía un correo electrónico para comprobar el correcto funcionamiento del servidor SMTP
	 */
	public static Boolean sendEmailSMTP(String dirIP, int servicePort, SmtpConfig emailConfig) {
		logger.info("Configuramos las propiedades relativas al servidor SMTP");
		
		Boolean resultado = false;
		Transport transport = null;
		
		// Configuración de las propiedades
		props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.auth.mechanisms", "LOGIN PLAIN");
        //props.put("mail.smtp.ssl.trust", dirIP);
        props.put("mail.smtp.ssl.enable", "false");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", dirIP);
        props.put("mail.smtp.port", String.valueOf(servicePort));
        props.put("mail.smtp.user", emailConfig.getUsername());
        props.put("mail.smtp.password", emailConfig.getPassword());
        
        // Habilitar debug para más detalles
        props.put("mail.debug", "true");
        
        logger.info("Propiedades configuradas");

        // Autenticación
        final String username = emailConfig.getUsername();
        final String password = emailConfig.getPassword();
        
        logger.info("Establecemos la sesion con el servidor smpt");
        
        session = Session.getInstance(props, new Authenticator() {
		    @Override
		    protected PasswordAuthentication getPasswordAuthentication() {
		        return new PasswordAuthentication(username, password);
		    }
		});
       
        logger.info("Sesion establecida");

        try {
            // Crear un objeto MimeMessage
        	Message message = new MimeMessage(session);
        	message.setFrom(new InternetAddress(emailConfig.getFrom()));
        	message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailConfig.getTo()));
			message.setSubject("Prueba");
			message.setText("Texto");
			
			logger.info("Msj MIME creado, enviamos el correo");
			
			transport = session.getTransport("smtp");
			logger.info("Conectando con el servidor smtp...");
			transport.connect();
			
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			
			logger.info("Enviando email...");
			transport.sendMessage(message, message.getAllRecipients());
			logger.info("Correo enviado exitosamente!");
			resultado = true;

        } catch (MessagingException  e) {
        	System.out.println("Error al enviar el correo: " + e.getMessage());
    		
        } finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}
		}
		
		return resultado;
	}
	
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		logger.info("Ejecutando VerificaServicioMail_WIH con los detalles de workItem " + workItem);

		Map<String, Object> parametros = workItem.getParameters();
		String dirIP = (String) parametros.get("dirIP");
		int servicePort = (int) parametros.get("servicePort");
		
		logger.info("Llamamos a sendEmailSMTP");
		Boolean resultado = sendEmailSMTP(dirIP, servicePort, emailConfig);
	
		Map<String,Object> resultados = Map.of("estadoServicio", resultado);
        logger.info("Resultado de sendEmailSMTP: " + resultado);
		manager.completeWorkItem(workItem.getId(), resultados);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

	}

}
