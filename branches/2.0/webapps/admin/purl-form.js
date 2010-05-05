var initializeForm = function() {
    var frag = document.location.hash.substr(1);
    if (frag.length === 0) frag = "simple";
    modifyForm(frag);
    $('#purl-type').val(frag);
};

var modifyForm = function(type) {
    $('#purl-form tr[class!=all]').hide();
    $('#purl-form tr.'+type).show();
    if ($('#purl-form input.type').length > 0)
        $('#purl-form').attr('typeof', $('#purl-form input.type.'+type).val());
};

$(document).ready(function() {
    initializeForm();
});
