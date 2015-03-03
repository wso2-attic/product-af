var ie = (function(){
       var undef, v = 3, div = document.createElement('div');

       while (
           div.innerHTML = '<!--[if gt IE '+(++v)+']><i></i><![endif]-->',
           div.getElementsByTagName('i')[0]
       );

       return v> 4 ? v : undef;
   }());
if( ie <= 9){
    createPlaceholders();
}
function createPlaceholders() {
    var inputs = jQuery("input[type=text],input[type=email],input[type=tel],input[type=url]");
    inputs.each(
            function() {
                var _this = jQuery(this);
                this.placeholderVal = _this.attr("placeholder");
                _this.val(this.placeholderVal);
                if (this.placeholderVal != "") {
                    _this.addClass("placeholderClass");
                }
            }
            )
            .bind("focus", function() {
        var _this = jQuery(this);
        var val = jQuery.trim(_this.val());
        if (val == this.placeholderVal || val == "") {
            _this.val("");
            _this.removeClass("placeholderClass");
        }
    })
            .bind("blur", function() {
        var _this = jQuery(this);
        var val = jQuery.trim(_this.val());
        if (val == this.placeholderVal || val == "") {
            _this.val(this.placeholderVal);
            _this.addClass("placeholderClass");
        }

    });
}