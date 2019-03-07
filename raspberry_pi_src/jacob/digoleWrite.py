#!/usr/bin/env python3

import smbus as smbus
from digole import lcd

i2c = smbus.SMBus(1)
address = 0x27
text = "TTHello World!"

i2c.write_block_data(address, 0x00, [0x43, 0x4c])
textToWrite = [ord(i) for i in text]
i2c.write_block_data(address, 0x00, textToWrite)