package services.publix.workers;

import controllers.publix.workers.JatosPublix;
import exceptions.publix.ForbiddenPublixException;
import models.common.Batch;
import models.common.Study;
import models.common.User;
import models.common.workers.JatosWorker;
import models.common.workers.Worker;
import play.mvc.Http;
import services.publix.PublixErrorMessages;
import services.publix.StudyAuthorisation;

import javax.inject.Singleton;

/**
 * StudyAuthorization for JatosWorker
 *
 * @author Kristian Lange
 */
@Singleton
public class JatosStudyAuthorisation extends StudyAuthorisation<JatosWorker> {

    @Override
    public void checkWorkerAllowedToStartStudy(Http.Request request, Worker worker, Study study, Batch batch)
            throws ForbiddenPublixException {
        if (!batch.isActive()) {
            throw new ForbiddenPublixException(PublixErrorMessages.batchInactive(batch.getId()));
        }
        checkMaxTotalWorkers(batch, worker);
        checkWorkerAllowedToDoStudy(request, worker, study, batch);
    }

    @Override
    public void checkWorkerAllowedToDoStudy(Http.Request request, Worker worker, Study study, Batch batch)
            throws ForbiddenPublixException {
        // Check if worker type is allowed
        if (!batch.hasAllowedWorkerType(worker.getWorkerType())) {
            throw new ForbiddenPublixException(PublixErrorMessages
                    .workerTypeNotAllowed(worker.getUIWorkerType(), study.getId(), batch.getId()));
        }
        User user = ((JatosWorker) worker).getUser();
        // User has to be a user of this study
        if (!study.hasUser(user)) {
            throw new ForbiddenPublixException(PublixErrorMessages.workerNotAllowedStudy(worker, study.getId()));
        }
        // User has to be logged in
        String username = request.session().getOptional(JatosPublix.SESSION_USERNAME).orElse("");
        if (!user.getUsername().equals(username)) {
            throw new ForbiddenPublixException(PublixErrorMessages.workerNotAllowedStudy(worker, study.getId()));
        }
    }

}
