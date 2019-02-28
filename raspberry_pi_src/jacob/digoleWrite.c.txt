#include <unistd.h>
#include <stdio.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/i2c-dev.h>
#include <string.h>

int main(void) {
	int file_i2c;
	int addr = 0x27;
	char *filename = (char*) "/dev/i2c-1";
	char message[100] = "Hello, World!";

	printf("\nStarting connection with Digole LCD...\n");
	// Opening I2C bus connection
	if ((file_i2c = open(filename, O_RDWR)) < 0) {
		printf("\nFailed to open I2C bus.");
		return -1;
	}
	// Setting LCD address communication
	if (ioctl(file_i2c, I2C_SLAVE, addr) < 0) {
		printf("\nFailed to communicate with slave");
		return -1;
	}
	// Main loop - Asking for message with stdin and displaying it on LCD
	while(strncmp(message, "-1", 2) != 0) {
		printf("Enter a message (max 100char) or -1 to quit: ");
		fflush(stdout);
		fgets(message, sizeof(message), stdin);
		// Clearing the LCD and setting the cursor on
		write(file_i2c, "CL", 2);
		write(file_i2c, "CS1", 3);
		// Entering text write mode. Writing the message. Closing write mode
		write(file_i2c, "TT", 2);
		write(file_i2c, message, sizeof(message));
		write(file_i2c, "0", 1);
	}
	// Clearing the screen and closing I2C file
	write(file_i2c, "CL", 2);
	close(file_i2c);

	return 0;
}

