#!/usr/bin/env python3
from flask import Flask, render_template, jsonify, request, url_for, redirect
import time, pyrebase, threading
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
user = auth.sign_in_with_email_and_password("rpi@gmail.com", "rpirpi")
db = firebase.database()

isNormalModeRunning = False

app = Flask(__name__)

#- Web Routes -#
@app.before_first_request
def normalModeThread():
    def run():
        global isNormalModeRunning
        while isNormalModeRunning:
            uid = pn532.read_passive_target(timeout=1)
            print('.', end="")
            # Try again if no card is available.
            if uid is None:
                continue
            else:
                
                #Saves uid to cardID as single hex value
                cardID = "".join([hex(i)[2:] for i in uid])
                
                #Dispalys cardID
                print(cardID)
                
                #Solenoid unlocking
                print("Solenoid Pulled")
                GPIO.output(26, GPIO.HIGH)
                
                i2c_digole.write_block_data(address, 0x00, [0x43, 0x4c])
                text = "TTUnlocked"
                textToWrite = [ord(i) for i in text]
                i2c_digole.write_block_data(address, 0x00, textToWrite)
                        
                time.sleep(3)

                #Solenoid locking
                print("Solenoid pushed!")
                GPIO.output(26, GPIO.LOW)

                i2c_digole.write_block_data(address, 0x00, [0x43, 0x4c])
                text = "TTLocked"
                textToWrite = [ord(i) for i in text]
                i2c_digole.write_block_data(address, 0x00, textToWrite)

    thread = threading.Thread(target=run)
    thread.start()

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/add_personnel')
def addPersonnel():
    return render_template('add_personnel.html')

@app.route('/assign_room')
def assignRoom():
    return render_template('assign_room.html')

@app.route('/running')
def running():
    return render_template('running.html')

@app.route('/stop_security')
def stopSecurity():
    return render_template('/index.html')

#- Gets card ID to assign to personnel -#
@app.route('/poll_for_card')
def pollForCard():
    cardId = getCardId()
    if cardId is None:
        return jsonify(gotCard='false', cardId="null")
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

    db.child("users").child("9FOHwo3m68dGwQfoCz0em6HJ0t73").child("personnel").child(cardId).set(personnel)
    print("Personnel with cardId: " + cardId + ", Name: " + name + ", and AL: " + accessLevel)
    i2c_digole.write_block_data(address, 0x00, [0x43, 0x4c])
    text = "TT" + name + " added"
    textToWrite = [ord(i) for i in text]
    i2c_digole.write_block_data(address, 0x00, textToWrite)
    # return render_template('/index.html')

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

    db.child("users").child("9FOHwo3m68dGwQfoCz0em6HJ0t73").child("rooms").child("security").child(macAddress).set(room)
    print("MAC: " + macAddress + "has been assgned with Name: " + name + " and AL: " + accessLevel)
    i2c_digole.write_block_data(address, 0x00, [0x43, 0x4c])
    text = "TTPi Assgned to " + name
    textToWrite = [ord(i) for i in text]
    i2c_digole.write_block_data(address, 0x00, textToWrite)
    # return render_template('/index.html')

@app.route('/start_normal_mode')
def startNormalMode():
    isNormalModeRunning = True

@app.route('/stop_normal_mode')
def stopNormalMode():
    isNormalModeRunning = False

#- Functionality -#
#- Polls for 10s and extracts card ID when found -#
def getCardId():
    startTime = time.time()
    while True:
        if startTime - time.time() > 10:
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
    keyCards = db.child("users").child("9FOHwo3m68dGwQfoCz0em6HJ0t73").child("personnel").shallow().get().val()

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

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', threaded=True)