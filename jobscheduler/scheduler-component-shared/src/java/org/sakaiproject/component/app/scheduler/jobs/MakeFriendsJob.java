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
 * This is the Make Friends job.
 *
 * @author Mitch Golden (mgolden@noodle.com)
 *
 */
public class MakeFriendsJob implements Job {

	private static final Logger log = Logger.getLogger(MakeFriendsJob.class);

	private final String BEAN_ID = "org.sakaiproject.component.app.scheduler.jobs.MakeFriendsJob";

	private SqlService sqlService;
	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {

		log.info("MakeFriendsJob starting");

		String sql = new StringBuilder()
			.append("insert into PROFILE_FRIENDS_T (USER_UUID, FRIEND_UUID, RELATIONSHIP, REQUESTED_DATE, CONFIRMED, CONFIRMED_DATE) ")
			.append("select distinct su1.USER_ID, su2.USER_ID, 1, now(), 1, now() ")
			.append("from SAKAI_SITE_USER ssu1 ")
			.append("inner join SAKAI_SITE_USER ssu2 ")
			.append("on ssu1.SITE_ID = ssu2.SITE_ID ")
			.append("inner join SAKAI_SITE ss ")
			.append("on ss.SITE_ID = ssu1.SITE_ID and ss.type='course' ")
			.append("inner join SAKAI_USER su1 ")
			.append("on su1.USER_ID = ssu1.USER_ID ")
			.append("inner join SAKAI_USER su2 ")
			.append("on su2.USER_ID = ssu2.USER_ID ")
			.append("left outer join PROFILE_FRIENDS_T pf1 ")
			.append("on pf1.user_uuid = ssu1.USER_ID ")
			.append("and pf1.friend_uuid = ssu2.USER_ID ")
			.append("left outer join  PROFILE_FRIENDS_T pf2 ")
			.append("on pf2.user_uuid = ssu2.USER_ID ")
			.append("and pf2.friend_uuid = ssu1.USER_ID ")
			.append("left outer join SAKAI_REALM_RL_GR srrg1 ")
			.append("on srrg1.USER_ID = su1.USER_ID ")
			.append("left outer join SAKAI_REALM_RL_GR srrg2 ")
			.append("on srrg1.USER_ID = su2.USER_ID ")
			.append("where pf1.USER_UUID is null ")
			.append("and pf2.USER_UUID is null ")
			.append("and ssu1.USER_ID < ssu2.USER_ID ")
			.append("and ssu1.USER_ID <> 'admin' and ssu2.USER_ID <> 'admin' ")
			.append("and COALESCE(srrg1.ROLE_KEY, 0) NOT IN (9,10,16) ")
			.append("and COALESCE(srrg2.ROLE_KEY, 0) NOT IN (9,10,16) ")
			.append("and su1.EMAIL_LC like '%@pepperdine.edu' and su2.EMAIL_LC like '%@pepperdine.edu'; ")
			.toString();

		log.info(sql);
		sqlService.dbWrite(sql);

		log.info("MakeFriendsJob finished");
	}

	public void init() {
		log.info("MakeFriendsJob.init()");
	}

}
