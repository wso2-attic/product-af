/**
 * jQuery plugin to detect clicks outside an element
 * And popover close by given link
 * 
 * developed by Nuwan Sameera (nuwansh)
 * Licensed under the MIT (MIT-LICENSE.txt)  licenses.
 * 
 */

(function($){
  $.fn.outsideClick = function(options){
    var opts = options

    return this.each(function(element){

      var self = this
      var $self = $(this)

      var close_button = $self.find('.cancel')

      $(document).on("mousedown.popover", function(e){
        elemIsParent = $.contains(self, e.target);

        if(e.target == self || e.target == opts.outerEvent.target || elemIsParent){ 
          return
        }else{
          opts.clickHandler.toggleClass("active")
          $self.hide();
        }

        // Remove bind 
        $(document).off('mousedown.popover');
        close_button.off()
      })
      
      // Click close button
      close_button.on('click.closePopover', function(e){
        opts.clickHandler.toggleClass("active")
        $self.hide()

        // remove Both callbacks
        $(this).off(); $(document).off("mousedown.popover")

        e.preventDefault()
      })

   })

  }
})(jQuery);
