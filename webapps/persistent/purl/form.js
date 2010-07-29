/*
 * form.js
 */

$(document).ready(function() {
	var enabled = true
    $('input[name=rdf-type]').each(function() {
        var type = $(this).attr('resource').replace('http://persistent.name/rdf/2010/purl#', 'purl:');
        if (type == "purl:Disabled") {
            $('#purl-table').replaceWith("<p>This PURL is disabled.</p>");
            $('#disable-button').remove();
            enabled = false
        } else if (type == "purl:Tombstoned") {
            $('#purl-table').replaceWith("<p>This PURL has been tombstoned.</p>");
            $('#disable-button').remove();
            $('#tombstone-button').remove();
            $('#save-button').remove();
            enabled = false
        }
    });
    if (enabled) {
        $('#enable-button').remove();
		if (!$("#m_target").length) {
			var input = $("<input id='m_target' />")
			input.attr('rel', $("#purl-rel-type").val())
			input.change(function() {
				 $(this).attr('resource', $(this).val())
			})
			$("#target-td").append(input)
		}
    }
    $("#domain-span[resource]").text($("#domain-span").attr("resource"))
    $("#domain-span:not([resource])").text($("base").attr("href"))
})

function enable(node) {
	$.post(diverted('?purl-enable', node), function(){
		location.reload()
	})
}

function disable(node) {
	if (confirm('Are you sure you want to disable this PURL?')) {
		$.post(diverted('?purl-disable', node), function(){
			location = diverted('?view', node)
		})
	}
}

function tombstone(node) {
	if (confirm('Are you sure you want to tombstone this PURL?')) {
		$.post(diverted('?purl-tombstone', node), function(){
			location = diverted('?view', node)
		})
	}
}
