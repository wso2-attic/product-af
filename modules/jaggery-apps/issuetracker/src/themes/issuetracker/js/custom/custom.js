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
     * this function use to append description block on app type selection
     * */
    $(document).on('click', '.cloud-app-type', function(){

        $('.listing').find('.longme').detach();
        if($('.cloud-app-type').hasClass('cloud-app-selected')){
            $('.cloud-app-type').removeClass('cloud-app-selected');
            $(this).addClass('cloud-app-selected');
        }else{
            $(this).addClass('cloud-app-selected');

        }

        var width = $( window ).width(),
            currentcount = parseInt($(this).attr('id')),
            appDescription = $(this).attr('data-description'),
            appName = $(this).attr('data-appname'),
            dataCount = parseInt($('.listing').attr('data-count'));

        //content replace with data attributes
        $('.app-type-info-template').find('.app-name').html(appName);
        $('.app-type-info-template').find('.app-description').html(appDescription);
        var appendHtml =$('.app-type-info-template').html();


        if(width >=1170){
            if(currentcount%7 == 0){
                $('#'+currentcount+'\\.0').parent().after(appendHtml);
                $('.longme').fadeIn('slow')
            }else{
                var ctest = currentcount+(7- currentcount%7);
                console.log(ctest)
                if(ctest > dataCount){
                    $('#'+ dataCount+'\\.0').parent().after(appendHtml);
                    $('.longme').fadeIn('slow')
                }else{
                    $('#'+ctest+'\\.0').parent().after(appendHtml);
                    $('.longme').fadeIn('slow')
                }

            }
        }else if(width >=970){
            if(currentcount%4 == 0){
                $('#'+currentcount+'\\.0').parent().after(appendHtml);
                $('.longme').fadeIn('slow')
            }else{
                var ctest = parseInt(currentcount)+(4- currentcount%4);
                if(ctest > dataCount){
                    $('#'+ dataCount+'\\.0').parent().after(appendHtml);
                    $('.longme').fadeIn('slow')
                }else{
                    $('#'+ctest+'\\.0').parent().after(appendHtml);
                    $('.longme').fadeIn('slow')

                }

            }
        }else if(width >=750 ){
            if(currentcount%2 == 0){
                $('#'+currentcount+'\\.0').parent().after(appendHtml);
                $('.longme').fadeIn('slow')
            }else{
                var ctest = parseInt(currentcount)+1;
                if(ctest > dataCount){
                    $('#'+ dataCount+'\\.0').parent().after(appendHtml);
                    $('.longme').fadeIn('slow')
                }else{
                    $('#'+ctest+'\\.0').parent().after(appendHtml);
                    $('.longme').fadeIn('slow')

                }

            }

        }else if(width <750 ){
            $('#'+currentcount+'\\.0').parent().after(appendHtml);
            $('.longme').fadeIn('slow')

        }


    })


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
