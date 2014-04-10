//GadgetID

var curId = 0;

var host;

var testGadgets = [];

$(function() {
	//initializing the common container
	CommonContainer.init();
	host = resolveHost();
	console.log(host);
	// TODO: we need to integrate the REST api get the following gadgets, in milestone 2
	testGadgets = [
		host + '/dashboard/gadgets/appfac-resources/by-category.xml',
	    host + '/dashboard/gadgets/appfac-users/by-category.xml',
		host + '/dashboard/gadgets/appfac-apps/stages.xml',
		host + '/dashboard/gadgets/appfac-apps/top-owners.xml',
		//host + '/dashboard/gadgets/appfac-issues/by-resolutions.xml',
		host + '/dashboard/gadgets/appfac-issues/by-stage.xml',
		host + '/dashboard/gadgets/appfac-issues/by-priority.xml',
		host + '/dashboard/gadgets/appfac-builds/counts.xml',
		//host + '/dashboard/gadgets/appfac-code/counts.xml',
		//host + '/dashboard/gadgets/appfac-code/top-repos-by-size.xml',
		host + '/dashboard/gadgets/appfac-issues/top-10-bug-assigners.xml',
		host + '/dashboard/gadgets/appfac-issues/top-10-bug-reporters.xml'
	];
	drawGadgets();
});

var drawGadgets = function() {
	CommonContainer.preloadGadgets(testGadgets, function(result) {
		for (var gadgetURL in result) {
			if (!result[gadgetURL].error) {
				buildGadget(result, gadgetURL);
				console.log(result+":"+gadgetURL);
				curId++;
			}else {
				alert(result[gadgetURL].error.toSource());
			}
		}
	});
};

var gadgetTemplate = '<div class="portlet">' + '<div class="portlet-header"></div>' + '<div id="gadget-site" class="portlet-content"></div>' + '</div>';

var buildGadget = function(result, gadgetURL) {
	result = result || {};
	var element = getNewGadgetElement(result, gadgetURL);
	$(element).data('gadgetSite', CommonContainer.renderGadget(gadgetURL, curId));

	//determine which button was click and handle the appropriate event.
	$('.portlet-header .ui-icon').click(function() {
		handleNavigateAction($(this).closest('.portlet'), $(this).closest('.portlet').find('.portlet-content').data('gadgetSite'), gadgetURL, this.id);
	});

};

var getNewGadgetElement = function(result, gadgetURL) {
	result[gadgetURL] = result[gadgetURL] || {};
    var gadgetName = result[gadgetURL].userPrefs.gadgetName.displayName;
	var newGadgetSite = gadgetTemplate;
	newGadgetSite = newGadgetSite.replace(/(gadget-site)/g, '$1-' + curId);

	$(newGadgetSite).appendTo($('#gadgetArea-' + gadgetName));

	var gadgetTitle = result[gadgetURL].modulePrefs.title;
	var gadgetSettings = '<a href="#"><i class="icon-cog icon-large"></i></a>';

	$('#gadgetArea-' + gadgetName + ' .portlet-header').html(gadgetTitle + gadgetSettings);

	return $('#gadget-site-' + gadgetName).get([0]);
}
