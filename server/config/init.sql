Use nyan;

CREATE TABLE Devices(
    deviceID int primary key AUTO_INCREMENT,
    macAddress varchar(17) unique not null, -- string format for mac address (ex: ff:ff:ff:ff:ff:ff)
    secret varchar(256) -- format TBD
);

CREATE TABLE GSPoints(
    time_stamp datetime(3) primary key,
    longitude float not null,
    latitude float not null,
    accuracy float,
    deviceID int not null,
    CONSTRAINT fk_deviceID foreign key (deviceID) references Devices(deviceID)
);