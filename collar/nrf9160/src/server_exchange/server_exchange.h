#ifndef SERVER_EXCHANGE_H
#define SERVER_EXCHANGE_H

/* @brief Init server exchange
 * @return 0 on succes
 */
int server_exchange_init();
/// @brief Seng gps data in CBOR format to the POST: /loc route on the server
int send_gps_data(void);
#endif