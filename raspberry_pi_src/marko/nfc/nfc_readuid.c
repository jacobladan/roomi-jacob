#include <stdlib.h>
#include <nfc/nfc.h>
static void
print_hex(const uint8_t *pbtData, const size_t szBytes)
{
  size_t  szPos;
  char uid[20];
  int uidint[20];

  for (szPos = 0; szPos < szBytes; szPos++) {
    	//Displays values correctly
	printf("Hex: %02x", pbtData[szPos]);
	uidint[szPos] = pbtData[szPos];
	//printf(uid, "%02x", pbtData[szPos]);
	//sprintf(uid, "%c  ", pbtData[szPos]);
	printf("%c",uid[szPos]);
	//sprintf(uid[szPos],"%02x",pbtData[szPos]);
	//printf("%c",&uid[szPos]);
        printf("Int:%d",uidint[szPos]);
  }
  printf("\n");
}
int main(int argc, const char *argv[])
{

  nfc_device *pnd;
  nfc_target nt;
  unsigned char *uid;
  nfc_context *context;
  nfc_init(&context);
  if (context == NULL) {
    printf("Unable to init libnfc (malloc)\n");
    exit(EXIT_FAILURE);
  }

  pnd = nfc_open(context, NULL);

  if (pnd == NULL) {
    printf("ERROR: %s\n", "Unable to open NFC device.");
    exit(EXIT_FAILURE);
  }
  if (nfc_initiator_init(pnd) < 0) {
    nfc_perror(pnd, "nfc_initiator_init");
    exit(EXIT_FAILURE);
  }

  //printf("NFC reader: %s opened\n", nfc_device_get_name(pnd));

  const nfc_modulation nmMifare = {
    .nmt = NMT_ISO14443A,
    .nbr = NBR_106,
  };
  if (nfc_initiator_select_passive_target(pnd, nmMifare, NULL, 0, &nt) > 0) {
    //printf("The following (NFC) ISO14443A tag was found:\n");
    //printf("    ATQA (SENS_RES): ");
    //print_hex(nt.nti.nai.abtAtqa, 2);
    //printf("       UID (NFCID%c): ", (nt.nti.nai.abtUid[0] == 0x08 ? '3' : '1'));
      print_hex(nt.nti.nai.abtUid, nt.nti.nai.szUidLen);
    //printf("%c",&uid);
    //printf("      SAK (SEL_RES): ");
    //print_hex(&nt.nti.nai.btSak, 1);
    if (nt.nti.nai.szAtsLen) {
      printf("          ATS (ATR): ");
      print_hex(nt.nti.nai.abtAts, nt.nti.nai.szAtsLen);
    }
  }
  nfc_close(pnd);
  nfc_exit(context);
  exit(EXIT_SUCCESS);
}
