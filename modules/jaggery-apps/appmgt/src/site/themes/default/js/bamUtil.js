//Use this file to create streams and publish data to bam using the REST API

function createStream(createStreamDefnURL , streamDefn, optionalHeaders){
	post(createStreamDefnURL, streamDefn, optionalHeaders);
	};
	
	 function publishEvent(publishEventURL, event, optionalHeaders){
	post(publishEventURL, event, optionalHeaders);
	};

	
	

