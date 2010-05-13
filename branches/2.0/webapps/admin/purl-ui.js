var Purl = {};
Purl.UI = {};
Purl.UI.queryArgs = {};

Purl.UI.init = function() {
    $('form.search').live('submit', function(e) {
        e.preventDefault();
        document.location.href = document.location.protocol + "//" + document.location.host + $(this).attr('action') + "&" + $('input', this).attr('name') + "=" + $('input', this).val();
    });
    $('#menu form.search label').live('click', function(e) {
        $(this).parent().find('input').get(0).focus();
    });
    $('#menu form.search input').bind('focus', function(e) {
        $(this).parent().css('opacity', 1);
    });
    $('#menu form.search input').bind('blur', function(e) {
        $(this).parent().css('opacity', 0.3);
    });
    // preferable to do this in document generation, not with client
    var args = document.location.search.substr(1).split('&');
    for (var i = 0, l = args.length; i < l; i++) {
        if (args[i].indexOf('=') > 0) {
            arg = args[i].split('=');
            var name = arg[0];
            var val = arg[1];
            Purl.UI.queryArgs[name] = val;
        } else {
            Purl.UI.queryArgs['type'] = args[i];
        }
    }
    $('.search form.search input').val(Purl.UI.queryArgs['q']);
    $('.search form.search').attr('action', '/?' + Purl.UI.queryArgs['type']);
};

$(document).ready(function() {
    Purl.UI.init();
});
