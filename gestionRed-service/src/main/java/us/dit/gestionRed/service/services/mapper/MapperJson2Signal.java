package us.dit.gestionRed.service.services.mapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import us.dit.gestionRed.model.Signal;
import us.dit.gestionRed.model.SyslogMsj;

@Service
public class MapperJson2Signal {
	private static final Logger logger = LogManager.getLogger();

	public Signal json2Signal(String json) {
		ObjectMapper objectMapper = new ObjectMapper();
		Signal signal = new Signal();

		try {
			SyslogMsj syslog_msj = objectMapper.readValue(json, SyslogMsj.class);
			signal.setMsj_logstash(syslog_msj);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return signal;
	}
}
