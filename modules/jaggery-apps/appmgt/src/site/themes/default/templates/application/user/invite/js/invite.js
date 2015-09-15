/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

var redirectPage = "application-team.jag";
// page initialization
$(document).ready(function () {

    //An external param stating the current logged in
    //user is an admin or not
    var isAdmin = hasTenantLevelUserMgtPermission;

    //Sample roles to be added to the new tags selector
    var rolesList = [
        {
            id:'developer',
            text:'Developer'
        },
        {
            id:'devops',
            text:'DevOps'
        },
        {
            id:'qa',
            text:'QA'
        },
        {
            id:'appowner',
            text:'Application Owner'
        },
        {
            id:'cxo',
            text:'CXO'
        }
    ];

    $.fn.select2.amd.require(['select2/selection/search'], function (Search) {
        var oldRemoveChoice = Search.prototype.searchRemoveChoice;

        Search.prototype.searchRemoveChoice = function () {
            oldRemoveChoice.apply(this, arguments);
            this.$search.val('');
        };


        $(".members").select2({
                tags: true,
                data: uninvitedTenantUsers,
                multiple:true,
                templateResult: memberResult,
                placeholder :'Type member emails',
                createTag: function (tag) {
                  return {
                      id: tag.term,
                      text: tag.term,
                      isNew : true
                  };
                },
                escapeMarkup:function(m){
                  return m;
                },
            }).on('select2:open',function(){
            $('.members-container .error').remove();
            if($('.new-tag').length > 0){
                var select2NewUser = $('.new-tag').data();

                $(".members [value='"+ select2NewUser.id +"']").remove();
                $('.members').trigger('change');

                $('.new-tag').remove();
            }
        }).on("select2:select",function(e){
            var memberContainer = $('.select2-container');

            if(e.params.data.isNew){
                if(isAdmin){
                    if(validateEmail(e.params.data.text)){
                        $('.new-tag').remove();

                        var htmlContent = '';

                        htmlContent += '<div class="new-tag" style="';

                        htmlContent += 'top:' + (memberContainer.offset().top + $(this).height()) + 'px;';
                        htmlContent += 'left:' + memberContainer.offset().left + 'px;';
                        htmlContent += 'width:' + memberContainer.width() + 'px;"';
                        htmlContent += '>';

                        htmlContent += '<div class="new-tag-desc">';
                        htmlContent += '<p>';
                        htmlContent += '<strong>' + e.params.data.text + '</strong>'
                        htmlContent += ' is not invited to the organization. You can add application related roles here.';
                        htmlContent += '</p>'
                        htmlContent += '</div>'

                        htmlContent += '<div class="form-group new-tag-container">';
                        htmlContent += '<label for="new-tag-roles">';
                        htmlContent += 'Select Roles';
                        htmlContent += '</label>';
                        htmlContent += '<select id="new-tag-roles" type="hidden" multiple="multiple" class="form-control new-tag-roles">';
                        htmlContent += '</select>';
                        htmlContent += '<div class="checkbox">';
                        htmlContent += '<label>';
                        htmlContent += '<input type="checkbox" class="add-all-tags">';
                        htmlContent += 'Assign all roles';
                        htmlContent += '</label>'
                        htmlContent += '</div>';
                        htmlContent += '<button class="cu-btn cu-btn-sm cu-btn-blue add-tag">Add</button>';
                        htmlContent += '</div>';

                        htmlContent += '</div>';

                        $('body').append(htmlContent);
                        $('.new-tag').data(e.params.data);

                        $('.new-tag-roles').select2({
                                                        data: rolesList,
                                                        placeholder:'Please select roles',
                                                        createSearchChoice: function(){ return null; }
                                                    })

                        $('.add-all-tags').click(function(){
                            if($(this).prop('checked')){
                                $('.new-tag-roles option').prop('selected',true).trigger('change')
                            }else{
                                $('.new-tag-roles option').prop('selected',false).trigger('change')
                            }
                        })

                        $('.add-tag').click(function(){
                            var selectedRoles = $('.new-tag-roles').select2('data');
                            if(selectedRoles.length < 1){
                                $('<label>').text('Please select roles to add.').addClass('error').css({
                                                                                                           'margin-top' : '10px'
                                                                                                       }).insertBefore('.add-tag');
                                return false;
                            }

                            var newUser = $('.new-tag').data();
                            var addedUsers = $('.members').select2('data');

                            var targetEl = $.grep(addedUsers, function(e){ return e.id == newUser.id; })

                            if(targetEl.length > 0 ){
                                targetEl[0]['roles'] = $('.new-tag-roles').select2('data');
                            }

                            $('.new-tag').remove();
                        })
                    }else{
                        $('.members-container').append('<label id="cname-error" class="error" for="cname">' +
                                                       'A valid email is required.</label>');

                        $(".members [value='"+ e.params.data.id +"']").remove();
                        $('.members').trigger('change');
                    }
                }else{
                    $('.members-container').append('<label id="cname-error" class="error" for="cname">' +
                                                   'You cannot add new users.</label>');

                    $(".members [value='"+ e.params.data.id +"']").remove();
                    $('.members').trigger('change');
                }
            }


        })

    });

    $('#members').change(function(){
        var users = $('#members').select2('data');
        if(!users.length){
            $('#inviteMembers').prop("disabled", true);
        } else {
            $('#inviteMembers').prop("disabled", false);
        }

    });

    $('#inviteMembers').click(function(){

        var users = $('#members').select2('data');
        if(users.length){
            $('#inviteMembers').loadingButton({action : "show"});
            inviteSelectedUsers(users);
        } else {
            jagg.message({
                content:"Please at least select one user",
                type: 'warning'
            });
        }

    });
});

/**
 * ************************************
 * BEG of UX functions
 * ************************************
 */
//Custom function for select 2 templating
function memberResult(result){

    if(result.loading != undefined && result.loading){
        return result.text;
    }

    var role = '';
    if(result.role != undefined){
        role = result.role.toString().replace(/,/g,', ');
    }

    return '<div class="members-result-container"><div class="pull-left member-detail"><h4>' + result.text +
           '</h4><p>' + role + '</p></div><div class="pull-right">' +
           '<span class="fw-stack fw-lg btn-action-ico"><i class="fw fw-ring fw-stack-2x"></i>' +
           '<i class="fw fw-add fw-stack-1x"></i></span></div><div class="clearfix"></div></div>'
}

//Validate email
function validateEmail(email) {
    var re = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;
    return re.test(email);
}
//END of UX functions

function inviteSelectedUsers(users){
    jagg.post("../blocks/application/user/invite/ajax/invite.jag", {
        action:"inviteUsers",
        applicationKey:applicationKey,
        users:JSON.stringify(users),
        welcomeMsg:$('#description').val()
    }, function (result) {
        $('#inviteMembers').loadingButton({action : "hide"});
        jagg.message({
            content:'Successfully added the users to application',
            type: 'success'
        });
        var redirectionPageParams = redirectPage+ "?applicationName=" + applicationName
                + "&applicationKey=" +applicationKey;
        window.location.replace(redirectionPageParams);
    }, function (jqXHR, textStatus, errorThrown) {
        $('#inviteMembers').loadingButton({action : "hide"});
        jagg.message({
           content:jqXHR.responseText,
           type: 'error'
        });
    });
}