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


@Component("consultaLDAP")
public class ConsultaLDAP_WIH implements WorkItemHandler {
	private static final Logger logger = LogManager.getLogger();
	
	@Value("${gestionRed.ldap.server}")
	private String ldapServer;
	
	@Value("${gestionRed.ldap.port}")
	private int ldapPort;
	
	
	/**
	 * Método para realizar una consulta más compleja
	 * @param connection
	 * @throws Exception
	 */
	private static void queryWithSearchRequest(LdapNetworkConnection connection) throws Exception
    {
		logger.info("CONSULTA CON SEARCHREQUEST");
		LdapMsj msj_ldap = new LdapMsj();
		
        // Create the SearchRequest object
        SearchRequest req = new SearchRequestImpl();
        req.setScope( SearchScope.SUBTREE );
        req.addAttributes( "process_service", "servicePort", "dirIP", "os", "hostname_service", "sshPort", "sshPass" ); // ???
        req.setTimeLimit( 0 );
        req.setBase( new Dn( "ou=services,dc=capital") );
        req.setFilter( "(cn=postgres)" );

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
                    
                    Attribute process_service = entry.get("process_service");
    				Attribute servicePort = entry.get("servicePort");
    				Attribute dirIP_ldap = entry.get("dirIP");
    				Attribute os = entry.get("os");
    				Attribute hostname_service_ldap = entry.get("hostname_service");
    				Attribute sshPort = entry.get("sshPort");
    				Attribute sshPass = entry.get("sshPass");
    				
    				logger.info("El atributo process_service es: " + process_service);
    				
    				msj_ldap.setProcess_service(process_service.getString());
    				msj_ldap.setServicePort(Integer.parseInt(servicePort.getString()));
    				msj_ldap.setDirIP(dirIP_ldap.getString());
    				msj_ldap.setOs(os.getString());
    				msj_ldap.setHostname_service(hostname_service_ldap.getString());
    				msj_ldap.setSshPort(Integer.parseInt(sshPort.getString()));
    				msj_ldap.setSshPass(sshPass.getString());
    				
                    Collection<Attribute> allAttributes = entry.getAttributes();
					int count=1;
					for(Attribute att:allAttributes){
						logger.info("El atributo #" + count + " es: " + att);
						count++;
					}
                    logger.info( entry );
                }
            }
        }
    }
	

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		logger.info("Se está ejecutando ConsultaLDAP_WIH con los detalles de workItem " + workItem);
		
		Map<String,Object> parametros = workItem.getParameters();
		String dirIP = (String) parametros.get("dirIP");
		String service = (String) parametros.get("service");
		String hostname_service = (String) parametros.get("hostname_service");
		
		LdapMsj msj_ldap = new LdapMsj();
		
		logger.info("Direccion IP a comprobar: " + dirIP);
		
		// Realizar consula al servidor de LDAP
		LdapNetworkConnection connection = new LdapNetworkConnection(ldapServer, ldapPort);
		try {
			connection.bind();
			if (connection.isConnected())
				logger.info("Creada sesión");
			Dn dn=new Dn("ou=services,dc=capital");
			/**
			 * SearchScope.OBJECT : return the entry for a given DN, if it exists.	
			 * SearchScope.SUBTREE : return all the elements starting from the given DN, including the element associated with the DN, whatever the depth of the tree	
			 */
			EntryCursor cursor = connection.search(dn, "(objectclass=*)", SearchScope.SUBTREE);
			{
				for (Entry entry : cursor) {
					logger.info(entry);
					Attribute process_service = entry.get("process_service");
					Attribute servicePort = entry.get("servicePort");
					Attribute dirIP_ldap = entry.get("dirIP");
					Attribute os = entry.get("os");
					Attribute hostname_service_ldap = entry.get("hostname_service");
					Attribute sshPort = entry.get("sshPort");
					Attribute sshPass = entry.get("sshPass");
					
					logger.info("El atributo process_service es: " + process_service);
					
					msj_ldap.setProcess_service(process_service.getString());
					msj_ldap.setServicePort(Integer.parseInt(servicePort.getString()));
					msj_ldap.setDirIP(dirIP_ldap.getString());
					msj_ldap.setOs(os.getString());
					msj_ldap.setHostname_service(hostname_service_ldap.getString());
					msj_ldap.setSshPort(Integer.parseInt(sshPort.getString()));
					msj_ldap.setSshPass(sshPass.getString());
					
					
					Collection<Attribute> allAttributes = entry.getAttributes();
					int count=1;
					for(Attribute att:allAttributes){
						logger.info("El atributo #" + count + " es: " + att);
						count++;
					}
				}
				
			}
			
			//queryWithSearchRequest(connection);	
			connection.unBind();
			if (!connection.isConnected())
				logger.info("Cerrada sesión");
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

