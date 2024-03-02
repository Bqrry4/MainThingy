#ifndef MODEM_CONFIG_H
#define MODEM_CONFIG_H

/// @brief Init lte
/// @return 0 on success, anything else otherwise
int lte_init();
/// @brief  Init and start GNSS
/// @return 0 on success, anything else otherwise
int gnss_init();
/// @brief Enable LTE
/// @return 0 on success, failed otherwise
int activate_lte();
/// @brief Disable LTE
/// @return 0 on success, failed otherwise
int deactivate_lte();
/// @brief Enable GNSS
/// @return 0 on success, failed otherwise
int activate_gnss();
/// @brief Disable GNSS
/// @return 0 on success, failed otherwise
int deactivate_gnss();

/// @brief Checks for current lte connection status and get blocked till Timeout
/// @param seconds Timeout in seconds
/// @return lte connection status
bool wait_for_lte_connection(uint16_t seconds);

#endif