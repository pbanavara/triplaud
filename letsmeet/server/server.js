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
        console.log("END OF REQUEST:::"+ postData);
        if(postData !== null) {
					var org = id[2];
					if(org !== undefined) {
						markSelectedPoints(org, userId, postData);
						return;
					}
				// If the initial array is empty imply this is the first request
				if(meAndMyFriends.length > 0) {
					for (var fIndex = 0;fIndex < meAndMyFriends.length;++fIndex) {
						var porganizer = meAndMyFriends[fIndex];
						// Check to make sure organizers with the same id do not get added to the global array
						console.log("PORGANIZERR :::" + porganizer.MYID);
						if(porganizer.MYID !== userId) {
         			storeAndroidUserData(postData);
						}
					}
				} else {
         	storeAndroidUserData(postData);
				}
			}
      response.writeHead(200,{"Context-type":"text/html"});
      response.end();
		});
		
    } else if(request.method =='GET') {
        response.writeHead(200,{"Context-type":"application/json"});
				for (var fIndex = 0;fIndex < meAndMyFriends.length;++fIndex) {
					var lorg = meAndMyFriends[fIndex];
					if(lorg.MYID === userId) {
						console.log("Sending response to the user ::::" + JSON.stringify(lorg) );
						response.write(JSON.stringify(lorg));
						response.end();
					}
  		}
	}
		
}

http.createServer(onRequest).listen(8888);


/*
 * Method is called when the android app posts data using the /upload in it's URL.
 */
function storeAndroidUserData(postData) {
  var data = JSON.parse(postData);
	var organizer = data;
  var dataArray = data.FRIENDS;
  sys.debug("data array is" + dataArray);
  if(dataArray !== undefined || data.MYLOCATION !== undefined) {
		sys.debug("Post sent from organizer");
		var obtAv = calculateAverage(organizer);
		organizer.average = obtAv;	
			getFourSquareData(obtAv, function(tempp) {
	 	  var resp = tempp.response;
			// The true flag is added so that only the organizer objects are pushed to the main array
			processFourSquareData(resp, organizer, true);
		});
		sys.debug("Global object now is" + JSON.stringify(meAndMyFriends));
  } else {
		sys.debug("Post sent from friends");
		var id = data.id;
		var loc = data.loc;
		for(var mIn = 0;mIn < meAndMyFriends.length; ++mIn) {
			var org = meAndMyFriends[mIn];
	 		var values = org.FRIENDS;
			if(values != undefined) {
						for(var i=0;i<values.length;++i) {
							if(values[i].PHONE_NUMBER === id) {
								sys.debug("Friends location found");
								values[i].LOC = loc;	
							}
						}	
			}
			var frAvg = calculateAverage(org);
			org.average = obtAv;
			getFourSquareData(frAvg, function(temp) {
	 	  	var resp = temp.response;
				processFourSquareData(resp, org, false);
			});
			
		}
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
	console.log("Four square URL" + fsUrl);
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
	if(friends != undefined) {
	var avInd = 1;
	for(var m=0;m<friends.length;++m) {
		var friendLoc = friends[m].LOC;
		if(friendLoc !== "") {
						avInd++;
						var friendArr = friendLoc.split(",");
						var friendLat = parseFloat(friendArr[0]);
						var friendLng = parseFloat(friendArr[1]);
						aLat = aLat + friendLat;
						aLng = aLng + friendLng;
		}
						averageLat = aLat / (avInd);
						averageLng = aLng / (avInd);
						console.log("In loop" + averageLat);
	}
  } else {
		averageLat = organizerLat;
		averageLng = organizerLng;
  }
	console.log ("Average Values" + averageLat + ":" + averageLng );
	var averageString = averageLat + "," + averageLng;
	return averageString;
}

/*
 * This method is for marking the foursquare locations as selected - 3 states - None, yes, maybe
 */

function markSelectedPoints(org, id, postData) {
	var data = JSON.parse(postData);
  var uIdA = id.split("&");
	var uID = uIdA[0];
	for(var i = 0; i< meAndMyFriends.length;++i) {
		var organizer = meAndMyFriends[i];
		if (organizer.MYID === org) {
			for(var j = 0; j< organizer.FSITEMS.length;++j) {
				if (parseInt(uID) === organizer.FSITEMS[j].id) {
						console.log("FS ITEM MARKED");
						console.log(data.selected);
						organizer.FSITEMS[j].selected = data.selected;
	
				}
			}	
	 }
}

}

function processFourSquareData(resp, organizer, flag) {
		 //console.log("Response from FS" + JSON.stringify(resp));
		 console.log("FLAG IN FS " + flag);
			 var groups = resp.groups;
			 if(groups !== null) {	
				 var venLen = groups.length;
				 for(var fsI = 0; fsI < venLen; ++fsI ) {
						var items = groups[fsI].items;
						var fsObjectArray = [];
     				for(var it=0;it<items.length;++it) {
							var fLat = items[it].venue.location.lat;	
							var fLng = items[it].venue.location.lng;	
							var address = items[it].venue.location.address;
							if(address === undefined || address === null) {
								address = "address";
							}
							var name = items[it].venue.name;
							var phoneNumber = items[it].venue.contact.phone;
							//console.log(fLat);
							var selected = "";
							var fsObject = {
									id:it,
									name:name,
									lat:fLat,
									lng:fLng,
									address:address,
									phone:phoneNumber,
									selected:selected
							};
							//console.log("FSOBJECT" + JSON.stringify(fsObject));
							fsObjectArray.push(fsObject);
						}
				}
			organizer.FSITEMS = fsObjectArray;
			if(flag === true) {
				meAndMyFriends.push(organizer);
			}
			console.log("Final organizer object" + JSON.stringify(meAndMyFriends));
			}
}

