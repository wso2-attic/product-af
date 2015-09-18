
// page initialization
$(document).ready(function() {
    init();
    drawTable();
});

function init() {
    initializeUserActivity("deployments", applicationKey, applicationName);
};

function drawTable() {

    var table = $('#databaselist').DataTable({
        responsive: true,
        "orderCellsTop": true,
        "bAutoWidth": false,

        "ajax": {
               "type"   : "POST",
               "url"    : '../blocks/deployments/list/ajax/list.jag',
               "data"   : function(param) {
                   param.action = "getAppVersionMainInfo";
                   param.buildableforstage ="true";
                   param.metaDataNeed = "false";
                   param.applicationKey = applicationKey;
                   param.userName = userName;
                   param.isRoleBasedPermissionAllowed = "false";
                }
        },
        "columns": [
            { "data": "version", "width": "1%"},
            { "data": "stage" , "width": "1%"},
            {"width": "5%","sClass" : "dt-body-center", "render": function ( data, type, full, meta ) {
                return '<a class="launch"><i class="fw fw-1-5x fw-view"></i></a>';
            }}
        ]
    });
}