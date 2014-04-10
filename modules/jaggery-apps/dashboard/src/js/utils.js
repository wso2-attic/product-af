    var setSliderPos =  function(){
        console.info('running');
            var top = $(window).scrollTop();
            var bottom = top - $(window).height();
            if(top>138){
                $('#slidingIcons').css('margin-top',top-138);
            }else{
                $('#slidingIcons').css('margin-top',0);
            }
            $('#slidingIcons a').each(function(){
                var elm = $($(this).attr('href')).position();
                var elmTop = elm.top;
                if(elmTop < top && elmTop > bottom){
                    $('#slidingIcons li').removeClass('active');
                    $(this).parent().addClass("active");
                }
            })
    };
    $(document).ready(function(){
        $('#slidingIcons a').tooltip("hide");
        $(document).scroll(setSliderPos);
        setSliderPos();
    });