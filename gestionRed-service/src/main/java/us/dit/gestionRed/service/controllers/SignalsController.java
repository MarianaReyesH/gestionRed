/**
 * 
 */
package us.dit.gestionRed.service.controllers;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import us.dit.gestionRed.service.services.kie.KieUtilService;
//import us.dit.gestionRed.model.Signal;      // da un error al lanzar la app (no se puede acceder a us.dit.model)


/**
 * 
 */
@RestController
@RequestMapping("/signals")
public class SignalsController {
	private static final Logger logger = LogManager.getLogger();	

	@Autowired
	private KieUtilService kie;
		
	@PostMapping()	
	public String sendSignal(@RequestBody String signal,HttpSession session) {
		/**
		 * Difunde una señal por todos los servidores KIE gestionados en la aplicación
		 */
//		logger.info("Enviando una señal a todos");
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//		UserDetails principal = (UserDetails) auth.getPrincipal();
//		logger.info("Datos de usuario (principal)" + principal);
		logger.info("LLEGA EL MSJ DEL LOGSTASH:" + signal);
		//kie.sendSignal(signal.getName(), signal.getMessage());	@RequestBody Signal signal	
		return "OK";  // esto habría que mirarlo
		}
}