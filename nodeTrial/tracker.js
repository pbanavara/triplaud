var http = require('http');
var sys = require('sys');
var url = require('url');
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
        console.log("GET ID :::" + id[1]);
        response.writeHead(200,{"Context-type":"text/html"});
        response.end();
        }
}
http.createServer(onRequest).listen(8888);
