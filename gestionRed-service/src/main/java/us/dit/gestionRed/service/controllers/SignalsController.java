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

import us.dit.gestionRed.model.Signal;
import us.dit.gestionRed.service.services.kie.KieUtilService;
import us.dit.gestionRed.service.services.mapper.MapperJson2Signal;

/**
 *
 */
@RestController
@RequestMapping("/signals")
public class SignalsController {
	private static final Logger logger = LogManager.getLogger();

	@Autowired
	private KieUtilService kie;

	@Autowired
	private MapperJson2Signal mapper;

	@PostMapping()
	public String sendSignal(@RequestBody String msj_logstash, HttpSession session) {
		/**
		 * Difunde una señal por todos los servidores KIE gestionados en la aplicación
		 */
		logger.info("LLEGA EL MSJ DEL LOGSTASH:" + msj_logstash);

		// Se mapea el msj que llega de logstash (un json) al objeto Java Signal
		Signal signal = mapper.json2Signal(msj_logstash);

		// Imprime los valores mapeados
		logger.info("Signal Name: " + signal.getSignalName());
		logger.info("Timestamp: " + signal.getMsj_logstash().getTimestamp());
		logger.info("Hostname: " + signal.getMsj_logstash().getHostname());
		logger.info("Process: " + signal.getMsj_logstash().getProcess());
		logger.info("Pid: " + signal.getMsj_logstash().getPid());
		logger.info("Message: " + signal.getMsj_logstash().getMessage());
//
//		// Se envía la señal al motor KIE
//		logger.info("Enviando una señal al motor KIE");
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//		UserDetails principal = (UserDetails) auth.getPrincipal();
//		logger.info("Datos de usuario (principal)" + principal);
//
//		kie.sendSignal(signal.getSignalName(), signal.getMsj_logstash());

		// Prueba del proceso comunicacionSeñalCorreo
//		kie.sendSignal("SignalCorreos", msj_logstash);

		return "OK";
	}
}