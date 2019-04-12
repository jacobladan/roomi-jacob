from flask import Flask, render_template, jsonify, request, url_for, redirect
import time, threading
import pyrebase
import RPi.GPIO as GPIO
from digole import lcd
import smbus as smbus
import board
import busio
from digitalio import DigitalInOut
from adafruit_pn532.i2c import PN532_I2C

# Solenoid Setup
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(26, GPIO.OUT)

#Digole Setup and clear screen
i2c_digole = smbus.SMBus(1)
address = 0x27
i2c_digole.write_block_data(address, 0x00, [0x43, 0x4c])

#NFC Setup
i2c_nfc = busio.I2C(board.SCL, board.SDA)
reset_pin = DigitalInOut(board.D6)
req_pin = DigitalInOut(board.D12)
pn532 = PN532_I2C(i2c_nfc, debug=False, reset=reset_pin, req=req_pin)
pn532.SAM_configuration()

config = {
    "apiKey": "AIzaSyDnDXIpTdqMeTbVK_6S-XvGpUE4juq0Ge4",
    "authDomain": "roomi-825d0.firebaseapp.com",
    "databaseURL": "https://roomi-825d0.firebaseio.com/",
    "storageBucket": "roomi-825d0.appspot.com"
}

#- Firebase Init -#
firebase = pyrebase.initialize_app(config)
auth = firebase.auth()

#- Authentication for Firebase -#
#user = auth.sign_in_with_email_and_password("rpi@gmail.com", "rpirpi")
user = auth.sign_in_with_email_and_password("roomi.develop@gmail.com", "roomi1")
db = firebase.database()

isNormalModeRunning = False
dbUserKey = "jKz8q9JKxjWOscY3OTj7mlLgrDA2"

app = Flask(__name__)

#- Web Routes -#
@app.before_first_request
def normalModeThread():
    def run():
        # print("Thread started")
        global isNormalModeRunning
        # print("%r" % isNormalModeRunning)
        while True:
            while isNormalModeRunning:
                uid = pn532.read_passive_target(timeout=1)
                if uid is None:
                    continue
                else:
                    macAddr = getMACAdd()
                    cardID = "".join([hex(i)[2:] for i in uid])
                    persAL = db.child("users").child(dbUserKey).child("personnel").child(cardID).child("accessLevel").get().val() 
                    piAL = db.child("users").child(dbUserKey).child("rooms").child("security").child(macAddr).child("accessLevel").get().val()
                    if persAL >= piAL:
                        #Solenoid unlocking
                        GPIO.output(26, GPIO.HIGH)
                        digoleWriteText("Unlocked")                
                        time.sleep(3)
                        #Solenoid locking
                        GPIO.output(26, GPIO.LOW)
                        digoleWriteText("Locked")
                    else:
                        digoleWriteText("Denied")
                        time.sleep(3)

    thread = threading.Thread(target=run)
    thread.start()

@app.route('/')
def index():
    digoleClearScreen()
    digoleWriteCommand("ETP99")
    digoleWriteText("ROOMI")
    return render_template('index.html')

@app.route('/add_personnel')
def addPersonnel():
    return render_template('add_personnel.html')

@app.route('/assign_room')
def assignRoom():
    return render_template('assign_room.html')

@app.route('/running')
def running():
    digoleClearScreen()
    digoleWriteCommand("ETP99")
    digoleWriteText("Security Enabled")
    return render_template('running.html')

@app.route('/stop_security')
def stopSecurity():
    return render_template('/index.html')

#- Gets card ID to assign to personnel -#
@app.route('/poll_for_card')
def pollForCard():
    digoleClearScreen()
    digoleWriteCommand("ETP99")
    digoleWriteText("Scanning for card...")
    cardId = getCardId()
    if cardId is None:
        digoleClearScreen()
        digoleWriteCommand("ETP99")
        digoleWriteText("No card found")
        return jsonify(gotCard='false', cardId="null")
    else:
        if not isCardAvailable(cardId):
            digoleClearScreen()
            digoleWriteCommand("ETP99")
            digoleWriteText("Card already in use")
            return jsonify(gotCard='unique', cardId="null")
        else: 
            return jsonify(gotCard='true', cardId=cardId)

@app.route('/add_personnel_to_db')
def addPersonnelToDB():
    name = request.args.get('name', 'name')
    accessLevel = request.args.get('accessLevel', '0')
    cardId = request.args.get('cardId', 'cardId')

    personnel = {
        "name": name,
        "accessLevel": accessLevel,
    }

    db.child("users").child(dbUserKey).child("personnel").child(cardId).set(personnel)
    digoleClearScreen()
    digoleWriteCommand("ETP99")
    digoleWriteText("Personnel Added")
    digoleWriteCommand("TRT")
    digoleWriteCommand("TRT")
    digoleWriteText("Name: " + name)
    digoleWriteCommand("TRT")
    digoleWriteText("Access Level: " + accessLevel)
    return render_template('/index.html')

#- Assignes the RPi to room with MAC as DB Key -#
@app.route('/add_room_to_db')
def addRoomToDB():
    name = request.args.get('name', 'name')
    accessLevel = request.args.get('accessLevel', '0')
    macAddress = getMACAdd()

    room = {
        "name": name,
        "accessLevel": accessLevel
    }

    db.child("users").child(dbUserKey).child("rooms").child("security").child(macAddress).set(room)
    digoleClearScreen()
    digoleWriteCommand("ETP99")
    digoleWriteText("RPi Assigned")
    digoleWriteCommand("TRT")
    digoleWriteCommand("TRT")
    digoleWriteText("Name: " + name)
    digoleWriteCommand("TRT")
    digoleWriteText("Access Level: " + accessLevel)
    return render_template('/index.html')

@app.route('/start_normal_mode')
def startNormalMode():
    global isNormalModeRunning
    isNormalModeRunning = True   
    return "Roomi started"

@app.route('/stop_normal_mode')
def stopNormalMode():
    global isNormalModeRunning
    isNormalModeRunning = False
    return "Roomi stopped"

#- Functionality -#
#- Polls for 10s and extracts card ID when found -#
def getCardId():
    startTime = time.time()
    while True:
        if time.time() - startTime > 11:
            return
        else:
            uid = pn532.read_passive_target(timeout=1)
            if uid is None: 
                continue
            else:   
                cardID = "".join([hex(i)[2:] for i in uid])
                return cardID

#- Checks if card is already in DB -#
def isCardAvailable(cardID):
    keyCards = db.child("users").child(dbUserKey).child("personnel").shallow().get().val()
    
    if keyCards is None:
        return True
    else:
        if cardID in keyCards:
            return False
        else:
            return True

#- Get RPi's MAC Address -#
def getMACAdd():
    interface = 'eth0'
    try:
        str = open('/sys/class/net/%s/address' % interface).read()
    except:
        str = "00:00:00:00:00:00"

    return (str[0:17])

#- Write text to LCD -#
def digoleWriteText(text):
    text = "TT" + text
    textToWrite = [ord(i) for i in text]
    i2c_digole.write_block_data(address, 0x00, textToWrite)

def digoleWriteCommand(text):
    textToWrite = [ord(i) for i in text]
    print(textToWrite)
    i2c_digole.write_block_data(address, 0x00, textToWrite)

def digoleClearScreen():
    i2c_digole.write_block_data(address, 0x00, [0x43, 0x4c])

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', threaded=True)