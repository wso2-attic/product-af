function queryDb(query) {

    var db = new Database("WSO2_AF_STAT_DB");
    var dbResult = db.query(query);
    db.close();
    return dbResult;

}