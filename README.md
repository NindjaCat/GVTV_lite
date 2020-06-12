# GVTV
Sat-IP Transcoding Server (uses VLCPlayer and GSTREAMER)<br>
This server application is able to get a SAT-IP stream via http or rtsp from a SAT > IP Server. The stream is then transcoded and restreamed at http://localhost:chosenStreamPort/go.ts If you configure your router accordingly, you can watch the stream outside of your LAN. The client needs to be configured with your DynDNS address or your current external ip-address.

The application scans for SAT-IP servers using DLNA and writes an initial config file that needs to be edited after the first run. The jar and the dlna_channellist.xml file have to be in the same directory.
Vlc player and GSTREAMER (full installation needed) are not included. They are expected to be located at C:\Program Files\VideoLAN\VLC and C:\gstreamer\1.0\x86_64\bin.

Currently triax SAT-IP Servers are supported. Kathrein and Xoro devices may work, but are not yet tested. FRITZ!WLAN Repeater DVB-C works using the tcp option, if you provide the files tvsd.m3u and tvhd.m3u. You can code your own client or contact me (mwegehaupt@web.de). Clients for ios, android and windows are available but still work in progress.

Read the wiki for further information: https://github.com/NindjaCat/GVTV_lite/wiki

<img src="https://github.com/NindjaCat/GVTV_lite/blob/master/GVTV_lite/SatIp2.png" alt="Sat > IP ">

<img src="https://github.com/NindjaCat/GVTV_lite/blob/master/GVTV_lite/gvtv_example.PNG" alt="Sat > IP example">


# Clients

<b>iOS</b>

My current build is being reviewed by Apple. If you like to test the app, contact me.

<b>Android</b>

<table>
  <tr>
    <td><img src="https://github.com/NindjaCat/GVTV_lite/blob/master/GVTV_lite/Android-Client.PNG" alt="AndroidClient1"></td>
    <td><img src="https://github.com/NindjaCat/GVTV_lite/blob/master/GVTV_lite/Android-Client2.PNG" alt="AndroidClient2"></td>
  </tr>
</table>

<b>Windows / Java</b>

<img src="https://github.com/NindjaCat/GVTV_lite/blob/master/GVTV_lite/Windows-Java.PNG" alt="WindowsClient">
