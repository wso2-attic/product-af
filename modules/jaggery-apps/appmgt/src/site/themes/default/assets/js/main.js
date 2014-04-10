function selectBoxOptionFormat(state) {
	var originalOption = $(state.element);
	if ( typeof originalOption.data('description') === "undefined") {
		return state.text;
	} else {
		if ( typeof originalOption.data('image') === "undefined") {
			return state.text + '<em class="describe">' + originalOption.data('description') + '</em>';
		} else {
			if (originalOption.data('image') != 'no') {
				return '<div class="image_list"><dl><dt>' + state.text + '</dt><dd class="img"><img src="assets/img/' + originalOption.data('image') + '" class="user_image" width="40" height="40" alt="User 1" /></dd><dd>' + originalOption.data('description') + '</dd></dl></div>';
			} else {
				return '<div class="image_list"><dl><dt>' + state.text + '</dt><dd class="img"><span class="icon-user"></span></dd><dd>' + originalOption.data('description') + '</dd></dl></div>';

			}
		}
	}

}


$('form').bind("keyup", function(e) {
	var code = e.keyCode || e.which;
	if (code == 13) {
		e.preventDefault();
		return false;
	}
});

$(window).load(function() {
	$(".message_box").sticky({
		topSpacing : 0
	});
});

$(document).ready(function() {
	$('.dropdown-toggle').dropdown();

	$('html').click(function() {
		if ($(".user .dropdown-toggle i").hasClass("icon-chevron-up")) {
			$(".user .dropdown-toggle i").removeClass("icon-chevron-up");
			$(".user .dropdown-toggle i").addClass("icon-chevron-down");
		}
	});
	$('.user .dropdown-toggle').on("click", function(event) {
		if ($(".user .dropdown-toggle i").hasClass("icon-chevron-down")) {
			$(".user .dropdown-toggle i").removeClass("icon-chevron-down");
			$(".user .dropdown-toggle i").addClass("icon-chevron-up");
		} else {
			$(".user .dropdown-toggle i").removeClass("icon-chevron-up");
			$(".user .dropdown-toggle i").addClass("icon-chevron-down");
		}
		event.preventDefault();
	});

	$('.js_create_branch').on("click", function(event) {
		$(this).toggleClass("active");
		$(this).next().toggle(function() {
			$(this).toggleClass("highlight");
		});
		event.preventDefault();
	});

	$('.js_build_option').on("click", function(event) {
		$(this).toggleClass("active");
		$(this).next().toggle(function() {
			$(this).toggleClass("highlight");
		});
		event.preventDefault();
	});

	$('.popover_form .cancel').on("click", function(event) {
		$(this).closest(".popover_form").toggle(function() {
			$(this).toggleClass("highlight");
		});
		$(this).closest(".popover_form").prev().toggleClass("active");
		event.preventDefault();
	});

	$('.block_view').on("click", function(event) {
		$(this).addClass('active');
		$('.table_view').removeClass('active');
		$('#item_list').addClass('box_items');
		$('#item_list').removeClass('table_items');
		event.preventDefault();
	});

	$('.table_view').on("click", function(event) {
		$(this).addClass('active');
		$('.block_view').removeClass('active');
		$('#item_list').addClass('table_items');
		$('#item_list').removeClass('box_items');
		event.preventDefault();
	});

	$(".select_list").select2({
		formatResult : selectBoxOptionFormat,
		minimumResultsForSearch : -1,
		containerCssClass : "error"
	});

	$('.js_next_elem_tooltip').each(function() {
		$(this).qtip({
			content : {
				text : $(this).next()
			},
			style : {
				classes : 'popup_status_box tooltip',
				widget : false,
				def : false
			},
			position : {
				my : 'left top',
				adjust : {
					y : -5,
					x : 5
				}
			},
			hide : {
				fixed : true,
				delay : 200
			}
		});
	}).bind('click', function(event) {
		event.preventDefault();
		return false;
	});

	$('.js_deta_tooltip').each(function() {
		$(this).qtip({
			content : {
				attr : 'data-tooltip'
			},
			style : {
				classes : 'popup_status_box tooltip',
				widget : false,
				def : false
			},
			position : {
				my : 'left top',
				adjust : {
					y : -5,
					x : 5
				}
			},
			hide : {
				fixed : true,
				delay : 200
			}
		});
	}).bind('click', function(event) {
		event.preventDefault();
		return false;
	});

	$('.js_next_issuedown_popover').each(function() {
		$(this).qtip({
			content : {
				text : $(this).next()
			},
			style : {
				classes : 'popover_box issue_down',
				widget : false,
				def : false,
				tip : false
			},
			hide : {
				fixed : true,
				event : null,
				effect : function(offset) {
					$(this).slideUp(200);
				}
			},
			show : {
				event : 'click',
				effect : function(offset) {
					$(this).slideDown(200);
				}
			},
			events : {
				show : function(event, api) {
					api.elements.target.addClass('active');
					var $el = $(api.elements.target[0]);
					$el.qtip('option', 'position.my', ($el.data('popover-my-position') == undefined) ? 'top right' : $el.data('popover-my-position'));
					$el.qtip('option', 'position.at', ($el.data('popover-target-position') == undefined) ? 'bottom right' : $el.data('popover-target-position'));

				},
				hide : function(event, api) {
					api.elements.target.removeClass('active');
				}
			}
		});
	}).bind('click', function(event) {
		event.preventDefault();
		return false;
	});

	$('.js_next_issueup_popover').each(function() {
		$(this).qtip({
			content : {
				text : $(this).next()
			},
			style : {
				classes : 'popover_box issue_up',
				widget : false,
				def : false,
				tip : false
			},
			hide : {
				fixed : true,
				event : null,
				effect : function(offset) {
					$(this).slideUp(200);
				}
			},
			show : {
				event : 'click',
				effect : function(offset) {
					$(this).slideDown(200);
				}
			},
			events : {
				show : function(event, api) {
					api.elements.target.addClass('active');

					var $el = $(api.elements.target[0]);
					$el.qtip('option', 'position.my', ($el.data('popover-my-position') == undefined) ? 'top right' : $el.data('popover-my-position'));
					$el.qtip('option', 'position.at', ($el.data('popover-target-position') == undefined) ? 'bottom right' : $el.data('popover-target-position'));

				},
				hide : function(event, api) {
					api.elements.target.removeClass('active');
				}
			}
		});
	}).bind('click', function(event) {
		event.preventDefault();
		return false;
	});

	$('.js_next_short_popover').each(function() {
		$(this).qtip({
			content : {
				text : $(this).next()
			},
			style : {
				classes : 'popover_box short_box',
				widget : false,
				def : false,
				tip : false
			},
			hide : {
				fixed : true,
				event : null,
				effect : function(offset) {
					$(this).slideUp(200);
				}
			},
			show : {
				event : 'click',
				effect : function(offset) {
					$(this).slideDown(200);
				}
			},
			events : {
				show : function(event, api) {
					api.elements.target.addClass('active');

					var $el = $(api.elements.target[0]);
					$el.qtip('option', 'position.my', ($el.data('popover-my-position') == undefined) ? 'top right' : $el.data('popover-my-position'));
					$el.qtip('option', 'position.at', ($el.data('popover-target-position') == undefined) ? 'bottom right' : $el.data('popover-target-position'));

				},
				hide : function(event, api) {
					api.elements.target.removeClass('active');
				}
			}
		});
	}).bind('click', function(event) {
		event.preventDefault();
		return false;
	});

	$('.popover_close').live('click', function() {
		$(this).parents('.qtip').qtip("hide");
	});

	/*

	 $('.collapse_indicator').off('click');

	 $('.js_accordion').live("click", function(event){
	 $(this).addClass("in");
	 $(this).removeClass("js_accordion").off('click');

	 var collapse_indicator = $(this).find('.collapse_indicator');

	 if( collapse_indicator.hasClass("icon-chevron-right")){
	 collapse_indicator.removeClass("icon-chevron-right")
	 .addClass("icon-chevron-down")
	 .on('click');
	 } else {
	 collapse_indicator.removeClass("icon-chevron-down")
	 .addClass("icon-chevron-right")
	 .off('click');

	 }

	 event.preventDefault();

	 });
	 */

	$('.in .collapse_indicator').live("click", function(event) {
		if ($(this).closest('.row').hasClass("in")) {
			$(this).closest('.row').removeClass("in").addClass("js_accordion").on('click');
			$(this).removeClass("icon-chevron-down").addClass("icon-chevron-right");
		} else {
			$(this).removeClass("icon-chevron-right").addClass("icon-chevron-down").on('click');
			$(this).closest('.row').addClass("in");
		}
		event.preventDefault();
	});

	$('.js_close_message').live("click", function(event) {
		$(this).closest('.message').slideUp(500, function() {
			$(this).remove();
		});
		event.preventDefault();
	});

});

