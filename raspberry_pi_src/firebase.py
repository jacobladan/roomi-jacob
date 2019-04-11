import pyrebase

config = {
    "apiKey": "AIzaSyDnDXIpTdqMeTbVK_6S-XvGpUE4juq0Ge4",
    "authDomain": "roomi-825d0.firebaseapp.com",
    "databaseURL": "https://roomi-825d0.firebaseio.com/",
    "storageBucket": "roomi-825d0.appspot.com"
}


############################################################
#   Adds a user with an default name and access level 0
#   Grabs the keyCard and inserts it in the database
###########################################################

def addPersonnel(cardID):
    if isCardAvailable(cardID):
        name = input("Enter the personnel name: ")

        while True:

            accessLevel = int(input("Enter the personnel's access level (0-5): "))

            if (accessLevel > 5 or accessLevel < 0):
                print("Invalid access level. Please try again")
                continue

            break

        newUser = {"name": name, "accessLevel": accessLevel, "keyCard": cardID}
        db.child("users").child("9FOHwo3m68dGwQfoCz0em6HJ0t73").child("personnel").child(cardID).set(newUser)
    else:
        print("Cannot Add User! KeyCard Already in Use!")




############################################################
#   Retrieves the device's MAC Address
###########################################################

def getMACAdd():
    interface = 'eth0'
    try:
        str = open('/sys/class/net/%s/address' % interface).read()
    except:
        str = "00:00:00:00:00:00"

    return (str[0:17])


############################################################
#   Retrieves the device's MAC Address
###########################################################
def addRoom():
    id = getMACAdd()
    name = input("Enter the room name: ")

    
    while True:

        accessLevel = int(input("Enter the room's access level (0-5): "))

        if (accessLevel > 5 or accessLevel < 0):
            print("Incorrect access level. Try again!")
            continue

        break

    room = {"name": name, "accessLevel": accessLevel}

    db.child("users").child("9FOHwo3m68dGwQfoCz0em6HJ0t73").child("rooms").child("security").child(id).set(room)

    return

############################################################
#   Check Card on the Database
############################################################
def isCardAvailable(cardID):
    keyCards = db.child("users").child("9FOHwo3m68dGwQfoCz0em6HJ0t73").child("personnel").shallow().get().val()

    if cardID in keyCards:
        return False
    else:
        return True


############################################################
#   Database initialization
###########################################################

firebase = pyrebase.initialize_app(config)
auth = firebase.auth()

# authenticate the Raspberry PI
user = auth.sign_in_with_email_and_password("rpi@gmail.com", "rpirpi")

db = firebase.database()
results = db.child("users").child("9FOHwo3m68dGwQfoCz0em6HJ0t73").child("personnel").get()
readAccessLevel = 4

addPersonnel("123456789")
#addRoom()

# personnel = results.val()

# for values in personnel.values():
# print("Access Level ", values["accessLevel"])


