var modifyForm = function(relType) {
    $('#purl-form tr.rel').hide();
    $('#purl-form tr.rel.'+relType).show();
};

var modifyType = function(type) {
    $('#purl-form').attr('typeof', type);
};

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
    $('#purl-rel-type').val(frag);
    $('#purl-rel-type').bind('change', function() {
        modifyForm($(this).val());
    });
    $('#purl-form input[name=m_purl_type]').bind('change', function() {
        modifyType($(this).val());
        console.log($(this));
        if ($(this).hasClass('partial')) {
            $('#purl-form tr.partial').show();
        } else {
            $('#purl-form tr.partial').hide();
        }
    })
    $('input[name=m_purl_type]:checked').trigger('change');
    $('#purl-form').bind('submit', function() {
        $('#purl-form tr:hidden td[rel] input').each(function() {
            $(this).removeAttr('about');
            $(this).val('');
        });
    });
};

$(document).ready(function() {
    initializeForm();
});
