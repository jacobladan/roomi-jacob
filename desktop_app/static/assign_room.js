$(document).ready(function(){
    $('a#add').bind('click', function() {
        var name = $('#name').val();
        var accessLevel = $('#access-level').val();
        $.getJSON($SCRIPT_ROOT + '/poll_for_card', {}, function(data) {
            if (data.gotCard === true) {
                validate(name, accessLevel);
            } else { alert("Didn't see a card") }
        })
    });
});

function validate(name, accessLevel) {
    var nameRegex = /^[a-zA-Z]+$/;
    var accessLevelRegex = /[1-5]/;

    if (name.length === 0 || name.length > 16 || !nameRegex.test(name)) {
        alert("Name Wrong");
        return false;
    } else if (!accessLevelRegex.test(accessLevel)) {
        alert("Access Level Wrong");
        return false;
    } else {
        addToDB(name, accessLevel);
    }
}

function addToDB(name, accessLevel) {
    
    $.get($SCRIPT_ROOT + '/add_room_to_db', {
        name: name,
        accessLevel: accessLevel
    });
}