function add_message(message_type, message_html) {
	message = '<div class="message ' + message_type + '_message" style="display:none"><div class="content"><div class="left">' + message_html + '</div><a href="#" class="close_message right icon-remove-sign js_close_message"></a></div></div>';
	$(".message_box").append(message);
	$(".message_box .message:last-child").slideDown(500);
}


$('.modal_next').live('click', function(event) {
	$(this).qtip({
		content : {
			text : function(api) {
				return $(this).next('.modal_content').clone();
			}
		},

		position : {
			my : 'center',
			at : 'center',
			target : $(window)
		},
		show : {
			event : event.type,
			ready : true,
			solo : true,
			modal : {
				blur : true,
				escape : true
			},
			when : false
		},
		hide : false,
		style : {
			classes : 'modal_box',
			widget : false,
			def : false
		}
	});
});

$('.modal_cancel').click(function() {
	$('#qtip').qtip("hide");
})
function showModal(message, action) {

	$('.qtip').remove();

	$('.modal_action').text(action);
	$('.modal_msg').text(message);

	$('#qtip').qtip({
		content : {
			text : function(api) {
				return $('.modal_content').clone();
			},
			prerender : true
		},

		position : {
			my : 'center',
			at : 'center',
			target : $(window)
		},
		show : {
			ready : true,
			solo : true,
			modal : {
				blur : true,
				escape : true
			},
			when : false
		},

		hide : true,
		style : {
			classes : 'modal_box',
			widget : false,
			def : false
		}
	});
}

