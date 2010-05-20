var Purl = {};
Purl.UI = {};
Purl.UI.queryArgs = {};

Purl.UI.init = function() {
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
    $('.search #content form.search input').val(Purl.UI.queryArgs['q']);
    $('.search #content form.search').attr('action', '/?' + Purl.UI.queryArgs['type']);

    $('#menu > li:has(form)').live('click', function(e) {
        e.preventDefault();
        if ($(this).hasClass('inactive')) {
            $('#menu li.active').removeClass('active').addClass('inactive');
            $(this).removeClass('inactive').addClass('active');
            if ($('input', this).length > 0)
                $('input', this).get(0).focus();
        } else {
            if ($('input', this).length > 0)
                $('input', this).get(0).focus();
        }
        return false;
    });
};

$(document).ready(function() {
    Purl.UI.init();
});
