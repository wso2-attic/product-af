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

// Required global variables
var urlPatternErrorMsg = "";

// page initialization
$(document).ready(function () {
    var productionVersionElem = $('#productionVersion');
    $('.loader').loading('hide');
    $('.loading-cover').overlay('hide');
    productionVersionElem.select2({placeholder: "Select a version", allowClear: hasDomainMappingPermission});
    $.validator.addMethod("validateCustomUrlPattern", validateCustomUrlPattern, getCustomUrlPatternErrMsg);
    var creationFormValidationOpts = getValidationOptions();
    $("#customUrlForm").validate(creationFormValidationOpts);
    $("#customUrlForm").ajaxForm({
                                     complete: function (result, status) { // on complete
                                         $("#addCustomUrl").prop("disabled", true);
                                         if (status == "error") {
                                             jagg.message({
                                                              content: result.responseText,
                                                              type: 'error',
                                                              id: 'myuniqeid'
                                                          });
                                         } else {
                                             jagg.message({
                                                              content: "Custom URL updated successfully",
                                                              type: 'success',
                                                              id: 'myuniqeid'
                                                          });
                                         }
                                         getAppVersionsByStage(applicationKey, $('#domainMappingAllowedStage').val());
                                     }
                                 });
    productionVersionElem.on("change", function (e) {
        var validator = $("#customUrlForm").validate();
        validator.form();
    });

});

function verifyCustomUrl() {
    if (hasDomainMappingPermission) {
        var customUrlForm = $("#customUrlForm");
        $('.loader').loading('show');
        $('.loading-cover').overlay('show');
        customUrlForm.submit();
    }

}

function submitForm() {
    if (hasDomainMappingPermission) {
        $("#addCustomUrl").prop("disabled", true);
        var customUrlForm = $("#customUrlForm");
        var validator = customUrlForm.validate();
        var formValidated = validator.form();
        if (formValidated) {
            $('.loader').loading('show');
            $('.loading-cover').overlay('show');
            customUrlForm.submit();
        }
    }
}

function validateCustomUrlPattern() {
    var tempCustUrl = $("#productionCustom").val().trim();
    var existingProdUrl = $("#existingProdUrl").val().trim();
    var defaultProdUrl = $("#production-url").val().trim();
    var pattern = /[\w-]+(\.[\w-]+)+/;
    var validated = true;
    if (!tempCustUrl.length || tempCustUrl == 'null' || !pattern.test(tempCustUrl)) { //if new custom url has not valid text
        var validator = $("#customUrlForm").validate();
        if (!existingProdUrl && !tempCustUrl && validator.element("#productionVersion")) {
            validated = true;
        } else {
            urlPatternErrorMsg = "Please specify a valid url";
            validated = false;
        }
    } else if ((defaultProdUrl == tempCustUrl)) {                        // if new url is equal to default url
        urlPatternErrorMsg = "Please specify different url than default url";
        validated = false;
    } else if (existingProdUrl && (existingProdUrl == tempCustUrl)) {    // if new url is equal to existing custom url
        urlPatternErrorMsg = "Please specify different url than existing url";
        validated = false;
    }
    return validated;
}

function getCustomUrlPatternErrMsg() {
    return urlPatternErrorMsg;
}

function getValidationOptions() {
    var errorFormGroupClasses = "has-error";        // if validation is failed
    var successFormGroupClasses = "has-success";    // if validation is successful
    var feedBack = "has-feedback";
    return {
        rules: {                                        // validation rules
            productionCustom: {                          // custom Url filed
                //validateCustomUrlPattern: true,
                require_from_group: [1, '.mygroup']
            },
            productionVersion: {
                require_from_group: [1, '.mygroup']
            }
        },
        highlight: function (element, errorClass, validClass) { // this is triggered when the "element" is invalid
            $(element).addClass(errorClass).removeClass(validClass);
            $(element.form).find("div[for=" + element.id + "]").addClass(errorFormGroupClasses)
                    .removeClass(successFormGroupClasses).addClass(feedBack);
        },
        unhighlight: function (element, errorClass, validClass) { // this is triggered when the "element" is valid
            $(element).removeClass(errorClass).addClass(validClass);
            $(element.form).find("div[for=" + element.id + "]").addClass(successFormGroupClasses).removeClass(errorFormGroupClasses)
                    .addClass(feedBack);
        },
        showErrors: function (event, validator) {
            // Disable url update button if the form is not valid
            if (this.numberOfInvalids() > 0) {
                $("#addCustomUrl").prop("disabled", true);
            } else {
                $("#addCustomUrl").prop("disabled", false);
            }
            this.defaultShowErrors();
        },
        errorPlacement: function (error, element) {
            if ($(element).parent().closest('div').hasClass("input-group")) {
                error.insertAfter($(element).closest('div'));
            } else if ($(element).is($("#productionVersion"))) {
                error.insertAfter($(element).next('span'));
            }
        }
    };
}

function getAppVersionsByStage(applicationKey, stage) {
    jagg.post("../blocks/application/get/ajax/list.jag", {
        action: "getAppVersionsByStage",
        stage: stage,
        applicationKey: applicationKey
    }, function (result) {
        $('.loader').loading('hide');
        $('.loading-cover').overlay('hide');
        var applicationInfo = jQuery.parseJSON(result);
        initialAppVersionInfo = applicationInfo;
        redrawPageElements(applicationInfo);
    }, function (jqXHR, textStatus, errorThrown) {
        jagg.message({
                         content: "Error occurred while getting data",
                         type: 'error',
                         id: 'myuniqeid'
                     });
    });
}

function redrawPageElements(applicationInfo) {
    $("#production-url").val(getDefaultProdUrl(applicationInfo.mappedSubDomain));
    if (applicationInfo.customUrl) {
        $("#productionCustom").val(applicationInfo.customUrl);
        $("#existingProdUrl").val(applicationInfo.customUrl);
        if (applicationInfo.customUrlVerificationCode != null) {
            $("#verifyUrl").prop("disabled", true);
        } else {
            $("#verifyUrl").prop("disabled", false);
        }
    } else {
        $("#verifyUrl").prop("disabled", true);
        $("#productionCustom").val('');
        $("#existingProdUrl").val('');
    }
    drawVersionList(applicationInfo.versions);
    $("#addCustomUrl").prop("disabled", true);
}

function getDefaultProdUrl(mappedSubDomain) {
    return mappedSubDomain + "." + defaultDomainName;
}

function drawVersionList(versionsArray) {
    var versionString = "";
    var mappedVersionExists = false;
    for (var i = 0; i < versionsArray.length; i++) {
        var version = versionsArray[i];
        if (version.productionMappedDomain) {
            versionString += "<option selected " + 'value="' + version.version + '" ' + ">" + version.version + "</option>";
        } else {
            versionString += "<option" + ' value="' + version.version + '" ' + ">" + version.version + "</option>";
        }
    }
    if (!mappedVersionExists) {
        versionString = "<option></option>" + versionString;
    }
    var productionVersionElement = $("#productionVersion");
    if (productionVersionElement.length) {
        productionVersionElement.select2('destroy')
                .html(versionString).select2({placeholder: "Select a version", allowClear: true});
    }

}

