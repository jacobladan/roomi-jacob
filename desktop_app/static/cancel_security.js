$(document).ready(function(){
    $.get($SCRIPT_ROOT + '/start_normal_mode', {},function() {});

    $('#cancel').bind('click', function() {
        window.location.replace($SCRIPT_ROOT + '/');
    });
});