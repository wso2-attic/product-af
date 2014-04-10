var numberOfSlides = 4;
var currentSlide = 1;
var sliderTimer;
var waitTimer;

function rotateSlides(){
	$('#slider-buttons li').removeClass('active');
	if (currentSlide >= numberOfSlides) {
        currentSlide = 1;
		$('.slide-left').addClass('active');
    }else if (currentSlide < 1) {
        currentSlide = numberOfSlides;
		$('.slide-right').addClass('active');
    }else{
		currentSlide++;
	}
	
	$('#slider-buttons li').each(function(index){
			if(index == currentSlide -1){
				$(this).addClass("active");
			}
	});
    goto(currentSlide);
}
$(document).ready(function() {
    if ($.browser.msie && $.browser.version == 7.0) {
        $('.contentbox').hide();
        $("#slide_1").show();
        $("#slide_image_1").show();
    }
	
	sliderTimer = setInterval(rotateSlides,5000);
});
function selectSlide(index,obj){
    clearInterval(waitTimer);
    clearInterval(sliderTimer);
    $('#slider-buttons li').removeClass('active');
    $(obj).parent().addClass("active");
     goto(index,obj);
}
function goto(index, t) {
    //animate to the div id.
    if(index==1){
        $(".contentbox-wrapper").css('left',$("#slide_0").position().left);
        $(".contentbox-image-wrapper").css('left',$("#slide_0").position().left);
        $("#small-clouds").css('left',0);
        $("#big-clouds").css('left',0);
    }
    if ($.browser.msie && $.browser.version == 7.0) {
        $('.contentbox').hide();
        $("#slide_" + index).show();
        $("#slide_image_" + index).show();
    }
    //if(index!= numberOfSlides){
        $(".contentbox-wrapper").delay(100).animate({"left": -($("#slide_" + index).position().left)}, 600);
        $(".contentbox-image-wrapper").delay(300).animate({"left": -($("#slide_" + index).position().left)}, 800);
    //}
    $("#small-clouds").animate({"left": -100 * index}, 700);
    $("#big-clouds").animate({"left": -350 * index}, 700);
}

