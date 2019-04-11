$(document).ready(function(){
    $.get($SCRIPT_ROOT + '/start_normal_mode', {},function() {});

    $('#cancel').bind('click', function() {
        $.get($SCRIPT_ROOT + '/stop_normal_mode', {},function() {});
        window.location.replace($SCRIPT_ROOT + '/');
    });
});