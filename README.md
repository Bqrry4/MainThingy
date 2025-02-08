## About

A prototype of a smart collar similar to tractive, consisting of a hardware, mobile and server components.
Some brief ideas about the project:

- The hardware component, the collar itself, is based on Thingy91 paired with BNO086.
- The firmware is written using [nRF Connect SDK](https://www.nordicsemi.com/Products/Development-software/nRF-Connect-SDK).
- To enhance battery life it uses CoAP and Bluetooth low energy as external communication protocols and LPUART for inter-chip.
<img src="readme/block_scheme.png" width="600">

- The mobile component is an Android native app built with [Jetpack Compose](https://developer.android.com/compose).
Example of app usage:


<div style="display: flex;">
  <img src="readme/map_module.png" width="600">
</div>
<div style="display: flex;">
  <img src="readme/profile.jpg" width="300">
  <img src="readme/track.gif" width="300">
</div>
<img src="readme/notifis.png" width="300">


- The server component act as a proxy between the two when those are not in close proximity, translating the CoAP request to HTTP.

<img src="readme/server_structure.png" width="400">
