package us.dit.gestionRed.service.services.mapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import us.dit.gestionRed.model.SyslogMsj;

/**
* 	Servicio para mapear un JSON a un objeto SyslogMsj
*	@author Mariana Reyes Henriquez
*/
@Service
public class MapperJson2Syslog_Msj {
	private static final Logger logger = LogManager.getLogger();

	public SyslogMsj json2Signal(String json) {
		ObjectMapper objectMapper = new ObjectMapper();
		SyslogMsj syslog_msj = new SyslogMsj();

		try {
			syslog_msj = objectMapper.readValue(json, SyslogMsj.class);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return syslog_msj;
	}
}
