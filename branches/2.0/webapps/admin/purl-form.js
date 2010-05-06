var valids = ['simple', 'renamed', 'redirect', 'described', 'copy'];

var initializeForm = function() {
    var frag = document.location.hash.substr(1);
    if (frag.length === 0) frag = "simple";
    $('#purl-form td.value').each(function() {
        $('input', this).val($(this).attr('about'));

        // get form typeof, set frag accordingly

        // if form typeof is purl:PURL, look through renamed, redirect, simple, copy, described for values - any one
        // with a value, set as frag
    });
    modifyForm(frag);
    $('#purl-type').val(frag);
    $('#purl-form').bind('submit', function() {
        $('#purl-form tr:hidden td[rel] input').each(function() {
            $(this).attr('about', null);
        });
    });
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
