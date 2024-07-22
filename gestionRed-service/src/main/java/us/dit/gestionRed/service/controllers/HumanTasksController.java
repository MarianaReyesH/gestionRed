package us.dit.gestionRed.service.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jbpm.services.api.UserTaskService;
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


import javax.servlet.http.HttpSession;

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
     * @param session
     * @param model
     * @return				HTML donde se presentan todas las tareas pendientes
     */
    @GetMapping("/tareasPendientes")
    public String listarTareasPendientes(HttpSession session, Model model) {
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
     * @param model
     * @return					HTML donde se presenta detalle de la tarea
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
     * @param tareaHumana			Resultado del checkbox (Booleano)
     * @param msj_tareaHumana		Msj devuelto del gestor
     * @return						Volvemos al HTML donde se listan todas las tareas pendientes
     */
    @PostMapping("/completeTask/{taskId}")
    public String completeTask(@PathVariable Long taskId, @RequestParam(required = false) Boolean tareaHumana, @RequestParam(required = false) String msj_tareaHumana) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String userId = auth.getName();
		
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		
		try {
	        // Obtener el estado actual de la tarea
	        TaskInstance task = review.findById(taskId);
	        String status = task.getStatus();
	        logger.info("Estado de la tarea: " + status);

	        // Manejar diferentes estados de la tarea
	        if ("Ready".equals(status)) {
	            // Reclamar la tarea
	        	client.claimTask(containerId, taskId, userId);
	            logger.info("Tarea reclamada por el usuario: " + userId);
	            client.startTask(containerId, taskId, userId);
	            logger.info("Tarea iniciada por el usuario: " + userId);
	        } else if ("Reserved".equals(status) || "Created".equals(status)) {
	        	client.startTask(containerId, taskId, userId);
	            logger.info("Tarea iniciada por el usuario: " + userId);
	        }

	        // Verificar si la tarea está ahora en estado 'InProgress'
	        task = review.findById(taskId);
	        status = task.getStatus();
	        logger.info("Nuevo estado de la tarea: " + status);

	        if (!"InProgress".equals(status)) {
	            logger.error("No se puede completar la tarea porque no está en estado 'InProgress'. Estado actual: " + status);
	            throw new RuntimeException("No se puede completar la tarea porque no está en estado 'InProgress'");
	        }

	        // Crear el mapa de datos de salida
	        Map<String, Object> outputData = new HashMap<>();
	        outputData.put("tareaHumana", tareaHumana);
	        outputData.put("msj_tareaHumana", msj_tareaHumana);
	        logger.info("tareaHumana: " + tareaHumana);
	        logger.info("msj_tareaHumana: " + msj_tareaHumana);
	        
	        // Asignar valores por defecto si son nulos
	        if (tareaHumana == null) {
	            tareaHumana = false;
	        }
	        if (msj_tareaHumana == null) {
	            msj_tareaHumana = "No se ha enviado ningún msj.";
	        }

	        // Completar la tarea
	        client.completeTask(containerId, taskId, userId, null);
	        logger.info("Tarea completada por el usuario: " + userId);

	    } catch (Exception e) {
	        logger.error("Error al completar la tarea: " + e.getMessage());
	    }

	     
		return "redirect:/tareasPendientes";
    }
}
