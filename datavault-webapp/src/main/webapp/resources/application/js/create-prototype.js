$(document).ready(function(){
    var current_fs, next_fs, previous_fs;
    var opacity;

    $("#affirmation-check").change(function(){
        $(this).parents("fieldset").children(".next").prop( "disabled", !$(this).is(":checked") );
    });

    $("#billing-choice-na").change(function(){
        $('.collapse').collapse('hide');
        $(this).parents("fieldset").children(".next").prop( "disabled", false );
    });

    $("#billing-choice-grantfunding").change(function(){
        if($(this).is(":checked")){
            $('.collapse').not('#billing-form').collapse('hide');
            $('#billing-form').collapse('show');
            $(this).parents("fieldset").children(".next").prop( "disabled", false );
        }
    });

    $("#billing-choice-budgetcode").change(function(){
        if($(this).is(":checked")) {
            $('.collapse').not('#billing-form').collapse('hide');
            $('#billing-form').collapse('show');
            $(this).parents("fieldset").children(".next").prop( "disabled", false );
        }
    });

    $("#billing-choice-slice").change(function(){
        if($(this).is(":checked")) {
            $('.collapse').not('#slice-form').collapse('hide');
            $('#slice-form').collapse('show');
            $(this).parents("fieldset").children(".next").prop( "disabled", false );
        }
    });

    $("input[name='isOwner']").change(function(){
        if(this.value === 'true'){
            $('#owner-uun').prop('disabled', true);
        } else {
            $('#owner-uun').prop('disabled', false);
        }
    })

    $("#add-depositor-btn").click(function(){
        $(".example-depositor").clone(true).appendTo("#extra-depositor-list")
            .removeClass("example-depositor")
            .removeClass("hidden")
            .addClass("extra-depositor").show();
    });

    $(".remove-depositor-btn").click(function(){
        const current_depositor = $(this).closest('.extra-depositor');
        current_depositor.remove();
    });

    $("#add-ndm-btn").click(function(){
        $(".example-ndm").clone(true).appendTo("#extra-ndm-list")
            .removeClass("example-ndm")
            .removeClass("hidden")
            .addClass("extra-ndm").show();
    });

    $(".remove-ndm-btn").click(function(){
        const current_depositor = $(this).closest('.extra-ndm');
        current_depositor.remove();
    });

    $("#add-creator-btn").click(function(){
        $(".example-creator").clone(true).appendTo("#extra-creator-list")
            .removeClass("example-creator")
            .removeClass("hidden")
            .addClass("extra-creator").show();
    });

    $(".remove-creator-btn").click(function(){
        const current_creator = $(this).closest('.extra-creator');
        current_creator.remove();
    });

    $(".next").click(function(){

        current_fs = $(this).parent();
        next_fs = $(this).parent().next();

        //Add Class Active
        $("#progressbar li").eq($("fieldset").index(next_fs)).addClass("active");

        //show the next fieldset
        next_fs.show();
        //hide the current fieldset with style
        current_fs.animate({opacity: 0}, {
            step: function(now) {
                // for making fielset appear animation
                opacity = 1 - now;

                current_fs.css({
                    'display': 'none',
                    'position': 'relative'
                });
                next_fs.css({'opacity': opacity});
            },
            duration: 600
        });
    });

    $(".previous").click(function(){

        current_fs = $(this).parent();
        previous_fs = $(this).parent().prev();

        //Remove class active
        $("#progressbar li").eq($("fieldset").index(current_fs)).removeClass("active");

        //show the previous fieldset
        previous_fs.show();

        //hide the current fieldset with style
        current_fs.animate({opacity: 0}, {
            step: function(now) {
                // for making fielset appear animation
                opacity = 1 - now;

                current_fs.css({
                    'display': 'none',
                    'position': 'relative'
                });
                previous_fs.css({'opacity': opacity});
            },
            duration: 600
        });
    });

    $('.radio-group .radio').click(function(){
        $(this).parent().find('.radio').removeClass('selected');
        $(this).addClass('selected');
    });

    $(".submit").click(function(){
        return false;
    })

});