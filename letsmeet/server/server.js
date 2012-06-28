/*
 * Backend logic as follows:
 * Global object meAndMyFriends is an array containing organizer objects.
 * [{"FRIENDS":[{"PHONE_NUMBER":"1234 5","LOC":"","NAME":"A1"}],"MYLOCATION":"13.03904389098032,77.55481144807625","MYID":"621503438","oldAverageLat":13.03904389098032,"oldAverageLng":77.55481144807625,"FSITEMS":[{"id":"fsobject","lat":13.033702018870002,"lng":77.56050057530747,"selected":""},{"id":"fsobject","lat":13.012304509616587,"lng":77.55502052145634,"selected":""},{"id":"fsobject","lat":13.030157,"lng":77.57057341,"selected":""},{"id":"fsobject","lat":13.039449854749858,"lng":77.5559161561223,"selected":""}]},{"FRIENDS":[{"PHONE_NUMBER":"1234 5","LOC":"","NAME":"A1"}],"MYLOCATION":"13.03904389098032,77.55481144807625","MYID":"-1630067907"}]
 * FSITEMS - Object containing the data returned from FourSquare
 * Here's the flow:
 * On each Post - Store the objects - Check if the Organizer id already exists and then store.
 * On each get - Loop through the array object
 *  - For each organizer , calculate the average of organizer location and friends location
 *  - Call FourSquare API only if the average has changed.
 *  - Populate the organizer object with the Foursquare locations.
 *  - Return
**/



var http = require("http");
var https = require("https");
var url = require("url");
var sys = require('sys');
var fs = require('fs');
var temp = '';
var meAndMyFriends = [];

function onRequest(request,response) {
  var postData = '';
  var pathName = url.parse(request.url).pathname;
	var id = pathName.split("=");
	var userId = id[1];
  var headers = request.headers;
// Post request processing
    if(request.method ==  'POST') {
      request.setEncoding('utf-8');
      request.on('data', function(chunk){
        sys.debug("In data event");
        postData += chunk.toString();
      }).on("end", function(){
        //console.log("END OF REQUEST:::"+ postData);
        if(postData !== null) {
				var pflag = false;
				console.log(meAndMyFriends.length);
				// If the initial array is empty imply this is the first request
				if(meAndMyFriends.length > 0) {
					for (var fIndex = 0;fIndex < meAndMyFriends.length;++fIndex) {
						var porganizer = meAndMyFriends[fIndex];
						if(porganizer.MYID === userId) {
							pflag = true;
						}
						var pfriends = porganizer.FRIENDS;
						for(var f = 0; f< pfriends.length; ++f) {
							var phoneNumber = pfriends[f].PHONE_NUMBER;
							if(phoneNumber === userId) {
								pflag = false;
							}
						}
					}
				 if(pflag === false) {
         	storeAndroidUserData(postData,pathName);
				 }
        	response.writeHead(200,{"Context-type":"text/html"});
        	response.end();
        	} else {
         	storeAndroidUserData(postData,pathName);
        	response.writeHead(200,{"Context-type":"text/html"});
        	response.end();
					}
				}
      });
    } else if(request.method =='GET') {
				var flag = false;
				for (var fIndex = 0;fIndex < meAndMyFriends.length;++fIndex) {
					var organizer = meAndMyFriends[fIndex];
				// Check if the average is new 
					if (organizer.oldAverageLat === undefined) {
						organizer.oldAverageLat = 0.0;
						organizer.oldAverageLng = 0.0;
					}
					if(organizer.MYID === userId) {
						flag = true;
					}
					var friends = organizer.FRIENDS;
					for(var f = 0; f< friends.length; ++f) {
						var phoneNumber = friends[f].PHONE_NUMBER;
						if(phoneNumber === userId) {
							flag = true;
							console.log("Matched with friend" + userId);
						}
					}
					if(flag === true) {
							sys.debug("in get" + userId);
							response.writeHead(200,{"Content-type":"application/json"});
							var average = calculateAverage(organizer);
							var averageArr = average.split(",");
							var averageLat = parseFloat(averageArr[0]);
							var averageLng = parseFloat(averageArr[1]);
							if(organizer.oldAverageLat === 0.0 && organizer.oldAverageLng === 0.0) {
								console.log("First time average");
								organizer.oldAverageLat = averageLat;
								organizer.oldAverageLng = averageLng;
								getFourSquareData(average, function(tempp) {
								 var resp = tempp.response;
								 //console.log("Response from FS" + JSON.stringify(resp));
								 var groups = resp.groups;
								 if(groups !== null) {	
								 var venLen = groups.length;
								 for(var fsI = 0; fsI < venLen; ++fsI ) {
									var items = groups[fsI].items;
									var fsObjectArray = [];
        					for(var it=0;it<items.length;++it) {
													var fLat = items[it].venue.location.lat;	
													var fLng = items[it].venue.location.lng;	
													//console.log(fLat);
													var selected = "";
													var fsObject = {
															id:"fsobject",
															lat:fLat,
															lng:fLng,
															selected:selected
													};
													//console.log("FSOBJECT" + JSON.stringify(fsObject));
													fsObjectArray.push(fsObject);
									}
								}
								organizer.FSITEMS = fsObjectArray;
								console.log("Int organizer" + JSON.stringify(organizer));
								}
							});
							response.write(JSON.stringify(organizer));
							response.end();
					
							} else if(organizer.oldAverageLat !== averageLat || organizer.oldAverageLng !== averageLng) {
								 getFourSquareData(average, function(temp) {
								 var resp = temp.response;
								 var venLen = resp.groups.length;
								 var groups = resp.groups;
								 for(var fsI = 0; fsI < venLen; ++fsI ) {
									var items = groups[fsI].items;
									var fsObjectArray = [];
        					for(var it=0;it<items.length;++it) {
													//alert(JSON.stringify(items[it].venue));
													var fLat = items[it].venue.location.lat;	
													var fLng = items[it].venue.location.lng;	
													var selected = "";
													var fsObject = {
															id: "fsobject",
															lat: fLat,
															lng:fLng,
															selected:selected
													};
													fsObjectArray.push(fsObject);
									}
								}
								organizer.FSITEMS = fsObjectArray;
								});
								organizer.oldAverageLat = averageLat;
								organizer.oldAverageLng = averageLng;
							response.write(JSON.stringify(organizer));
							response.end();
							}
							response.write(JSON.stringify(organizer));
							response.end();
						}
  		}
	}
		
}

