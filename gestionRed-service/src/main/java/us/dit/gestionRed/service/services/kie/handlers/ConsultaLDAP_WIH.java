package us.dit.gestionRed.service.services.kie.handlers;


import java.util.Map;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Collection;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.directory.api.ldap.model.message.Response;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchResultEntry;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;

import us.dit.gestionRed.model.LdapMsj;

/**
* 	WIH para realizar una consulta al servidor LDAP
*	@author Mariana Reyes Henriquez
*/
@Component("consultaLDAP")
public class ConsultaLDAP_WIH implements WorkItemHandler {
	private static final Logger logger = LogManager.getLogger();
	
	@Value("${gestionRed.ldap.server}")
	private String ldapServer;
	
	@Value("${gestionRed.ldap.port}")
	private int ldapPort;
	
	@Value("${gestionRed.ldap.sshPass}")
	private String sshPass;
	

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		logger.info("Se está ejecutando ConsultaLDAP_WIH con los detalles de workItem " + workItem);
		
		Map<String,Object> parametros = workItem.getParameters();
		String service = (String) parametros.get("service");
		logger.info("service: " + service);
		LdapMsj msj_ldap = new LdapMsj();
		
		// Realizar consula al servidor de LDAP
		logger.info("ldapServer: " + ldapServer);
		logger.info("ldapPort: " + ldapPort);
		LdapNetworkConnection connection = new LdapNetworkConnection(ldapServer, ldapPort);
		try {
			connection.bind();
			if (connection.isConnected())
				logger.info("Creada sesión");
			Dn dn = new Dn("dc=capital");
			String cn = service.replace("service", "");
			logger.info("CN: " + cn);
			
			/**
			 * SearchScope.OBJECT : return the entry for a given DN, if it exists.	
			 * SearchScope.SUBTREE : return all the elements starting from the given DN, including the element associated with the DN, whatever the depth of the tree	
			 */
			logger.info("CONSULTA CON SEARCHREQUEST");
	        // Create the SearchRequest object
	        SearchRequest req = new SearchRequestImpl();
	        req.setScope( SearchScope.SUBTREE );
	        req.addAttributes( "jbpm-servicePort", "jbpm-dirIP", "jbpm-so-distribution", "jbpm-sshPort" );
	        req.setTimeLimit( 0 );
	        req.setBase( dn );
	        // Siempre llega: nombreservice (se queda con el nombre)
	        req.setFilter( "(cn=" + cn + ")" );

	        // Process the request
	        try ( SearchCursor searchCursor = connection.search(req) ) 
	        {
	            while ( searchCursor.next() )
	            {
	                Response response = searchCursor.get();
	                // process the SearchResultEntry
	                if ( response instanceof SearchResultEntry )
	                {
	                    Entry entry = ( ( SearchResultEntry ) response ).getEntry();
	                    
	    				Attribute servicePort = entry.get("jbpm-servicePort");
	    				Attribute dirIP = entry.get("jbpm-dirIP");
	    				Attribute os = entry.get("jbpm-so-distribution");
	    				//Attribute hostname_service = entry.get("hostname_service");
	    				Attribute sshPort = entry.get("jbpm-sshPort");
	    				
	    				logger.info(servicePort);
	    				logger.info(dirIP);
	    				logger.info(os);
	    				logger.info("hostname_service: " + " ");
	    				logger.info(sshPort);
	    				logger.info("sshPass: " + sshPass);
	    				
	    				msj_ldap.setProcess_service(cn);
	    				msj_ldap.setServicePort(Integer.parseInt(servicePort.getString()));
	    				msj_ldap.setDirIP(dirIP.getString());
	    				msj_ldap.setOs(os.getString());
	    				msj_ldap.setHostname_service(" ");
	    				msj_ldap.setSshPort(Integer.parseInt(sshPort.getString()));
	    				msj_ldap.setSshPass(sshPass);
	                }
	            }
	        }	
			connection.unBind();
			if (!connection.isConnected())
				logger.info("Cerrada sesión");
			connection.close();
		} catch (Exception e) {
			logger.info("Error iniciando sesión o haciendo consulta");
			e.printStackTrace();
		}
		
		
		Map<String, Object> resultados = new HashMap<String, Object>();
		resultados.put("msj_ldap", msj_ldap);
		manager.completeWorkItem(workItem.getId(), resultados);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

	}

}

