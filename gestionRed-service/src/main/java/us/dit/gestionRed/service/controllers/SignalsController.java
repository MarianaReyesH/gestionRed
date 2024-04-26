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
		logger.info("LLEGA EL MSJ DEL LOGSTASH:" + msj_logstash + "\r\n");

		// Se mapea el msj que llega de logstash (un json) al objeto Java Signal
		Signal signal = mapper.json2Signal(msj_logstash);

		// Se le asigna un nombre a la señal -> proceso en el servidor kie que lo "escuchará"
		signal.setSignalName("signalGestionRed");

		// Imprime los valores mapeados
		logger.info("Signal Name: " + signal.getSignalName() + "\r\n");
		logger.info("Timestamp: " + signal.getMsj_logstash().getTimestamp() + "\r\n");
		logger.info("Hostname: " + signal.getMsj_logstash().getHostname() + "\r\n");
		logger.info("Process: " + signal.getMsj_logstash().getProcess() + "\r\n");
		logger.info("Pid: " + signal.getMsj_logstash().getPid() + "\r\n");
		logger.info("Msj: " + signal.getMsj_logstash().getMsj() + "\r\n");

		// Se envía la señal al motor KIE
		logger.info("Enviando una señal al motor KIE");
		kie.sendSignal(signal.getSignalName(), signal.getMsj_logstash());

		return "OK";
	}
}