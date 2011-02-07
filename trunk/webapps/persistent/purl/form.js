/*
 * form.js
 */

$(document).ready(function() {
	var enabled = true;
    $('input[name=rdf-type]').each(function() {
        var type = $(this).attr('resource').replace('http://persistent.name/rdf/2010/purl#', 'purl:');
        if (type == "purl:Disabled") {
            $('#purl-table').replaceWith("<p>This PURL is disabled.</p>");
            $('#disable-button').remove();
            enabled = false
        } else if (type == "purl:Tombstoned") {
            $('#purl-table').replaceWith("<p>This PURL has been tombstoned.</p>");
        	$('#enable-button').remove();
            $('#disable-button').remove();
            $('#tombstone-button').remove();
            $('#save-button').remove();
            enabled = false;
        }
    });
    if (enabled) {
        $('#enable-button').remove();
		if ($("#m_target").length) {
			$("#purl-rel-type").val($("#m_target").attr("rel"));
		} else {
			var input = $("<input id='m_target' size='40' />");
			input.attr('rel', $("#purl-rel-type").val());
			input.change(function() {
				 $(this).attr('resource', $(this).val());
			})
			$("#target-td").append(input);
		}
    }
    $("#domain-span[resource]").text($("#domain-span").attr("resource"));
    $("#domain-span:not([resource])").text($("body").attr("about"));
})

function enable(node) {
    var href = location.href;
	$.post(href.substring(0, href.indexOf('?')) + '?purl-enable', function(){
		location.reload();
	})
}

function disable(node) {
    var href = location.href;
	if (confirm('Are you sure you want to disable this PURL?')) {
		$.post(href.substring(0, href.indexOf('?')) + '?purl-disable', function(){
			location = href.substring(0, href.indexOf('?')) + '?view';
		})
	}
}

function tombstone(node) {
    var href = location.href;
	if (confirm('Are you sure you want to tombstone this PURL?')) {
		$.post(href.substring(0, href.indexOf('?')) + '?purl-tombstone', function(){
			location = href.substring(0, href.indexOf('?')) + '?view';
		})
	}
}
