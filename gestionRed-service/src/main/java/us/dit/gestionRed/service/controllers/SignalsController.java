/**
 *
 */
package us.dit.gestionRed.service.controllers;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import us.dit.gestionRed.model.SyslogMsj;
import us.dit.gestionRed.service.services.kie.KieUtilService;
import us.dit.gestionRed.service.services.mapper.MapperJson2Syslog_Msj;

/**
 * Controlador que captura las señales de gestión
 */
@RestController
@RequestMapping("/signals")
public class SignalsController {
	private static final Logger logger = LogManager.getLogger();

	@Autowired
	private KieUtilService kie;

	@Autowired
	private MapperJson2Syslog_Msj mapper;

	@PostMapping()
	public String sendSignal(@RequestBody String msj_logstash, HttpSession session) {
		/**
		 * Difunde una señal por todos los servidores KIE gestionados en la aplicación
		 */
		logger.info("LLEGA EL MSJ DEL LOGSTASH:" + msj_logstash + "\r\n");

		// Se mapea el msj que llega de logstash (un json) al objeto Java Signal
		SyslogMsj syslog_msj = mapper.json2Signal(msj_logstash);

		// Imprime los valores mapeados
		logger.info("Timestamp: " + syslog_msj.getTimestamp() + "\r\n");
		logger.info("Hostname_client: " + syslog_msj.getHostname_client() + "\r\n");
		logger.info("Process: " + syslog_msj.getProcess() + "\r\n");
		logger.info("Pid: " + syslog_msj.getPid() + "\r\n");
		logger.info("Service: " + syslog_msj.getService() + "\r\n");
		logger.info("Hostname_service: " + syslog_msj.getHostname_service() + "\r\n");
		logger.info("Msj_error: " + syslog_msj.getMsj_error() + "\r\n");

		// Se envía la señal al motor KIE
		logger.info("Enviando una señal al motor KIE");
		kie.sendSignal("signalErrorRed", syslog_msj);

		return "OK";
	}
}