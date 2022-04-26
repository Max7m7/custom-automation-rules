package omv.jira.custom.automation.rule.handler.then;

import com.atlassian.fugue.Option;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.servicedesk.plugins.automation.api.configuration.ruleset.validation.ValidationResult;
import com.atlassian.servicedesk.plugins.automation.spi.rulethen.ThenActionValidator;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class CopyCustomFieldsThenValidator implements ThenActionValidator {
    private static final Logger logger = LoggerFactory.getLogger(CopyCustomFieldsThenValidator.class);
    private final String ERROR_FIELD_NAME_KEY = "target-field";

    @Override
    public ValidationResult validate(@Nonnull ThenActionValidationParam thenActionValidationParam) {
        //Retrieving main options.
        final Option<String> assetFieldIdAsOption = thenActionValidationParam.getConfiguration().getData().getValue(CopyCustomFieldsThenAction.ASSET_FIELD_ID_KEY);
        final Option<String> sourceFieldIdAsOption = thenActionValidationParam.getConfiguration().getData().getValue(CopyCustomFieldsThenAction.SOURCE_FIELD_ID_KEY);
        final Option<String> targetFieldIdAsOption = thenActionValidationParam.getConfiguration().getData().getValue(CopyCustomFieldsThenAction.TARGET_FIELD_ID_KEY);

        Map<String, List<String>> errorList = newHashMap();

        try {
            CustomField assetCustomField;
            CustomField sourceCustomField;
            CustomField targetCustomField;

            CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();

            String assetCustomFieldID = "";
            if (!assetFieldIdAsOption.isEmpty()) {
                assetCustomFieldID = assetFieldIdAsOption.get();

                assetCustomField = customFieldManager.getCustomFieldObject(Long.valueOf(assetCustomFieldID));

                if (assetCustomField == null) {
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
                return ValidationResult.PASSED();
            } else {
                throw new Exception("Source and Target custom fields need to have the same type.");
            }
        } catch (NumberFormatException e) {
            errorList.put(ERROR_FIELD_NAME_KEY, newArrayList("Custom field ID must contain only numeric symbols without spaces."));

            logger.warn("Custom field ID must contain only numeric symbols without spaces.", e);

            return ValidationResult.FAILED(errorList);
        } catch (Throwable e) {
            String errorMessage = e.getMessage();

            if (Strings.isNullOrEmpty(errorMessage)) {
                errorMessage = "Common Error. See JIRA log.";
                logger.warn("Common Error.", e);
            }
            errorList.put(ERROR_FIELD_NAME_KEY, newArrayList(errorMessage));

            return ValidationResult.FAILED(errorList);
        }
    }
}
