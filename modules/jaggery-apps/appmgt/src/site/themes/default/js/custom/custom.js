$('.cloud-menu-popover').popover({
    html : true,
    title: function() {
        return $("#popover-head").html();
    },
    content: function() {
        return $("#popover-content").html();
    }
});


/**
 * Use to handle file upload
 */
$(document).on('change', '.btn-file :file', function() {
    var input = $(this),
        numFiles = input.get(0).files ? input.get(0).files.length : 1,
        label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
    input.trigger('fileselect', [numFiles, label]);
});

$('.btn-file :file').on('fileselect', function(event, numFiles, label) {

        var input = $(this).parents('.input-group').find(':text'),
            log = numFiles > 1 ? numFiles + ' files selected' : label;

        if( input.length ) {
            input.val(log);
        } else {
            if( log ) alert(log);
        }
});


(function($) {

    /* ========================================================================
     * loading function
     * ======================================================================== */
    $.fn.loading = function(action) {

        var html = '<div class="loading-animation"> \
                        <div class="loading-logo"> \
                            <svg version="1.1" xmlns="http://www.w3.org/2000/svg" \
                            xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" \
                                 viewBox="0 0 14 14" enable-background="new 0 0 14 14" xml:space="preserve"> \
                                <path class="circle" stroke-width="1.4" stroke-miterlimit="10" d="M6.534,\
                                0.748C7.546,0.683,8.578,0.836,9.508,1.25 c1.903,0.807,3.339,2.615,3.685,4.654c0.244,\
                                1.363,0.028,2.807-0.624,4.031c-0.851,1.635-2.458,2.852-4.266,3.222 c-1.189,0.25-2.45,\
                                0.152-3.583-0.289c-1.095-0.423-2.066-1.16-2.765-2.101C1.213,9.78,0.774,8.568,0.718,\
                                7.335 C0.634,5.866,1.094,4.372,1.993,3.207C3.064,1.788,4.76,0.867,6.534,0.748z"/> \
                                <path class="pulse-line" stroke-width="0.55" stroke-miterlimit="10" d="M12.602,\
                                7.006c-0.582-0.001-1.368-0.001-1.95,0 c-0.491,0.883-0.782,1.4-1.278,2.28C8.572,\
                                7.347,7.755,5.337,6.951,3.399c-0.586,1.29-1.338,3.017-1.923,\
                                4.307 c-1.235,0-2.38-0.002-3.615,0"/> \
                            </svg> \
                            <div class="signal"></div> \
                        </div> \
                        <p>LOADING...</p> \
                    </div> \
                    <div class="loading-bg"></div>';

        return $(this).each(function(){
            if (action === 'show') {
                $(this).prepend(html).addClass('loading');
            }
            if (action === 'hide') {
                $(this).removeClass('loading');
                $('.loading-animation, .loading-bg', this).remove();
            }
        });

    };

    /* ========================================================================
     * loading button function
     * ======================================================================== */
    $.fn.loadingButton = function(action) {
        var html = '<span class="button-loader"><div class="loading-logo">\
                    <svg version="1.1" xmlns="http://www.w3.org/2000/svg" width="3em"\
                    xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"\
                    viewBox="0 0 14 14" enable-background="new 0 0 14 14" xml:space="preserve">\
                        <path class="circle" stroke-width="1.4" stroke-miterlimit="10" d="M6.534,\
                        0.748C7.546,0.683,8.578,0.836,9.508,1.25 c1.903,0.807,3.339,2.615,3.685,4.654c0.244,\
                        1.363,0.028,2.807-0.624,4.031c-0.851,1.635-2.458,2.852-4.266,3.222 c-1.189,0.25-2.45,\
                        0.152-3.583-0.289c-1.095-0.423-2.066-1.16-2.765-2.101C1.213,9.78,0.774,8.568,0.718,\
                        7.335 C0.634,5.866,1.094,4.372,1.993,3.207C3.064,1.788,4.76,0.867,6.534,0.748z"/>\
                        <path class="pulse-line" stroke-width="0.55" stroke-miterlimit="10" d="M12.602,\
                        7.006c-0.582-0.001-1.368-0.001-1.95,0 c-0.491,0.883-0.782,1.4-1.278,2.28C8.572,\
                        7.347,7.755,5.337,6.951,3.399c-0.586,1.29-1.338,3.017-1.923,\
                        4.307 c-1.235,0-2.38-0.002-3.615,0"/>\
                    </svg>\
                    <div class="signal"></div>\
                    </div></span>';

        return $(this).each(function(){
            if (action === 'show') {
                $(this).prop( "disabled", true );
                $(this).find('span').css('display','none');
                $(this).prepend(html).addClass('loading');
            }
            if (action === 'hide') {
                $(this).prop( "disabled", false );
                $(this).find('.button-loader').remove();
                $(this).find('span').css('display','inline-block');
            }
        });
    }

}(jQuery));



    /* ========================================================================
     * copy to clipboard function
     * ======================================================================== */
(function ( $ ) {

    $.fn.copyToClipboard = function( element ) {

        var $temp = $("<input>");
        $("body").append($temp);
        $temp.val($(element).val()).select();
        document.execCommand("copy");
        var t = $temp.val();
        copiedText = t +'   : copied to clipboard';
        $(element).attr('data-original-title', copiedText).tooltip('show',{ placement: 'top',trigger:'manual'});
        setTimeout(function(){
            $(element).tooltip('destroy');
        }, 3000);
        $temp.remove();

        return this;

    };

}( jQuery ));

//fix popover close issue
function hidePopover(e) {
    $('[data-toggle="popover"]').each(function () {
        if (!$(this).is(e.target) && $(this).has(e.target).length === 0 && $('.popover').has(e.target).length === 0) {
            $(this).popover('hide');
        }
    });
}

$('body').on('click', function (e) {
    hidePopover(e);
});

$('#username-btn').on('click', function (e) {
    hidePopover(e);
});