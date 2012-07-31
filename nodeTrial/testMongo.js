var Db = require('mongodb').Db;
var Server = require('mongodb').Server;
var client = new Db('test', new Server('127.0.0.1', 27017, {}));
var insertData = function(err, collection) {
	collection.insert({name:xyz, latitude:123, longitude:456, speed:54});
	collection.insert({name:xyz, latitude:124, longitude:457, speed:55});
}


