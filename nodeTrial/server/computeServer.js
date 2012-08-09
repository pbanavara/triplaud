var http = require("http");
var url = require("url");
var sys = require('sys');
var fs = require('fs');
var temp = '';
var meAndMyFriends = {};
function onRequest(request,response) {
  var postData = '';
  var pathName = url.parse(request.url).pathname;
  var headers = request.headers;
  console.log("Headers :::" + headers);
    if(request.method ==  'POST') {
      request.setEncoding('utf-8');

      request.on('data', function(chunk){
        sys.debug("In data event");
        postData += chunk.toString();
      }).on("end", function(){
        console.log("END OF REQUEST:::"+ postData);
        if(postData !== null) {
         storeAndroidUserData(postData,pathName);
        }
        response.writeHead(200,{"Context-type":"text/html"});
        response.end();
      });
    } else if(request.method =='GET') {
				sys.debug("in get");
        var pathName = url.parse(request.url).pathname;
	sys.debug("Get PathName" + pathName + ":" + request.url);
	var myLoc = meAndMyFriends.MYLOCATION;
	sys.debug("My Location" + myLoc);
	var locations = meAndMyFriends.FRIENDS;
	sys.debug("LOCATIONS: " + locations.length);
	var baseUrl = 'http://ec2-122-248-211-48.ap-southeast-1.compute.amazonaws.com:8080/letsmeet.html?locations=' + myLoc;
	for(var iLoc = 0;iLoc < locations.length; ++iLoc) {
		var loc = locations[iLoc].LOC;
		sys.debug("LOC::: " + loc);
		baseUrl = baseUrl + ":" + loc;
	}
	sys.debug("PATH:::" + baseUrl);
       	response.writeHead(302,{"location":baseUrl});
       	response.end();
	}
		
}

http.createServer(onRequest).listen(8888);


/*
 * Method is called when the android app posts data using the /upload in it's URL.
 */
function storeAndroidUserData(postData,pathName) {
  console.log("PathName:::" + pathName);
  //var id = pathName.split("=");
  //var uniqueId = id[1];
  var data = JSON.parse(postData);
  var dataArray = data.FRIENDS;
  sys.debug("data array is" + dataArray);
  if(dataArray !== undefined) {
	sys.debug("Post sent from originator");
  	meAndMyFriends = data;
	sys.debug(meAndMyFriends.MYID);
  } else {
	sys.debug("Data coming from friends");
	var id = data.id;
	var loc = data.loc;
		var values = meAndMyFriends.FRIENDS;
		for(var i=0;i<values.length;++i) {
			if(values[i].PHONE_NUMBER === id) {
				sys.debug("Friends location found");
				values[i].LOC = loc;	
			}
		}
		sys.debug(meAndMyFriends.FRIENDS);
	}
  
}

function showMapWithIcons(myLocation, friendLocation){
   var temp='';
    var options = {
      host: 'localhost',
      port: 8080,
      path: '/temp.html?mylocation=' + myLocation + '&locations=' + friendLocation ,
      method: 'GET'
    };
  var req = http.request(options, function(res) {
    console.log('STATUS: ' + res.statusCode);
    console.log('HEADERS: ' + JSON.stringify(res.headers));
    res.setEncoding('utf8');
    res.on('data', function (chunk) {
      temp = temp.concat(chunk);
    });
    res.on('end', function(){
	return temp;
      });
    });
  req.end();
  return temp;
}


