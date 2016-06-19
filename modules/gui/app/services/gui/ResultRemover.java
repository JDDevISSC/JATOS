package services.gui;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import daos.common.ComponentResultDao;
import daos.common.GroupResultDao;
import daos.common.StudyResultDao;
import daos.common.worker.WorkerDao;
import exceptions.gui.BadRequestException;
import exceptions.gui.ForbiddenException;
import exceptions.gui.NotFoundException;
import models.common.Component;
import models.common.ComponentResult;
import models.common.GroupResult;
import models.common.Study;
import models.common.StudyResult;
import models.common.User;
import models.common.workers.Worker;
import play.Logger;
import play.Logger.ALogger;

/**
 * Service class that removes ComponentResults or StudyResults. It's used by
 * controllers or other services.
 * 
 * @author Kristian Lange
 */
@Singleton
public class ResultRemover {

	private static final ALogger LOGGER = Logger.of(ResultRemover.class);

	private final Checker checker;
	private final ResultService resultService;
	private final ComponentResultDao componentResultDao;
	private final StudyResultDao studyResultDao;
	private final GroupResultDao groupResultDao;
	private final WorkerDao workerDao;

	@Inject
	ResultRemover(Checker checker, ResultService resultService,
			ComponentResultDao componentResultDao,
			StudyResultDao studyResultDao, GroupResultDao groupResultDao,
			WorkerDao workerDao) {
		this.checker = checker;
		this.resultService = resultService;
		this.componentResultDao = componentResultDao;
		this.studyResultDao = studyResultDao;
		this.groupResultDao = groupResultDao;
		this.workerDao = workerDao;
	}

	/**
	 * Retrieves all ComponentResults that correspond to the IDs in the given
	 * String, checks them and if yes, removes them.
	 * 
	 * @param componentResultIds
	 *            Takes a comma separated list of IDs of ComponentResults.
	 * @param user
	 *            For each ComponentResult it will be checked that the given
	 *            user is a user of the study that the ComponentResult belongs
	 *            too.
	 * @throws BadRequestException
	 * @throws NotFoundException
	 * @throws ForbiddenException
	 */
	public void removeComponentResults(String componentResultIds, User user)
			throws BadRequestException, NotFoundException, ForbiddenException {
		List<Long> componentResultIdList = resultService
				.extractResultIds(componentResultIds);
		List<ComponentResult> componentResultList = resultService
				.getComponentResults(componentResultIdList);
		checker.checkComponentResults(componentResultList, user, true);
		componentResultList.forEach(this::removeComponentResult);
	}

	/**
	 * Retrieves all StudyResults that correspond to the IDs in the given
	 * String, checks if the given user is allowed to remove them and if yes,
	 * removes them.
	 * 
	 * @param studyResultIds
	 *            Takes a comma separated list of IDs of StudyResults.
	 * @param user
	 *            For each StudyResult it will be checked that the given user is
	 *            a user of the study that the StudyResult belongs too.
	 * @throws BadRequestException
	 * @throws NotFoundException
	 * @throws ForbiddenException
	 */
	public void removeStudyResults(String studyResultIds, User user)
			throws BadRequestException, NotFoundException, ForbiddenException {
		List<Long> studyResultIdList = resultService
				.extractResultIds(studyResultIds);
		List<StudyResult> studyResultList = resultService
				.getStudyResults(studyResultIdList);
		checker.checkStudyResults(studyResultList, user, true);
		studyResultList.forEach(this::removeStudyResult);
	}

	/**
	 * Removes all ComponentResults that belong to the given component.
	 * Retrieves all ComponentResults of the given component, checks if the
	 * given user is allowed to remove them and if yes, removes them.
	 */
	public void removeAllComponentResults(Component component, User user)
			throws ForbiddenException, BadRequestException {
		List<ComponentResult> componentResultList = componentResultDao
				.findAllByComponent(component);
		checker.checkComponentResults(componentResultList, user, true);
		componentResultList.forEach(this::removeComponentResult);
	}

	/**
	 * Removes all StudyResults that belong to the given study. Retrieves all
	 * StudyResults of the given study, checks if the given user is allowed to
	 * remove them and if yes, removes them.
	 */
	public void removeAllStudyResults(Study study, User user)
			throws ForbiddenException, BadRequestException {
		List<StudyResult> studyResultList = studyResultDao
				.findAllByStudy(study);
		checker.checkStudyResults(studyResultList, user, true);
		studyResultList.forEach(this::removeStudyResult);
	}

	/**
	 * Removes all StudyResults that belong to the given worker. Retrieves all
	 * StudyResults that belong to the given worker, checks if the given user is
	 * allowed to remove them and if yes, removes them.
	 */
	public void removeAllStudyResults(Worker worker, User user)
			throws ForbiddenException, BadRequestException {
		List<StudyResult> allowedStudyResultList = resultService
				.getAllowedStudyResultList(user, worker);
		checker.checkStudyResults(allowedStudyResultList, user, true);
		allowedStudyResultList.forEach(this::removeStudyResult);
	}

	/**
	 * Remove ComponentResult from its StudyResult and then remove itself.
	 */
	public void removeComponentResult(ComponentResult componentResult) {
		StudyResult studyResult = componentResult.getStudyResult();
		if (studyResult != null) {
			studyResult.removeComponentResult(componentResult);
			studyResultDao.update(studyResult);
		} else {
			LOGGER.error(".removeComponentResult: StudyResult is null - "
					+ "but a ComponentResult always belongs to a StudyResult "
					+ "(ComponentResult's ID is " + componentResult.getId()
					+ ")");
		}
		componentResultDao.remove(componentResult);
	}

	/**
	 * Removes all ComponentResults of the given StudyResult, removes this
	 * StudyResult from the given worker, removes this StudyResult from the
	 * GroupResult and then remove StudyResult itself.
	 */
	public void removeStudyResult(StudyResult studyResult) {
		// Remove all component results of this study result
		studyResult.getComponentResultList()
				.forEach(componentResultDao::remove);

		// Remove study result from worker
		Worker worker = studyResult.getWorker();
		worker.removeStudyResult(studyResult);
		workerDao.update(worker);

		// Remove studyResult as member from group result
		GroupResult groupResult = studyResult.getActiveGroupResult();
		if (groupResult != null) {
			groupResult.removeActiveMember(studyResult);
			groupResultDao.update(groupResult);
		}

		// Remove studyResult
		studyResultDao.remove(studyResult);
	}

}
