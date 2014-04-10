$().ready(function() {

    // **checked**
    $('#saveIssue').click(function() {
        var projectKey = $("#projectKey").attr('value');

        var issue = new Object();
        issue.summary       = $("#summary").attr('value');
        issue.description   = $("#description").attr('value');
        issue.type          = $("#type").attr('value');
        issue.priority      = $("#priority").attr('value');
        issue.status        = $("#issue_status").attr('value');
        issue.assignee      = $("#assignee").attr('value');
        issue.versionId     = $('#version').val();
        issue.severity      = $("#severity").attr('value');
        issue.version       = $( "#version option:selected" ).text();

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
                window.location.href = "get?issuePkey="+response;
            },
            error: function(XMLHttpRequest, textStatus, errorThrown) {
                window.location.href = "/issuetracker";
            },
            dataType: 'json',
            async:false
        });
    });

    $('#editIssue').click(function() {
        var key = $("#key").attr('value');

        var issue = new Object();
        //issue.projectId=$("#projectId").attr('value');
        issue.key=key;
        issue.summary=$("#summary").attr('value');
        issue.description= $("#description").attr('value');
        issue.type=$("#type").attr('value');
        issue.priority=$("#priority").attr('value');
        issue.status=$("#issue_status").attr('value');
        issue.assignee=$("#assignee").attr('value');
        issue.versionId=$("#version").attr('value');
        issue.severity=$("#severity").attr('value');
        issue.version       = $( "#version option:selected" ).text();

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
                window.location.href = "/issuetracker";
            },
            dataType: 'json',
            async:false
        });
        if(isSuccess)  {
            window.location.href = "/issuetracker";
        }

    });

    // **checked**
    $('#commentAdd').click(function() {
        var issueUniqueKey = $("#ukey").attr('value');
        var description = $("#commentVal").attr('value').trim();

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
                    window.location.href = "/issuetracker";
                },
                dataType: 'json',
                async:false
            });

            if(isSuccess)  {
                window.location.href = "get?issuePkey="+issueUniqueKey;
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
                window.location.href = "/issuetracker";
            },
            dataType: 'json',
            async:false
        });
        if(isSuccess)  {
            window.location.href = "get?issuePkey="+key;
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
                    window.location.href = "/issuetracker";
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

                window.location.href = "/issuetracker";
            },
            dataType: 'json',
            async:false
        });
        if(isSuccess)  {
            //alert("Data successfully updated");
            window.location.href = "get?issuePkey="+issueUniqueKey;
        }
    }
}