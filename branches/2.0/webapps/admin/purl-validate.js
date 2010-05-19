var validatePURL = function() {
    var url = document.location.href.replace(document.location.search, '') + '?head';
    $.ajax({
        type: "GET",
        url: url,
        error: function(xhr, textStatus, thrown) {
            showError("Error:" + textStatus);
        },
        success: function(data, textStatus, xhr) {
            showError("Successfully validated.");
        }
    });
};

$(document).ready(function() {
    $('#validate').bind('click', validatePURL);
});
