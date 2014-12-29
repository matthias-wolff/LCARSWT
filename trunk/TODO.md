### TODO
* publish on Maven Central, see http://central.sonatype.org/pages/ossrh-guide.html
* implement locked state of screen
* WWJ: add a (WMS?) cloud layer
  * example for WMS Layers: `gov.nasa.worldwindx.examples.WMSLayerManager`
  * cloud WMS e.g. at http://wms.openweathermap.org/service

### TO-THINK-ABOUT
* a browser-"`GShape`" acting as the screen-side front end of `EBrowser` (which 
    would then become an `EElement`)
  * comm. screen->panel: `BrowserEvent`
  * comm. panel->screen: fields in browser-"`GShape`"(?)