#ifndef ASSISTANCE_H
#define ASSISTANCE_H

/// @brief Init the assistance location module
/// @return 0 on success initialization
int assistance_init();

/// @brief Request agnss assistance
/// @param agnss_request agnss frame that was read from NRF_MODEM_GNSS_EVT_AGNSS_REQ event
/// @return 
int assistance_request(struct nrf_modem_gnss_agnss_data_frame *agnss_request);
#endif