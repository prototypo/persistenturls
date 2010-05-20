var validatePURL = function() {
    var url = diverted($('#target').attr('href') + '?head');
    $.ajax({
        type: "GET",
        url: url,
        error: function(xhr, textStatus, thrown) {
            showError("Error:" + textStatus);
        },
        success: function(data, textStatus, xhr) {
        	if (data.indexOf("200") > 0) {
            	showError("Successfully validated.");
            } else {
            	showError("Failed to validated.");
            }
        }
    });
};

$(document).ready(function() {
    $('#validate').bind('click', validatePURL);
});
