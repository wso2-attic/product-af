
include("/jagg/jagg.jag");
include("/jagg/constants.jag");
include("/jagg/config_reader.jag");

var log = new Log("sessionDestroyedListener.js");
var uid = jagg.getUser();

var modEvents = jagg.module("events");
if (log.isDebugEnabled()){
	log.debug("stopping all subscriptions of uid :" + uid);
}

modEvents.stopAllSubscriptions(uid);
