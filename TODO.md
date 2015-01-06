### TODO
* publish on Maven Central, see http://central.sonatype.org/pages/ossrh-guide.html
* implement locked state of screen
* add video player demo
  * using http://jcodec.org pure Java codecs?

### TO-THINK-ABOUT
* a browser-"`GShape`" acting as the screen-side front end of `EBrowser` (which 
    would then become an `EElement`)
  * comm. screen->panel: `BrowserEvent`
  * comm. panel->screen: fields in browser-"`GShape`"(?)