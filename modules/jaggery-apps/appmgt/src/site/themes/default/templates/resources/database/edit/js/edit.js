function getUsersOfDatabaseForDataTable(applicationKey, dbName, environment){
    jagg.post("../blocks/resources/database/users/list/ajax/list.jag", {
        action:"getAvailableUsersToAttachToDatabase",
        applicationKey:applicationKey,
        databaseName:dbName,
        dbServerInstanceName:environment
    }, function (result) {
        console.log(result); 
    },function (jqXHR, textStatus, errorThrown) {
            jagg.message({content:'Error occured while getting users of  database!' , type:'error', id:'databasecreation'});
    }); 

}
