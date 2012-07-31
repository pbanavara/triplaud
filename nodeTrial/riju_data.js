var map,cloudmadeAttrib,cloudmadeUrl,cloudmadeAttribution,cloudmade;
var mapCenter = new L.LatLng(12.970214, 77.56029); //Default the map Center to  Bangalore
var markerData = [];

function initializeMap() {
  map = new L.Map('map');
  cloudmadeAttrib = 'Data, imagery and map information provided by <a href="http://open.mapquest.co.uk" target="_blank">MapQuest</a>, <a href="http://www.openstreetmap.org/" target="_blank">OpenStreetMap</a> and contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/" target="_blank">CC-BY-SA</a>';

  cloudmadeUrl = 'http://{s}.tile.cloudmade.com/BC9A493B41014CAABB98F0471D759707/22677/256/{z}/{x}/{y}.png';
  cloudmadeAttribution = 'Map data &copy; 2011 OpenStreetMap contributors, Imagery &copy; 2011 CloudMade';
  cloudmade = new L.TileLayer(cloudmadeUrl, {maxZoom: 18, attribution: cloudmadeAttribution});
  map.setView(mapCenter,13).addLayer(cloudmade);

}

function getInput() {
  var url;
  var phone = document.getElementById("phone").value;
  var baseUrl = "https://api.parse.com/1/classes/taxilocations?where={\"ID\":\"";
  if(phone !== "") {
    url = baseUrl.concat(phone).concat("\"}").concat("&limit=10000");
  } else {
    url ="https://api.parse.com/1/classes/taxilocations?limit=10000";  
  }
  getDataFromParse(url);
}

/*
 * Retrieve data from the parse library
 */
function getDataFromParse(url) {
  var xhr = new XMLHttpRequest();
  xhr.open("GET", url, true);
  // To Do add the API Keys when you demo to clients.
  xhr.setRequestHeader("X-Parse-Application-Id", "J3Q5NuBIcYNTeHFmMy3SzNQL2Mi9UKKG5Vga0H9O");
  xhr.setRequestHeader("X-Parse-REST-API-Key", "HO7pHkkSNuUTA8tVlEfQVziNknTVlsUcamAexuNJ");
  xhr.setRequestHeader("Content-Type", "application/json");
  xhr.send();
  xhr.onreadystatechange = function() {
    if (xhr.readyState == 4) {
      var result = JSON.parse(xhr.responseText);
      alert(result.results.length);
      displayTaxiPoints(result); 
    }
  }

}
/*
 * Display the data points using markers on a leaflet based UI
 */
function displayTaxiPoints(result) {
  var marker;
  clearMap();
  var data = result.results; 
  for(var index=0;index<data.length;++index){
    var lat = data[index].locations.latitude;
    var lon = data[index].locations.longitude;
    var phoneNumber = data[index].ID;
    var latLng = new L.LatLng(lat,lon);
      marker = new L.Marker(latLng);
    map.addLayer(marker);
    markerData[index] = marker;
  }
}

function clearMap() {
  if(markerData !== null) {
   for(var mapIndex=0; mapIndex<markerData.length;++mapIndex) {
     map.removeLayer(markerData[mapIndex]);
   }
  }
  markerData = [];
}
