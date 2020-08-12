<fieldset>
    <div class="form-card">
        <h2 class="fs-title">Affirmation</h2>
        <p>
            In order to create a Vault you'll have to answer several questions.
            You can go through each steps at your own pace and
            come back to previous steps whenever you want.
            <strong>
                Don't forget to save your progress if your leaving the website
                to make sure everything is there when you come back.
            </strong>
        </p>
        <p>
            I understand that the ‘owner’ /PI will be billed for any deposit
            (rates are advertised on the Research Services charges page).
            I understand that any files I deposit in the Vault belong to
            the University of Edinburgh. I understand that access is for
            authorised University of Edinburgh users only.
            Therefore I will only be able to retrieve the data as long as
            I am employed by the University of Edinburgh (as long as my
            University login is active).
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
    <button type="submit" name="save" value="Save" class="save action-button-previous btn btn-default" >
        <span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span> Save
    </button>
    <button type="button" name="next" class="next action-button btn btn-primary" disabled>Next Step &raquo;</button>
</fieldset>