define("omv/jira/custom/automation/rule/js/copy-custom-field-then/copy-custom-field-action-model", [
    "servicedesk/backbone-brace"
], function (Brace) {
    return Brace.Model.extend([{
        namedAttributes: {
            assetFieldId: String
        },
        defaults: {
            assetFieldId: ""
        }
    },
        {
            namedAttributes: {
                sourceFieldId: String
            },
            defaults: {
                sourceFieldId: ""
            }
        },
        {
            namedAttributes: {
                targetFieldId: String
            },
            defaults: {
                targetFieldId: ""
            }
        }
    ]);
});