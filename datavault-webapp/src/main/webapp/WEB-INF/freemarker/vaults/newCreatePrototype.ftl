<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<#import "/spring.ftl" as spring />
<#assign sec=JspTaglibs["http://www.springframework.org/security/tags"] />

<@layout.vaultLayout>

    <div class="container">
        <div class="row">
            <div class="col-md-12">
                <div class="panel panel-uoe-low">

                    <div class="associated-image">
                        <figure class="uoe-panel-image uoe-panel-image"></figure>
                    </div>

                    <div class="panel-body">
                        <h2>
                            Create new vault
                            <small>
                            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                  title="Multiple deposits may be made into one vault. Usually one vault will correspond to one Pure record">
                            </span>
                            </small>
                        </h2>

                        <br/>

                        <!-- MultiStep Form -->
                        <div class="container">
                            <div class="row justify-content-center mt-0">
                                <div class="col-10 text-center p-0 mt-3 mb-2">
                                    <div class="card px-0 pt-4 pb-0 mt-3 mb-3">
                                        <div class="row">
                                            <div class="col-md-12 mx-0">
                                                <form id="vault-creation-form" class="form" role="form" action="${springMacroRequestContext.getContextPath()}/vaults/stepCreate" method="post" novalidate="novalidate" _lpchecked="1">
                                                    <#include "progressFieldset.ftl"/>
                                                    <!-- fieldsets -->
                                                    <#include "affirmationFieldset.ftl"/>
                                                    <#include "billingFieldset.ftl"/>
                                                    <#include "infoFieldset.ftl"/>
                                                    <#include "usersFieldset.ftl"/>
                                                    <#include "summaryFieldset.ftl"/>
                                                    <#include "pendingFieldset.ftl"/>
                                                    <input type="hidden" id="submitAction" name="action" value="submit" />
                                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                                </form>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
<#--                        </div>-->
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script type="text/javascript">
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

    //$(".submit").click(function(){
    //    return false;
    //})

    $('button[type="submit"]').on("click", function() {
        $('#submitAction').val($(this).attr('value'));
    });

});
    </script>
</@layout.vaultLayout>