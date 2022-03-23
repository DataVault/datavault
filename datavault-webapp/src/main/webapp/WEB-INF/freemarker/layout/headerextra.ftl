<div class="container-fluid newcolour">
        <#if welcome?has_content>
        <div class="container newcolour">
            <div>
                <h3 class="leftwhite">${welcome}</h3>
            </div>
        </div>
        <#else>
        <div class="container newcolour">
            <div class="row">
                <div class="col-sm-1"></div>
                <div class="col-sm-3">
                    <h3 class="white">Protect data from accidental change or deletion</h3>
                </div>
                <div class="col-sm-3">
                    <h3 class="white">Meet funder requirements to keep data safe for a set time period</h3>
                </div>
                <div class="col-sm-3">
                    <h3 class="white">Keep copies of data you are not able to share openly</h3>
                </div>
            </div>
        </div>
        </#if>

</div>

<style>
    .left {
        float: left;
        width: auto;
        border: 3px solid #73AD21;
        padding: 10px;
    }
</style>
