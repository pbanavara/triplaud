var https = require('https');
var options = {
	host:"api.parse.com",
	port: 443,
 	path: '/1/classes/taxilocations',
	method:'GET',
	headers: {
		"X-Parse-Application-Id": "3EN6GtbpYtprWOJyqHNaPjXrJixp66F2qTQVOS30",
 		"X-Parse-REST-API-Key": "KTNYweSLVvp8hI2mR3ekXUCzbJW4fIqay21aQk1O",
  		"Content-Type": "application/json"
	}
};

var request = https.request(options, function(res) {
	var temp ="";
	res.on('data', function (chunk) {
      temp = temp.concat(chunk);
  });
  res.on('end', function(){
  	var newData = JSON.parse(temp);
    var len = newData.results.length;
		console.log("Length" + len);
  });
});
request.end();

