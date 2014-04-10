// //This will return a data publisher object which can be used to create streams and publish data to BAM in a asynchronous manner.

function createAsyncDatapublisher(url, username, password){
    var AsyncDataPublisher = Packages.org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
    var asyncDataPublisher = new AsyncDataPublisher(url, username, password);
    return  asyncDataPublisher;
}


