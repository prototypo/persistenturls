var validatePURL = function() {
    var url = diverted($('#target').attr('href') + '?head');
    $.ajax({
        type: "GET",
        url: url,
        dataTpe: "text",
        error: function(xhr, textStatus, thrown) {
            showError("Error:" + textStatus);
        },
        success: function(data, textStatus, xhr) {
            var pattern = /^HTTP\/([0-9\.]+) [23]0[0-9]/;
        	if (pattern.test(data)) {
            	showError("Successfully validated.");
            } else {
            	showError("Failed to validate.");
            }
        }
    });
};

$(document).ready(function() {
    $('#validate').bind('click', validatePURL);
});
