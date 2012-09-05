/*
 * Backend logic as follows:
 * [{"FRIENDS":[{"PHONE_NUMBER":"1234 5","LOC":"","NAME":"A1"}],"MYLOCATION":"13.03904389098032,77.55481144807625","MYID":"621503438","oldAverageLat":13.03904389098032,"oldAverageLng":77.55481144807625,"FSITEMS":[{"id":"fsobject","lat":13.033702018870002,"lng":77.56050057530747,"selected":""},{"id":"fsobject","lat":13.012304509616587,"lng":77.55502052145634,"selected":""},{"id":"fsobject","lat":13.030157,"lng":77.57057341,"selected":""},{"id":"fsobject","lat":13.039449854749858,"lng":77.5559161561223,"selected":""}]},{"FRIENDS":[{"PHONE_NUMBER":"1234 5","LOC":"","NAME":"A1"}],"MYLOCATION":"13.03904389098032,77.55481144807625","MYID":"-1630067907"}]
 * FSITEMS - Object containing the data returned from FourSquare
 * Here's the flow:
 * On each Post - Store the objects in the database - Check if the Organizer id already exists and then store.
 * On each get - Loop through the array object (This has now changed to a mongodb database)
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
var fs = require('fs');
var mongodb = require('mongodb');
var server = new mongodb.Server("127.0.0.1", 27017, {});
var db = new mongodb.Db('test', server, {});

// Open the global database object. It's a bad practice to open and close connection for every database operation.
// https://groups.google.com/forum/?fromgroups=#!topic/node-mongodb-native/8qAsFsUTPMk
db.open(function (err, db) {
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
    sys.log("END OF POST REQUEST:::" + postData);
    if(postData !== null) {
     var data = JSON.parse(postData);
     var org = id[2];
     if(org !== undefined) {
      markSelectedPoints(org, userId, postData);
      return;
     }
     if(pathName.indexOf('updateOrganizerLocation') !== -1) {
      sys.log("ORGANIZER LOCATION UPDATE");
      var orgId = data.MYID;
      var orgLocation = data.MYLOCATION;
      updateOrganizerLocation(orgId, orgLocation);
      response.writeHead(200,{"Context-type":"text/html"});
      response.end();
     } else if(pathName.indexOf('updateFriendLocation') !== -1) {
      sys.log("FRIEND LOCATION UPDATE");
      var friendId = data.MYID;
      var friendLocation = data.MYLOCATION;
      updateFriendLocation(friendId, friendLocation);
      response.writeHead(200,{"Context-type":"text/html"});
      response.end();
     }else {
      storeAndroidUserData(data, userId);
     }
    }
    response.writeHead(200,{"Context-type":"text/html"});
    response.end();
   });

  } else if(request.method =='GET') { // Get requests are repeated requests for fetching data. This is a high traffic query.
   response.writeHead(200,{"Context-type":"application/json"});
   if(pathName.indexOf("getLocation") != -1) {
    var parameterArray = pathName.split('&');
    var incomingArray = parameterArray[1].split('=');
    var incomingId = incomingArray[0];
    var incomingIdValue = incomingArray[1];
    //Check if the query is for locations and call the appropriate functions based on organizer or friend ids.
    if(incomingId === 'organizer') {
     getOrganizerLocation(incomingIdValue, function(values) {
      console.log("location request coming from organizer");
      if(values !== null) {
       response.write(JSON.stringify(values));
      } else {
       response.write(JSON.stringify({"Value":"None"}));
      }
      response.end();
     });
    } else if (incomingId === 'friend') {
     console.log("location request coming from friend");
     getFriendLocation(incomingIdValue, function(values) {
      response.write(JSON.stringify(values));
      response.end(); 
     });
    }
   } else {
    queryJSONObjects(userId, function(lorg) {
     if(lorg !== undefined) {
      sys.log("Sending response to the user ::::" + JSON.stringify(lorg) );
      response.write(JSON.stringify(lorg));
      response.end();
     } else {
      response.write(JSON.stringify({"Message":"No objects found yet"}));
      response.end();
     }
    });
   }

  }

 }
 http.createServer(onRequest).listen(8890);

 /*
  * Method is called when the android app posts data using the /upload in it's URL.
  */
 function storeAndroidUserData(data, userId) {
  var organizer = data;
  var occasion = organizer.OCCASION;
  var dataArray = data.FRIENDS;
  //sys.log("data array is" + dataArray);
  if(dataArray !== undefined || data.MYLOCATION !== undefined) {
   sys.debug("Post sent from organizer");
   //Check if user has already specified the location in the FSITEMS object
   if(organizer.FSITEMS === undefined || organizer.NOFS === undefined) {
    var obtAv = calculateAverage(organizer);
    if(organizer.average === undefined) {
     organizer.average = obtAv; 
     getFourSquareData(obtAv, occasion, function(tempp) {
      var resp = tempp.response;
      // The true flag is added so that only the organizer objects are pushed to the main array
      processFourSquareData(resp, organizer, true);
      sys.log ("Four square API called for the first organizer  Average");  
     });

    } else if(organizer.average !== obtAv) {
     getFourSquareData(obtAv, occasion,function(tempp) {
      var resp = tempp.response;
      // The true flag is added so that only the organizer objects are pushed to the main array
      processFourSquareData(resp, organizer, true);
      sys.log ("Four square API called for a new organizer Average");
     });
     organizer.average = obtAv;
    }
   }
   // User has specified the location, just push the organizer object into the main array.
   //sys.log("User has specified the location, just push the organizer object into the main array");
   writeJSONObjectsToDb(organizer);
   sys.log("Adding first organizer object to the main array " + JSON.stringify(organizer));

  } else {
   sys.log("Post sent from friends");
   //sys.log("Global object" + JSON.stringify(meAndMyFriends));
   var id = data.id;
   var loc = data.loc;
   updateFriendLocation(id, loc);
   queryJSONObjects(userId, function(org) {

    var values = org.FRIENDS;
    if(org.NOFS !== "yes") {
     var frAvg = calculateAverage(org);
     if(org.average === undefined) {
      org.average = frAvg; 
      getFourSquareData(frAvg, org.OCCASION, function(temp) {
       var resp = temp.response;
       sys.log ("Four square API called for the first Friend's Average");
       // The true flag is added so that only the organizer objects are pushed to the main array
       processFourSquareData(resp, org, false);
      });
     } else if(org.average !== frAvg) {
      getFourSquareData(frAvg, org.OCCASION, function(temp) {
       var resp = temp.response;
       // The true flag is added so that only the organizer objects are pushed to the main array
       processFourSquareData(resp, org, false);
       sys.log ("Four square API called for a new Friend's Average" );
       org.average = frAvg;

      });
     }
    }
   });

  }

 } 

 /*
  * Obtain data from foursquare, the callback is called once the request returns.
  */

 function calculateAverage(organizer) {
  var averageLat = 0.0;
  var averageLng = 0.0;
  var aLat = 0.0;
  var aLng = 0.0;
  var myLoc = "" + organizer.MYLOCATION;

  var organizerArr = myLoc.split(",");
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
   }
  } else {
   averageLat = organizerLat;
   averageLng = organizerLng;
  }
  sys.log ("Average Values" + averageLat + ":" + averageLng );
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
  queryJSONObjects(org, function(organizer) {
   for(var j = 0; j< organizer.FSITEMS.length;++j) {
    if (parseInt(uID) === organizer.FSITEMS[j].id) {
     sys.log("FS ITEM MARKED for organizer " + uID);
     sys.log(data.selected);
     organizer.FSITEMS[j].selected = data.selected;
    }
   } 
   updateOrganizerFourSquare(organizer.MYID, organizer.FSITEMS);
  });
 }

 /*
  * Process FourSquare data and append the organizer object to the main array.
  * Process foursquare data
  * Construct FSITEMS array in Organizer object.
  * Add organizer object to the main array meAndMyFriends if and only if the organizer doesn't exist already.
  */
 function processFourSquareData(resp, organizer, flag) {
  sys.log(JSON.stringify(resp));
  if(resp !== null) {
   var groups = resp.groups;
   if(groups !== undefined) { 
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
      //sys.log(fLat);
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
      sys.log("FSOBJECT" + JSON.stringify(fsObject));
      fsObjectArray.push(fsObject);
     }
    }
    organizer.FSITEMS = fsObjectArray;
    updateOrganizer(organizer.MYID, fsObjectArray);
   }
  } 
 }


 /*
  * The main array is an array of JSON Objects. Write this file to disk at an interval of 2 hours and flush the datastructure accordingly.
  */
 function writeMainArrayToFile() {
  var date = new Date().toISOString();
  var fileName = "mainJsonObjects";
  fileName = fileName.concat(date);
  var absoluteFileName = "/home/ec2-user/socialEyez/".concat(fileName).concat(".txt");
  fs.writeFile(absoluteFileName, JSON.stringify(meAndMyFriends), function(err) {
   if(err) {
    sys.log(err);
   } else {
    sys.log("The file was saved!");
    //meAndMyFriends = [];
   }
  }); 

 }

 function writeJSONObjectsToDb(lRecord) {
  db.collection('test_collection', function(error, collection) { 
   collection.insert(lRecord, {safe:true}, function(error, result) {
    if(error) { 
     throw error; 
    } else {
     console.log("Written successfully");

    }
   });
  });
 }

 function queryJSONObjects(lOrganizerId, getData) {
  var input = {};
  input.MYID = lOrganizerId;
  sys.log("INPUT" + JSON.stringify(input));
  db.collection('test_collection', function(error,collection) {
   collection.find(input).toArray(function(err, docs) {
    //console.log(docs);
    console.log("DDDD");
    getData(docs[0]);
   });
  });
 }

 function updateFriendLocation(friendId, loc) {
  var friend = "" + friendId;
  var location = "" + loc;
  var input = JSON.parse(JSON.stringify({'FRIENDS.PHONE_NUMBER' : friend}));
  var output = JSON.parse(JSON.stringify({'FRIENDS.$.LOC' : location}));
  db.collection('test_collection', function(error, collection) {
   collection.update(input, {"$set":output}, {safe:true}, function(err) {
    if(err) throw error;
   });
  });
 }

 function updateOrganizerLocation(organizerId, location) {
  var id = "" + organizerId;
  var loc = "" + location;
  var criteria = {"MYID":id};
  var output = {"MYLOCATION":loc};
  sys.log("INPUTTT :::" + JSON.stringify(criteria)+ ":::" + JSON.stringify(output) );
  db.collection('test_collection', function(error, collection) {
   collection.update(criteria, {"$set":output}, {upsert:true}, {safe:true}, function(err) {
    if(err) throw error;
    console.log("Organizer location updated");

   });
  });
 }

 function updateOrganizer(organizerid, fsObjectArray) {
  var id = "" + organizerid;
  var input = JSON.parse(JSON.stringify({'MYID' : id}));
  var output = JSON.parse(JSON.stringify({'FSITEMS' : fsObjectArray}));
  sys.log("INPUT" + JSON.stringify(input));
  sys.log("OUTPUT" + JSON.stringify(output));
  db.collection('test_collection', function(error, collection) {
   sys.log("inside collection");
   collection.update(input, {"$set":output}, {safe:true}, function(err) {
    if(err) throw error;
    sys.log("Organizer updated");
   });
  });
 }

 function updateOrganizerFourSquare(organizerid, fsItems) {
  var id = "" + organizerid;
  var input = JSON.parse(JSON.stringify({'MYID' : id}));
  var output = JSON.parse(JSON.stringify({'FSITEMS' : fsItems}));
  sys.log("INPUT" + JSON.stringify(input));
  sys.log("OUTPUT" + JSON.stringify(output));
  db.collection('test_collection', function(error, collection) {
   sys.log("inside collection");
   collection.update(input, {"$set":output}, {safe:true}, function(err) {
    if(err) throw error;
    sys.log("Organizer updated");
   });
  });
 }

 function getFourSquareData(average, occasion, getData) {
  var fsAverageArr = average.split(",");
  var fsLat = fsAverageArr[0];
  var fsLng = fsAverageArr[1];
  var fsUrl;
  if(occasion != undefined) {
   fsUrl = '/v2/venues/explore?client_id=N3RMDIQFPLHPLTJMRKLNV4ULXJCXWOPY3HZ2EOMXBSJWU1SW&client_secret=EQKC52S2W5RP1N2CQYQ2CPM1H55PISXUE41UIG55LEJQSDTY&section=coffee&oauth_token=4ENF4MW3PJMMUPS5D5FJC1OP5WXRVF2FFAZCMFG1PDSLBRAH&v=20120516&intent=browse&radius=2000&ll=' + fsLat + ',' + fsLng + '&query=' + occasion +'&limit=6';
  } else {
   fsUrl = '/v2/venues/explore?client_id=N3RMDIQFPLHPLTJMRKLNV4ULXJCXWOPY3HZ2EOMXBSJWU1SW&client_secret=EQKC52S2W5RP1N2CQYQ2CPM1H55PISXUE41UIG55LEJQSDTY&section=coffee&oauth_token=4ENF4MW3PJMMUPS5D5FJC1OP5WXRVF2FFAZCMFG1PDSLBRAH&v=20120516&intent=browse&radius=2000&limit=10&ll=' + fsLat + ',' + fsLng;
  } 
  sys.log("FS URL" + fsUrl);
  var temp = "";
  https.get({host:'api.foursquare.com', path:fsUrl}, function(res) {
   res.on('data', function(chunk) {
    temp = temp.concat(chunk);
   });
   res.on('end', function() {
    sys.log("Foursquare method called and returned"); 
    getData(JSON.parse(temp));
   });
  }).on('error', function(e) {
   sys.log("ERROR IN FS" + e.message);
  });

 }

 /*
  * Replacement for Parse - Used to get the current location for the organizer and friends, given the friend id
  */
 function getFriendLocation(friendId, getData) {
  var input = {"FRIENDS.PHONE_NUMBER" :friendId};
  console.log("INPUT FRIEND" + input);

  db.collection('test_collection', function(error,collection) {
   collection.find(input).toArray(function(err, docs) {
    console.log(docs);
    var returnObject = docs[0];
    var friends = returnObject.FRIENDS;
    var newObject = {};
    newObject.FRIENDS = friends;
    newObject.ORGID = returnObject.MYID;
    newObject.ORG_LOC = returnObject.MYLOCATION;
    getData(newObject);
   });
  });
 }

 /*
  * Replacement for Parse - Used to get the current location for the organizer and friends, given the organizer id
  */
 function getOrganizerLocation(organizerId, getData) {
  var input = {}
  input.MYID = organizerId;
  db.collection('test_collection', function(error,collection) {
   if(error) {
    throw error;
   }
   collection.find(input).toArray(function(err, docs) {
    if(err) {
     throw err;
    }
    if(docs.length > 0) {
     console.log(JSON.stringify(docs));
     var returnObject = docs[0];
     var friends = returnObject.FRIENDS;
     var newObject = {};
     newObject.FRIENDS = friends;
     newObject.ORGID = returnObject.MYID;
     newObject.ORG_LOC = returnObject.MYLOCATION;
     getData(newObject);
    } else {
     getData(null);
    }
   });
  });
 }
});
