define("omv/jira/custom/automation/rule/js/copy-custom-field-then/copy-custom-field-action", [
    "servicedesk/jQuery",
    "servicedesk/underscore",
    "omv/jira/custom/automation/rule/js/copy-custom-field-then/copy-custom-field-action-model",
    "omv/jira/custom/automation/rule/js/copy-custom-field-then/copy-custom-field-action-view"
], function ($,
             _,
             CopyCustomFieldModel,
             CopyCustomFieldView) {
    var copyCustomFieldView = function (controller) {
        var template = ServiceDesk.myRules.CopyCustomFieldThen.CopyCustomFieldContainer;
        var $el = $(controller.el);

        function onError(errors) {
            $el.find('.error').remove();
            _applyFieldErrors(errors.fieldErrors);
            _applyGlobalErrors(errors.globalErrors);
        }

        function _applyFieldErrors(errors) {
            // If errors is an array
            _.each(errors, controller.renderFieldError)
        }

        function _applyGlobalErrors(errors) {
            for (var i = 0; i < errors.length; i++) {
                var thisError = errors[i];
                controller.renderGlobalError(thisError)
            }
        }

        // Listen to the 'error' event, which is fired when the form validation fails.
        controller.on('error', onError.bind(this));

        return {
            render: function (config, errors) {
                var assetFieldId = config && config.assetFieldId ? config.assetFieldId : "";
                var sourceFieldId = config && config.sourceFieldId ? config.sourceFieldId : "";
                var targetFieldId = config && config.targetFieldId ? config.targetFieldId : "";

                var testNullUnDefinedEmptyOrBlank = function (str) {
                    var pattern = /^[\s]+$/;
                    return (!str || str.size === 0 || pattern.test(str))
                };

                // Render the template
                $el.html(template());

                this.mailNotificationView = new CopyCustomFieldView({
                    model: new CopyCustomFieldModel({
                        assetFieldId: assetFieldId,
                        sourceFieldId: sourceFieldId,
                        targetFieldId: targetFieldId
                    }),
                    el: $el.find(".automation-servicedesk-copy-custom-field-then-action-container")
                }).render();

                return this;
            },

            serialize: function () {
                return {
                    assetFieldId: $el.find("#asset-field-id").val(),
                    sourceFieldId: $el.find("#source-field-id").val(),
                    targetFieldId: $el.find("#target-field-id").val()
                }
            },

            validate: function (deferred) {
                /*As practice shows, don't need make deferred.reject(), when error happens.*/
                deferred.resolve();
            },

            dispose: function () {
            }
        };
    };

    return function (controller) {
        return copyCustomFieldView(controller);
    };
});