package us.dit.gestionRed.service.services.kie;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
	private static final Logger logger = LogManager.getLogger();
	
	@Autowired
	private KieUtilService kie;
	
	public List<TaskSummary> findTasksToReview(String principal, String proceso) {
		logger.info("En findTasksToReview con principal= "+principal);

		List<TaskSummary> taskList = null;
		List<TaskSummary> reservedRevisions = new ArrayList<TaskSummary>();

		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		logger.info("Llamo a FINDTASKS de UserTaskServicesClient con principal= " + principal);
		
		taskList = client.findTasks(principal, 0, 0);
		for(TaskSummary summary:taskList) {
			logger.info("Tarea en estado " + summary.getStatus() + " y del proceso " + summary.getProcessId());
			if(summary.getProcessId().equals(proceso)) {
				reservedRevisions.add(summary);		
				logger.info("La incluye en la lista");
			}
						
		}
	
		for (TaskSummary task : reservedRevisions) {
			logger.info("Tarea: " + task);
		}
		logger.info("Termino findTasksToReview");
		return reservedRevisions;
	}
	
	
	public TaskInstance findById(Long taskId) {
		logger.info("En findById de ReviewService");

		TaskInstance task = null;

		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		logger.info("Llamo a findTaskById de UserTaskServicesClient");
		task = client.findTaskById(taskId);

		return task;
	}
}
