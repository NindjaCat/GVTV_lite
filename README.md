# GVTV
Sat-IP Transcoding Server (uses VLCPlayer)<br>
This server application requests a SAT > IP Server stream. The stream is then transcoded and restreamed at http://localhost:streamPort/go.ts If you configure your router accordingly, you can watch the stream outside of your LAN. The client needs to be configured with your DynDNS address or your current external ip-address.

The application scans for SAT-IP servers using DLNA and writes an initial config file that needs to be edited after the first run. The jar and the dlna_channellist.xml file have to be in the same directory.
Vlc player is not included.

Currently triax SAT-IP Servers are supported. Kathrein and Xoro devices may work, but are not yet tested. AVM FRITZ DVB-C Devices are supported. You need to provide tvsd.m3u and tvhd.m3u. You can code your own client or contact me (mwegehaupt@web.de). Clients for ios, android and windows are available and being tested.

Read the wiki for further information: https://github.com/NindjaCat/GVTV_lite/wiki

<img src="https://github.com/NindjaCat/GVTV_lite/blob/master/SatIp2.png" alt="Sat > IP ">

<img src="https://github.com/NindjaCat/GVTV_lite/blob/master/gvtv_example.PNG" alt="Sat > IP example">


# Clients

<b>iOS</b>

https://testflight.apple.com/join/tElTxhtH

<b>Android</b>

Closed testing has started. Contact me.

<table>
  <tr>
    <td><img src="https://github.com/NindjaCat/GVTV_lite/blob/master/Android-Client.PNG" alt="AndroidClient1"></td>
    <td><img src="https://github.com/NindjaCat/GVTV_lite/blob/master/Android-Client2.PNG" alt="AndroidClient2"></td>
  </tr>
</table>

<b>Windows / Java</b>

<img src="https://github.com/NindjaCat/GVTV_lite/blob/master/Windows-Java.PNG" alt="WindowsClient">