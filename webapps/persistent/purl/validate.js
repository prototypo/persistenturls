var validatePURL = function() {
    var href = location.href;
    $.ajax({
        type: "POST",
        url: href.substring(0, href.indexOf('?')) + '?validate',
        dataType: "text",
        error: function(xhr, textStatus, thrown) {
            showError("Error:" + textStatus);
        },
        success: function(data, textStatus, xhr) {
            var resolvePattern = /^HTTP\/[0-9\.]+ 20[023456]/;
            var redirectPattern = /^HTTP\/[0-9\.]+ 30[0123457]/;
        	if (resolvePattern.test(data)) {
            	showError("Target resolves.");
            } else if (redirectPattern.test(data)) {
            	showError("Target redirects.");
            } else {
            	showError("Failed to validate.");
            }
        }
    });
};

$(document).ready(function() {
    $('#validate').bind('click', validatePURL);
});
