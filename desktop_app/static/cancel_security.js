$(document).ready(function(){
       
    $.getJSON($SCRIPT_ROOT + '/start_normal_mode', {},function(data) {
        if (data.piAssigned === "false") {
                    alert("Please assign this Roomi device to a room first")
                    window.location.replace($SCRIPT_ROOT + '/');
            } 
    });

    $('#cancel').bind('click', function() {
        $.get($SCRIPT_ROOT + '/stop_normal_mode', {},function() {});
        window.location.replace($SCRIPT_ROOT + '/');
    });
});