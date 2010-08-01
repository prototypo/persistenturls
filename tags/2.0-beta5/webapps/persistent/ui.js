Purl.UI = {};
Purl.UI.queryArgs = {};

Purl.UI.init = function() {
    // preparing query arguments
    var args = document.location.search.substr(1).split('&');
    for (var i = 0, l = args.length; i < l; i++) {
        if (args[i].indexOf('=') > 0) {
            arg = args[i].split('=');
            var name = arg[0];
            var val = unescape(arg[1]);
            Purl.UI.queryArgs[name] = val;
        }
    }

    // search result page
    $('.search #content form.search input[name="q"]').val(Purl.UI.queryArgs['q']);

    // search result form and menu form
    var purlOrTarget = function(e, form) {
        if ($('input[name="q"]', form).val().indexOf(':') > 0
            && $('input.profile', form).attr('name') === "purl") {
            $('input.profile', form).attr('name', 'target');
        }
    };
    $('form img.submit').live('click', function(e) {
        var use = $(this).parents('form');
        purlOrTarget(e, use);
        use.submit();
    });
    $('form.search').live('submit', function(e) {
        purlOrTarget(e, $(this));
    });

    // menu search
    $('#menu form.search input[name="q"]').bind('focus', function(e) {
        $(this).parent().css('opacity', 1);
    });
    $('#menu form.search input[name="q"]').bind('blur', function(e) {
        $(this).parent().css('opacity', 0.7);
    });

    // menu expand / collapse function
    $('#menu > li:has(form)').live('click', function(e) {
        e.preventDefault();
        if ($(this).hasClass('inactive')) {
            $('#menu li.active').removeClass('active').addClass('inactive');
            $(this).removeClass('inactive').addClass('active');
            if ($('input[name="q"]', this).length > 0)
                $('input[name="q"]', this).get(0).focus();
        } else {
            if ($('input[name="q"]', this).length > 0)
                $('input[name="q"]', this).get(0).focus();
        }
        return false;
    });

    // content empty :header hiding
    $('#content ul').filter(function(index) {
        return $('li', this).length === 0;
    }).remove();
    $('#content h3').filter(function(index) {
        return $(this).next(':not(:header, div.clear)').length === 0;
    }).hide();

    // user name
    // it would be preferable to come up with the user URL and then
    // get ?label on redirect instead of ?view - but impossible to do with just HTTP and jQuery
    $.ajax({
        url: $('#username').attr('href'),
        type: "GET",
        dataType: "text",
        success: function(data, status) {
        	var label = data;
        	if (data.match(/^</)) {
        		label = /<title[^>]*>([^<]*)<\/title>/i.exec(data)[1]
        	}
            $('#username').text(label);
            $('.logout').show();
        },
        error: function(xhr, status, err) {
            $('#username').hide();
            $('#login').show();
        }
    });
};

$(document).ready(function() {
    Purl.UI.init();
});
