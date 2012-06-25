var http = require("http");
var url = require("url");
var sys = require('sys');
var fs = require('fs');
var temp = '';
var meAndMyFriends = [];
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
	var id = pathName.split("=");
	var userId = id[1];
	for (var fIndex = 0;fIndex < meAndMyFriends.length;++fIndex) {
		var organizer = meAndMyFriends[fIndex];
		console.log("IN Get" + userId);
		console.log(JSON.stringify(organizer));
		if(organizer.MYID === userId) {
       			response.writeHead(200,{"Content-type":"application/json"});
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
  console.log("PathName:::" + pathName);
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


