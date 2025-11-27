### FITTONIA

<ins>**FI**</ins>le <ins>**T**</ins>ransfer <ins>**T**</ins>ech <ins>**O**</ins>ver a <ins>**N
**</ins>etwork using <ins>**I**</ins>P <ins>**A**</ins>ddresses

An app for transfering files between Android devices.

1. Peer to peer: Connects to other devices by supplying IP addresses. IP addresses can be encoded as
   something easier to communicate with a peer. Supports adding devices as 'destinations' which
   saves their connection info to reuse quickly later.
2. Security: User must enter the destination phone's access-code in order to send files. It's
   impossible to send files to a recipient that hasn't shared their access-code. Files are also
   encrypted before being send over the network using Android keystore encryption.
3. No limits: Each transfer can send any amount of files at any size, and the app can send and
   receive files in the background.

Limitations (there are plans to address these eventually):

- Only supports local networks. Connecting devices must be on the same local network to see each
  other.
- Doesn't support sending folders.
- Doesn't support advanced transfer actions (pausing, resuming, throttling, etc.).