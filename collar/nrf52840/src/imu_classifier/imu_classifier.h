#ifndef IMU_CLASSIFIER_H
#define IMU_CLASSIFIER_H

/**
 * @return 0 on succes, negative otherwise
 */
int imu_classifier_init();

extern uint64_t stationary_time;
extern uint64_t motion_time;
#endif