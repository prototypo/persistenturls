var Purl = {};

Purl.notify = function(msg) {
    $('#message-container').append('<p>'+msg+'</p>');
};

Purl.error = function(msg) {
    $('#message-container').append('<p class="error">'+msg+'</p>');
};

Purl.initMessages = function() {
    $('body').live('error.calli', function(e) {
            Purl.error(e.data);
    });
    $('body').live('delete.calli', function(e) {
            Purl.notify(e.data);
    });
};
