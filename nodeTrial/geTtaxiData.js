var http = require("http");
var redis = require("redis")
var mysql = require('db-mysql');
var url = require("url");
var sys = require('sys');
var cl = redis.createClient();
function onRequest(request,response) {
  var postData = '';
  var headers = request.headers;
  console.log("Headers :::" + headers);
    if(request.method =='GET') {
	sys.debug("in get");
        var getPathName = url.parse(request.url).pathname;
        sys.debug("GET PATH NAME" + getPathName);
	//var id = getPathName.split("=");
        //console.log("GET ID :::" + id[1]);
	//var sendData = getValuesFromDB(id[1]);
	getValuesFromMysqlDB();
        //sys.debug("From Get" + sendData);
        response.writeHead(200,{"Context-type":"text/html"});
        //response.write(JSON.stringify(sendData));
        response.end();
	}
}


http.createServer(onRequest).listen(8888);

function getValuesFromDB(id) {
   redis.debug_mode = true;
   var dataToSend;
   console.log("ID in DB" + id);
   cl.on("error",function(err){
        console.log("Error connection"+ err);
   });
   cl.on("ready", function() {
       console.log("Server is ready");
   });
   cl.lrange(id, 0, -1, function (err, reply){
         	console.log("REPLIES::" + reply.toString());
		dataToSend = reply;
   	cl.quit();
   });
  return dataToSend;
}

function  getValuesFromMysqlDB() {
var db = new mysql.Database({
    hostname: 'localhost',
    user: 'user',
    password: 'password',
    database: 'tgdrivers'
});     
db.connect(function(error) {
    if (error) {
        return console.log('CONNECTION error: ' + error);
    }
    this.query('SELECT * FROM' + this.name('drivers')).execute(function(error, rows, cols) {
        if(error) {
                console.log("ERROR");
        } 
        console.log(rows);
        });
});

}
