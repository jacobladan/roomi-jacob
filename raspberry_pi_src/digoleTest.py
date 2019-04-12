from digole import lcd
import smbus as smbus

def digoleWriteCommand(text):
    textToWrite = [ord(i) for i in text]
    print(textToWrite)
    i2c_digole.write_block_data(address, 0x00, textToWrite)
    
def digoleWriteText(text):
    #i2c_digole.write_block_data(address, 0x00, [0x43, 0x4c])
    string = "TT" + text
    textToWrite = [ord(i) for i in string]
    i2c_digole.write_block_data(address, 0x00, textToWrite)


#Digole Setup and clear screen
i2c_digole = smbus.SMBus(1)
address = 0x27
i2c_digole.write_block_data(address, 0x00, [0x43, 0x4c])
digoleWriteCommand("ETP99")
digoleWriteText("RPi Assigned To")
digoleWriteCommand("TRT")
digoleWriteCommand("TRT")
digoleWriteText("Name: Bedroom")
digoleWriteCommand("TRT")
digoleWriteText("Access Level: 3")


