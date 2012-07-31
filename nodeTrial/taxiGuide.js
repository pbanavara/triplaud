var http = require("http");
var redis = require("redis")
var url = require("url");
var sys = require('sys');
function onRequest(request,response) {
  var postData = '';
  var headers = request.headers;
  console.log("Headers :::" + headers);
    if(request.method ==  'POST') {
      var postPathName = url.parse(request.url).pathname;
      request.setEncoding('utf-8');

      request.on('data', function(chunk){
        sys.debug("In data event");
        postData += chunk.toString();
      }).on("end", function(){
        console.log("END OF REQUEST:::"+ postData);
        if(postData !== null) {
         storeAndroidUserData(postData,postPathName);
        }
        response.writeHead(200,{"Context-type":"text/html"});
        response.end();
      });
    } else if(request.method =='GET') {
	sys.debug("in get");
        var getPathName = url.parse(request.url).pathname;
        sys.debug("GET PATH NAME" + getPathName);
	var id = getPathName.split("=");
        console.log("GET ID :::" + id[1]);
	var sendData = getValuesFromDB(id[1]);
        sys.debug("From Get" + sendData);
        response.writeHead(200,{"Context-type":"text/html"});
        response.write(JSON.stringify(sendData));
        response.end();
	}
}


http.createServer(onRequest).listen(8888);


/*
 * Method is called when the android app posts data using the /upload in it's URL.
 * Method captures the JSON data and stores the unique id of the calling object and the corresponding JSON object
 * in a redis database.
 */
function storeAndroidUserData(postData,pathName) {
  console.log("PathName:::" + pathName);
  //var id = pathName.split("=");
  //var uniqueId = id[1];
  var data = JSON.parse(postData);
  var uniqueId = data.ID;
  var locations = data.locations;
  var client = redis.createClient();
  client.on("error", function(err) {
     console.log("ERROR CONNECTING TO DB :::" + err); 
  });
  sys.debug("DB Values" + uniqueId + ":::" + JSON.stringify(locations));
  client.rpush(uniqueId, JSON.stringify(locations));
  client.quit();

}


function getValuesFromDB(id) {
   debugger;
   redis.debug_mode = true;
   console.log("ID in DB" + id);
   var cl = redis.createClient();
   cl.on("error",function(err){
        console.log("Error connection"+ err);
   });
   cl.on("ready", function() {
       console.log("Server is ready");
   });
   cl.get('abc', function(errror, replies) {
	console.log("REPLIES" + replies);
	cl.quit();
   }); 
   cl.lrange(id, 0, -1, function (err, reply){
         if(reply) {
         	console.log("REPLIES::" + reply.length);
         } else {
		console.log("No data");
	}
   	cl.quit();
   });
}

