

$(document).ready(function() {
    $("#projectKey").select2();
    $("#type").select2();
    $("#priority").select2();
    $("#issue_status").select2();
    $("#severity").select2();
    $("#assignee").select2();

    $('#version').html('');
    $.ajax({
    type: 'GET',
    url: "getProjectVersion",
    data: {
    projectKey:$("#projectKey").attr('value')
    },
success: function(result){

    $.each(result, function(i, obj) {

        $('#version')
            .append($("<option></option>")
                .attr("value",obj.id)
                .text(obj.version));
    });

    $("#version").select2({});

    },
    dataType: 'json',
    async:false
    });
});
