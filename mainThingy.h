#ifndef MODEM_CONFIG_H
#define MODEM_CONFIG_H

int mainThingy_init();
/// @brief Seng gps data in CBOR format to the PUT: /gps route on the server
int send_gps_data(void);
#endif