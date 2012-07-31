var map,cloudmadeAttrib,cloudmadeUrl,cloudmadeAttribution,cloudmade;
var mapCenter = new L.LatLng(12.970214, 77.56029); //Default the map Center to  Bangalore
var markerData = [];
var intervalId;
var markerCount = 0;

function initializeMap () {
  map = new L.Map('map');
  cloudmadeAttrib = 'Data, imagery and map information provided by <a href="http://open.mapquest.co.uk" target="_blank">MapQuest</a>, <a href="http://www.openstreetmap.org/" target="_blank">OpenStreetMap</a> and contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/" target="_blank">CC-BY-SA</a>';

  cloudmadeUrl = 'http://{s}.tile.cloudmade.com/BC9A493B41014CAABB98F0471D759707/22677/256/{z}/{x}/{y}.png';
  cloudmadeAttribution = 'Map data &copy; 2011 OpenStreetMap contributors, Imagery &copy; 2011 CloudMade';
  cloudmade = new L.TileLayer(cloudmadeUrl, {maxZoom: 18, attribution: cloudmadeAttribution});
  map.setView(mapCenter,13).addLayer(cloudmade);
  map.on('click', onMapClick);

}

function onMapClick(e) {
  var lat = e.latlng.lat;
  var lon = e.latlng.lng;
  var baseUrl ="https://api.parse.com/1/classes/taxilocations"; 
  var rijuUrl = baseUrl + "?where={\"locations\": {\"$nearSphere\":{\"__type\":\"GeoPoint\",\"latitude\":";
  var newUrl = rijuUrl + lat + (",\"longitude\":") + lon + "},\"$maxDistanceInKilometers\":1.0}}&limit=1000";
  //var url = "https://api.parse.com/1/classes/taxilocations?where={\"location\": {\"$nearSphere\":{\"_type\":\"GeoPoint\",\"latitude\":";
  getDataFromParse(newUrl);
}

/*
 * Retrieve data from the parse library
 */
function getDataFromParse(url) {
  var xhr = new XMLHttpRequest();
  xhr.open("GET", url, true);
  /*
   * See Parse documentation. Setting the headers for the Parse Application
   */
  xhr.setRequestHeader("X-Parse-Application-Id", "tg0f9TzBklEtZuTpD8rVWgtrY6KtwKOBCZzzX7rY");
  xhr.setRequestHeader("X-Parse-REST-API-Key", "DkZgczH3SUbzxYdnDEOwwl5dGWJmib42jYkQJukU");
  xhr.setRequestHeader("Content-Type", "application/json");
  xhr.send();
  xhr.onreadystatechange = function() {
    if (xhr.readyState == 4) {
      var result = JSON.parse(xhr.responseText);
      var totalLength = result.results.length;
      if(totalLength == 0) {
       alert("Nothing found around this area");
      } else {
        displayTaxiPoints(result); 
      }
  }
}
}
/*
 * Display the data points using markers on a leaflet based UI
 */
function displayTaxiPoints(result) {
  clearMap();
  var data = result.results; 
  for(var index=0;index<data.length;++index){
    var lat = data[index].locations.latitude;
    var lon = data[index].locations.longitude;
    var phoneNumber = data[index].ID;
    var latLng = new L.LatLng(lat,lon);
    var marker = new L.Marker(latLng);
    map.addLayer(marker);
    markerData[index] = marker;
  }
  markerCount = markerCount + 1;
}

function clearMap() {
  if(markerData !== null) {
   for(var mapIndex=0; mapIndex<markerData.length;++mapIndex) {
     map.removeLayer(markerData[mapIndex]);
   }
  }
  markerData = [];
}
