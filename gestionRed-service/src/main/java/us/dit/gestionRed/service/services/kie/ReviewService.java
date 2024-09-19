package us.dit.gestionRed.service.services.kie;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* 	Servicio para interaccionar con los métodos de UserTaskServicesClient
*	@author Mariana Reyes Henriquez
*/
@Service
public class ReviewService {
	private static final Logger logger = LogManager.getLogger();
	
	@Autowired
	private KieUtilService kie;
	
	/**
	 * 
	 * @param principal			Usuario con el que se ha iniciado sesión
	 * @return					Lista de tareas pendientes por principal
	 */
	public List<TaskSummary> findTasksToReview(String principal) {
		logger.info("En findTasksToReview con principal = " + principal);

		List<TaskSummary> taskList = null;

		try {
            UserTaskServicesClient client = kie.getUserTaskServicesClient();
            logger.info("Llamo a FINDTASKS de UserTaskServicesClient con principal= " + principal);

            taskList = client.findTasksAssignedAsPotentialOwner(principal, 0, 0);
            logger.info("Numero de tareas encontradas: " + (taskList != null ? taskList.size() : 0));

            for (TaskSummary task : taskList) {
                logger.info("Tarea: " + task);
            }
        } catch (Exception e) {
            logger.error("Error en findTasksToReview", e);
        }
		
		logger.info("Termino findTasksToReview");
		return taskList;
	}
	
	
	/**
	 * 
	 * @param taskId			Identificador de la tarea a buscar
	 * @return					Instancia de la tarea con id = taskId
	 */
	public TaskInstance findById(Long taskId) {
		logger.info("En findById de ReviewService");

		TaskInstance task = null;

		try {
            UserTaskServicesClient client = kie.getUserTaskServicesClient();
            task = client.findTaskById(taskId);
        } catch (Exception e) {
            logger.error("Error en findById", e);
        }

		return task;
	}
}
