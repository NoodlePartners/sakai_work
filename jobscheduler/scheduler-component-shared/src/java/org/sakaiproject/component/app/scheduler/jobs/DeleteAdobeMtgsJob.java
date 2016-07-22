package org.sakaiproject.component.app.scheduler.jobs;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.StatefulJob;

import lombok.Setter;

import org.sakaiproject.db.api.SqlService;

/**
 * This job deletes all the CALENDAR_EVENT entries of type Adobe Calendar Event.
 *
 * @author Mitch Golden (mgolden@noodle.com)
 *
 */
public class DeleteAdobeMtgsJob implements Job {

	private static final Logger log = Logger.getLogger(DeleteAdobeMtgsJob.class);

	private final String BEAN_ID = "org.sakaiproject.component.app.scheduler.jobs.DeleteAdobeMtgsJob";

	private SqlService sqlService;
	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {

		log.info("DeleteAdobeMtgsJob starting");
		
		sqlService.dbWrite("DELETE from CALENDAR_EVENT where XML like '%QWRvYmUgQ29ubmVjdCBNZWV0aW5n%';");
		
		log.info("DeleteAdobeMtgsJob finished");
	}

	public void init() {
		log.info("DeleteAdobeMtgsJob.init()");
	}

}
