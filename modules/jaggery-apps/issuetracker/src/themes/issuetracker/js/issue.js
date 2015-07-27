$().ready(function() {

    // **checked**
    $('#saveIssue').click(function() {
        var projectKey = $("#projectKey").attr('value');
        var issue = new Object();
        issue.summary       = $("#summary").val();
        issue.description   = $("#description").val();
        issue.type          = $("#type option:selected").val();
        issue.priority      = $("#priority option:selected").val();
        issue.status        = "Open";
        issue.assignee      = $("#assignee option:selected").val();
        issue.versionId     = $("#version").val();
        issue.severity      = $("#severity option:selected").val();
        issue.version       = $("#version option:selected" ).val();
        
        var jsonString = JSON.stringify(issue);
        if(issue.summary == null || issue.summary == "" || issue.summary==undefined){
            jagg.message({content:'Issue summary can not be empty',type:'error' });
            return;
        }
        var response = "";
        $.ajax({
            type: 'POST',
            url: "save",
            data: {
                action:"addIssue",
                jsonString:jsonString,
                projectKey:projectKey
            },
            success: function(result){
                response = result.data;
                window.location.href = "get?issuePkey="+response + "&appkey=" + getParameterByName('appkey');
            },
            error: function(XMLHttpRequest, textStatus, errorThrown) {
                window.location.href = "/issuetracker?appkey=" + getParameterByName('appkey');
            },
            dataType: 'json',
            async:false
        });
    });

    $('#updateIssue').click(function() {
        var key = $("#key").val();

        var issue = new Object();
        //issue.projectId=$("#projectId").attr('value');
        issue.key = key;
        issue.summary = $("#summary").val();
        issue.description = $("#description").val();
        issue.type = $("#type option:selected").val();
        issue.priority = $("#priority option:selected").val();
        issue.status = $("#status").val();
        issue.assignee = $("#assignee").val();
        issue.versionId = $("#version").val();
        issue.severity = $("#severity option:selected").val();
        issue.version = $("#version option:selected").text();

        var jsonString = JSON.stringify(issue);
        var isSuccess = false;
        $.ajax({
            type: 'POST',
            url: "save",
            data: {
                action:"editIssue",
                jsonString:jsonString,
                issueKey: key
            },
            success: function(result){
                isSuccess = result.data.responseBean.success;
            },
            error: function(XMLHttpRequest, textStatus, errorThrown) {
                window.location.href = "/issuetracker?appkey=" + getParameterByName('appkey');
            },
            dataType: 'json',
            async:false
        });
        if(isSuccess)  {
            window.location.href = "/issuetracker?appkey=" + getParameterByName('appkey');
        }

    });

    // **checked**
    $('#commentAdd').click(function() {
        var issueUniqueKey = $("#ukey").attr('value');
        var description = $("#commentVal").val();
        if(description){

            var comment = new Object();
            comment.description=description;

            var commentString = JSON.stringify(comment);
            var isSuccess = false;

            $.ajax({
                type: 'POST',
                url: "../comment/save",
                data: {
                    action:"addComment",
                    commentJsonString:commentString,
                    issueUniqueKey: issueUniqueKey
                },
                success: function(result){
                    isSuccess = result.data.responseBean.success;
                },
                error: function(XMLHttpRequest, textStatus, errorThrown) {
                    window.location.href = "/issuetracker?appkey=" + getParameterByName('appkey');
                },
                dataType: 'json',
                async:false
            });

            if(isSuccess)  {
                window.location.href = "get?issuePkey=" + issueUniqueKey + "&appkey=" + getParameterByName('appkey');
            }
        }
    });

    // **checked**
    $('#commentEdit').click(function() {
        var issueUniqueKey = $("#ukey").attr('value');
        var commentId =  $("#comment_id").attr('value');

        var comment = new Object();
        comment.description=$("#commentpopup").attr('value');
        comment.id=commentId;

        var commentJsonString = JSON.stringify(comment);
        var isSuccess = false;
        $.ajax({
            type: 'POST',
            url: "../comment/save",
            data: {
                action:"editComment",
                commentJsonString:commentJsonString,
                issueUniqueKey: issueUniqueKey,
                commentId:commentId
            },
            success: function(result){
                isSuccess = result.data.responseBean.success;
            },
            error: function(XMLHttpRequest, textStatus, errorThrown) {
                window.location.href = "/issuetracker?appkey=" + getParameterByName('appkey');
            },
            dataType: 'json',
            async:false
        });
        if(isSuccess)  {
            window.location.href = "get?issuePkey="+key + "&appkey=" + getParameterByName('appkey');
        }
    });


    // **checked**
    $("#projectKey").change(function () {
        //$('#version').find('option').remove();
        $('#version').html('');
            $.ajax({
                type: 'GET',
                url: "getProjectVersion",
                data: {
                    projectKey:this.value
                },
                success: function(result){
                    var firstVersionId = 0;
                    $.each(result, function(i, obj) {
                       firstVersionId= obj.id;
                       $('#version')
                           .append($("<option></option>")
                               .attr("value", firstVersionId)
                               .text(obj.version));
                    });
                    $('#version').select2('val', userName);

                },
                error: function(XMLHttpRequest, textStatus, errorThrown) {
                    window.location.href = "/issuetracker?appkey=" + getParameterByName('appkey');
                },
            dataType: 'json',
            async:false
         });
    });
});

// **checked**
function deleteComment(commentId){
    var msgBox=confirm("Do you want to delete comment?");
    if (msgBox==true)
    {
        var issueUniqueKey = $("#ukey").attr('value');
        $.ajax({
            type: 'POST',
            url: "../comment/save",
            data: {
                action:"deleteComment",
                issueUniqueKey: issueUniqueKey,
                commentId:commentId

            },
            success: function(result){
                isSuccess = result.data.responseBean.success;
            },
            error: function(XMLHttpRequest, textStatus, errorThrown) {

                window.location.href = "/issuetracker?appkey=" + getParameterByName('appkey');
            },
            dataType: 'json',
            async:false
        });
        if(isSuccess)  {
            //alert("Data successfully updated");
            window.location.href = "get?issuePkey="+issueUniqueKey + "&appkey=" + getParameterByName('appkey');
        }
    }
}

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}
