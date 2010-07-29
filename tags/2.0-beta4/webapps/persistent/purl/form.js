Purl.PURLForm = {};

Purl.PURLForm.modifyForm = function(relType) {
    $('#purl-form tr.rel').hide();
    $('#purl-form tr.rel.'+relType).show();
    if ($('#purl-form tr.rel:visible input').length === 0) {
        var input = $('<input type="text" id="m_target" resource="" value="" rel="' + $('input[name=' + relType + ']').val() + '" />');
        input.bind('change', function() {
            $(this).attr('resource', $(this).val());
        });
        $('#purl-form tr.rel:visible td.value').append(input);
    }
};

Purl.PURLForm.modifyType = function(type) {
    $('#purl-form').attr('typeof', type);
};

Purl.PURLForm.initializeForm = function() {
    var frag = document.location.hash.substr(1);
    var qs = document.location.search.substr(1);
    if (frag.length === 0 && qs !== "edit") {
        frag = "alternate";
    } else {
        frag = $('#purl-form tr.rel input').filter(function() {
            return $(this).val() !== '';
        }).parents('tr.rel').attr('class');
        if (frag) {
        	frag = frag.replace('rel ','');
        }
        var isDisabled = false;
        var isTombstoned = false;
        $('input[name=rdf-type]').each(function() {
            var type = $(this).attr('resource').replace('http://persistent.name/rdf/2010/purl#', 'purl:');
            if (type == "purl:Disabled") {
                $('#purl-rel-type').empty();
                $('#purl-rel-type').append("<option value='disabled'>Disabled (404)</option>");
                $('#disable-button').remove();
                frag = "disabled";
            } else if (type == "purl:Tombstoned") {
                $('#purl-rel-type').empty();
                $('#purl-rel-type').append("<option value='tombstoned'>Tombstoned (410)</option>");
                $('#enable-button').remove();
                $('#disable-button').remove();
                $('#tombstone-button').remove();
                $('#save-button').remove();
                frag = "tombstoned";
            }
        });
    }
    if (frag != "disabled") {
        $('#enable-button').remove();
    }
    $('#purl-rel-type').bind('change', function() {
        Purl.PURLForm.modifyForm($(this).val());
    });
    $('#purl-rel-type').val(frag);
    $('#purl-rel-type').trigger('change');
    $('#purl-form input.purl_type').trigger('change');
    $('#purl-form').bind('submit', function() {
        $('#purl-form tr:hidden input').each(function() {
            $(this).removeAttr('about').removeAttr('resource').remove('content').val('');
        });
    });
    $('#domain-span').text($('#m_domain').attr('href'))
};

$(document).ready(function() {
    Purl.PURLForm.initializeForm();
});