/* register form */

jQuery.validator.addMethod("domain", function(value, element) {
	return this.optional(element) || /^((?:(?:(?:\w[\.\-\+]?)*)\w)+)((?:(?:(?:\w[\.\-\+]?){0,62})\w)+)\.(\w{2,6})$/.test(value);
}, "Invalid domain name");

jQuery.validator.addMethod("password_match", function(value, element) {
   return $('#password').val() == $('#password2').val()
}, "Password does not match");


$('#adminUsername').on('keyup', function(e) {
	var admin = $(this).val();
	$('#preAdmin').text(admin);
	if($('#domainName').val() != '') $('#lblLoginID').show();
});

$('#organization').on('keyup', function(e) {
	// TODO: check domain availability
	var org = $(this).val();
	var domain = org.replace(/\s/g, "-").replace(/([^a-zA-Z0-9\-])/gi, "") + ".com";
	if (!org)
		domain = "";
	$('#domainName').val(domain);
	$('#preDomain').text(domain);
	if($('#adminUsername').val() != '') $('#lblLoginID').show();
});

$('#domainName').on('keyup', function(e) {
	// TODO: check domain availability
	var domain = $(this).val();
	$('#preDomain').text(domain);
	if($('#adminUsername').val() != '') $('#lblLoginID').show();
}); 
