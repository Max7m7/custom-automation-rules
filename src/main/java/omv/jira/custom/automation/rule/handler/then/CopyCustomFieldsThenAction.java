package omv.jira.custom.automation.rule.handler.then;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.pocketknife.api.commons.error.AnError;
import com.atlassian.pocketknife.api.commons.error.ErrorMessage;
import com.atlassian.servicedesk.plugins.automation.api.execution.error.ThenActionError;
import com.atlassian.servicedesk.plugins.automation.api.execution.error.ThenActionErrorHelper;
import com.atlassian.servicedesk.plugins.automation.api.execution.message.RuleMessage;
import com.atlassian.servicedesk.plugins.automation.api.execution.message.helper.IssueMessageHelper;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import static com.atlassian.fugue.Either.left;
import static com.atlassian.fugue.Either.right;

/**
 * Send notification message.
 */

public class CopyCustomFieldsThenAction implements com.atlassian.servicedesk.plugins.automation.spi.rulethen.ThenAction {
    public static final String SOURCE_FIELD_ID_KEY = "sourceFieldId";
    public static final String TARGET_FIELD_ID_KEY = "targetFieldId";

    static final String ASSET_FIELD_ID_KEY = "assetFieldId";
    private static final Logger logger = LoggerFactory.getLogger(CopyCustomFieldsThenAction.class);

    @ComponentImport
    @Autowired
    EventPublisher eventPublisher;

    @ComponentImport
    @Autowired
    ThenActionErrorHelper thenActionErrorHelper;

    @ComponentImport
    @Autowired
    private IssueMessageHelper issueMessageHelper;

    public CopyCustomFieldsThenAction(ThenActionErrorHelper thenActionErrorHelper) {
        this.thenActionErrorHelper = thenActionErrorHelper;
    }

    @Override
    public Either<ThenActionError, RuleMessage> invoke(final ThenActionParam thenActionParam) {
        //Getting issue object.
        final Either<AnError, Issue> issueEither = issueMessageHelper.getIssue(thenActionParam.getMessage());

        Issue currentIssue = issueEither.right().getOrNull();

        if (currentIssue == null)
            return left(new ThenActionError(ErrorMessage.builder().message("Issue for rule not found..").build(), 404));


        //Retrieving main options.
        final Option<String> assetFieldIdAsOption = thenActionParam.getConfiguration().getData().getValue(ASSET_FIELD_ID_KEY);
        final Option<String> sourceFieldIdAsOption = thenActionParam.getConfiguration().getData().getValue(SOURCE_FIELD_ID_KEY);
        final Option<String> targetFieldIdAsOption = thenActionParam.getConfiguration().getData().getValue(TARGET_FIELD_ID_KEY);

        try {
            Issue sourceIssue = null;

            CustomField assetCustomField;
            CustomField sourceCustomField;
            CustomField targetCustomField;

            CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();

            if (!assetFieldIdAsOption.isEmpty()) {
                String assetCustomFieldID = assetFieldIdAsOption.get();

                assetCustomField = customFieldManager.getCustomFieldObject(Long.valueOf(assetCustomFieldID));

                if (assetCustomField != null) {
                    Object customFieldValueAsObj = assetCustomField.getValue(currentIssue);

                    if (customFieldValueAsObj != null) {
                        String sourceIssueKey = (String) customFieldValueAsObj;

                        if (!Strings.isNullOrEmpty(sourceIssueKey)) {
                            sourceIssue = ComponentAccessor.getIssueManager().getIssueByKeyIgnoreCase(sourceIssueKey);

                            if (sourceIssue == null) {
                                throw new Exception("Source issue is null. Go to issue \"" +
                                        currentIssue.getKey() + "\" and check custom field that ID was specified in \"asset field\" in the \"THEN\" section of custom automation rule.");
                            }
                        } else {
                            throw new Exception("Source issue key is null or empty.");
                        }
                    } else {
                        throw new Exception("Custom field \"" + assetCustomField.getFieldName() + "\" in issue \"" +
                                currentIssue.getKey() + "\" has null or empty value.");
                    }
                } else {
                    throw new Exception("Custom field with ID " + assetCustomFieldID + " does not exist.");
                }
            } else {
                throw new Exception("Asset Field Id option is empty.");
            }

            //Compare source and target custom fields
            if (!sourceFieldIdAsOption.isEmpty()) {
                String sourceCustomFieldID = sourceFieldIdAsOption.get();

                sourceCustomField = customFieldManager.getCustomFieldObject(Long.valueOf(sourceCustomFieldID));

                if (sourceCustomField == null) {
                    throw new Exception("Custom field with ID " + sourceCustomFieldID + " does not exist.");
                }
            } else {
                throw new Exception("Source Field Id option is empty.");
            }

            if (!targetFieldIdAsOption.isEmpty()) {
                String targetCustomFieldID = targetFieldIdAsOption.get();

                targetCustomField = customFieldManager.getCustomFieldObject(Long.valueOf(targetCustomFieldID));

                if (targetCustomField == null) {
                    throw new Exception("Custom field with ID " + targetCustomFieldID + " does not exist.");
                }
            } else {
                throw new Exception("Target Field Id option is empty.");
            }

            //Compare keys of custom fields types.
            //if they equal, types of fields are equals too.
            if (sourceCustomField.getCustomFieldType().getKey().equals(targetCustomField.getCustomFieldType().getKey())) {
                //copying value from source custom field to target.
                targetCustomField.updateValue(null,
                        currentIssue,
                        new ModifiedValue(targetCustomField.getValue(currentIssue), sourceCustomField.getValue(sourceIssue)),
                        new DefaultIssueChangeHolder());


            } else {
                throw new Exception("Source and Target custom fields need to have the same type.");
            }
        } catch (Throwable e) {
            logger.warn("Common Error.", e);

            return thenActionErrorHelper.error("Common Error in Copy Custom Fields THEN section." + e.getMessage());
        }

        return right(thenActionParam.getMessage());
    }
}