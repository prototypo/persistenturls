Purl.PURLDelete = {};

Purl.PURLDelete.success = function(event) {
    Purl.notify("Resource deleted.");
};

Purl.PURLDelete.failure = function(event) {
    Purl.error("Failed to delete: " + event.data);
};

Purl.PURLDelete.init = function() {
    $('form[about]').bind('deleteSuccess.calli', function(e) {
        Purl.PURLDelete.success(e);
    });
    $('form[about]').bind('deleteFailure.calli', function(e) {
        Purl.PURLDelete.failure(e);
    });
};

$(document).ready(function() {
    Purl.PURLDelete.init();
});
