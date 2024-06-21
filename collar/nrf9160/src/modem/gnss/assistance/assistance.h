#ifndef ASSISTANCE_H
#define ASSISTANCE_H

/// @brief Init the assistance location module
/// @param work_q a pointer to an existing working queue for reusing purposes
/// @return 0 on success initialization
int assistance_init(struct k_work_q *work_q);

/// @brief Request agnss assistance
/// @param agnss_request agnss frame that was read from NRF_MODEM_GNSS_EVT_AGNSS_REQ event
/// @return 
int assistance_request(struct nrf_modem_gnss_agnss_data_frame *agnss_request);
#endif