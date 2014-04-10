
//This will return a data publisher object which can be used to create streams and publish data to BAM. The data publishisng is not asynchronous.

function createDatapublisher(url, username, password){
    var DataPublisher =  Packages.org.wso2.carbon.databridge.agent.thrift.DataPublisher;
    var dataPublisher = new DataPublisher(url, username, password);
    return dataPublisher;
}

