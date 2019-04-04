#!/usr/bin/env python3
import RPi.GPIO as GPIO
import time
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

# Loop that controls the operation
while True:
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
        
