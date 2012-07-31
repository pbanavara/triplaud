var mysql = require('db-mysql');
var http = require('http');
function onRequest(request, res) {
        var temp ="";
        res.on('data', function (chunk) {
      temp = temp.concat(chunk);
  });
  res.on('end', function(){
        console.log("Length" + len);
	connectDB();
  });
});
request.end();

function connectDB() {
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
    this.query('SELECT * FROM' + this.name('drivers').execute(function(error, rows, cols) {
	if(error) {
		console.log("ERROR");
	} 
	console.log(rows);
	});
});
}
