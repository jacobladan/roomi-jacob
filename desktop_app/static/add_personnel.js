$(document).ready(function(){
    $('a#add').bind('click', function() {
        var name = $('#name').val();
        var accessLevel = $('#access-level').val();

        if (validate(name, accessLevel)) {
            var timeleft = 10;
            var downloadTimer = setInterval(function(){
            document.getElementById("progressBar").value = 10 - timeleft;
            timeleft -= 1;
            if(timeleft <= 0) { clearInterval(downloadTimer) }
            }, 1000);

            $.getJSON($SCRIPT_ROOT + '/poll_for_card', {}, function(data) {
                console.log(data);
                if (data.gotCard === 'true') {
                    addToDB(name, accessLevel);
                } else { alert("Didn't see a card") }
            })
        }
    });
});

function validate(name, accessLevel) {
    var nameRegex = /^[a-zA-Z]+$/;
    var accessLevelRegex = /[0-5]/;

    if (name.length === 0 || name.length > 16 || !nameRegex.test(name)) {
        alert("Name Wrong: 1-16 Letters");
        return false;
    } else if (!accessLevelRegex.test(accessLevel)) {
        alert("Access Level Wrong: Number Between 0 and 5");
        return false;
    } else {
        return true;
    }
}

function addToDB(name, accessLevel) {
    
    $.get($SCRIPT_ROOT + '/add_personnel_to_db', {
        name: name,
        accessLevel: accessLevel
    },function() {
            alert("Personnel has been added!")
            window.location = $SCRIPT_ROOT + '/'
        }   
    );
}