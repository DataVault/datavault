<fieldset id="affirmation-fieldset">
    <div class="form-card">
        <p>
            We need some information about your new Vault. You can save your progress at any time before completing this form.
        </p>
        <p>
            Once we have approved your Vault, you will receive an email confirming that you can go ahead and start archiving your data.
            If you have any questions please don’t hesitate to contact the Research Data Service using the
            <a href="https://www.ed.ac.uk/information-services/research-support/research-data-service/contact" target="_blank">Contact</a> link at the top of this page.
        </p>
        <h2 class="fs-title">Affirmation</h2>
        <p>
            I understand that the ‘owner’ /PI will be billed for any deposit (for information about rates, please use the
            <a href="http://www.ed.ac.uk/is/research-support/datavault" target="_blank">More Info</a> link at the top of
            the page to the university website DataVault information, and go to the
            <a href="https://www.ed.ac.uk/information-services/research-support/research-data-service/after/datavault/cost" target="_blank">Cost</a> page).
        </p>
        <p>
            I understand that any data I and my team deposit in this vault belongs to the University of Edinburgh,
            and I will only be able to retrieve the data as long as I have an active University of Edinburgh login.
        </p>
        <div class="checkbox">
            <label>
                <@spring.bind "vault.affirmed" />
                <input type="hidden" name="_${spring.status.expression}" value="false"/>
                <input id="affirmation-check" type="checkbox" name="${spring.status.expression}"
                       <#if spring.status.value?? && spring.status.value?string=="true">checked="true"</#if> /> Accept
            </label>
        </div>
    </div>
    <#if vault.confirmed?c=="false">
    <button type="submit" name="save" value="Save" class="save action-button-previous btn btn-default" >
        <span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span> Save
    </button>
    </#if>
    <button type="button" name="next" class="next action-button btn btn-primary" disabled>Next Step &raquo;</button>
</fieldset>