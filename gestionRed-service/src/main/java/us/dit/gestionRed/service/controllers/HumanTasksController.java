package us.dit.gestionRed.service.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import us.dit.gestionRed.service.services.kie.KieUtilService;
import us.dit.gestionRed.service.services.kie.ReviewService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
* 	Controlador para la gestión de las tareas humanas
*	@author Mariana Reyes Henriquez
*/
@Controller
public class HumanTasksController {
	private static final Logger logger = LogManager.getLogger();

    @Autowired
	private ReviewService review;
    
    @Autowired
    private KieUtilService kie;
    
    private String containerId = "gestionRed-kjar-1_0-SNAPSHOT";
    
    @GetMapping()
	public String menu() {
    	return "menuPrincipal";
	}
    
    /**
     * 
     * @param model			Utilizado para añadir atributos al modelo
     * @return				HTML donde se presentan todas las tareas pendientes
     */
    @GetMapping("/tareasPendientes")
    public String listarTareasPendientes(Model model) {
    	logger.info("Buscando todas las tareas pendientes del usuario...");
    	
    	List<TaskSummary> tasksList = null;
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		logger.info("Datos de usuario (principal): " + auth.getName());
		
        tasksList = review.findTasksToReview(auth.getName());
        logger.info("Lista de tareas: " + tasksList);
        model.addAttribute("tasks", tasksList);
        logger.info("Vuelve de consultar tareas");
		return "myTasks";
    }
    
    /**
     * 
     * @param taskId			Identificador de la tarea que se selecciona en el HTML
     * @param model				Utilizado para añadir atributos al modelo
     * @return					HTML donde se presenta el detalle de la tarea
     */
    @GetMapping("/tareaPendiente/{taskId}")
    public String listarTareaPendienteById(@PathVariable Long taskId, Model model) {
    	logger.info("Buscando la tarea " + taskId);
    	UserTaskServicesClient client = kie.getUserTaskServicesClient();
    	
    	Map<String, Object> inputData = client.getTaskInputContentByTaskId(containerId, taskId);
        
        model.addAttribute("dirIP", inputData.get("dirIP"));
        model.addAttribute("process_service", inputData.get("process_service"));
        model.addAttribute("sshPort", inputData.get("sshPort"));
        model.addAttribute("sshPass", inputData.get("sshPass"));
        model.addAttribute("os", inputData.get("os"));
        model.addAttribute("servicePort", inputData.get("servicePort"));
        model.addAttribute("taskId", taskId);
    	
		TaskInstance task = review.findById(taskId);
		
		logger.info("Tarea localizada " + task);
		model.addAttribute("task", task);
		return "task";
    }

    /**
     * 
     * @param taskId				Identificador de la tarea seleccionada para completar
     * @param tareaHumana			Resultado del checkbox (Booleano)indicando si se ha completado la tarea por parte del administrador de la red
     * @param msj_tareaHumana		Msj devuelto por el administrador de la red
     * @return						Volvemos al HTML donde se listan todas las tareas pendientes
     */
    @PostMapping("/completeTask/{taskId}")
    public String completeTask(@PathVariable Long taskId, @RequestParam(required = false) Boolean tareaHumana, @RequestParam(required = false) String msj_tareaHumana) {
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		logger.info("Datos de usuario (principal): " + auth.getName());
		
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		
		try {	        
	        // Asignar valores por defecto si no se marcan/rellenan
	        if (tareaHumana == null) {
	            tareaHumana = false;
	        }
	        if (msj_tareaHumana == null) {
	            msj_tareaHumana = "No se ha enviado ningún msj.";
	        }
	        
	        // Crear el mapa de datos de salida
	        Map<String, Object> outputData = new HashMap<String, Object>();
	        outputData.put("tareaHumana", tareaHumana);
	        outputData.put("msj_tareaHumana", msj_tareaHumana);
	        logger.info("tareaHumana: " + tareaHumana);
	        logger.info("msj_tareaHumana: " + msj_tareaHumana);

	        // Completar la tarea
	        client.startTask(containerId, taskId, auth.getName());
	        client.completeTask(containerId, taskId, auth.getName(), outputData);
	        logger.info("Tarea completada por el usuario: " + auth.getName());

	    } catch (Exception e) {
	        logger.error("Error al completar la tarea: " + e.getMessage());
	    }

	     
		return "redirect:/tareasPendientes";
    }
}
