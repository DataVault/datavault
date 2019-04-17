<div class="container-fluid  newcolour">
    <div class="container newcolor">
        <div class="row">
        <#if welcome?has_content>
            <div class="col-sm-1"></div>
            <div class="col-sm-9">
                <h3 class="white">${welcome}</h3>
            </div>

        <#else>
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
        </#if>

        </div>
    </div>
</div>
