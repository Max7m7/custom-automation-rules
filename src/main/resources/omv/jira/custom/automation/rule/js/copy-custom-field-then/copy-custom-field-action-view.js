define("omv/jira/custom/automation/rule/js/copy-custom-field-then/copy-custom-field-action-view", [
    "servicedesk/jQuery",
    "servicedesk/underscore",
    "servicedesk/backbone-brace"
], function ($,
             _,
             Brace) {
    return Brace.View.extend(
        {
            template: ServiceDesk.myRules.CopyCustomFieldThen.CopyCustomFieldForm,
            dispose: function () {
            },

            render: function () {
                this.$el.html(this.template(this.model.toJSON()));
                return this;
            }
        }
    );
});