/*
 *wyf
 *
*/

var exec = require('cordova/exec');

var VSpeaker = {
    register:function(callback, appid, userId) {
        exec(callback, null, "VSpeaker", "register", [appid,userId]);
    },
    verify:function(callback, appid, userId) {
        exec(callback, null, "VSpeaker", "verify", [appid,userId]);
    }
};

module.exports = VSpeaker;
