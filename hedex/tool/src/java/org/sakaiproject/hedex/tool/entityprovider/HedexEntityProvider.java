package org.sakaiproject.hedex.tool.entityprovider;

import java.util.Map;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HedexEntityProvider extends AbstractEntityProvider implements
		AutoRegisterEntityProvider, ActionsExecutable, Outputable {

	public final static String ENTITY_PREFIX		= "hedex";
	public final static String DEFAULT_ID			= ":ID:";
	
    public final static String TENANT_ID = "tenantId";
	public final static String REQUESTING_AGENT = "RequestingAgent";
	public final static String TERMS = "terms";
	public final static String START_DATE = "startDate";
	public final static String SEND_CHANGES_ONLY = "sendChangedOnly";
	public final static String KEY_ENROLLMENT_SET_ID = "lastRunDate";
	public final static String KEY_ENROLLMENT_STATUS = "includeAllTermHistory";

	public String getEntityPrefix() { return ENTITY_PREFIX; }

	public String[] getHandledOutputFormats() {
		return new String[] { Formats.JSON };
	}
	
	@EntityCustomAction(action = "Get_Retention_Engagement_EngagementActivity", viewKey = EntityView.VIEW_SHOW)
	public Object getRetentionEngagementEngagementActivity(EntityReference reference, Map<String, Object> parameters) {

        String userId = developerHelperService.getCurrentUserId();

        if (userId == null) {
            throw new EntityException("You must be logged in to get hedex data.", reference.getReference());
        }

        return "";
	}
}