http.createServer(onRequest).listen(8888);


/*
 * Method is called when the android app posts data using the /upload in it's URL.
 */
function storeAndroidUserData(postData,pathName) {
  //console.log("PathName:::" + pathName);
  //var id = pathName.split("=");
  //var uniqueId = id[1];
  var data = JSON.parse(postData);
  var dataArray = data.FRIENDS;
  sys.debug("data array is" + dataArray);
  if(dataArray !== undefined) {
	sys.debug("Post sent from organizer");
  	var organizer = data;
	meAndMyFriends.push(data);
	sys.debug(JSON.stringify(meAndMyFriends));
  } else {
	sys.debug("Data coming from friends");
	var id = data.id;
	var loc = data.loc;
		for(var mIn = 0;mIn < meAndMyFriends.length; ++mIn) {
		var values = meAndMyFriends[mIn].FRIENDS;
		for(var i=0;i<values.length;++i) {
			if(values[i].PHONE_NUMBER === id) {
				sys.debug("Friends location found");
				values[i].LOC = loc;	
			}
		}	
		}
		sys.debug(meAndMyFriends.FRIENDS);
	}
  
}

/*
 * Obtain data from foursquare, the callback is called once the request returns.
 */
function getFourSquareData(average, getData) {
	var fsAverageArr = average.split(",");
	var fsLat = fsAverageArr[0];
	var fsLng = fsAverageArr[1];
    var fsUrl = '/v2/venues/explore?client_id=N3RMDIQFPLHPLTJMRKLNV4ULXJCXWOPY3HZ2EOMXBSJWU1SW&client_secret=EQKC52S2W5RP1N2CQYQ2CPM1H55PISXUE41UIG55LEJQSDTY&section=coffee&oauth_token=4ENF4MW3PJMMUPS5D5FJC1OP5WXRVF2FFAZCMFG1PDSLBRAH&v=20120516&limit=4&intent=browse&radius=3000&ll=' + fsLat + ',' + fsLng ;
	var temp = "";
	//console.log("Four square URL" + fsUrl);
	https.get({host:'api.foursquare.com', path:fsUrl}, function(res) {
		console.log("Got FS Response" + res.statusCode);
		res.on('data', function(chunk) {
			temp = temp.concat(chunk);
		});
		res.on('end', function() {
			//console.log("Inside the foursquare method" + JSON.stringify(temp));
			getData(JSON.parse(temp));
		});
	}).on('error', function(e) {
			console.log("ERROR IN FS" + e.message);
	});

}

/*
 * Calculate the average location value for each organizer.
 * Note if the friends reply with the location, the average will be calculated in the next call
 */
function calculateAverage(organizer) {
	var averageLat = 0.0;
  var averageLng = 0.0;
  var aLat = 0.0;
  var aLng = 0.0;

	var organizerArr = organizer.MYLOCATION.split(",");
	var organizerLat = parseFloat(organizerArr[0]);
	var organizerLng = parseFloat(organizerArr[1]);

	aLat = aLat + organizerLat;
	aLng = aLng + organizerLng;
	var friends = organizer.FRIENDS;
	for(var m=0;m<friends.length;++m) {
		var friendLoc = friends[m].LOC;
		if(friendLoc !== "") {
						var friendArr = friendLoc.split(",");
						var friendLat = parseFloat(friendArr[0]);
						var friendLng = parseFloat(friendArr[1]);
						aLat = aLat + friendLat;
						aLng = aLng + friendLng;
						averageLat = aLat / (m+2);
						averageLng = aLng / (m+2);
		} else {
			averageLat = aLat;
			averageLng = aLng;
		}
		
	}
	console.log ("Average Values" + averageLat + ":" + averageLng );
	var averageString = averageLat + "," + averageLng;
	return averageString;
}
