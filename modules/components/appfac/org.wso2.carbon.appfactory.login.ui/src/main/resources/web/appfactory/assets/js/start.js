 $(document).ready(function(){ 
	$('.start input').on('focusout', function(event){ 
		 
		if($(this).val()) { 
			$(this).addClass('filled');
		}
	});
	
	$('.help').each(function() {  
		$(this).qtip({
			content: {
				text: $(this).next() 
			},
			style: {
					classes: 'help_box',
					widget: false, 
					def: false,
					tip: {
						corner: true,
						width: 15,
						height: 8
					} 
				},
			position: {
				my: 'left Middle', 
				at: 'right Middle',
				adjust: {
					y: 0
				} 
		   },
			show:  
				{
					event: 'click' 
				},
			hide: { 
				fixed: true, 
				delay: 200 
			}
		});
	}).bind('click', function(event){ event.preventDefault(); return false; });
	
});